package org.codewiz.droid64.emu;

import org.codewiz.droid64.util.LogManager;
import org.codewiz.droid64.util.Logger;

/**
 * Created by roland on 02.09.2016.
 */

public class DiskFilter {

    private final static Logger logger = LogManager.getLogger(DiskFilter.class.getName());

    private char startChar;
    private char endChar;
    private String extension;
    private String label;
    private IFilter filter;

    public interface IFilter {
        boolean match(DiskImage img);
    }

    public DiskFilter(IFilter filter, String label) {
        this.filter = filter;
        this.label = label;
    }

    public DiskFilter(char startChar, char endChar, String label) {
        set(startChar, endChar, "", label);
    }

    public DiskFilter(char startChar, char endChar) {
        set(startChar, endChar);
    }

    public DiskFilter(String extension, String label) {
        set(extension, label);
    }

    public DiskFilter(char filterChar) {
        set(filterChar);
    }

    public void set(char startChar, char endChar, String extension, String label) {

        this.startChar = startChar;
        this.endChar = endChar;
        this.extension = extension;

        if (null == label  || label.isEmpty()) {
            this.label = generateLabel();
        } else {
            this.label = label;
        }
    }

    public void set(char startChar, char endChar) {
        set(startChar, endChar, null, null);
    }

    public void set(char startChar) {
        set(startChar, '\0');
    }

    public void set(String extension, String label) {
        set('\0', '\0', extension, label);
    }

    private String generateLabel() {

        String label;

        if (endChar != 0) {
            label = (startChar + "-" + endChar).toUpperCase();
        } else {
            label = String.valueOf(startChar).toUpperCase();
        }

        return label;
    }

    public char getStartChar() {
        return startChar;
    }

    public void setStartChar(char startChar) {
        this.startChar = startChar;
    }

    public char getEndChar() {
        return endChar;
    }

    public void setEndChar(char endChar) {
        this.endChar = endChar;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public String toString() {
        return label;
    }

    public boolean match(DiskImage img) {

        if (null == img) {
            return false;
        }

        String name = img.getName();
        if (null == name || name.isEmpty()) {
            return false;
        }

        if (null != filter) {
            return filter.match(img);
        }

        if (null != extension && !extension.isEmpty()) {
            int pos = name.lastIndexOf('.');
            if (pos <= 0) return false;

            String nameExt = name.substring(pos+1);
            if (!nameExt.equalsIgnoreCase(extension)) {
                return false;
            }
        }

        if (startChar == 0) {
            return true;
        }

        String filterStr = name.toLowerCase();
        char c = filterStr.charAt(0);

        if (endChar != 0 ) {
            return (c >= startChar && c <= endChar);
        } else {
            return (c == startChar);
        }
    }
}
