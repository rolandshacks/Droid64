package emu;

import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class KeyMap {

    public static int MODE_SHIFT = 0x0;

    private static Object[] KEY_MAP_DATA = {

        // Key code                         C64 code                                    Name                Flags

        // space
        KeyEvent.KEYCODE_SPACE,             (int) KeyCode.C64KEY_SPACE,                 "Space",            "",

        // row 1
        KeyEvent.KEYCODE_GRAVE,             (int) KeyCode.C64KEY_LEFT_ARROW,            "Arrow Left",       "",
        KeyEvent.KEYCODE_1,                 (int) KeyCode.C64KEY_1,                     "1",                "",
        KeyEvent.KEYCODE_2,                 (int) KeyCode.C64KEY_2,                     "2",                "",
        KeyEvent.KEYCODE_3,                 (int) KeyCode.C64KEY_3,                     "3",                "",
        KeyEvent.KEYCODE_4,                 (int) KeyCode.C64KEY_4,                     "4",                "",
        KeyEvent.KEYCODE_5,                 (int) KeyCode.C64KEY_5,                     "5",                "",
        KeyEvent.KEYCODE_6,                 (int) KeyCode.C64KEY_6,                     "6",                "",
        KeyEvent.KEYCODE_7,                 (int) KeyCode.C64KEY_7,                     "7",                "",
        KeyEvent.KEYCODE_8,                 (int) KeyCode.C64KEY_8,                     "8",                "",
        KeyEvent.KEYCODE_9,                 (int) KeyCode.C64KEY_9,                     "9",                "",
        KeyEvent.KEYCODE_0,                 (int) KeyCode.C64KEY_0,                     "0",                "",
        KeyEvent.KEYCODE_PLUS,              (int) KeyCode.C64KEY_PLUS,                  "+",                "",
        KeyEvent.KEYCODE_MINUS,             (int) KeyCode.C64KEY_MINUS,                 "-",                "",
        KeyEvent.KEYCODE_POUND,             (int) KeyCode.C64KEY_POUND,                 "Pound",            "",
        KeyEvent.KEYCODE_HOME,              (int) KeyCode.C64KEY_CLRHOME,               "Clr/Home",         "",
        KeyEvent.KEYCODE_DEL,               (int) KeyCode.C64KEY_INSTDEL,               "Inst/Del",         "",

        // row 2
        KeyEvent.KEYCODE_Q,                 (int) KeyCode.C64KEY_Q,                     "Q",                "",
        KeyEvent.KEYCODE_W,                 (int) KeyCode.C64KEY_W,                     "W",                "",
        KeyEvent.KEYCODE_E,                 (int) KeyCode.C64KEY_E,                     "E",                "",
        KeyEvent.KEYCODE_R,                 (int) KeyCode.C64KEY_R,                     "R",                "",
        KeyEvent.KEYCODE_T,                 (int) KeyCode.C64KEY_T,                     "T",                "",
        KeyEvent.KEYCODE_Y,                 (int) KeyCode.C64KEY_Y,                     "Y",                "",
        KeyEvent.KEYCODE_U,                 (int) KeyCode.C64KEY_U,                     "U",                "",
        KeyEvent.KEYCODE_I,                 (int) KeyCode.C64KEY_I,                     "I",                "",
        KeyEvent.KEYCODE_O,                 (int) KeyCode.C64KEY_O,                     "O",                "",
        KeyEvent.KEYCODE_P,                 (int) KeyCode.C64KEY_P,                     "P",                "",
        KeyEvent.KEYCODE_AT,                (int) KeyCode.C64KEY_AT,                    "@",                "",
//        KeyEvent.KEYCODE_BACKSLASH,         (int) KeyCode.C64KEY_ASTERISK,              "*",
        KeyEvent.KEYCODE_STAR,       	    (int) KeyCode.C64KEY_ASTERISK,              "*",                "",
        KeyEvent.KEYCODE_BACKSLASH,         (int) KeyCode.C64KEY_UP_ARROW,              "Arrow Up",         "",
        KeyEvent.KEYCODE_BUTTON_MODE,       (int) KeyCode.C64KEY_RESTORE,               "Restore",          "",


        // row 3
        KeyEvent.KEYCODE_TAB,               (int) KeyCode.C64KEY_RUNSTOP,               "Run/Stop",         "",
        KeyEvent.KEYCODE_SHIFT_LEFT,        (int) KeyCode.C64KEY_LEFT_SHIFT,            "Left Shift",       "",
        KeyEvent.KEYCODE_A,                 (int) KeyCode.C64KEY_A,                     "A",                "",
        KeyEvent.KEYCODE_S,                 (int) KeyCode.C64KEY_S,                     "S",                "",
        KeyEvent.KEYCODE_D,                 (int) KeyCode.C64KEY_D,                     "D",                "",
        KeyEvent.KEYCODE_F,                 (int) KeyCode.C64KEY_F,                     "F",                "",
        KeyEvent.KEYCODE_G,                 (int) KeyCode.C64KEY_G,                     "G",                "",
        KeyEvent.KEYCODE_H,                 (int) KeyCode.C64KEY_H,                     "H",                "",
        KeyEvent.KEYCODE_J,                 (int) KeyCode.C64KEY_J,                     "J",                "",
        KeyEvent.KEYCODE_K,                 (int) KeyCode.C64KEY_K,                     "K",                "",
        KeyEvent.KEYCODE_L,                 (int) KeyCode.C64KEY_L,                     "L",                "",
        KeyEvent.KEYCODE_COMMA,             (int) KeyCode.C64KEY_COLON,                 ",",                "",
        KeyEvent.KEYCODE_SEMICOLON,         (int) KeyCode.C64KEY_SEMICOLON,             ";",                "",
        KeyEvent.KEYCODE_EQUALS,            (int) KeyCode.C64KEY_EQUAL,                 "=",                "",
        KeyEvent.KEYCODE_ENTER,             (int) KeyCode.C64KEY_RETURN,                "Return",           "",

        // row 4
        KeyEvent.KEYCODE_ALT_LEFT,          (int) KeyCode.C64KEY_COMMODORE,             "Commodore",        "",
        KeyEvent.KEYCODE_SHIFT_LEFT,        (int) KeyCode.C64KEY_LEFT_SHIFT,            "Left Shift",       "",
        KeyEvent.KEYCODE_Z,                 (int) KeyCode.C64KEY_Z,                     "Z",                "",
        KeyEvent.KEYCODE_X,                 (int) KeyCode.C64KEY_X,                     "X",                "",
        KeyEvent.KEYCODE_C,                 (int) KeyCode.C64KEY_C,                     "C",                "",
        KeyEvent.KEYCODE_V,                 (int) KeyCode.C64KEY_V,                     "V",                "",
        KeyEvent.KEYCODE_B,                 (int) KeyCode.C64KEY_B,                     "B",                "",
        KeyEvent.KEYCODE_N,                 (int) KeyCode.C64KEY_N,                     "N",                "",
        KeyEvent.KEYCODE_M,                 (int) KeyCode.C64KEY_M,                     "M",                "",
        KeyEvent.KEYCODE_COMMA,             (int) KeyCode.C64KEY_COMMA,                 ",",                "",
        KeyEvent.KEYCODE_PERIOD,            (int) KeyCode.C64KEY_PERIOD,                ".",                "",
        KeyEvent.KEYCODE_SLASH,             (int) KeyCode.C64KEY_SLASH,                 "/",                "",
        KeyEvent.KEYCODE_SHIFT_RIGHT,       (int) KeyCode.C64KEY_RIGHT_SHIFT,           "Right Shift",      "",
        KeyEvent.KEYCODE_DPAD_LEFT,         (int) KeyCode.C64KEY_CRSR_LEFTRIGHT | (int) KeyCode.C64KEY_FLAG_SHIFT, "Cursor Left",    "",
        KeyEvent.KEYCODE_DPAD_RIGHT,        (int) KeyCode.C64KEY_CRSR_LEFTRIGHT,        "Cursor Right",     "",
        KeyEvent.KEYCODE_DPAD_UP,           (int) KeyCode.C64KEY_CRSR_UPDOWN | (int) KeyCode.C64KEY_FLAG_SHIFT, "Cursor Up",    "",
        KeyEvent.KEYCODE_DPAD_DOWN,         (int) KeyCode.C64KEY_CRSR_UPDOWN,           "Cursor Down",      "",

        KeyEvent.KEYCODE_APOSTROPHE,        (int) KeyCode.C64KEY_2 | (int) KeyCode.C64KEY_FLAG_SHIFT, "Quotes",    "",

        KeyEvent.KEYCODE_F1,                (int) KeyCode.C64KEY_F1F2,                                      "F1", "",
        KeyEvent.KEYCODE_F2,                (int) KeyCode.C64KEY_F1F2 |  (int) KeyCode.C64KEY_FLAG_SHIFT,   "F2", "",
        KeyEvent.KEYCODE_F3,                (int) KeyCode.C64KEY_F3F4,                                      "F3", "",
        KeyEvent.KEYCODE_F4,                (int) KeyCode.C64KEY_F3F4 |  (int) KeyCode.C64KEY_FLAG_SHIFT,   "F4", "",
        KeyEvent.KEYCODE_F5,                (int) KeyCode.C64KEY_F5F6,                                      "F5", "",
        KeyEvent.KEYCODE_F6,                (int) KeyCode.C64KEY_F5F6 |  (int) KeyCode.C64KEY_FLAG_SHIFT,   "F6", "",
        KeyEvent.KEYCODE_F7,                (int) KeyCode.C64KEY_F7F8,                                      "F7", "",
        KeyEvent.KEYCODE_F8,                (int) KeyCode.C64KEY_F7F8 |  (int) KeyCode.C64KEY_FLAG_SHIFT,   "F8", "",

        0, 0, "", ""
    };

    private static List<KeyMapEntry> def = null;
    private static Map<Integer, KeyMapEntry> keyMap = null;

    private static void initialize() {

        def = new ArrayList<KeyMapEntry>();
        keyMap = new HashMap<Integer, KeyMapEntry>();

        for (int i = 0; i < KEY_MAP_DATA.length - 1; i += 4)
        {
            int keyCode = (Integer) KEY_MAP_DATA[i];
            int c64Code = (Integer) KEY_MAP_DATA[i+1];
            String keyName = (String) KEY_MAP_DATA[i+2];
            String flags = (String) KEY_MAP_DATA[i+3];

            if (0 != keyCode || 0 != c64Code) {
                KeyMapEntry entry = new KeyMapEntry(keyCode, c64Code, keyName, flags);
                def.add(entry);
                keyMap.put(keyCode, entry);
            }
        }

    }

    public static int translate(int systemCode)
    {
        if (null == keyMap) {
            initialize();
        }

        KeyMapEntry entry = keyMap.get(systemCode);
        if (null != entry) {
            return entry.getC64Code();
        }

        return -1;
    }

    public static Map<Integer, KeyMapEntry> getMap() {
        if (null == keyMap) {
            initialize();
        }

        return keyMap;
    }

    public static List<KeyMapEntry> getList() {
        if (null == def) {
            initialize();
        }

        return def;
    }

}
