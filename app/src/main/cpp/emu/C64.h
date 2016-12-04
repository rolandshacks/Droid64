/*
 *  C64.h - Put the pieces together
 *
 *  Frodo (C) 1994-1997,2002 Christian Bauer
 */

#ifndef _C64_H
#define _C64_H

//#include <SDL.h>

// false: Frodo, true: FrodoSC
extern bool IsFrodoSC;

#define FLAG_UNPACK_GRAPHICS 0x1
#define FLAG_USE_GAMMA_CORRECTION 0x2

class Prefs;
class C64Display;
class C64Input;
class MOS6510;
class MOS6569;
class MOS6581;
class MOS6526_1;
class MOS6526_2;
class IEC;
class REU;
class MOS6502_1541;
class Job1541;
class CmdPipe;
class VirtualJoystick;
class Input;

class C64 {
    public:
	    C64();
	    ~C64();

        bool loadPrefs(const char* preferenceData);
        bool init();
        void shutdown();
        bool doInput(int keyCode);
        bool doStep(int joystickInput, void* videoOutput, void* audioOutput, int flags);
        bool isCancelled();

    public:
	    void Quit(void);
	    void Pause(void);
	    void Resume(void);
	    void Reset(void);
	    void NMI(void);
	    void VBlank(bool draw_frame);
	    void NewPrefs(Prefs *prefs);
	    void PatchKernal(bool fast_reset, bool emul_1541_proc);
	    void SaveRAM(uint8* buf);
	    int SaveSnapshot(uint8* buf, int buffer_size);
	    bool LoadSnapshot(const void* data, int data_size);

	    int SaveCPUState(uint8*& buf);
	    int Save1541State(uint8*& buf);
	    bool Save1541JobState(uint8*& buf);
	    bool SaveVICState(uint8*& buf);
	    bool SaveSIDState(uint8*& buf);
	    bool SaveCIAState(uint8*& buf);
	    bool LoadCPUState(const uint8*& buf);
	    bool Load1541State(const uint8*& buf);
	    bool Load1541JobState(const uint8*& buf);
	    bool LoadVICState(const uint8*& buf);
	    bool LoadSIDState(const uint8*& buf);
	    bool LoadCIAState(const uint8*& buf);
        bool isPaused();

        int ShowRequester(const char* text, const char* button1=NULL, const char* button2=NULL);

    public:
	    uint8 *RAM, *Basic, *Kernal,
		      *Char, *Color;		// C64
	    uint8 *RAM1541, *ROM1541;	// 1541

	    MOS6510 *TheCPU;			// C64
	    MOS6569 *TheVIC;
	    MOS6581 *TheSID;
	    MOS6526_1 *TheCIA1;
	    MOS6526_2 *TheCIA2;
	    IEC *TheIEC;
	    REU *TheREU;

	    MOS6502_1541 *TheCPU1541;	// 1541
	    Job1541 *TheJob1541;

        Input* TheInput;
        
    private:
        bool loadRomFiles();
	    void emulationStep();

	    bool quit_thyself;		// Emulation thread shall quit
	    bool have_a_break;		// Emulation thread shall pause
        bool paused;            // Emulation thread paused
        bool vblank_occured;    // Emulation processed vblank

	    int joy_minx, joy_maxx, joy_miny, joy_maxy; // For dynamic joystick calibration

	    uint8 orig_kernal_1d84,	// Original contents of kernal locations $1d84 and $1d85
		      orig_kernal_1d85;	// (for undoing the Fast Reset patch)

        uint32 ticksPerFrame;
        uint32 startTime;
        uint32 speedometerUpdateTime;
        uint32 nextVBlankTime;
	    uint8 joy_state;			// Current state of joystick
	    bool state_change;
        uint8 joystick_input;

    public:
        #ifdef FRODO_SC
    	    uint32 CycleCounter;
    	    void EmulateCycles();
        #endif

    private:
        uint8* diskImageBuffer;
        int diskImageSize;

    public:
        void attachDiskImage(const uint8* imageData, int imageSize);
        void detachDiskImage();
        const uint8* getDiskImage();
        int getDiskImageSize();

};

#endif
