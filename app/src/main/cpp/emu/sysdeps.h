/*
 *  sysdeps.h - Try to include the right system headers and get other
 *              system-specific stuff right
 *
 *  Frodo (C) 1994-1997,2002 Christian Bauer
 */

#include "sysconfig.h"

extern "C"
{

#if USE_SDL    
    #include <SDL.h>
    #include <SDL_thread.h>
    #include <SDL_ttf.h>
    #include <SDL_audio.h>
#endif

#include <time.h>

#if USE_OPENGL
    #ifdef WEBOS
        #include <GLES/gl.h>
        #define HAVE_GLES
    #else
        #ifdef WINDOWS
            #include <windows.h>
		#endif
        #include <GL/gl.h>
    #endif

    #ifndef GL_BGR
        #define GL_BGR 0x80E0 
    #endif

    #ifndef GL_BGRA
        #define GL_BGRA 0x80E1
    #endif

#endif

#ifdef WIN32
    #pragma comment(lib, "winmm.lib")
    #if USE_SDL
        #pragma comment(lib, "SDLmain.lib")
        #pragma comment(lib, "SDL.lib")
        #pragma comment(lib, "SDL_image.lib")
        #pragma comment(lib, "SDL_ttf.lib")
    #endif
    #if USE_OPENGL
        #pragma comment(lib, "opengl32.lib")
    #endif
#endif

#ifdef WEBOS
#include <PDL.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <ctype.h>

#ifndef __PSXOS__
#include <errno.h>
#include <signal.h>
#endif

#ifdef HAVE_SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef HAVE_VALUES_H
#include <values.h>
#endif

#ifdef HAVE_STRINGS_H
#include <strings.h>
#endif
#ifdef HAVE_STRING_H
#include <string.h>
#endif

#ifdef HAVE_UNISTD_H
#include <unistd.h>
#endif
#ifdef HAVE_FCNTL_H
#include <fcntl.h>
#endif

#ifdef HAVE_UTIME_H
#include <utime.h>
#endif

#ifdef HAVE_SYS_PARAM_H
#include <sys/param.h>
#endif

#ifdef HAVE_SYS_SELECT_H
#include <sys/select.h>
#endif

#ifdef HAVE_SYS_VFS_H
#include <sys/vfs.h>
#endif

#ifdef HAVE_SYS_STAT_H
#include <sys/stat.h>
#endif

#ifdef HAVE_SYS_MOUNT_H
#include <sys/mount.h>
#endif

#ifdef HAVE_SYS_STATFS_H
#include <sys/statfs.h>
#endif

#ifdef HAVE_SYS_STATVFS_H
#include <sys/statvfs.h>
#endif

#if TIME_WITH_SYS_TIME
# include <sys/time.h>
# include <time.h>
#else
# if HAVE_SYS_TIME_H
#  include <sys/time.h>
# else
#ifndef __PSXOS__
#  include <time.h>
#endif
# endif
#endif

#if HAVE_DIRENT_H
# include <dirent.h>
#else
# define dirent direct
# if HAVE_SYS_NDIR_H
#  include <sys/ndir.h>
# endif
# if HAVE_SYS_DIR_H
#  include <sys/dir.h>
# endif
# if HAVE_NDIR_H
#  include <ndir.h>
# endif
#endif

#ifndef __PSXOS__
#include <errno.h>
#endif
#include <assert.h>

#if EEXIST == ENOTEMPTY
#define BROKEN_OS_PROBABLY_AIX
#endif

#ifdef HAVE_LINUX_JOYSTICK_H
#include <linux/joystick.h>
#endif

#ifdef __NeXT__
#define S_IRUSR S_IREAD
#define S_IWUSR S_IWRITE
#define S_IXUSR S_IEXEC
#define S_ISDIR(val) (S_IFDIR & val)
struct utimbuf
{
    time_t actime;
    time_t modtime;
};
#endif

#ifdef __DOS__
#include <pc.h>
#include <io.h>
#else
#undef O_BINARY
#define O_BINARY 0
#endif

#ifdef WIN32
#include <windows.h>
#include <direct.h>
#if !defined(M_PI)
#define M_PI 3.14159265358979323846
#endif
#define S_ISDIR(m) (((m) & S_IFMT) == S_IFDIR)
#if _MSC_VER < 1100
#define bool char
#endif
#define LITTLE_ENDIAN_UNALIGNED 1
#ifndef UBYTE
#define UBYTE unsigned char
#endif
#endif

/* If char has more then 8 bits, good night. */
typedef unsigned char uint8;
typedef signed char int8;

#if SIZEOF_SHORT == 2
typedef unsigned short uint16;
typedef short int16;
#elif SIZEOF_INT == 2
typedef unsigned int uint16;
typedef int int16;
#else
#error No 2 byte type, you lose.
#endif

#if SIZEOF_INT == 4
typedef unsigned int uint32;
typedef int int32;
#elif SIZEOF_LONG == 4
typedef unsigned long uint32;
typedef long int32;
#else
#error No 4 byte type, you lose.
#endif

#define UNUSED(x) (x = x)
}
