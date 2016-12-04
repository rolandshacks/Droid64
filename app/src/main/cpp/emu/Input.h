/*
 *  Display.h - C64 graphics display, emulator window handling
 *
 *  Frodo (C) 1994-1997,2002 Christian Bauer
 */

#ifndef _INPUT_H
#define _INPUT_H

class Input
{
    public:
	    Input();
	    ~Input();
	    void getState(uint8 *key_matrix, uint8 *rev_matrix);

    public: // implements InputHandler
        void handleKeyEvent(int keyCode);

    public:
        void pushKey(int key);
        void pushKeyPress(int key);
        void pushKeySequence(int* keys, int count);

    private:
        int popKey();
        void translateKey(int key, bool key_up, uint8 *key_matrix, uint8 *rev_matrix);
};

#endif /* _INPUT_H */
