package org.codewiz.droid64.emu;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.codewiz.droid64.util.LogManager;
import org.codewiz.droid64.util.Logger;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.PresetReverb;
import android.os.Process;
import android.util.Log;

public class EmuControl {

	private final static Logger logger = LogManager.getLogger(EmuControl.class.getName());

    public static final int FRAMES_PER_SECOND = 50;
    private static final int NORMAL_SLEEP_TIME = 1000 / FRAMES_PER_SECOND;
    private static final int PAUSE_SLEEP_TIME = 100;
    
	public static final int DISPLAY_X = 0x180;
	public static final int DISPLAY_Y = 0x110;
	private static final int DISPLAY_PIXELS = DISPLAY_X * DISPLAY_Y;
	private static final int NUM_VIDEO_BUFFERS = 2;

	private static final int FLAG_UNPACK_GRAPHICS = 0x1;
	private static final int FLAG_USE_GAMMA_CORRECTION = 0x2;

	private Thread emuThread;

	private final Object emuLock = new Object();
	private EmuBindings emu;

	private Object[] videoBuffers = new Object[NUM_VIDEO_BUFFERS];
	private int videoBufferIndex;
	private volatile boolean textureBufferFilled;
	private int textureBufferIndex;

	private int keyboardInputDelay;
	private Queue<Integer> keyboardInputQueue = new ConcurrentLinkedQueue<Integer>();

	private byte[] snapshotBuffer;
	private int snapshotBufferUsage;

	private volatile boolean running;
	private volatile boolean paused;
	private volatile boolean warpMode;

	private int stickMask = 0x0;
	
	private EmuListener listener;
	private Statistics emuStatistics = new Statistics();

	private static EmuControl globalInstance = null;

	private AudioControl audioControl = new AudioControl();
	
	public static EmuControl instance() {
	    return globalInstance;
	}
	
	public EmuControl() {
		globalInstance = this;
	}

	public void init() {
		EmuPrefs prefs = EmuPrefs.instance();
		boolean textureCompressed = prefs.isTextureCompressionEnabled();

		int bitsPerPixels = textureCompressed ? 4 : 32;
		int videoBufferSize = DISPLAY_PIXELS * bitsPerPixels / 8;

		for (int i=0; i<NUM_VIDEO_BUFFERS; i++) {
			videoBuffers[i] = new byte[videoBufferSize];
		}

		audioControl.init();
	}
	
	public void addListener(EmuListener listener) {
		this.listener = listener;
	}

	public synchronized void start() {
		
		logger.info("starting emulator");

		if (running) return;

		running = true;

		EmuPrefs prefs = EmuPrefs.instance();

		textureBufferFilled = false;
		textureBufferIndex = 0;
		videoBufferIndex = 0;

		emuThread = new Thread(new Runnable() {

			@Override
			public void run() {
				emuLoop();
			}
		});
		emuThread.start();
	}

