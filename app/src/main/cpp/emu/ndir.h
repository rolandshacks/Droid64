/*  ndir.c - portable directory routines
    Copyright (C) 1990 by Thorsten Ohl, td12@ddagsi3.bitnet

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 1, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

    $Header: /u/src/master/ccvs/windows-NT/ndir.h,v 1.4 1996/01/03 21:15:00 kingdon Exp $
 */

/* Everything non trivial in this code is taken from: @(#)msd_dir.c 1.4
   87/11/06.  A public domain implementation of BSD directory routines
   for MS-DOS.  Written by Michael Rendell ({uunet,utai}michael@garfield),
   August 1897 */
#ifdef WIN32

#include <sys/types.h>	/* ino_t definition */

#define	rewinddir(dirp)	seekdir(dirp, 0L)

/* 255 is said to be big enough for Windows NT.  The more elegant
   solution would be declaring d_name as one byte long and allocating
   it to the actual size needed.  */
#define	MAXNAMLEN	255

#define DT_UNKNOWN 0
#define DT_REG 1
#define DT_DIR 2

struct dirent
{
  ino_t d_ino;			/* a bit of a farce */
  int d_reclen;			/* more farce */
  int d_namlen;			/* length of d_name */
  char d_name[MAXNAMLEN + 1];	/* garentee null termination */
  unsigned char d_type;   
};

struct _dircontents
{
  char *_d_entry;
  int _d_type;
  struct _dircontents *_d_next;
};

typedef struct _dirdesc
{
  int dd_id;			/* uniquely identify each open directory */
  long dd_loc;			/* where we are in directory entry is this */
  struct _dircontents *dd_contents;	/* pointer to contents of dir */
  struct _dircontents *dd_cp;	/* pointer to current position */
} DIR;

extern "C" void seekdir (DIR *, long);
extern "C" long telldir (DIR *);
extern "C" DIR *opendir (const char *);
extern "C" void closedir (DIR *);
extern "C" struct dirent *readdir (DIR *);

#endif
/* 
 * Local Variables:
 * mode:C
 * ChangeLog:ChangeLog
 * compile-command:make
 * End:
 */
