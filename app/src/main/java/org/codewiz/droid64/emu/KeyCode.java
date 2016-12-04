package org.codewiz.droid64.emu;

public class KeyCode {
    public static int KEYCODE_MASK            = 0x00FFFF;
    public static int KEYFLAG_MASK            = 0xFF0000;

    public static int KEYFLAG_NONE            = 0x0;
    public static int KEYFLAG_PRESSED         = 0x10000;
    public static int KEYFLAG_RELEASED        = 0x20000;
    public static int KEYFLAG_COMMAND         = 0x40000;

    public static int C64KEY_FLAG_SHIFT       = 0x80;

    public static int C64KEY_INSTDEL          = 0x00;
    public static int C64KEY_RETURN           = 0x01;
    public static int C64KEY_CRSR_LEFTRIGHT   = 0x02;
    public static int C64KEY_F7F8             = 0x03;
    public static int C64KEY_F1F2             = 0x04;
    public static int C64KEY_F3F4             = 0x05;
    public static int C64KEY_F5F6             = 0x06;
    public static int C64KEY_CRSR_UPDOWN      = 0x07;
    public static int C64KEY_3                = 0x08;
    public static int C64KEY_W                = 0x09;
    public static int C64KEY_A                = 0x0A;
    public static int C64KEY_4                = 0x0B;
    public static int C64KEY_Z                = 0x0C;
    public static int C64KEY_S                = 0x0D;
    public static int C64KEY_E                = 0x0E;
    public static int C64KEY_LEFT_SHIFT       = 0x0F;   // unused

    public static int C64KEY_5                = 0x10;
    public static int C64KEY_R                = 0x11;
    public static int C64KEY_D                = 0x12;
    public static int C64KEY_6                = 0x13;
    public static int C64KEY_C                = 0x14;
    public static int C64KEY_F                = 0x15;
    public static int C64KEY_T                = 0x16;
    public static int C64KEY_X                = 0x17;
    public static int C64KEY_7                = 0x18;
    public static int C64KEY_Y                = 0x19;
    public static int C64KEY_G                = 0x1A;
    public static int C64KEY_8                = 0x1B;
    public static int C64KEY_B                = 0x1C;
    public static int C64KEY_H                = 0x1D;
    public static int C64KEY_U                = 0x1E;
    public static int C64KEY_V                = 0x1F;

    public static int C64KEY_9                = 0x20;
    public static int C64KEY_I                = 0x21;
    public static int C64KEY_J                = 0x22;
    public static int C64KEY_0                = 0x23;
    public static int C64KEY_M                = 0x24;
    public static int C64KEY_K                = 0x25;
    public static int C64KEY_O                = 0x26;
    public static int C64KEY_N                = 0x27;
    public static int C64KEY_PLUS             = 0x28;
    public static int C64KEY_P                = 0x29;
    public static int C64KEY_L                = 0x2A;
    public static int C64KEY_MINUS            = 0x2B;
    public static int C64KEY_PERIOD           = 0x2C;    // .
    public static int C64KEY_COLON            = 0x2D;    // [
    public static int C64KEY_AT               = 0x2E;
    public static int C64KEY_COMMA            = 0x2F;    // <

    public static int C64KEY_QUESTIONMARK     = 0x30;
    public static int C64KEY_POUND            = 0x30;

    public static int C64KEY_ASTERISK         = 0x31;
    public static int C64KEY_SEMICOLON        = 0x32;    // ]
    public static int C64KEY_CLRHOME          = 0x33;
    public static int C64KEY_RIGHT_SHIFT      = 0x34;    // unused
    public static int C64KEY_EQUAL            = 0x35;
    public static int C64KEY_UP_ARROW         = 0x36;
    public static int C64KEY_SLASH            = 0x37;
    public static int C64KEY_1                = 0x38;
    public static int C64KEY_LEFT_ARROW       = 0x39;
    public static int C64KEY_CONTROL          = 0x3A;   // unused
    public static int C64KEY_2                = 0x3B;
    public static int C64KEY_SPACE            = 0x3C;
    public static int C64KEY_COMMODORE        = 0x3D;   // unused
    public static int C64KEY_Q                = 0x3E;
    public static int C64KEY_RUNSTOP          = 0x3F;

    // fake key: restore -> cause NMI (non maskable interrupt)
    public static int C64KEY_RESTORE          = 0x40;

    public static int C64STICK_NONE           = 0x0;
    public static int C64STICK_FIRE           = 0x1;
    public static int C64STICK_LEFT           = 0x2;
    public static int C64STICK_RIGHT          = 0x4;
    public static int C64STICK_UP             = 0x8;
    public static int C64STICK_DOWN           = 0x10;


}
