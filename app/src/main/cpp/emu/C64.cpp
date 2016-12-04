/*
 *  C64.cpp - Put the pieces together
 *
 *  Frodo (C) 1994-1997,2002 Christian Bauer
 */
#include "sysdeps.h"
#include "C64.h"
#include "CPUC64.h"
#include "CPU1541.h"
#include "VIC.h"
#include "SID.h"
#include "CIA.h"
#include "REU.h"
#include "IEC.h"
#include "1541job.h"
#include "Prefs.h"
#include "Input.h"

// ROM file names
#define BASIC_ROM_FILE	"resources/Basic.ROM"
#define KERNAL_ROM_FILE	"resources/Kernal.ROM"
#define CHAR_ROM_FILE	"resources/Char.ROM"
#define FLOPPY_ROM_FILE	"resources/1541.ROM"

extern unsigned char* BasicRom;
extern unsigned char* KernalRom;
extern unsigned char* CharacterRom;
extern unsigned char* Drive1541Rom;

#ifdef FRODO_SC
bool IsFrodoSC = true;
#else
bool IsFrodoSC = false;
#endif

#define FRAME_INTERVAL		(1000/SCREEN_FREQ)	// in milliseconds
#ifdef FRODO_SC
#define SPEEDOMETER_INTERVAL	4000			// in milliseconds
#else
#define SPEEDOMETER_INTERVAL	1000			// in milliseconds
#endif
#define JOYSTICK_SENSITIVITY	40			// % of live range
#define JOYSTICK_MIN		0x0000			// min value of range
#define JOYSTICK_MAX		0xffff			// max value of range
#define JOYSTICK_RANGE		(JOYSTICK_MAX - JOYSTICK_MIN)

#define AUDIO_SAMPLE_RATE 44100
#define AUDIO_SAMPLE_BITS 16
#define AUDIO_SAMPLE_COUNT (AUDIO_SAMPLE_RATE / SCREEN_FREQ)

// C64 color palette (theoretical values)
const uint8_t palette_red[16] = {
    0x00, 0xff, 0xff, 0x00, 0xff, 0x00, 0x00, 0xff, 0xff, 0x80, 0xff, 0x40, 0x80, 0x80, 0x80, 0xc0
};

const uint8_t palette_green[16] = {
    0x00, 0xff, 0x00, 0xff, 0x00, 0xff, 0x00, 0xff, 0x80, 0x40, 0x80, 0x40, 0x80, 0xff, 0x80, 0xc0
};

const uint8_t palette_blue[16] = {
    0x00, 0xff, 0x00, 0xff, 0xff, 0x00, 0xff, 0x00, 0x00, 0x00, 0x80, 0x40, 0x80, 0x80, 0xff, 0xc0
};

const uint32_t palette_rgb[16] = {
    0x000000,     // 0 black
    0xffffff,     // 1 white
    0xff0000,     // 2 red
    0x00ffff,     // 3 cyan
    0xff00ff,     // 4 purple
    0x00ff00,     // 5 green
    0x0000ff,     // 6 blue
    0xffff00,     // 7 yellow
    0xff8000,     // 8
    0x804000,     // 9
    0xff8080,     // 10
    0x404040,     // 11
    0x808080,     // 12
    0x80ff80,     // 13
    0x8080ff,     // 14
    0xc0c0c0      // 15
};

const uint32_t palette_rgb_gamma[16] = {

	0x000000,     // 0 black
	0xffffff,     // 1 white
	0x68372b,     // 2 red
	0x70a4b2,     // 3 cyan
	0x6f3d86,     // 4 purple
	0x588d43,     // 5 green
	0x352879,     // 6 blue
	0xb8c76f,     // 7 yellow
	0x6f4f25,     // 8
	0x433900,     // 9
	0x9a6759,     // 10
	0x444444,     // 11
	0x6c6c6c,     // 12
	0x9ad284,     // 13
	0x6c5eb5,     // 14
	0x959595      // 15
};