	private void emuLoop() {

		logger.info("started emulator process");

		if (!Thread.interrupted()) {
			emu = new EmuBindings();

			Log.d("emu", "initializing emulator kernel");

			StringBuilder prefsDocument = new StringBuilder();

			EmuPrefs prefs = EmuPrefs.instance();

			//sendCommand(prefs.isDriveEmulationEnabled() ? EmuBindings.COMMAND_TRUEDRIVE_ON : EmuBindings.COMMAND_TRUEDRIVE_OFF);
			//sendCommand(prefs.isJoystickSwapEnabled() ? EmuBindings.COMMAND_JOYSTICK_SWAP_ON : EmuBindings.COMMAND_JOYSTICK_SWAP_OFF);

			prefsDocument.append("Emul1541Proc = " + (prefs.isDriveEmulationEnabled() ? "TRUE" : "FALSE") + "\n");
			prefsDocument.append("JoystickSwap = " + (prefs.isJoystickSwapEnabled() ? "TRUE" : "FALSE") + "\n");

			if (0 != emu.init(prefsDocument.toString(), 0x0)) {
				Log.e("emu", "failed to initialize emulator kernel");
				return;
			}

			logger.info("initialized emulator kernel");


			DiskImage currentDisk = DiskManager.instance().getCurrent();
			if (null != currentDisk) {
				attachDisk(currentDisk);
			}

			if (null != listener) {
				listener.onEmuStartup();
			}

		}

		emuStatistics.reset();
		clearKeyboardInputQueue();

		long nextUpdateTime = 0;
		long cycleTime = (long) NORMAL_SLEEP_TIME * 1000000;

		boolean vblankOccured = false;

		audioControl.start();
		audioControl.pause();

		EmuPrefs prefs = EmuPrefs.instance();

		while (running && !Thread.interrupted()) {

			if (paused) {

				audioControl.pause();

				while (running && paused && !Thread.interrupted()) {
					try {
						Thread.sleep(PAUSE_SLEEP_TIME);
					} catch (InterruptedException e) {
						break;
					}

					//logger.info("PAUSED...");
				}

				if (Thread.interrupted()) {
					break;
				}

				audioControl.reset();

			} else {

				// time sync on vblank
				if (vblankOccured) {

					emuStatistics.update();

					processKeyboardInputQueue();

					if (false == warpMode) {

						long currentTime = System.nanoTime();

						if (currentTime < nextUpdateTime) {
							long waitTime = (nextUpdateTime - currentTime);

							//LockSupport.parkNanos(waitTime);

							try {
								Thread.sleep((long) (waitTime / 1000000));
							} catch (InterruptedException e) {
								break;
							}
						}

						if (0 == nextUpdateTime) {
							nextUpdateTime = currentTime + cycleTime;
						} else {
							nextUpdateTime += cycleTime;
							if (nextUpdateTime < currentTime - cycleTime * 2) {
								//logger.info("Update limiter (" + (currentTime - nextUpdateTime) + ")");
								nextUpdateTime = currentTime - cycleTime * 2;
							}
						}
					}
				}

				byte[] videoBuffer = (byte[]) videoBuffers[videoBufferIndex];
				byte[] audioBuffer = audioControl.getInputBuffer();

				int flags = 0;

				if (!prefs.isTextureCompressionEnabled()) flags |= FLAG_UNPACK_GRAPHICS;
				if (prefs.isGammaCorrectionEnabled()) flags |= FLAG_USE_GAMMA_CORRECTION;

				/////////////////////////////////////////////////////////////////////////////////
				// UPDATE EMULATOR
				/////////////////////////////////////////////////////////////////////////////////

				int updateStatus = 0;

				synchronized(emuLock) {
					updateStatus = emu.update(stickMask, videoBuffer, audioBuffer, flags);
				}

				/////////////////////////////////////////////////////////////////////////////////
				/////////////////////////////////////////////////////////////////////////////////

				vblankOccured = (1 == updateStatus);

				if (vblankOccured) {

					if (false == textureBufferFilled) {
						textureBufferIndex = videoBufferIndex;
						videoBufferIndex = (videoBufferIndex+1) % NUM_VIDEO_BUFFERS;
						textureBufferFilled = true;
						//logger.info("texture buffer filled");
					}

					audioControl.nextInputBuffer();

				}

			}

		}

		audioControl.cleanup();

		logger.info("shutdown emulator");

		if (null != listener) {
			listener.onEmuShutdown();
		}

		clearKeyboardInputQueue();

		emu.shutdown();
		emu = null;

		running = false;

		logger.info("finished emulator process");
	}

	public synchronized void stop() {

		if (!running) return;

	    running = false;
	    paused = false;

		if (null != emuThread) {
			emuThread.interrupt();
		}

		audioControl.stop();

		if (null != emuThread) {
			try {
				emuThread.join();
			} catch (InterruptedException e) {
				;
			}
			emuThread = null;

			logger.info("stopped emulator");
		}
	}
	
	public synchronized void pause() {
		paused = true;
		logger.info("paused emulator");
	}
	
	public synchronized void resume() {
		paused = false;
		logger.info("resumed emulator");
	}

	public boolean isPaused() {
		return paused;
	}

	public void keyInputDelay(int delayCycles) {
		keyboardInputDelay += delayCycles;
	}
	
	public void keyInput(int keyCode) {
		pushKeyboardInputQueue(keyCode);
	}
	
	public void keyInput(int[] keySequence) {

	    for (int keyCode : keySequence) {
			pushKeyboardInputQueue(keyCode);
	    }
	}

	private void pushKeyboardInputQueue(int keyCode) {
		if (keyboardInputQueue.size() < 1024) {
			keyboardInputQueue.add(keyCode);
		}
	}

	private void clearKeyboardInputQueue() {
		keyboardInputQueue.clear();
	}

