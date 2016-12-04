/*
 *  Display.cpp - C64 graphics display, emulator window handling
 *
 *  Frodo (C) 1994-1997,2002 Christian Bauer
 */

#include "sysdeps.h"
#include "Version.h"

#include "Input.h"
#include "keycodes.h"

/*
  C64 keyboard matrix:

    Bit 7   6   5   4   3   2   1   0
  0    CUD  F5  F3  F1  F7 CLR RET DEL
  1    SHL  E   S   Z   4   A   W   3
  2     X   T   F   C   6   D   R   5
  3     V   U   H   B   8   G   Y   7
  4     N   O   K   M   0   J   I   9
  5     ,   @   :   .   -   L   P   +
  6     /   ^   =  SHR HOM  ;   *   ?
  7    R/S  Q   C= SPC  2  CTL  <-  1
*/


static const int key_queue_size = 512;
static int key_queue_elements = 0;
static int key_queue_inptr = 0;
static int key_queue_outptr = 0;
static int key_queue[key_queue_size] = { 0 };

// LOAD"*",8,1 <CR> RUN <CR>
int SEQUENCE_LOADFIRST[] = { 42, 38, 10, 18, 15, 187, 49, 15, 187, 47, 27, 47, 56, 1, 17, 30, 39, 1 };

// LOAD"%",8 <CR> LIST <CR>
int SEQUENCE_LIST[] = { 42, 38, 10, 18, 15, 187, 139, 187, 47, 27, 1, 42, 33, 13, 22, 1 };

#define MATRIX(a,b) (((a) << 3) | (b))

Input::Input()
{
}

Input::~Input()
{
}

void Input::pushKey(int key)
{
    // printf(", %d", key); if (key == -13) printf("\n");

    //if (0 == SDL_LockMutex(inputLock))
    {

        if (key_queue_elements < key_queue_size)
        {
            key_queue_elements++;
            key_queue[key_queue_inptr++] = key;
            if (key_queue_inptr >= key_queue_size) key_queue_inptr = 0;
        }

        //SDL_UnlockMutex(inputLock);
    }

}

void Input::pushKeyPress(int key)
{
    pushKey(key | KEYFLAG_PRESSED);
    pushKey(key | KEYFLAG_RELEASED);
}

void Input::pushKeySequence(int* keys, int count)
{
    for (int i=0; i<count; i++)
   {
        pushKeyPress(keys[i]);
    }
}

int Input::popKey()
{
    //if (0 != key_queue_elements) printf("%d\n", key_queue_elements);

    int sym = 0xffff;

    //if (0 == SDL_LockMutex(inputLock))
    {

        if (key_queue_elements > 0)
        {

            key_queue_elements--;

            sym = key_queue[key_queue_outptr++];
            if (key_queue_outptr >= key_queue_size) key_queue_outptr = 0;
        }

        //SDL_UnlockMutex(inputLock);
    }

    if (0xffff != sym)
    {
        printf("SYM: 0x%x\n", sym);
    }

    return sym;
}

void Input::translateKey(int c64_key,
                         bool key_up,
                         uint8 *key_matrix,
                         uint8 *rev_matrix)
{
	bool shifted = c64_key & 0x80;

	int c64_byte = (c64_key >> 3) & 7;
	int c64_bit  = c64_key & 7;

	if (key_up)
    {
		if (shifted) 
        {
			key_matrix[6]    |= 0x10;
			rev_matrix[4]    |= 0x40;
		}
		key_matrix[c64_byte] |= (1 << c64_bit);
		rev_matrix[c64_bit]  |= (1 << c64_byte);
	}
    else
    {
		if (shifted) 
        {
			key_matrix[6]    &= 0xef;
			rev_matrix[4]    &= 0xbf;
		}
		key_matrix[c64_byte] &= ~(1 << c64_bit);
		rev_matrix[c64_bit]  &= ~(1 << c64_byte);
	}
}

void Input::getState(uint8 *key_matrix, uint8 *rev_matrix)
{
    int keyInput = popKey();
    if (0xffff != keyInput)
    {
        int rawKey = (keyInput & KEYCODE_MASK);

        if ((keyInput & KEYFLAG_PRESSED) != 0)
        {
            translateKey(rawKey, false, key_matrix, rev_matrix);
        }
        else if ((keyInput & KEYFLAG_RELEASED) != 0)
        {
            translateKey(rawKey, true, key_matrix, rev_matrix);
        }
    }
}

static void swap(int& a, int& b)
{
    int c = a;
    a = b;
    b = c;
}

void Input::handleKeyEvent(int keyCode)
{
    if (false && (keyCode & KEYFLAG_COMMAND) != 0)
    {
        if (keyCode == KEYCOMMAND_LOADFIRST)
        {
            pushKeySequence(SEQUENCE_LOADFIRST, sizeof(SEQUENCE_LOADFIRST)/sizeof(SEQUENCE_LOADFIRST[0]));
        }
        else if (keyCode == KEYCOMMAND_LIST)
        {
            pushKeySequence(SEQUENCE_LIST, sizeof(SEQUENCE_LIST)/sizeof(SEQUENCE_LIST[0]));
        }
    }
    else
    {
        /*
        if (keyCode & KEYFLAG_PRESSED)
        {
            int code = keyCode & KEYCODE_MASK;
            printf("%d, ", code);
        }
        */

        pushKey(keyCode);
    }
}