const uint32_t palette_bgr[16] = {
    0x000000,     // 0 black
    0xffffff,     // 1 white
    0x0000ff,     // 2 red
    0xffff00,     // 3 cyan
    0xff00ff,     // 4 purple
    0x00ff00,     // 5 green
    0xff0000,     // 6 blue
    0x00ffff,     // 7 yellow
    0x0080ff,     // 8
    0x004080,     // 9
    0x8080ff,     // 10
    0x404040,     // 11
    0x808080,     // 12
    0x80ff80,     // 13
    0xff8080,     // 14
    0xc0c0c0      // 15
};

const uint32_t palette_bgr_gamma[16] = {

	0x000000,     // 0 black
	0xffffff,     // 1 white
	0x2b3768,     // 2 red
	0xb2a470,     // 3 cyan
	0x863d6f,     // 4 purple
	0x438d58,     // 5 green
	0x792835,     // 6 blue
	0x6fc7b8,     // 7 yellow
	0x254f6f,     // 8
	0x003943,     // 9
	0x59679a,     // 10
	0x444444,     // 11
	0x6c6c6c,     // 12
	0x84d29a,     // 13
	0xb55e6c,     // 14
	0x959595      // 15

};

static void readMem(void* dest, const uint8*& src, size_t count)
{
    memcpy(dest, src, count); src += count;
}

static void writeMem(const void* src, uint8*& dest, size_t count)
{
    memcpy(dest, src, count); dest += count;
}


/*
 *  Constructor: Allocate objects and memory
 */

C64::C64()
{
    diskImageBuffer = NULL;
    diskImageSize = 0;
}


/*
 *  Destructor: Delete all objects
 */

C64::~C64()
{
    shutdown();
}

bool C64::loadPrefs(const char* preferenceData)
{
	ThePrefs.Load(preferenceData);
    return true;
}


/*
 * Init emulation
 */
bool C64::init()
{
	int i,j;
	uint8 *p;

	// The thread is not yet running
	quit_thyself   = false;
	have_a_break   = false;
    paused         = false;
    vblank_occured = false;

	// Initialize joystick variables.
	joy_state = 0xff;

	// No need to check for state change.
	state_change = false;

	// Allocate RAM/ROM memory
	RAM = new uint8[0x10000];
	Basic = new uint8[0x2000];
	Kernal = new uint8[0x2000];
	Char = new uint8[0x1000];
	Color = new uint8[0x0400];
	RAM1541 = new uint8[0x0800];
	ROM1541 = new uint8[0x4000];

	// Create the chips
	TheCPU = new MOS6510(this, RAM, Basic, Kernal, Char, Color);
	TheJob1541 = new Job1541(RAM1541);
	TheCPU1541 = new MOS6502_1541(this, TheJob1541, RAM1541, ROM1541);
	TheVIC = TheCPU->TheVIC = new MOS6569(this, TheCPU, RAM, Char, Color);
	TheSID = TheCPU->TheSID = new MOS6581(this);
	TheCIA1 = TheCPU->TheCIA1 = new MOS6526_1(TheCPU, TheVIC);
	TheCIA2 = TheCPU->TheCIA2 = TheCPU1541->TheCIA2 = new MOS6526_2(TheCPU, TheVIC, TheCPU1541);
	TheIEC = TheCPU->TheIEC = new IEC();
	TheREU = TheCPU->TheREU = new REU(TheCPU);

	// Initialize RAM with powerup pattern
	for (i=0, p=RAM; i<512; i++) {
		for (j=0; j<64; j++)
			*p++ = 0;
		for (j=0; j<64; j++)
			*p++ = 0xff;
	}

	// Initialize color RAM with random values
	for (i=0, p=Color; i<1024; i++)
    {
		*p++ = rand() & 0x0f;
    }

	// Clear 1541 RAM
	memset(RAM1541, 0, 0x800);

    TheInput = new Input();
    joystick_input = 0;

    #ifdef FRODO_SC
	    CycleCounter = 0;
    #endif

	// System-dependent things

    if (!loadRomFiles())
    {
        return false;
    }

	// Reset chips
	TheCPU->Reset();
	TheSID->Reset();
	TheCIA1->Reset();
	TheCIA2->Reset();
	TheCPU1541->Reset();

	// Patch kernal IEC routines
	orig_kernal_1d84 = Kernal[0x1d84];
	orig_kernal_1d85 = Kernal[0x1d85];
	PatchKernal(ThePrefs.FastReset, ThePrefs.Emul1541Proc);

    ThePrefs.SkipFrames = 2;

    ticksPerFrame = 1000/SCREEN_FREQ;

    return true;
}

