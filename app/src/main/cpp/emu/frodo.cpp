/*
 *  main.cpp - Main program
 *
 *  Frodo (C) 1994-1997,2002 Christian Bauer
 */
#include "../emu_bindings.h"

#include "sysdeps.h"
#include "Version.h"

#include "C64.h"
#include "Prefs.h"

#include "./frodo.h"

#include "./keycodes.h"

C64 *TheC64 = NULL;     // The C64

static bool running = false;
static int flags = 0x0;

extern "C" int DLLBINDING emu_init(const char* prefs, int init_flags)
{
    srand( (unsigned)time( NULL ) );

	TheC64 = new C64;
    if (NULL == TheC64)
    {
        return -1;
    }

    TheC64->loadPrefs(prefs);

    if (false == TheC64->init())
    {
        delete TheC64;
        TheC64 = NULL;
        return -1;
    }

    flags = init_flags;

    running = true;

    return 0;
}

extern "C" int emu_input(int keyCode)
{
    if (false == running ||
        TheC64->isCancelled())
    {
        return -1;
    }

    if ((keyCode & 0xffff) == C64KEY_RESTORE)
    {
        TheC64->NMI();
        return 0;
    }

    TheC64->doInput(keyCode);

    return 0;
}

extern "C" int emu_store(int data_type, void* buffer, int buffer_size)
{
    int data_size = 0;

    TheC64->Pause();

    if (FRODO_DATATYPE_SNAPSHOT == data_type)
    {
        data_size = TheC64->SaveSnapshot((uint8*) buffer, buffer_size);
    }
    else if (FRODO_DATATYPE_DISK == data_type)
    {
        return -1;
    }

    TheC64->Resume();

    return data_size;
}

extern "C" int emu_load(int data_type, const void* data, int data_size, const char* filename)
{
    int status = 0;

    TheC64->Pause();

    if (FRODO_DATATYPE_SNAPSHOT == data_type)
    {
        TheC64->LoadSnapshot(data, data_size);
    }
    else if (FRODO_DATATYPE_DISK == data_type || FRODO_DATATYPE_TAPE == data_type)
    {
        TheC64->attachDiskImage((const uint8*) data, data_size);

        Prefs *prefs = new Prefs(ThePrefs);
        strcpy(prefs->DrivePath[0], filename);
        prefs->DriveType[0] = (FRODO_DATATYPE_DISK == data_type) ? DRVTYPE_D64 : DRVTYPE_T64;
	    TheC64->NewPrefs(prefs);
	    ThePrefs = *prefs;
	    delete prefs;

    }
    else
    {
        status = 1;
    }

    TheC64->Resume();

    return status;
}

extern "C" int emu_command(int command)
{
    switch (command)
    {
        case COMMAND_TRUEDRIVE_ON:
        {
            ThePrefs.Emul1541Proc = true;
            break;
        }
        case COMMAND_TRUEDRIVE_OFF:
        {
            ThePrefs.Emul1541Proc = false;
            break;
        }
        case COMMAND_JOYSTICK_SWAP_ON:
        {
            ThePrefs.JoystickSwap = true;
            break;
        }
        case COMMAND_JOYSTICK_SWAP_OFF:
        {
            ThePrefs.JoystickSwap = false;
            break;
        }
        case COMMAND_RESET:
        {
            // must be called within VBLANK!
            TheC64->Reset();
            break;
        }
        default:
        {
            return -1;
        }
    }

    return 0;
}

extern "C" int emu_update(int joystickInput, void* videoOutput, void* audioOutput, int flags)
{
    if (false == running ||
        TheC64->isCancelled())
    {
        return -1;
    }

    uint8 joystickState = 0xff;

    if (0 != (joystickInput & C64STICK_FIRE)) {
		joystickState &= 0xef; // Button
	}

    if (0 != (joystickInput & C64STICK_LEFT)) {
		joystickState &= 0xfb; // Left
	} else if (0 != (joystickInput & C64STICK_RIGHT)) {
		joystickState &= 0xf7; // Right
	}

    if (0 != (joystickInput & C64STICK_UP)) {
		joystickState &= 0xfe; // Up
	} else if (0 != (joystickInput & C64STICK_DOWN)) {
		joystickState &= 0xfd; // Down
	}

    bool vBlank = TheC64->doStep(joystickState, videoOutput, audioOutput, flags);

    return vBlank ? 1 : 0;
}

extern "C" int emu_shutdown()
{
    running = false;

    if (NULL != TheC64)
    {
        delete TheC64;
        TheC64 = NULL;
    }

    return 0;
}
