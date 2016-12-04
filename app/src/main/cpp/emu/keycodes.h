#ifndef KEYCODES_H
#define KEYCODES_H

#define KEYFLAG_NONE            0x0
#define KEYFLAG_PRESSED         0x10000
#define KEYFLAG_RELEASED        0x20000
#define KEYFLAG_COMMAND         0x40000

#define KEYCODE_MASK            0xFFFF
#define KEYCODE_BASE            1000

#define KEYCODE_a               'a'
#define KEYCODE_b               'b'
#define KEYCODE_c               'c'
#define KEYCODE_d               'd'
#define KEYCODE_e               'e'
#define KEYCODE_f               'f'
#define KEYCODE_g               'g'
#define KEYCODE_h               'h'
#define KEYCODE_i               'i'
#define KEYCODE_j               'j'
#define KEYCODE_k               'k'
#define KEYCODE_l               'l'
#define KEYCODE_m               'm'
#define KEYCODE_n               'n'
#define KEYCODE_o               'o'
#define KEYCODE_p               'p'
#define KEYCODE_q               'q'
#define KEYCODE_r               'r'
#define KEYCODE_s               's'
#define KEYCODE_t               't'
#define KEYCODE_u               'u'
#define KEYCODE_v               'v'
#define KEYCODE_w               'w'
#define KEYCODE_x               'x'
#define KEYCODE_y               'y'
#define KEYCODE_z               'z'

#define KEYCODE_0               '0'
#define KEYCODE_1               '1'
#define KEYCODE_2               '2'
#define KEYCODE_3               '3'
#define KEYCODE_4               '4'
#define KEYCODE_5               '5'
#define KEYCODE_6               '6'
#define KEYCODE_7               '7'
#define KEYCODE_8               '8'
#define KEYCODE_9               '9'

#define KEYCODE_SPACE           KEYCODE_BASE + 1
#define KEYCODE_BACKQUOTE       KEYCODE_BASE + 2
#define KEYCODE_BACKSLASH       KEYCODE_BASE + 3
#define KEYCODE_COMMA           KEYCODE_BASE + 4
#define KEYCODE_PERIOD          KEYCODE_BASE + 5
#define KEYCODE_MINUS           KEYCODE_BASE + 6
#define KEYCODE_EQUALS          KEYCODE_BASE + 7
#define KEYCODE_LEFTBRACKET     KEYCODE_BASE + 8
#define KEYCODE_RIGHTBRACKET    KEYCODE_BASE + 9
#define KEYCODE_SEMICOLON       KEYCODE_BASE + 10
#define KEYCODE_QUOTE           KEYCODE_BASE + 11
#define KEYCODE_SLASH           KEYCODE_BASE + 12
#define KEYCODE_QUESTION        KEYCODE_BASE + 13
#define KEYCODE_ESCAPE          KEYCODE_BASE + 14
#define KEYCODE_QUOTEDBL        KEYCODE_BASE + 15
#define KEYCODE_DOLLAR          KEYCODE_BASE + 16
#define KEYCODE_RETURN          KEYCODE_BASE + 17
#define KEYCODE_BACKSPACE       KEYCODE_BASE + 18
#define KEYCODE_INSERT          KEYCODE_BASE + 19
#define KEYCODE_HOME            KEYCODE_BASE + 20
#define KEYCODE_END             KEYCODE_BASE + 21
#define KEYCODE_PAGEUP          KEYCODE_BASE + 22
#define KEYCODE_PAGEDOWN        KEYCODE_BASE + 23
#define KEYCODE_LCTRL           KEYCODE_BASE + 24
#define KEYCODE_RCTRL           KEYCODE_BASE + 25
#define KEYCODE_LSHIFT          KEYCODE_BASE + 26
#define KEYCODE_RSHIFT          KEYCODE_BASE + 27
#define KEYCODE_LALT            KEYCODE_BASE + 28
#define KEYCODE_RALT            KEYCODE_BASE + 29
#define KEYCODE_UP              KEYCODE_BASE + 30
#define KEYCODE_DOWN            KEYCODE_BASE + 31
#define KEYCODE_LEFT            KEYCODE_BASE + 32
#define KEYCODE_RIGHT           KEYCODE_BASE + 33
#define KEYCODE_F1              KEYCODE_BASE + 34
#define KEYCODE_F2              KEYCODE_BASE + 35
#define KEYCODE_F3              KEYCODE_BASE + 36
#define KEYCODE_F4              KEYCODE_BASE + 37
#define KEYCODE_F5              KEYCODE_BASE + 38
#define KEYCODE_F6              KEYCODE_BASE + 39
#define KEYCODE_F7              KEYCODE_BASE + 40
#define KEYCODE_F8              KEYCODE_BASE + 41
#define KEYCODE_KP0             KEYCODE_BASE + 42
#define KEYCODE_KP1             KEYCODE_BASE + 43
#define KEYCODE_KP2             KEYCODE_BASE + 44
#define KEYCODE_KP3             KEYCODE_BASE + 45
#define KEYCODE_KP4             KEYCODE_BASE + 46
#define KEYCODE_KP5             KEYCODE_BASE + 47
#define KEYCODE_KP6             KEYCODE_BASE + 48
#define KEYCODE_KP7             KEYCODE_BASE + 49
#define KEYCODE_KP8             KEYCODE_BASE + 50
#define KEYCODE_KP9             KEYCODE_BASE + 51
#define KEYCODE_KP_DIVIDE       KEYCODE_BASE + 52
#define KEYCODE_KP_ENTER        KEYCODE_BASE + 53
#define KEYCODE_DELETE          KEYCODE_BASE + 54
#define KEYCODE_TAB             KEYCODE_BASE + 55
#define KEYCODE_LMETA           KEYCODE_BASE + 56
#define KEYCODE_RMETA           KEYCODE_BASE + 57

