/*
 *  sysconfig.h - System configuration
 *
 *  Frodo (C) 1994-1997,2002 Christian Bauer
 */

#define USE_OPENGL 0
#define USE_SDL 0

#if defined(WIN32)
    #include "./sysconfig_WIN32.h"
#elif defined(ANDROID)
    #include "./sysconfig_ANDROID.h"
#else
    #include "./sysconfig_POSIX.h"
#endif
