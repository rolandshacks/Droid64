package org.codewiz.droid64.emu;


public class EmuBindings {

	public static int COMMAND_UNKNOWN         = 0;
	public static int COMMAND_TRUEDRIVE_ON    = 1;
	public static int COMMAND_TRUEDRIVE_OFF   = 2;
	public static int COMMAND_RESET           = 3;
	public static int COMMAND_DEBUGGER_TOGGLE = 4;
	public static int COMMAND_RESTORE         = 5;
	public static int COMMAND_JOYSTICK_SWAP_ON  = 6;
	public static int COMMAND_JOYSTICK_SWAP_OFF  = 7;

	static {
        System.loadLibrary("Droid64");
    }
	
	public native int init(String prefs, int flags);
	public native int input(int keyCode); // buffered, does not need to be synchronized!
	public native int load(int dataType, byte[] buffer, int bufferSize, String filename); // to be synchronized
	public native int store(int dataType, byte[] buffer, int bufferSize); // to be synchronized
	public native int command(int command); // to be synchronized
	public native int update(int joystickInput, byte[] videoOutput, byte[] audioOutput, int flags); // to be synchronized
	public native int shutdown();
}