bool C64::doInput(int keyCode)
{
    TheInput->handleKeyEvent(keyCode);

    return true;
}

static void unpackGraphics(void* dest, const void* src, size_t pixels, int flags)
{
    const uint8_t* ptrSrc = (const uint8_t*) src;
    uint32_t* ptrDest = (uint32_t*) dest;

	const uint32_t* palette = (flags & FLAG_USE_GAMMA_CORRECTION) ? palette_bgr_gamma : palette_bgr;

    for (int i=0; i<pixels; i++) {
        uint32_t argb = palette[(*(ptrSrc++) & 0xf)];
        *(ptrDest++) = (0xff000000 | argb);
        //*(ptrDest++) = 0x00ff0000;
    }
}

/*
 *  Run emulation
 */

bool C64::doStep(int joystickInput, void* videoOutput, void* audioOutput, int flags)
{
    // update joystick input
    // will be loaded at next vblank
    joystick_input = (uint8) joystickInput;

    if (!paused)
    {
        vblank_occured = false;

        emulationStep();

        if (vblank_occured)
        {
            if (NULL != videoOutput)
            {
                if (0 != (flags & FLAG_UNPACK_GRAPHICS))
                {
                    unpackGraphics(videoOutput, TheVIC->getFrontBuffer(), TheVIC->getBufferSize(), flags);
                }
                else
                {
                    memcpy(videoOutput, TheVIC->getFrontBuffer(), TheVIC->getBufferSize());
                }
            }

            if (NULL != audioOutput)
            {
                //printf("Render Audio\n");
                TheSID->RenderAudio((int16*) audioOutput, AUDIO_SAMPLE_COUNT*2);
            }
        }

        return vblank_occured;
    }

    return false;
}

void C64::shutdown()
{
    delete TheInput;
	delete TheJob1541;
	delete TheREU;
	delete TheIEC;
	delete TheCIA2;
	delete TheCIA1;
	delete TheSID;
	delete TheVIC;
	delete TheCPU1541;
	delete TheCPU;

	delete[] RAM;
	delete[] Basic;
	delete[] Kernal;
	delete[] Char;
	delete[] Color;
	delete[] RAM1541;
	delete[] ROM1541;

    detachDiskImage();
}

/*
 *  Reset C64
 */

void C64::Reset(void)
{
	TheCPU->AsyncReset();
	TheCPU1541->AsyncReset();
	TheSID->Reset();
	TheCIA1->Reset();
	TheCIA2->Reset();
	TheIEC->Reset();
}


/*
 *  NMI C64
 */

void C64::NMI(void)
{
	TheCPU->AsyncNMI();
}


/*
 *  The preferences have changed. prefs is a pointer to the new
 *   preferences, ThePrefs still holds the previous ones.
 *   The emulation must be in the paused state!
 */

void C64::NewPrefs(Prefs *prefs)
{
	PatchKernal(prefs->FastReset, prefs->Emul1541Proc);

	//TheDisplay->NewPrefs(prefs);

	TheIEC->NewPrefs(prefs);
	TheJob1541->NewPrefs(prefs);

	TheREU->NewPrefs(prefs);
	TheSID->NewPrefs(prefs);

	// Reset 1541 processor if turned on
	if (!ThePrefs.Emul1541Proc && prefs->Emul1541Proc)
    {
		TheCPU1541->AsyncReset();
    }
}


