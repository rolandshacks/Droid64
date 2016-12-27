package emu;

/**
 * Created by roland on 29.08.2016.
 */
public class KeyMapEntry {

    private final int keyCode;
    private final int c64Code;
    private final String keyName;
    private boolean hidden;

    public KeyMapEntry(int keyCode, int c64Code, String keyName, String flags) {
        this.keyCode = keyCode;
        this.c64Code = c64Code;
        this.keyName = keyName;

        parseFlags(flags);
    }

    private void parseFlags(String flags) {
        if (null == flags || flags.isEmpty()) return;

        String[] flagList = flags.split("[,;/]+");
        if (null == flagList || flagList.length < 1) return;

        for (String flag : flagList) {
            String f = flag.trim().toLowerCase();

            if (f.equals("hidden")) {
                hidden = true;
            }
        }
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int getC64Code() {
        return c64Code;
    }

    public String getKeyName() {
        return keyName;
    }

    public boolean  isHidden() {
        return hidden;
    }

    @Override
    public String toString() {
        if (null != keyName && !keyName.isEmpty()) {
            return keyName;
        }

        return super.toString();
    }

}
