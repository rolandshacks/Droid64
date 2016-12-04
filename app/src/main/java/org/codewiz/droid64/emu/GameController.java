package org.codewiz.droid64.emu;

import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.codewiz.droid64.util.LogManager;
import org.codewiz.droid64.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roland on 15.08.2016.
 */

public class GameController {

    private final static Logger logger = LogManager.getLogger(GameController.class.getName());

    public static int ID_NONE = 0x0;
    public static int ID_FIRE = 0x1;
    public static int ID_LEFT = 0x2;
    public static int ID_RIGHT = 0x4;
    public static int ID_UP = 0x8;
    public static int ID_DOWN = 0x10;

    public static int ID_BUTTON_A = 0x100;
    public static int ID_BUTTON_B = 0x200;
    public static int ID_BUTTON_X = 0x400;
    public static int ID_BUTTON_Y = 0x800;
    public static int ID_BUTTON_SELECT = 0x1000;
    public static int ID_BUTTON_START = 0x2000;
    public static int ID_BUTTON_L1 = 0x4000;
    public static int ID_BUTTON_L2 = 0x8000;
    public static int ID_BUTTON_R1 = 0x10000;
    public static int ID_BUTTON_R2 = 0x20000;
    public static int ID_BUTTON_THUMB_LEFT = 0x40000;
    public static int ID_BUTTON_THUMB_RIGHT = 0x80000;

    public static int ID_BUTTON_MENU = 0x100000;
    public static int ID_BUTTON_PLAY_PAUSE = 0x200000;
    public static int ID_BUTTON_BACK = 0x400000;
    public static int ID_BUTTON_REWIND = 0x800000;
    public static int ID_BUTTON_FAST_FORWARD = 0x1000000;

    private List<GameControllerListener> listeners = new ArrayList<GameControllerListener>();

    private int deviceId;
    private List<Integer> deviceIds;

    private boolean stateLeft;
    private boolean stateRight;
    private boolean stateUp;
    private boolean stateDown;

    private boolean stateCenter;
    private boolean stateA;
    private boolean stateB;
    private boolean stateX;
    private boolean stateY;
    private boolean stateSelect;
    private boolean stateStart;
    private boolean stateL1;
    private boolean stateL2;
    private boolean stateR1;
    private boolean stateR2;
    private boolean stateThumbLeft;
    private boolean stateThumbRight;

    private boolean stateMenu;
    private boolean statePlayPause;
    private boolean stateBack;
    private boolean stateRewind;
    private boolean stateFastForward;

    private float stickX;
    private float stickY;

    private int stateMask = ID_NONE;

    private boolean stateFire;

    public GameController() {
        init();
        deviceIds = getDeviceIds();
    }

    public void init() {
        
        stateLeft = false;
        stateRight = false;
        stateUp = false;
        stateDown = false;

        stateCenter = false;
        stateA = false;
        stateB = false;
        stateX = false;
        stateY = false;
        stateSelect = false;
        stateStart = false;
        stateL1 = false;
        stateL2 = false;
        stateR1 = false;
        stateR2 = false;
        stateThumbLeft = false;
        stateThumbRight = false;
        stateMenu = false;
        statePlayPause = false;
        stateBack = false;
        stateRewind = false;
        stateFastForward = false;

        stickX = 0.0f;
        stickY = 0.0f;

        stateMask = ID_NONE;

        stateFire = false;

    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    private List<Integer> getDeviceIds() {

        List<Integer> gameControllerDeviceIds = new ArrayList<Integer>();
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            int sources = dev.getSources();

            // Verify that the device has gamepad buttons, control sticks, or both.
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((sources & InputDevice.SOURCE_JOYSTICK)
                    == InputDevice.SOURCE_JOYSTICK)) {
                // This device is a game controller. Store its device ID.
                if (!gameControllerDeviceIds.contains(deviceId)) {
                    logger.info("found game controller: " + deviceId);
                    gameControllerDeviceIds.add(deviceId);
                }
            }
        }

        if (gameControllerDeviceIds.isEmpty()) {
            return null;
        }