/*
 *  Patch kernal IEC routines
 */

void C64::PatchKernal(bool fast_reset, bool emul_1541_proc)
{
	if (fast_reset) {
		Kernal[0x1d84] = 0xa0;
		Kernal[0x1d85] = 0x00;
	} else {
		Kernal[0x1d84] = orig_kernal_1d84;
		Kernal[0x1d85] = orig_kernal_1d85;
	}

	if (emul_1541_proc) {
		Kernal[0x0d40] = 0x78;
		Kernal[0x0d41] = 0x20;
		Kernal[0x0d23] = 0x78;
		Kernal[0x0d24] = 0x20;
		Kernal[0x0d36] = 0x78;
		Kernal[0x0d37] = 0x20;
		Kernal[0x0e13] = 0x78;
		Kernal[0x0e14] = 0xa9;
		Kernal[0x0def] = 0x78;
		Kernal[0x0df0] = 0x20;
		Kernal[0x0dbe] = 0xad;
		Kernal[0x0dbf] = 0x00;
		Kernal[0x0dcc] = 0x78;
		Kernal[0x0dcd] = 0x20;
		Kernal[0x0e03] = 0x20;
		Kernal[0x0e04] = 0xbe;
	} else {
		Kernal[0x0d40] = 0xf2;	// IECOut
		Kernal[0x0d41] = 0x00;
		Kernal[0x0d23] = 0xf2;	// IECOutATN
		Kernal[0x0d24] = 0x01;
		Kernal[0x0d36] = 0xf2;	// IECOutSec
		Kernal[0x0d37] = 0x02;
		Kernal[0x0e13] = 0xf2;	// IECIn
		Kernal[0x0e14] = 0x03;
		Kernal[0x0def] = 0xf2;	// IECSetATN
		Kernal[0x0df0] = 0x04;
		Kernal[0x0dbe] = 0xf2;	// IECRelATN
		Kernal[0x0dbf] = 0x05;
		Kernal[0x0dcc] = 0xf2;	// IECTurnaround
		Kernal[0x0dcd] = 0x06;
		Kernal[0x0e03] = 0xf2;	// IECRelease
		Kernal[0x0e04] = 0x07;
	}

	// 1541
	ROM1541[0x2ae4] = 0xea;		// Don't check ROM checksum
	ROM1541[0x2ae5] = 0xea;
	ROM1541[0x2ae8] = 0xea;
	ROM1541[0x2ae9] = 0xea;
	ROM1541[0x2c9b] = 0xf2;		// DOS idle loop
	ROM1541[0x2c9c] = 0x00;
	ROM1541[0x3594] = 0x20;		// Write sector
	ROM1541[0x3595] = 0xf2;
	ROM1541[0x3596] = 0xf5;
	ROM1541[0x3597] = 0xf2;
	ROM1541[0x3598] = 0x01;
	ROM1541[0x3b0c] = 0xf2;		// Format track
	ROM1541[0x3b0d] = 0x02;
}


/*
 *  Save RAM contents
 */

void C64::SaveRAM(uint8* buf)
{
	writeMem((void*)RAM, buf, 0x10000);
	writeMem((void*)Color, buf, 0x400);
	if (ThePrefs.Emul1541Proc)
		writeMem((void*)RAM1541, buf, 0x800);
}


/*
 *  Save CPU state to snapshot
 *
 *  0: Error
 *  1: OK
 *  -1: Instruction not completed
 */

int C64::SaveCPUState(uint8*& buf)
{
	MOS6510State state;
	TheCPU->GetState(&state);

	if (!state.instruction_complete)
		return -1;

	writeMem(RAM, buf, 0x10000);
	writeMem(Color, buf, 0x400);
	writeMem((void*)&state, buf, sizeof(state));

    return 1;
}

/*
 *  Load CPU state from snapshot
 */

