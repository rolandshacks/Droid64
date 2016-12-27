package system;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.PresetReverb;
import android.os.Process;

import emu.Control;
import util.LogManager;
import util.Logger;

/**
 * Created by roland on 12.09.2016.
 */

public class AudioControl {

    private final static Logger logger = LogManager.getLogger(AudioControl.class.getName());

    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_SAMPLES = AUDIO_SAMPLE_RATE / Control.FRAMES_PER_SECOND;
    private static final int AUDIO_SAMPLE_BITS = 16;
    private static final int NUM_AUDIO_BUFFERS = 6;
    private static final int MAX_FILLED_AUDIO_BUFFERS = NUM_AUDIO_BUFFERS-1;
    private static final int MIN_FILLED_AUDIO_BUFFERS = MAX_FILLED_AUDIO_BUFFERS;
    private static final int AUDIO_BUFFER_SIZE = AUDIO_SAMPLES * AUDIO_SAMPLE_BITS / 8;

    private Thread audioThread;
    private Object[] audioBuffers = new Object[NUM_AUDIO_BUFFERS];
    private byte[] audioBufferZero;
    private volatile int audioOutputBufferFilled;
    private volatile boolean audioOutputBufferReady;
    private int audioBufferInputIndex;
    private int audioBufferOutputIndex;
    private final Object audioDataAvailableSync = new Object();

    private AudioTrack audioTrack;
    private PresetReverb audioReverb;

    private volatile boolean running;

    public AudioControl() {
        ;
    }

    public void init() {

        audioBufferZero = new byte[AUDIO_BUFFER_SIZE];
        for (int b=0; b<AUDIO_BUFFER_SIZE; b++) {
            audioBufferZero[b] = (byte) 0;
        }

        for (int i=0; i<NUM_AUDIO_BUFFERS; i++) {
            audioBuffers[i] = new byte[AUDIO_BUFFER_SIZE];
        }

    }

    public void cleanup() {
        if (null != audioTrack) {
            audioTrack.pause();
            audioTrack.flush();
            audioTrack.release();
            audioTrack = null;
        }
    }

    public void start() {

        running = true;

        Preferences prefs = Preferences.instance();

        int minBufferSize = 2 * AudioTrack.getMinBufferSize(AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int minBufferFrames = 5;

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                Math.max(AUDIO_BUFFER_SIZE * minBufferFrames, minBufferSize),
                AudioTrack.MODE_STREAM);

        setVolume(prefs.getAudioVolumePercent());

        // initialize reverb effect
        try {
            audioReverb = new PresetReverb(0, audioTrack.getAudioSessionId());
            audioReverb.setPreset(PresetReverb.PRESET_LARGEHALL);
            setReverb(prefs.isReverbEnabled());
        } catch (IllegalArgumentException e) {
            audioReverb = null;
            logger.info("Audio reverb not supported. Disabling effect.");
        }

        if (null != audioReverb) {
            audioTrack.attachAuxEffect(audioReverb.getId());
            audioTrack.setAuxEffectSendLevel(1.0f);
        }

        audioThread = new Thread(new Runnable() {

            @Override
            public void run() {
                audioLoop();
            }

        });

        try {
            audioThread.setPriority(Thread.MAX_PRIORITY);
        } catch (SecurityException e) {
            logger.warning("MAX_PRIORITY not allowed for audio emuThread");
        }

        audioThread.start();
        logger.info("started audio output");

    }

    public void stop() {

        running = false;

        if (null != audioThread) {
            audioThread.interrupt();
        }

        if (null != audioThread) {
            try {
                audioThread.join();
            } catch (InterruptedException e) {
                ;
            }
            audioThread = null;
            logger.info("stopped audio output");
        }

    }

    public void pause() {

        if (null != audioTrack) {
            audioTrack.pause();
            audioTrack.flush();
        }
    }

    public void resume() {

        if (null != audioTrack) {
            audioTrack.play();
        }
    }

    public byte[] getInputBuffer() {
        return (byte[]) audioBuffers[audioBufferInputIndex];
    }

    public void nextInputBuffer() {

        if (audioOutputBufferFilled < MAX_FILLED_AUDIO_BUFFERS) {
            audioOutputBufferFilled++;
            audioBufferInputIndex = (audioBufferInputIndex+1) % NUM_AUDIO_BUFFERS;
            //logger.info("audio: set input buffer " + audioBufferInputIndex);

            if (false == audioOutputBufferReady) {
                if (audioOutputBufferFilled >= MIN_FILLED_AUDIO_BUFFERS) {

                    synchronized(audioDataAvailableSync) {
                        //logger.trace("audio recovered buffer underrun. signalling new data to audio emuThread.");
                        audioOutputBufferReady = true;
                        audioDataAvailableSync.notify();
                    }
                }
            }
        }
    }

    private void audioLoop() {

        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

        while (running && !Thread.interrupted()) {
            boolean status = updateAudio();
            if (false == status) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        running = false;

        logger.info("stopped audio loop");
    }

    public boolean updateAudio() {

        if (null == audioTrack) {
            return false;
        }

        //Log.w("emu", "audio statis: " + audioBufferInputIndex + " / " + audioBufferOutputIndex + " / " + audioOutputBufferFilled);

        // get audio buffer
        if (audioOutputBufferFilled < 1) {

            if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.pause();
            }

            //logger.trace("audio paused");

            audioOutputBufferReady = false;

            synchronized(audioDataAvailableSync) {

                if (audioOutputBufferReady == false) {
                    //logger.info("audio thread buffer underrun. waiting for new data.");
                    try {
                        audioDataAvailableSync.wait();
                    } catch (InterruptedException e) {
                        return true;
                    }

                }
            }

            if (audioOutputBufferReady == false) {
                //logger.info("audio thread has no data");
                return true;
            }

            audioTrack.play();
            //logger.trace("audio resumed");

        }

        //logger.info("audio: reading from buffer " + audioBufferOutputIndex);

        byte[] audioData = (byte[]) audioBuffers[audioBufferOutputIndex];

        // playback
        audioTrack.write(audioData, 0,  audioData.length);

        // free buffer and increase index
        if (audioOutputBufferFilled > 0) {
            audioOutputBufferFilled--;
            audioBufferOutputIndex = (audioBufferOutputIndex+1) % NUM_AUDIO_BUFFERS;
        }

        return true;
    }

    public void reset() {
        audioBufferInputIndex = 0;
        audioBufferOutputIndex = 0;
        audioOutputBufferFilled = 0;
        audioOutputBufferReady = false;
    }

    public void setReverb(boolean reverbEnabled) {
        if (null != audioReverb) {
            audioReverb.setEnabled(reverbEnabled);
        }
    }

    public void setVolume(int volume) {
        if (null != audioTrack) {
            float gain = ((float) volume) / 100.0f;
            audioTrack.setStereoVolume(gain, gain);
        }
    }
}