	private void processKeyboardInputQueue() {

		if (keyboardInputDelay > 0) {
			keyboardInputDelay--;
			return;
		}

		Integer i = keyboardInputQueue.poll();
		if (null != i) {
			emu.input(i);
			//logger.info("injected key: 0x" + Integer.toHexString(i&0xff) + " (0x" + Integer.toHexString(i) + ") stick: 0x" + Integer.toHexString(stickMask));
			//keyboardInputDelay += 2; // add 2 VBlanks
		}
	}
	
	private void sendCommand(int command) {

		if (null == emu) {
			return;
		}

		synchronized (emuLock) {
			emu.command(command);
		}
	}

	public boolean attachDisk(DiskImage image) {

		byte[] imageBuffer = image.load();
		if (null != imageBuffer) {
			DiskManager.instance().setCurrent(image);
			return attachDisk(image.getType(), imageBuffer, imageBuffer.length,  image.getUrl());
		}

		return false;
	}

	private boolean attachDisk(int type, byte[] data, int dataLength, String filename) {

		int status = 0;

		synchronized(emuLock) {
			status = emu.load(type, data, dataLength, filename);
		}

		if (0 != status) {
			logger.warning("failed to attach disk");
			return false;
		}

		keyboardInputDelay = 50; // delay keyboard input for X VBlanks

		return true;
	}

	public byte[] lockTextureData() {
		
		if (false == textureBufferFilled) {
			//logger.info("lock texture data: buffer not filled");
			return null;
		}

		byte[] buffer = (byte[]) videoBuffers[textureBufferIndex];

		if (null == buffer) {
			logger.warning("lock texture data: buffer is NULL");
		}

		//logger.info("lock texture data: successfull");
		
		return buffer;
	}
	
	public void unlockTextureData() {
		textureBufferFilled = false;
	}
	
	public void setStick(int stickMask) {
		if (this.stickMask != stickMask) {
			this.stickMask = stickMask;
            //logger.info("COMMAND setStick(" + stickMask + ")");
		}
	}
	
	public void setStickFlag(int flag) {
		setStick(this.stickMask | flag);
	}
	
	public void clearStickFlag(int flag) {
		setStick(this.stickMask & (~flag));
	}
	
	public int getStick() {
		return this.stickMask;
	}

    public double getUpdatesPerSecond() {
        return emuStatistics.getUpdatesPerSecond();
    }

	public void softReset(DiskImage diskImage) {
		sendCommand(EmuBindings.COMMAND_RESET);
		keyboardInputDelay = 50;
	}

	public void hardReset(DiskImage autoStartImage) {
		stop();
		DiskManager.instance().setCurrent(autoStartImage);
		start();
		keyboardInputDelay = 50;
	}

	public void setTrueDriveMode(boolean enabled) {
		sendCommand(enabled ? EmuBindings.COMMAND_TRUEDRIVE_ON : EmuBindings.COMMAND_TRUEDRIVE_OFF);
	}

	public void setJoystickSwap(boolean enabled) {
		sendCommand(enabled ? EmuBindings.COMMAND_JOYSTICK_SWAP_ON : EmuBindings.COMMAND_JOYSTICK_SWAP_OFF);
	}

	public void setWarpMode(boolean warpEnabled) {
		warpMode = warpEnabled;
	}

	public void setReverbEnabled(boolean reverbEnabled) {
		if (null != audioControl) {
			audioControl.setReverb(reverbEnabled);
		}
	}

	public void setAudioVolume(int volume) {
		if (null != audioControl) {
			audioControl.setVolume(volume);
		}
	}

	public void storeSnapshot() {

		if (null == snapshotBuffer) {
			snapshotBuffer = new byte[128*1024];
		}

		int result = 0;

		synchronized (emuLock) {
			result = emu.store(DiskImage.TYPE_SNAPSHOT, snapshotBuffer, snapshotBuffer.length);
		}

		if (result < 1) {
			logger.info("failed to store snapshot");
			snapshotBufferUsage = 0;
			return;
		}

		snapshotBufferUsage = result;

		logger.info("stored snapshot: " + snapshotBufferUsage + " bytes");

		DiskManager.instance().storeSnapshot(snapshotBuffer, snapshotBufferUsage);

	}

	public void restoreSnapshot() {
		if (null == snapshotBuffer || snapshotBufferUsage == 0) {
			return;
		}

		int result = 0;

		synchronized (emuLock) {
			result = emu.load(DiskImage.TYPE_SNAPSHOT, snapshotBuffer, snapshotBufferUsage, "snapshot");
		}

		if (result != 0) {
			logger.info("failed to restore snapshot");
			return;
		}

		logger.info("restored snapshot");
	}

}