bool C64::LoadCPUState(const uint8*& buf)
{
	MOS6510State state;

    readMem(RAM, buf, 0x10000);
    readMem(Color, buf, 0x400);
	readMem((void*)&state, buf, sizeof(state));

	TheCPU->SetState(&state);
	return true;
}


/*
 *  Save 1541 state to snapshot
 *
 *  0: Error
 *  1: OK
 *  -1: Instruction not completed
 */

int C64::Save1541State(uint8*& buf)
{
	MOS6502State state;
	TheCPU1541->GetState(&state);

	if (!state.idle && !state.instruction_complete)
		return -1;

	writeMem(RAM1541, buf, 0x800);
	writeMem((void*)&state, buf, sizeof(state));

	return 1;
}


/*
 *  Load 1541 state from snapshot
 */

bool C64::Load1541State(const uint8*& buf)
{
	MOS6502State state;

    readMem(RAM1541, buf, 0x800);
    readMem((void*)&state, buf, sizeof(state));

	TheCPU1541->SetState(&state);
	return true;
}


/*
 *  Save VIC state to snapshot
 */

bool C64::SaveVICState(uint8*& buf)
{
	MOS6569State state;
	TheVIC->GetState(&state);
	writeMem((void*)&state, buf, sizeof(state));

    return true;
}


/*
 *  Load VIC state from snapshot
 */

bool C64::LoadVICState(const uint8*& buf)
{
	MOS6569State state;

	readMem((void*)&state, buf, sizeof(state));
	TheVIC->SetState(&state);
	return true;
}


/*
 *  Save SID state to snapshot
 */

bool C64::SaveSIDState(uint8*& buf)
{
	MOS6581State state;
	TheSID->GetState(&state);
	writeMem((void*)&state, buf, sizeof(state));

    return true;
}


/*
 *  Load SID state from snapshot
 */

bool C64::LoadSIDState(const uint8*& buf)
{
	MOS6581State state;

	readMem((void*)&state, buf, sizeof(state));
	TheSID->SetState(&state);
	return true;
}


/*
 *  Save CIA states to snapshot
 */

bool C64::SaveCIAState(uint8*& buf)
{
	MOS6526State state;
	TheCIA1->GetState(&state);
	writeMem((void*)&state, buf, sizeof(state));
	TheCIA2->GetState(&state);
	writeMem((void*)&state, buf, sizeof(state));

    return true;
}


/*
 *  Load CIA states from snapshot
 */

bool C64::LoadCIAState(const uint8*& buf)
{
	MOS6526State state;

	readMem((void*)&state, buf, sizeof(state));
	TheCIA1->SetState(&state);
	readMem((void*)&state, buf, sizeof(state));
	TheCIA2->SetState(&state);

	return true;
}


/*
 *  Save 1541 GCR state to snapshot
 */

bool C64::Save1541JobState(uint8*& buf)
{
	Job1541State state;
	TheJob1541->GetState(&state);
	writeMem((void*)&state, buf, sizeof(state));
    return true;
}


/*
 *  Load 1541 GCR state from snapshot
 */

bool C64::Load1541JobState(const uint8*& buf)
{
	Job1541State state;

	readMem((void*)&state, buf, sizeof(state));
	TheJob1541->SetState(&state);
	return true;
}


#define SNAPSHOT_HEADER "FrodoSnapshot"
#define SNAPSHOT_1541 1

#define ADVANCE_CYCLES	\
	TheVIC->EmulateCycle(); \
	TheCIA1->EmulateCycle(); \
	TheCIA2->EmulateCycle(); \
	TheCPU->EmulateCycle(); \
	if (ThePrefs.Emul1541Proc) { \
		TheCPU1541->CountVIATimers(1); \
		if (!TheCPU1541->Idle) \
			TheCPU1541->EmulateCycle(); \
	}


