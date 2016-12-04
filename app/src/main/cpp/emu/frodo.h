/*
 *
 *  frodo.h - Frodo Control Interface
 *
 */

#ifndef _FRODO_INTERFACE_H
#define _FRODO_INTERFACE_H

#define FRODO_FLAGS_VIDEO_INDEXED   0x0
#define FRODO_FLAGS_VIDEO_ARGB      0x1

#define FRODO_DATATYPE_SNAPSHOT 1
#define FRODO_DATATYPE_DISK     2
#define FRODO_DATATYPE_TAPE     3
#define FRODO_DATATYPE_DIR     3

#ifdef FRODO_DLL
    #define DLLBINDING __declspec(dllexport)
#else
    #ifdef WIN32
        #define DLLBINDING __declspec(dllimport)
    #else
        #define DLLBINDING /* */
    #endif
#endif

extern "C" int DLLBINDING emu_init(const char* prefs, int flags);
extern "C" int DLLBINDING emu_input(int keyCode);
extern "C" int DLLBINDING emu_load(int data_type, const void* data, int data_size, const char* filename);
extern "C" int DLLBINDING emu_store(int data_type, void* buffer, int buffer_size);
extern "C" int DLLBINDING emu_command(int command);
extern "C" int DLLBINDING emu_update(int joystickInput, void* videoOutput, void* audioOutput, int flags);
extern "C" int DLLBINDING emu_shutdown();

#endif /* _FRODO_INTERFACE_H */