#define KEYCOMMAND_BASE         KEYFLAG_COMMAND
#define KEYCOMMAND_RESET        KEYCOMMAND_BASE + 1
#define KEYCOMMAND_RESTORE      KEYCOMMAND_BASE + 2
#define KEYCOMMAND_LOADFIRST    KEYCOMMAND_BASE + 3
#define KEYCOMMAND_LIST         KEYCOMMAND_BASE + 4


/////////////////////////////////////////////////////////
#define C64KEY_FLAG_SHIFT       0x80


#define C64KEY_INSTDEL          0x00
#define C64KEY_RETURN           0x01
#define C64KEY_CRSR_LEFTRIGHT   0x02
#define C64KEY_F7F8             0x03
#define C64KEY_F1F2             0x04
#define C64KEY_F3F4             0x05
#define C64KEY_F5F6             0x06
#define C64KEY_CRSR_UPDOWN      0x07
#define C64KEY_3                0x08
#define C64KEY_W                0x09
#define C64KEY_A                0x0A
#define C64KEY_4                0x0B
#define C64KEY_Z                0x0C
#define C64KEY_S                0x0D
#define C64KEY_E                0x0E
#define C64KEY_LEFT_SHIFT       0x0F    // unused

#define C64KEY_5                0x10
#define C64KEY_R                0x11
#define C64KEY_D                0x12
#define C64KEY_6                0x13
#define C64KEY_C                0x14
#define C64KEY_F                0x15
#define C64KEY_T                0x16
#define C64KEY_X                0x17
#define C64KEY_7                0x18
#define C64KEY_Y                0x19
#define C64KEY_G                0x1A
#define C64KEY_8                0x1B
#define C64KEY_B                0x1C
#define C64KEY_H                0x1D
#define C64KEY_U                0x1E
#define C64KEY_V                0x1F

#define C64KEY_9                0x20
#define C64KEY_I                0x21
#define C64KEY_J                0x22
#define C64KEY_0                0x23
#define C64KEY_M                0x24
#define C64KEY_K                0x25
#define C64KEY_O                0x26
#define C64KEY_N                0x27
#define C64KEY_PLUS             0x28
#define C64KEY_P                0x29
#define C64KEY_L                0x2A
#define C64KEY_MINUS            0x2B
#define C64KEY_PERIOD           0x2C    // .
#define C64KEY_COLON            0x2D    // [
#define C64KEY_AT               0x2E
#define C64KEY_COMMA            0x2F    // <

#define C64KEY_QUESTIONMARK     0x30
#define C64KEY_POUND            0x30

#define C64KEY_ASTERISK         0x31
#define C64KEY_SEMICOLON        0x32    // ]
#define C64KEY_CLRHOME          0x33
#define C64KEY_RIGHT_SHIFT      0x34    // unused
#define C64KEY_EQUAL            0x35
#define C64KEY_UP_ARROW         0x36
#define C64KEY_SLASH            0x37
#define C64KEY_1                0x38
#define C64KEY_LEFT_ARROW       0x39
#define C64KEY_CONTROL          0x3A    // unused
#define C64KEY_2                0x3B
#define C64KEY_SPACE            0x3C
#define C64KEY_COMMODORE        0x3D    // unused
#define C64KEY_Q                0x3E
#define C64KEY_RUNSTOP          0x3F

// fake key: restore -> cause NMI (non maskable interrupt)
#define C64KEY_RESTORE          0x40

#define C64STICK_NONE           0x0
#define C64STICK_FIRE           0x1
#define C64STICK_LEFT           0x2
#define C64STICK_RIGHT          0x4
#define C64STICK_UP             0x8
#define C64STICK_DOWN           0x10

#define COMMAND_UNKNOWN         0
#define COMMAND_TRUEDRIVE_ON    1
#define COMMAND_TRUEDRIVE_OFF   2
#define COMMAND_RESET           3
#define COMMAND_DEBUGGER_TOGGLE = 4;
#define COMMAND_RESTORE         = 5;
#define COMMAND_JOYSTICK_SWAP_ON 6
#define COMMAND_JOYSTICK_SWAP_OFF 7

#endif // KEYCODES_H