/*
 *  Save snapshot (emulation must be paused and in VBlank)
 *
 *  To be able to use SC snapshots with SL, SC snapshots are made thus that no
 *  partially dealt with instructions are saved. Instead all devices are advanced
 *  cycle by cycle until the current instruction has been finished. The number of
 *  cycles this takes is saved in the snapshot and will be reconstructed if the
 *  snapshot is loaded into FrodoSC again.
 */

int C64::SaveSnapshot(uint8* buf, int buffer_size)
{
	uint8 flags;
	uint8 delay;
	int stat;

    uint8* ptr = buf;

	sprintf((char*) ptr, "%s%c", SNAPSHOT_HEADER, 10); ptr += strlen(SNAPSHOT_HEADER) + 1;
	*(ptr++) = 0;
	flags = 0;
	if (ThePrefs.Emul1541Proc)
		flags |= SNAPSHOT_1541;
	*(ptr++) = flags;
	SaveVICState(ptr);
	SaveSIDState(ptr);
	SaveCIAState(ptr);

    #ifdef FRODO_SC
	    delay = 0;
	    do {
		    if ((stat = SaveCPUState(ptr)) == -1) {	// -1 -> Instruction not finished yet
			    ADVANCE_CYCLES;	// Advance everything by one cycle
			    delay++;
		    }
	    } while (stat == -1);
	    *(ptr++) = delay;	// Number of cycles the saved CPUC64 lags behind the previous chips
    #else
	    SaveCPUState(ptr);
	    *(ptr++) = 0;		// No delay
    #endif

	if (ThePrefs.Emul1541Proc) 
    {
        writeMem(ThePrefs.DrivePath[0], ptr, 256);
        #ifdef FRODO_SC
		    delay = 0;
		    do {
			    if ((stat = Save1541State(f)) == -1) {
				    ADVANCE_CYCLES;
				    delay++;
			    }
		    } while (stat == -1);
		    fputc(delay, f);
        #else
		    Save1541State(ptr);
		    *(ptr++) = 0;	// No delay
        #endif
		Save1541JobState(ptr);
	}

    int storedBytes = (ptr - buf);

    return storedBytes;

}


/*
 *  Load snapshot (emulation must be paused and in VBlank)
 */

bool C64::LoadSnapshot(const void* data, int data_size)
{
	if (NULL == data || data_size < 64*1024)
    {
		ShowRequester("Can't open snapshot file", "OK", NULL);
        return false;
    }

	char Header[] = SNAPSHOT_HEADER;
	char *b = Header;
	uint8 delay, i;

    const uint8* ptr = (const uint8*) data;

	// For some reason memcmp()/strcmp() and so forth utterly fail here.
	while (*b > 32) 
    {
		if (*(ptr++) != *b++) 
        {
    		ShowRequester("Not a Frodo snapshot file", "OK", NULL);
			return false;
		}
	}

	uint8 flags;
	bool error = false;
    #ifndef FRODO_SC
		const uint8* vicptr;	// File offset of VIC data
    #endif

	while (*ptr != 0) ptr++;	// Shouldn't be necessary
	if (*(ptr++) != 0) {
		ShowRequester("Unknown snapshot format", "OK", NULL);
		return false;
	}
	flags = *(ptr++);
    #ifndef FRODO_SC
    	vicptr = ptr;
    #endif

	error |= !LoadVICState(ptr);
	error |= !LoadSIDState(ptr);
	error |= !LoadCIAState(ptr);
	error |= !LoadCPUState(ptr);

	delay = *(ptr++);	// Number of cycles the 6510 is ahead of the previous chips
    #ifdef FRODO_SC
		// Make the other chips "catch up" with the 6510
		for (i=0; i<delay; i++) {
			TheVIC->EmulateCycle();
			TheCIA1->EmulateCycle();
			TheCIA2->EmulateCycle();
		}
    #endif

	if ((flags & SNAPSHOT_1541) != 0) {
		Prefs *prefs = new Prefs(ThePrefs);
	
		// First switch on emulation
		//error |= (fread(prefs->DrivePath[0], 256, 1, f) != 1);
        memcpy(prefs->DrivePath[0], ptr, 256); ptr += 256;

		prefs->Emul1541Proc = true;
		NewPrefs(prefs);
		ThePrefs = *prefs;
		delete prefs;
	
		// Then read the context
		error |= !Load1541State(ptr);
	
		delay = *(ptr++);	// Number of cycles the 6502 is ahead of the previous chips
        #ifdef FRODO_SC
			// Make the other chips "catch up" with the 6502
			for (i=0; i<delay; i++) {
				TheVIC->EmulateCycle();
				TheCIA1->EmulateCycle();
				TheCIA2->EmulateCycle();
				TheCPU->EmulateCycle();
			}
        #endif
		Load1541JobState(ptr);
	} else if (ThePrefs.Emul1541Proc) {	// No emulation in snapshot, but currently active?
		Prefs *prefs = new Prefs(ThePrefs);
		prefs->Emul1541Proc = false;
		NewPrefs(prefs);
		ThePrefs = *prefs;
		delete prefs;
	}

    #ifndef FRODO_SC
		ptr = vicptr;
		LoadVICState(ptr);	// Load VIC data twice in SL (is REALLY necessary sometimes!)
    #endif
	
	if (error) {
		ShowRequester("Error reading snapshot file", "OK", NULL);
		Reset();
		return false;
	} else
		return true;
}