        return gameControllerDeviceIds;
    }

    public boolean handleEvent(KeyEvent event) {

        if (0 == deviceId) {
            deviceId = event.getDeviceId();
        }

        logger.info("gamecontroller: " + event.toString());

        int buttonCode = event.getKeyCode();
        boolean buttonState = (event.getAction() == KeyEvent.ACTION_DOWN);

        boolean handled = true;
        boolean stickChange = false;

        switch (buttonCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER: {
                stateCenter = buttonState;
                break;
            }
            case KeyEvent.KEYCODE_BUTTON_A: {
                stateA = buttonState;
                fireButtonEvent(ID_BUTTON_A, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_BUTTON_B: {
                stateB = buttonState;
                fireButtonEvent(ID_BUTTON_B, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_BUTTON_X: {
                stateX = buttonState;
                fireButtonEvent(ID_BUTTON_X, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_BUTTON_Y: {
                stateY = buttonState;
                fireButtonEvent(ID_BUTTON_Y, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_BUTTON_SELECT: {
                stateSelect = buttonState;
                fireButtonEvent(ID_BUTTON_SELECT, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_BUTTON_START: {
                stateStart = buttonState;
                fireButtonEvent(ID_BUTTON_START, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_BUTTON_L1: {
                stateL1 = buttonState;
                fireButtonEvent(ID_BUTTON_L1, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_BUTTON_L2: {
                stateL2 = buttonState;
                fireButtonEvent(ID_BUTTON_L2, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_BUTTON_R1: {
                stateR1 = buttonState;
                fireButtonEvent(ID_BUTTON_R1, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_BUTTON_R2: {
                stateR2 = buttonState;
                fireButtonEvent(ID_BUTTON_R2, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_BUTTON_THUMBL: {
                stateThumbLeft = buttonState;
                fireButtonEvent(ID_BUTTON_THUMB_LEFT, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_BUTTON_THUMBR: {
                stateThumbRight = buttonState;
                fireButtonEvent(ID_BUTTON_THUMB_RIGHT, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_BACK: {
                stateBack = buttonState;
                fireButtonEvent(ID_BUTTON_BACK, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_MENU: {
                stateMenu = buttonState;
                fireButtonEvent(ID_BUTTON_MENU, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: {
                statePlayPause = buttonState;
                fireButtonEvent(ID_BUTTON_PLAY_PAUSE, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_MEDIA_REWIND: {
                stateRewind = buttonState;
                fireButtonEvent(ID_BUTTON_REWIND, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD: {
                stateFastForward = buttonState;
                fireButtonEvent(ID_BUTTON_FAST_FORWARD, buttonState);
                break;
            }
            case KeyEvent.KEYCODE_DPAD_UP: {
                //stick = (stick & (~KeyCode.C64STICK_UP)) | (keyDown ? KeyCode.C64STICK_UP : 0);
                stateUp = buttonState;
                stickChange = true;
                break;
            }
            case KeyEvent.KEYCODE_DPAD_DOWN: {
                //stick = (stick & (~KeyCode.C64STICK_DOWN)) | (keyDown ? KeyCode.C64STICK_DOWN : 0);
                stateDown = buttonState;
                stickChange = true;
                break;
            }
            case KeyEvent.KEYCODE_DPAD_LEFT: {
                //stick = (stick & (~KeyCode.C64STICK_LEFT)) | (keyDown ? KeyCode.C64STICK_LEFT : 0);
                stateLeft = buttonState;
                stickChange = true;
                break;
            }
            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                //stick = (stick & (~KeyCode.C64STICK_RIGHT)) | (keyDown ? KeyCode.C64STICK_RIGHT : 0);
                stateRight = buttonState;
                stickChange = true;
                break;
            }
            default: {
                handled = false;
                break;
            }
        }

        stateFire = (stateCenter || stateA);

        int oldState = stateMask;
        stateMask = (stateMask & (~ID_FIRE)) | (stateFire ? ID_FIRE : 0);

        stateMask = (stateMask & 0xff) |
                (stateA ? ID_BUTTON_A : 0) |
                (stateB ? ID_BUTTON_B : 0) |
                (stateX ? ID_BUTTON_X : 0) |
                (stateY ? ID_BUTTON_Y : 0) |
                (stateSelect ? ID_BUTTON_SELECT : 0) |
                (stateStart ? ID_BUTTON_START : 0) |
                (stateL1 ? ID_BUTTON_L1 : 0) |
                (stateL2 ? ID_BUTTON_L2 : 0) |
                (stateR1 ? ID_BUTTON_R1 : 0) |
                (stateR2 ? ID_BUTTON_R2 : 0) |
                (stateMenu ? ID_BUTTON_MENU : 0) |
                (statePlayPause ? ID_BUTTON_PLAY_PAUSE : 0) |
                (stateBack ? ID_BUTTON_BACK : 0) |
                (stateRewind ? ID_BUTTON_REWIND : 0) |
                (stateFastForward ? ID_BUTTON_FAST_FORWARD : 0) |
                0;

        if (stickChange) {
            stateMask =
                    (stateMask & (0xfffff00 | ID_FIRE)) |
                            (stateLeft ? ID_LEFT : 0) |
                            (stateRight ? ID_RIGHT : 0) |
                            (stateUp ? ID_UP : 0) |
                            (stateDown ? ID_DOWN : 0);
        }

        logger.info("buttons: 0x" + Integer.toHexString(stateMask));

        if (stateMask != oldState) {
            onStateChange(stateMask, oldState);
        }

        return handled;
    }

    public boolean handleEvent(MotionEvent event) {
        //logger.info("motion event: " + event);

        boolean stateChange = false;

        // Process all historical movement samples in the batch
        final int historySize = event.getHistorySize();

        // Process the movements starting from the
        // earliest historical position in the batch
        for (int i = 0; i < historySize; i++) {
            // Process the event at historical position i
            //logger.info("historic joystic data...");
            if (processJoystickInput(event, i)) {
                stateChange = true;
            }
        }

        // Process the current movement sample in the batch (position -1)
        //logger.info("current joystic data...");
        if (processJoystickInput(event, -1)) {
            stateChange = true;
        }

        return stateChange;
    }

    private boolean processJoystickInput(MotionEvent event,
                                      int historyPos) {

        //logger.info("process input (" + historyPos + ")");

        InputDevice inputDevice = event.getDevice();

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        float x = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_X, historyPos);
        if (x == 0.0f) {
            x = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_HAT_X, historyPos);
        }
        if (x == 0.0f) {
            x = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Z, historyPos);
        }

        // Calculate the vertical distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat switch, or the right control stick.
        float y = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Y, historyPos);
        if (y == 0.0f) {
            y = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_HAT_Y, historyPos);
        }
        if (y == 0.0f) {
            y = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_RZ, historyPos);
        }

        stickX = x;
        stickY = y;

        float triggerX = 0.5f;
        float triggerY = 0.5f;

        stateLeft = (stickX < -triggerX);
        stateRight = (stickX > triggerX);
        stateUp = (stickY < -triggerY);
        stateDown = (stickY > triggerY);

        int oldState = stateMask;

        stateMask =
                (stateMask & (0xfffff00 | ID_FIRE)) |
                (stateLeft ? ID_LEFT : 0) |
                (stateRight ? ID_RIGHT : 0) |
                (stateUp ? ID_UP : 0) |
                (stateDown ? ID_DOWN : 0);

        logger.info("stick: " + stickX + " / " + stickY + " / state: 0x" + Integer.toHexString(stateMask));

        if (stateMask != oldState) {
            onStateChange(stateMask, oldState);
            return true;
        }

        return false;
    }

    private void onStateChange(int state, int oldState) {
        logger.info("gamecontroller state change: " + Integer.toHexString(state) + " / " + Integer.toHexString(oldState));
    }

    public int getState() {
        return stateMask;
    }

    public boolean isPressedFire() {
        return stateFire;
    }

    public boolean isPressedLeft() {
        return stateLeft;
    }

    public boolean isPressedRight() {
        return stateRight;
    }

    public boolean isPressedUp() {
        return stateUp;
    }

    public boolean isPressedDown() {
        return stateDown;
    }

    public static boolean isGameControllerEvent(InputEvent event) {
        if (((event.getSource() & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) ||
                ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)){
            return true;
        }

        return false;
    }

    private static float getCenteredAxis(MotionEvent event, InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    public void addListener(GameControllerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameControllerListener listener) {
        listeners.remove(listener);
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    private void fireButtonEvent(int buttonId, boolean buttonState) {
        for (GameControllerListener listener : listeners) {
            if (buttonState) {
                listener.onButtonDown(buttonId);
            } else {
                listener.onButtonUp(buttonId);
            }
        }
    }
}
