package org.codewiz.droid64;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import org.codewiz.droid64.emu.DiskFilter;
import org.codewiz.droid64.emu.DiskImage;
import org.codewiz.droid64.emu.DiskManager;
import org.codewiz.droid64.emu.EmuControl;
import org.codewiz.droid64.emu.EmuListener;
import org.codewiz.droid64.emu.EmuPrefs;
import org.codewiz.droid64.emu.GameController;
import org.codewiz.droid64.emu.GameControllerListener;
import org.codewiz.droid64.emu.KeyCode;
import org.codewiz.droid64.emu.KeyMap;
import org.codewiz.droid64.emu.KeyMapEntry;
import org.codewiz.droid64.emu.KeySequence;
import org.codewiz.droid64.util.LogManager;
import org.codewiz.droid64.util.Logger;
import org.codewiz.droid64.util.SystemUiHider;
import org.codewiz.droid64.view.EmuControlFragment;
import org.codewiz.droid64.view.EmuViewFragment;
import org.codewiz.droid64.view.VirtualGamepad;

import java.util.HashMap;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends FragmentActivity implements EmuListener, FileDialog.OnDiskSelectHandler, EmuControlFragment.OnFragmentInteractionListener, EmuViewFragment.OnFragmentInteractionListener {

    private final static Logger logger = LogManager.getLogger(FullscreenActivity.class.getName());

    private static final int AREA_TOP = 0x1;
    private static final int AREA_MIDDLE = 0x2;
    private static final int AREA_BOTTOM = 0x4;
    private static final int AREA_LEFT = 0x10;
    private static final int AREA_CENTER = 0x20;
    private static final int AREA_RIGHT = 0x40;

    private static final int COMMAND_CLICK_AREA = 15;
    private static final boolean EMU_PAUSED_WHILE_CONTROL = false;

    private EmuPrefs emuPrefs;
    private EmuControl emuControl;

    private View emuView;
    private View controlsView;
    private android.app.DialogFragment settingsDialog;
    private android.app.DialogFragment fileDialog;

    private int eventSourceViewId = 0;

    private float mouseX = 0.0f;
    private float mouseY = 0.0f;

    private float mouseDownX = 0.0f;
    private float mouseDownY = 0.0f;

    private float screenWidth = 0.0f;
    private float screenHeight = 0.0f;

    private boolean mouseDown = false;
    private boolean keyboardVisible = false;

    private GameController gameController;
    private DiskManager diskManager;

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            Integer itemId = mIdMap.get(item);
            return itemId;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    private static final int REQUEST_READWRITE_STORAGE = 1234;

    public FullscreenActivity() {
        instantiateEmu();
    }

    private void instantiateEmu() {
        emuPrefs = EmuPrefs.instance();
        if (null == emuPrefs) emuPrefs = new EmuPrefs();

        diskManager = new DiskManager();

        emuControl = EmuControl.instance();
        if (null == emuControl) emuControl = new EmuControl();

        emuPrefs.init(this);
        emuControl.addListener(this);
        emuControl.init();
    }

    private boolean checkPermissions() {

        int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        logger.info("Permission check: " + permissionCheck1 + " / " + permissionCheck2);

        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED ||
            permissionCheck2 != PackageManager.PERMISSION_GRANTED) {

            logger.info("Permission check failed - request permissions");

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READWRITE_STORAGE);
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_READWRITE_STORAGE) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                //finishCreationStep();

                logger.info("PERMISSION HAS BEEN GRANTED!!!");
            }
        }
    }

    boolean isEmuView(View v) {
        if (null == v) {
            return false;
        }

        int id = v.getId();

        if (id == eventSourceViewId) {
            return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger.info("Activity.onCreate()");

        diskManager.bindContext(this.getApplicationContext());

        keyboardVisible = false;

        checkOpenGL();

        checkPermissions();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen);

        emuPrefs.load();

        emuView = findViewById(R.id.view_fragment);
        eventSourceViewId = R.id.view_fragment;

        controlsView = findViewById(R.id.controls_fragment);

        createKeyButtons();

        emuView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (0.0f == screenWidth && 0.0f == screenHeight) {
                    updateScreenSize();
                }
            }

        });

        emuView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent e) {

                int action = e.getActionMasked();

                if (MotionEvent.ACTION_DOWN == action || MotionEvent.ACTION_POINTER_DOWN == action) {

                    if (isControlsVisible()) {
                        logger.info("Disabled controls");
                        setControlsVisible(false);
                        return true;
                    }

                    mouseDown = true;

                    mouseX = e.getX();
                    mouseY = e.getY();

                    mouseDownX = mouseX;
                    mouseDownY = mouseY;

                    int area = getTouchArea(mouseX, mouseY);
                    if (0 != (area & AREA_MIDDLE)) {
                        if (isEmuView(v)) {
                            VirtualGamepad.instance().onTouch(v, e);
                        } else {
                            return false; // wrong view
                        }
                    }

                } else if (MotionEvent.ACTION_UP == action || MotionEvent.ACTION_POINTER_UP == action) {

                    if (isControlsVisible()) {
                        logger.info("Disabled controls");
                        setControlsVisible(false);
                        return false;
                    }

                    if (!mouseDown) {
                        return false;
                    }

                    mouseDown = false;
                    mouseX = e.getX();
                    mouseY = e.getY();

                    int area = getTouchArea(mouseX, mouseY);
                    if (0 != (area & AREA_MIDDLE)) {
                        if (isEmuView(v)) {
                            //logger.info("Clicked to emuview");
                            VirtualGamepad.instance().onTouch(v, e);
                        } else {
                            return false; // wrong view
                        }
                    } else {

                        // logger.info("H DISTANCE: " + Math.abs(mouseX - mouseDownX));
                        // logger.info("V DISTANCE: " + Math.abs(mouseY - mouseDownY));

                        if (Math.abs(mouseX - mouseDownX) < (float) COMMAND_CLICK_AREA
                                && Math.abs(mouseY - mouseDownY) < (float) COMMAND_CLICK_AREA) {
                            handleCommandClick(area);
                        }
                    }

                } else if (MotionEvent.ACTION_MOVE == action) {

                    mouseX = e.getX();
                    mouseY = e.getY();

                    if (isControlsVisible()) {
                        return true;
                    }

                    int area = getTouchArea(mouseX, mouseY);
                    if (0 != (area & AREA_MIDDLE)) {
                        if (isEmuView(v)) {
                            VirtualGamepad.instance().onTouch(v, e);
                        } else {
                            return false; // wrong view
                        }
                    }

                } else {
                    return false;
                }

                return true;
            }
        });

        emuView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //logger.info("focus change: " + v.getId() + " status: " + hasFocus);

                if (isEmuView(v)) {
                    if (hasFocus && isControlsVisible()) {
                        setControlsVisible(false);
                    }
                }
            }
        });

        setControlsVisible(false);

        gameController = new GameController();
        gameController.addListener(new GameControllerListener() {
            @Override
            public void onButtonDown(int buttonId) {

                logger.info("game controller button down: 0x" + Integer.toHexString(buttonId));

                if (buttonId == GameController.ID_BUTTON_L1) { // F1

                    emuControl.keyInput(KeyCode.C64KEY_F1F2 | KeyCode.KEYFLAG_PRESSED);
                    emuControl.keyInput(KeyCode.C64KEY_F1F2 | KeyCode.KEYFLAG_RELEASED);

                } else if (buttonId == GameController.ID_BUTTON_L2) { // F2

                    emuControl.keyInput(KeyCode.C64KEY_F1F2 | KeyCode.C64KEY_FLAG_SHIFT | KeyCode.KEYFLAG_PRESSED);
                    emuControl.keyInput(KeyCode.C64KEY_F1F2 | KeyCode.C64KEY_FLAG_SHIFT | KeyCode.KEYFLAG_RELEASED);

                } else if (buttonId == GameController.ID_BUTTON_R1) { // F3

                    emuControl.keyInput(KeyCode.C64KEY_F3F4 | KeyCode.KEYFLAG_PRESSED);
                    emuControl.keyInput(KeyCode.C64KEY_F3F4 | KeyCode.KEYFLAG_RELEASED);

                } else if (buttonId == GameController.ID_BUTTON_R2) { // F4

                    emuControl.keyInput(KeyCode.C64KEY_F3F4 | KeyCode.C64KEY_FLAG_SHIFT | KeyCode.KEYFLAG_PRESSED);
                    emuControl.keyInput(KeyCode.C64KEY_F3F4 | KeyCode.C64KEY_FLAG_SHIFT | KeyCode.KEYFLAG_RELEASED);

                } else if (buttonId == GameController.ID_BUTTON_Y) { // SPACE key

                    emuControl.keyInput(KeyCode.C64KEY_SPACE | KeyCode.KEYFLAG_PRESSED);
                    emuControl.keyInput(KeyCode.C64KEY_SPACE | KeyCode.KEYFLAG_RELEASED);

                } else if (buttonId == GameController.ID_BUTTON_START) {

                    emuControl.keyInput(KeySequence.sequenceAllKeys);

                } else if (buttonId == GameController.ID_BUTTON_SELECT || buttonId == GameController.ID_BUTTON_MENU) {

                    setControlsVisible(!isControlsVisible());

                } else if (buttonId == GameController.ID_BUTTON_PLAY_PAUSE) {

                    if (emuControl.isPaused()) {
                        emuControl.resume();
                    } else {
                        emuControl.pause();
                    }

                } else if (buttonId == GameController.ID_BUTTON_REWIND) {

                    if (emuPrefs.isWarpEnabled()) {
                        emuPrefs.setWarpEnabled(false);
                    }

                } else if (buttonId == GameController.ID_BUTTON_FAST_FORWARD) {

                    if (emuControl.isPaused()) {
                        emuControl.resume();
                    }

                    emuPrefs.setWarpEnabled(!emuPrefs.isWarpEnabled());

                } else if (buttonId == GameController.ID_BUTTON_THUMB_RIGHT) {

                    setKeyboardVisible(!isKeyboardVisible());

                }
            }

            @Override
            public void onButtonUp(int buttonId) {
                ;
            }

        });

        VirtualGamepad.instance().init(this, emuView);
    }

    private void createKeyButtons() {

        ViewGroup view = (ViewGroup) findViewById(R.id.keyButtons);
        if (null == view) {
            logger.warning("Key button view not found");
            return;
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                Button button = (Button) v;
                KeyMapEntry entry = (KeyMapEntry) button.getTag();
                if (null != entry) {
                    int c64Code = entry.getC64Code();
                    enterKey(c64Code);
                }
            }
        };

        int buttonStyle = android.R.attr.buttonStyleSmall;

        int[] shownC64Keys = {
            KeyEvent.KEYCODE_SPACE,
            KeyEvent.KEYCODE_ENTER,

            KeyEvent.KEYCODE_F1,
            KeyEvent.KEYCODE_F2,
            KeyEvent.KEYCODE_F3,
            KeyEvent.KEYCODE_F4,
            KeyEvent.KEYCODE_F5,
            KeyEvent.KEYCODE_F6,
            KeyEvent.KEYCODE_F7,
            KeyEvent.KEYCODE_F8,

            KeyEvent.KEYCODE_0,
            KeyEvent.KEYCODE_1,
            KeyEvent.KEYCODE_2,
            KeyEvent.KEYCODE_3,
            KeyEvent.KEYCODE_4,
            KeyEvent.KEYCODE_5,
            KeyEvent.KEYCODE_6,
            KeyEvent.KEYCODE_7,
            KeyEvent.KEYCODE_8,
            KeyEvent.KEYCODE_9,

            KeyEvent.KEYCODE_A,
            KeyEvent.KEYCODE_B,
            KeyEvent.KEYCODE_C,
            KeyEvent.KEYCODE_D,
            KeyEvent.KEYCODE_E,
            KeyEvent.KEYCODE_F,
            KeyEvent.KEYCODE_G,
            KeyEvent.KEYCODE_H,
            KeyEvent.KEYCODE_I,
            KeyEvent.KEYCODE_J,
            KeyEvent.KEYCODE_K,
            KeyEvent.KEYCODE_L,
            KeyEvent.KEYCODE_M,
            KeyEvent.KEYCODE_N,
            KeyEvent.KEYCODE_O,
            KeyEvent.KEYCODE_P,
            KeyEvent.KEYCODE_Q,
            KeyEvent.KEYCODE_R,
            KeyEvent.KEYCODE_S,
            KeyEvent.KEYCODE_T,
            KeyEvent.KEYCODE_U,
            KeyEvent.KEYCODE_V,
            KeyEvent.KEYCODE_W,
            KeyEvent.KEYCODE_X,
            KeyEvent.KEYCODE_Y,
            KeyEvent.KEYCODE_Z,
        };

        for (int k: shownC64Keys) {
            KeyMapEntry entry = KeyMap.getMap().get(k);
            if (null == entry) {
                continue;
            }

            Button button = new Button(this, null, buttonStyle);
            button.setText(entry.getKeyName().toUpperCase());
            button.setOnClickListener(clickListener);
            button.setFocusable(true);
            button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            button.setTag(entry);

            view.addView(button);
        }

    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {

        /*
        if (isControlsVisible()) {
            return super.dispatchGenericMotionEvent(event);
        }
        */

        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK && event.getAction() == MotionEvent.ACTION_MOVE) {
            if (!isControlsVisible()) {
                if (gameController.handleEvent(event)) {
                    logger.info("update emu stick");
                    emuControl.setStick(gameController.getState() & 0xff);
                }
                return true;
            }
        }

        return super.dispatchGenericMotionEvent(event);
    }

    private void updateScreenSize() {

        if (null == emuView) {
            return;
        }

        int receivedWidth = emuView.getWidth();
        int receivedHeight = emuView.getHeight();

        if (0 != receivedWidth && 0 != receivedHeight) {

            // check for rotation
            if (receivedWidth > receivedHeight) {
                screenWidth = receivedWidth;
                screenHeight = receivedHeight;
            } else {
                screenWidth = receivedHeight;
                screenHeight = receivedWidth;
            }
        }

    }

    private boolean handleKeyEvent(KeyEvent event) {

        boolean keyUp = event.getAction()== KeyEvent.ACTION_UP;
        boolean keyDown = event.getAction()== KeyEvent.ACTION_DOWN;

        logger.info("KEY: " + event.toString());

        int keyCode = event.getKeyCode();
        char keyChar = (char) event.getUnicodeChar();

        boolean handled = true;

        if (keyCode == KeyEvent.KEYCODE_ENTER && keyDown) {
            setKeyboardVisible(false);
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_UNKNOWN: {
                handled = false;
                break;
            }
            case KeyEvent.KEYCODE_MENU: {
                if (keyDown) {
                    setControlsVisible(!isControlsVisible());
                }
                break;
            }
            default: {
                int c64Code = KeyMap.translate(keyCode);
                if (-1 != c64Code) {
                    logger.info("c64 key: " + keyCode + " -> 0x" + Integer.toHexString(c64Code));
                    emuControl.keyInput(c64Code | (keyDown ? KeyCode.KEYFLAG_PRESSED : KeyCode.KEYFLAG_RELEASED));
                } else {
                    logger.info("no c64 key: " + keyCode);
                    handled = false;
                }

                break;
            }
        }

        return handled;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (isControlsVisible()) {
            return super.onKeyDown(keyCode, event);
        }

        if (!GameController.isGameControllerEvent(event)) {
            if (keyCode != KeyEvent.KEYCODE_ENTER && handleKeyEvent(event) == true) {
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (isControlsVisible()) {
            return super.onKeyUp(keyCode, event);
        }

        if (!GameController.isGameControllerEvent(event)) {
            if (keyCode != KeyEvent.KEYCODE_ENTER && handleKeyEvent(event) == true) {
                return true;
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (isControlsVisible()) {

            if (event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() != KeyEvent.ACTION_UP) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        setControlsVisible(false);
                    }
                    return true;
                }
            }

            return super.dispatchKeyEvent(event);
        }

        if (null != gameController) {
            if (((event.getSource() & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) ||
                    ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)){
                if (event.getRepeatCount() == 0) {
                    if (gameController.handleEvent(event)) {
                        int stickMask = gameController.getState()&0xff;
                        logger.info("update emu stick mask: " + stickMask);
                        emuControl.setStick(stickMask);
                    }
                }
                return true;
            }
        }

        if (event.getAction() != KeyEvent.ACTION_DOWN && event.getAction() != KeyEvent.ACTION_UP) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                return super.dispatchKeyEvent(event);
            }
        }

        if (handleKeyEvent(event)) {
            return true;
        }

        return super.dispatchKeyEvent(event);

    }

    private void setControlsVisible(boolean show) {

        logger.info("COMMAND: setControlsVisible(" + show + ")");

        if (EMU_PAUSED_WHILE_CONTROL) {
            if (show) {
                emuControl.pause();
            } else {
                emuControl.resume();
            }
        }

        if (show) {
            emuControl.setStick(0x0); // clear stick input
        }
        
        controlsView.setVisibility(show ? View.VISIBLE : View.GONE);

        if (show) {
            controlsView.requestFocus();
        } else {
            emuView.requestFocus();
        }

        updateScreenSize();

    }

    private boolean isControlsVisible() {
        return (View.VISIBLE == controlsView.getVisibility());
    }

    private int getTouchArea(float x, float y) {

        updateScreenSize();

        if (screenWidth < 1.0f || screenHeight < 1.0f)
            return 0x0;

        float xPercent = (x * 100.0f) / screenWidth;
        float yPercent = (y * 100.0f) / screenHeight;

        int area = 0x0;

        if (yPercent <= 10.0f) {
            area |= AREA_TOP;
        } else if (yPercent >= 20.0f && yPercent <= 80.0f) {
            area |= AREA_MIDDLE;
        } else if (yPercent >= 90.0f) {
            area |= AREA_BOTTOM;
        }

        if (xPercent <= 10.0f) {
            area |= AREA_LEFT;
        } else if (xPercent >= 20.0f && xPercent <= 80.0f) {
            area |= AREA_CENTER;
        } else if (xPercent >= 90.0f) {
            area |= AREA_RIGHT;
        }

        // logger.info("MOUSE: " + (int) x + "/" + (int) screenWidth + "  -  "
        // + (int) y + "/" + (int) screenHeight);
        // displayArea(area);

        return area;
    }

    @SuppressWarnings("unused")
    private void displayArea(int area) {
        if ((area & AREA_TOP) != 0)
            logger.info("AREA_TOP");
        if ((area & AREA_MIDDLE) != 0)
            logger.info("AREA_MIDDLE");
        if ((area & AREA_BOTTOM) != 0)
            logger.info("AREA_BOTTOM");
        if ((area & AREA_LEFT) != 0)
            logger.info("AREA_LEFT");
        if ((area & AREA_CENTER) != 0)
            logger.info("AREA_CENTER");
        if ((area & AREA_RIGHT) != 0)
            logger.info("AREA_RIGHT");
    }

    private void handleCommandClick(int area) {

        logger.info(System.currentTimeMillis() + "Handle command click: 0x" + Long.toHexString(area));

        if (0x0 == area) {
            return;
        }

        if (isControlsVisible() && (AREA_BOTTOM + AREA_RIGHT) != area) {
            logger.info("Disable control panel");
            setControlsVisible(false);
        }

        if (isKeyboardVisible() && (AREA_BOTTOM + AREA_LEFT) != area) {
            logger.info("Disable virtual keyboard");
            setKeyboardVisible(false);
        }

        if (AREA_TOP + AREA_RIGHT == area) {

            emuControl.keyInput(KeySequence.sequence_Load_Asterisk_8_1_Run);

        } else if (AREA_BOTTOM + AREA_CENTER == area) {

            emuControl.keyInput(KeyCode.C64KEY_SPACE | KeyCode.KEYFLAG_PRESSED);
            emuControl.keyInput(KeyCode.C64KEY_SPACE | KeyCode.KEYFLAG_RELEASED);

        } else if ((AREA_BOTTOM + AREA_LEFT) == area) {
            //if (false == isKeyboardVisible()) {
                logger.info("Enable virtual keyboard");
                setKeyboardVisible(true);
            //}
        } else if ((AREA_BOTTOM + AREA_RIGHT) == area) {
            if (false == isControlsVisible()) {
                logger.info("Enable control panel");
                setControlsVisible(true);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void removeLayoutListenerPre16(OnGlobalLayoutListener listener) {
        emuView.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
    }

    @TargetApi(16)
    private void removeLayoutListenerPost16(OnGlobalLayoutListener listener) {
        emuView.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
    }

    public void onClickReset(View v) {
        logger.info("command: reset");
        setControlsVisible(false);
        //emuControl.softReset();
        emuControl.hardReset(null);
    }

    public void onClickShowSettings(View v) {
        setControlsVisible(false);
        if (null == settingsDialog) {
            settingsDialog = new SettingsDialog();
        }
        settingsDialog.show(getFragmentManager(), "emu_settings_dialog");
    }

    public void onClickSelectDisk(View v) {
        setControlsVisible(false);
        if (null == fileDialog) {
            fileDialog = new FileDialog();
        }
        fileDialog.show(getFragmentManager(), "emu_file_dialog");
    }

    @Override
    public void onDiskSelect(DiskImage diskImage, DiskFilter diskFilter, boolean longClick) {

        EmuControl emuControl = EmuControl.instance();

        boolean status = true;
        if (!longClick) {
            status = emuControl.attachDisk(diskImage); // just insert disk
        } else {
            emuControl.hardReset(diskImage); // reset and autostart
        }

        gameController.init();
        emuControl.setStick(0x0); // clear stick input

        if (diskImage.getType() == DiskImage.TYPE_SNAPSHOT) {
            setControlsVisible(false);
            Toast.makeText(getApplicationContext(), "Restore snapshot: " + diskImage.getName(), Toast.LENGTH_LONG).show();
            return;
        }

        emuControl.keyInputDelay(20);
        emuControl.keyInput(KeySequence.sequence_Load_Asterisk_8_1_Run);
        setControlsVisible(false);

        if (!longClick) {
            if (status) {
                Toast.makeText(getApplicationContext(), "Selected image: " + diskImage.getName(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to select image: " + diskImage.getName(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Quick start: " + diskImage.getName(), Toast.LENGTH_LONG).show();
        }

    }

    public void onClickExitApp(View v) {
        finish();
    }

    public void onClickPauseResume(View v) {
        if (!emuControl.isPaused()) {
            emuControl.pause();
        } else {
            emuControl.resume();
        }
    }

    public void onClickAnyKey(View v) {
        setControlsVisible(false);
        emuControl.keyInput(KeySequence.sequenceAllKeys);
    }

    private void enterKey(int key) {
        enterKey(key, false);
    }

    private void enterKey(int key, boolean shift) {
        setControlsVisible(false);
        if (shift) key |= KeyCode.C64KEY_FLAG_SHIFT;
        emuControl.keyInput(key | KeyCode.KEYFLAG_PRESSED);
        emuControl.keyInput(key | KeyCode.KEYFLAG_RELEASED);
    }

    private void setKeyboardVisible(boolean visible) {

        if (visible == keyboardVisible) {
            // return;
        }

        logger.info("command: setKeyboardVisible(" + visible + ")");

        keyboardVisible = visible;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;

        if (visible) {

            logger.info("Show virtual keyboard");
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        } else {
            logger.info("Hide virtual keyboard");
            imm.hideSoftInputFromWindow(emuView.getWindowToken(), 0);
        }

    }

    private boolean isKeyboardVisible() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        logger.info("VK: " + keyboardVisible + " / " + imm.isActive() + " / " + imm.isAcceptingText());

        return keyboardVisible;

    }

    public void onClickLoad(View v) {
        setControlsVisible(false);
        emuControl.keyInput(KeySequence.sequence_Load_Asterisk_8_1);
    }

    public void onClickRun(View v) {
        setControlsVisible(false);
        emuControl.keyInput(KeySequence.sequence_Run);
    }

    public void onClickRunStop(View v) {
        enterKey(KeyCode.C64KEY_RUNSTOP);
    }

    public void onClickRunStopRestore(View v) {
        setControlsVisible(false);
        emuControl.keyInput(KeyCode.C64KEY_RUNSTOP | KeyCode.KEYFLAG_PRESSED);
        emuControl.keyInput(KeyCode.C64KEY_RESTORE | KeyCode.KEYFLAG_PRESSED);
        emuControl.keyInput(KeyCode.C64KEY_RESTORE | KeyCode.KEYFLAG_RELEASED);
        emuControl.keyInput(KeyCode.C64KEY_RUNSTOP | KeyCode.KEYFLAG_RELEASED);
    }

    public void onClickShiftRunStop(View v) {
        enterKey(KeyCode.C64KEY_RUNSTOP, true);
    }

    private void checkOpenGL() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        logger.info("GLES version: 0x" + Integer.toHexString(info.reqGlEsVersion)); // >= 0x20000;
    }

    @Override
    protected void onStart() {
        super.onStart();
        logger.info("Activity.onStart()");
        emuControl.start();
        if (isControlsVisible()) {
            emuControl.pause();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        logger.info("Activity.onPause()");
        emuControl.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        logger.info("Activity.onResume()");
        if (isControlsVisible()) {
            emuControl.pause();
        } else {
            emuControl.resume();
        }
    }

    @Override
    protected void onRestart() {
        logger.info("Activity.onRestart()");
        emuControl.stop();
        emuControl.start();
        if (isControlsVisible()) {
            emuControl.pause();
        }
        super.onRestart();
    }

    @Override
    protected void onStop() {
        logger.info("Activity.onStop()");
        emuControl.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        logger.info("Activity.onDestroy()");

        if (null != emuControl) {
            emuControl.stop();
            emuControl = null;
        }

        super.onDestroy();
    }

    @Override
    public void onEmuStartup() {
    }

    @Override
    public void onEmuShutdown() {
    }

    public void onClickStoreSnapshot(View v) {
        logger.info("command: store snapshot");
        setControlsVisible(false);
        emuControl.storeSnapshot();
    }

    public void onClickRestoreSnapshot(View v) {
        logger.info("command: restore snapshot");
        setControlsVisible(false);
        emuControl.restoreSnapshot();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