bool C64::loadRomFiles()
{
    memcpy(Basic, BasicRom, 0x2000);
    memcpy(Kernal, KernalRom, 0x2000);
    memcpy(Char, CharacterRom, 0x1000);
    memcpy(ROM1541, Drive1541Rom, 0x4000);

	//FILE *file;

	// Load Basic ROM
	/*
	if ((file = fopen(BASIC_ROM_FILE, "rb")) != NULL) {
		if (fread(Basic, 1, 0x2000, file) != 0x2000) {
			ShowRequester("Can't read 'Basic ROM'.", "Quit");
			return false;
		}
		fclose(file);
	} else {
		ShowRequester("Can't find 'Basic ROM'.", "Quit");
		return false;
	}

	// Load Kernal ROM
	if ((file = fopen(KERNAL_ROM_FILE, "rb")) != NULL) {
		if (fread(Kernal, 1, 0x2000, file) != 0x2000) {
			ShowRequester("Can't read 'Kernal ROM'.", "Quit");
			return false;
		}
		fclose(file);
	} else {
		ShowRequester("Can't find 'Kernal ROM'.", "Quit");
		return false;
	}

	// Load Char ROM
	if ((file = fopen(CHAR_ROM_FILE, "rb")) != NULL) {
		if (fread(Char, 1, 0x1000, file) != 0x1000) {
			ShowRequester("Can't read 'Char ROM'.", "Quit");
			return false;
		}
		fclose(file);
	} else {
		ShowRequester("Can't find 'Char ROM'.", "Quit");
		return false;
	}

	// Load 1541 ROM
	if ((file = fopen(FLOPPY_ROM_FILE, "rb")) != NULL) {
		if (fread(ROM1541, 1, 0x4000, file) != 0x4000) {
			ShowRequester("Can't read '1541 ROM'.", "Quit");
			return false;
		}
		fclose(file);
	} else {
		ShowRequester("Can't find '1541 ROM'.", "Quit");
		return false;
	}
	*/

	return true;
}

bool C64::isCancelled()
{
    return quit_thyself;
}


/*
 *  Stop emulation
 */

void C64::Quit()
{
	// Ask the thread to quit itself if it is running
	quit_thyself = true;
	state_change = true;
}


/*
 *  Pause emulation
 */

void C64::Pause()
{
	TheSID->PauseSound();
	have_a_break = true;
	state_change = true;
}

bool C64::isPaused()
{
    return have_a_break;
}

/*
 *  Resume emulation
 */

void C64::Resume()
{
	TheSID->ResumeSound();
	have_a_break = false;
    paused = false;
}

