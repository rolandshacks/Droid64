package org.codewiz.droid64.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import org.codewiz.droid64.FullscreenActivity;
import org.codewiz.droid64.emu.EmuControl;
import org.codewiz.droid64.emu.KeyCode;
import org.codewiz.droid64.util.LogManager;
import org.codewiz.droid64.util.Logger;
import org.codewiz.droid64.util.Util;

/**
 * Created by roland on 19.08.2016.
 */

public class VirtualGamepad {

    private final static Logger logger = LogManager.getLogger(FullscreenActivity.class.getName());

    private static VirtualGamepad _instance;

    private boolean leftPressed = false;
    private long leftPressedTime = 0;
    private boolean rightPressed = true;
    private float stickX = 0.0f;
    private float stickY = 0.0f;
    private float stickXGap = 0.0f;
    private float stickYGap = 0.0f;

    private float pixelsPerCmX;
    private float pixelsPerCmY;

    private float stickVisibility = 0.0f;

    private boolean pressed = false;
    private float lastX, lastY;

    public static VirtualGamepad instance() {
        if (null == _instance) {
            _instance = new VirtualGamepad();
        }

        return _instance;
    }

    public VirtualGamepad() {

    }

    public float getStickX() {
        return stickX;
    }
    public float getStickY() {
        return stickY;
    }
    public float getStickXGap() { return stickXGap; }
    public float getStickYGap() { return stickYGap; }

    public void init(Context context, View v) {

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        pixelsPerCmX = ((metrics.xdpi > 100.0f) ? metrics.xdpi : DisplayMetrics.DENSITY_HIGH) / 2.54f;
        pixelsPerCmY = ((metrics.ydpi > 100.0f) ? metrics.ydpi : DisplayMetrics.DENSITY_HIGH) / 2.54f;

        stickX = 1.5f * pixelsPerCmX;
        stickY = metrics.heightPixels/2.0f;

        stickXGap = pixelsPerCmX*0.8f;
        stickYGap = pixelsPerCmY*0.8f;

        lastX = 0.0f;
        lastY = 0.0f;

        pressed = false;
    }

    public void onTouch(View v, MotionEvent e) {

        EmuControl emu = EmuControl.instance();

        int oldStick = emu.getStick();

        int stick = oldStick;

        int action = e.getActionMasked();

        int numPointers = Math.min(2, e.getPointerCount());

        if (MotionEvent.ACTION_MOVE == action) {

            for (int p=0; p<numPointers; p++) {
                stick = updateStick(v, action, e.getX(p), e.getY(p), stick);
            }

        } else {
            //logger.info(e.toString());
            int p = e.getActionIndex();
            stick = updateStick(v, action, e.getX(p), e.getY(p), stick);
        }

        /*
    	if (stick != oldStick) {
    	    displayStick(stick);
    	}
    	*/

        emu.setStick(stick);
    }

    private int updateStick(View v, int action, float x, float y, int stick) {

        updateState();

        if (null == v) return 0x0;

        boolean isLeft = (x < (float) v.getWidth() / 2.0f);

        float xDelta = 0.0f;
        float yDelta = 0.0f;

        if (MotionEvent.ACTION_DOWN == action ||
            MotionEvent.ACTION_POINTER_DOWN == action ||
            MotionEvent.ACTION_MOVE == action) {

            if (isLeft) {
                leftPressed = true;
                leftPressedTime = System.currentTimeMillis();

                if (MotionEvent.ACTION_MOVE == action) {
                    xDelta = x - lastX;
                    yDelta = y - lastY;
                }

                lastX = x;
                lastY = y;

            } else {
                rightPressed = true;
            }

        } else if (MotionEvent.ACTION_UP == action ||
                MotionEvent.ACTION_POINTER_UP == action) {
            if (isLeft) {
                leftPressed = false;
                leftPressedTime = System.currentTimeMillis();
            } else {
                rightPressed = false;
            }
        }

        if (isLeft) {

            stick &= KeyCode.C64STICK_FIRE;

            if (leftPressed) {

                if (pressed) { // just moving

                    if (x < stickX - stickXGap / 2.0f) {
                        stick |= KeyCode.C64STICK_LEFT;
                    } else if (x > stickX + stickXGap / 2.0f) {
                        stick |= KeyCode.C64STICK_RIGHT;
                    }

                    if (y < stickY - stickYGap / 2.0f) {
                        stick |= KeyCode.C64STICK_UP;
                    } else if (y > stickY + stickYGap / 2.0f) {
                        stick |= KeyCode.C64STICK_DOWN;
                    }

                    float i = 0.2f;
                    float j = 1.0f - i;

                    if (Math.abs(stickX-x) > stickXGap) stickX = x * i + stickX * j;
                    if (Math.abs(stickY-y) > stickYGap) stickY = y * i + stickY * j;

                } else {

                    // fresh press - do init

                    pressed = true;
                    stickX = x;
                    stickY = y;

                }

                if (stickX < stickXGap/2.0f) stickX = stickXGap/2.0f;
                if (stickY < stickYGap/2.0f) stickY = stickYGap/2.0f;

            }

        } else {

            stick &= (~KeyCode.C64STICK_FIRE);
            stick |= (rightPressed ? KeyCode.C64STICK_FIRE : 0);

        }

        return stick;
    }

    public void updateState() {

        if (true == leftPressed) {
            stickVisibility = 1.0f;
            return;
        }

        long currentTime = System.currentTimeMillis();
        long elapsed = (currentTime - leftPressedTime);

        if (elapsed < 1000) {
            stickVisibility = 1.0f;
        } else if (elapsed < 2000) {
            stickVisibility = ((float) (2000 - elapsed)) / 1000.0f;
        } else {
            stickVisibility = 0.0f;
        }

        if (true == pressed && elapsed > 500) { // released more than x millis
            pressed = false;
        }

        //logger.info("V: " + stickVisibility + " / " + pressed);

    }

    public float getVisibility() {

        updateState();

        return stickVisibility;
    }

}