/*
 *  Vertical blank: Poll keyboard and joysticks, update window
 */

void C64::VBlank(bool draw_frame)
{
    vblank_occured = true;

	// poll keyboard
	TheInput->getState(TheCIA1->KeyMatrix, TheCIA1->RevMatrix);

    // update joystick
    // uint8 joykey = TheJoystick->getState();
    uint8 joykey = joystick_input;

	if (!ThePrefs.JoystickSwap) 
    {
        TheCIA1->Joystick1  = 0xff;
		TheCIA1->Joystick2  = joykey;
	}
    else
    {
		TheCIA1->Joystick1  = joykey;
        TheCIA1->Joystick2  = 0xff;
    }

	// Count TOD clocks.
	TheCIA1->CountTOD();
	TheCIA2->CountTOD();

	if (have_a_break)
    {
        paused = true;
		return;
    }
}

/*
 * One emulation step
 */

void C64::emulationStep()
{
    if (quit_thyself)
    {
        return;
    }

    #ifdef FRODO_SC

		EmulateCycles();
		state_change = false;

    #else
		// The order of calls is important here
		int cycles = TheVIC->EmulateLine();
		TheSID->EmulateLine();

        #if !PRECISE_CIA_CYCLES
		    TheCIA1->EmulateLine(ThePrefs.CIACycles);
		    TheCIA2->EmulateLine(ThePrefs.CIACycles);
        #endif

		if (ThePrefs.Emul1541Proc) 
        {
			int cycles_1541 = ThePrefs.FloppyCycles;
			TheCPU1541->CountVIATimers(cycles_1541);

			if (!TheCPU1541->Idle) 
            {
				// 1541 processor active, alternately execute
				//  6502 and 6510 instructions until both have
				//  used up their cycles
				while (cycles >= 0 || cycles_1541 >= 0)
                {
					if (cycles > cycles_1541)
                    {
						cycles -= TheCPU->EmulateLine(1);
                    }
					else
                    {
						cycles_1541 -= TheCPU1541->EmulateLine(1);
                    }
                }
			} 
            else
            {
				TheCPU->EmulateLine(cycles);
            }
		}
        else
        {
			// 1541 processor disabled, only emulate 6510
			TheCPU->EmulateLine(cycles);
        }
    #endif
}

int C64::ShowRequester(const char* text, const char* button1, const char* button2)
{
    printf("%s\n", text);

    return 1;
}

void C64::attachDiskImage(const uint8* imageData, int imageSize)
{
    detachDiskImage();

    if (NULL != imageData && imageSize > 0)
    {
        diskImageBuffer = new uint8[imageSize];
        memcpy(diskImageBuffer, imageData, imageSize);
        diskImageSize = imageSize;
    }
}

void C64::detachDiskImage()
{
    diskImageSize = 0;

    if (NULL != diskImageBuffer)
    {
        delete [] diskImageBuffer;
        diskImageBuffer = NULL;
    }

}

const uint8* C64::getDiskImage()
{
    return diskImageBuffer;
}

int C64::getDiskImageSize()
{
    return diskImageSize;
}


#ifdef FRODO_SC

void C64::EmulateCycles()
{
    bool emul1541 = ThePrefs.Emul1541Proc;
    bool vicCycleFinished = false;

	while (!state_change) 
    {

		// The order of calls is important here
        vicCycleFinished = TheVIC->EmulateCycle();

		if (vicCycleFinished)
        {
			TheSID->EmulateLine();
        }

		TheCIA1->CheckIRQs();
		TheCIA2->CheckIRQs();

        #ifndef BATCH_CIA_CYCLES
		    TheCIA1->EmulateCycle();
		    TheCIA2->EmulateCycle();
        #endif

		TheCPU->EmulateCycle();

        if (emul1541)
        {
		    TheCPU1541->CountVIATimers(1);
		    if (!TheCPU1541->Idle)
            {
			    TheCPU1541->EmulateCycle();
            }
        }

		CycleCounter++;
	}

}

#endif
