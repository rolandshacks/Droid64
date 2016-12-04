package org.codewiz.droid64.emu;

import org.codewiz.droid64.util.LogManager;
import org.codewiz.droid64.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by roland on 14.08.2016.
 */

public class DiskImage {

    private final static Logger logger = LogManager.getLogger(DiskImage.class.getName());
    private static final int DISK_IMAGE_SIZE = 174848; // bytes

    public static final int TYPE_SNAPSHOT = 1;
    public static final int TYPE_DISK     = 2;
    public static final int TYPE_TAPE     = 3;
    public static final int TYPE_DIR      = 4;

    private boolean zip;

    private String url;
    private String name;
    private String archiveElement;
    private int type;

    public DiskImage(String url) {
        this.url = url;
        set(url);
        zip = false;
    }

    public DiskImage(String archiveUrl, String archiveElement) {
        this.url = archiveUrl;
        this.archiveElement = archiveElement;
        set(archiveElement);
        zip = true;
    }

    private void set(String path) {

        int pos = path.lastIndexOf('/');
        if (pos <= 0) {
            name = path;
        } else {
            name = path.substring(pos+1);
        }

        type = TYPE_DISK;

        pos = name.lastIndexOf('.');
        if (pos > 0) {
            String ext = name.substring(pos + 1).toLowerCase();
            if (ext.equals("snap")) {
                type = TYPE_SNAPSHOT;
            } else if (ext.equals("t64")) {
                type = TYPE_TAPE;
            } else if (ext.equals("prg")) {
                type = TYPE_DIR;
            }
        }

    }

    public boolean isZip() {
        return zip;
    }

    public String getUrl() {
        return url;
    }

    public String getArchivePath() { return archiveElement; }

    public String getName() {
        return name;
    }

    public int getType() { return type; }

    @Override
    public String toString() {
        return getName();
    }

    /*
	private void dumpBytes(byte[] bytes) {
		int num = 512;

		StringBuilder s = new StringBuilder();

		for (int i=0; i<bytes.length; i++) {
			int b = bytes[i];
			if (0 != b) {
				s.append(Integer.toHexString((bytes[i])&0xff));
				num--;
				if (num < 1) break;
			}
		}

		System.out.println(s.toString());
	}

    private static byte[] lastBuffer;

    private static boolean memcompare(byte[] a, byte[] b, int ofs, int length) {

        if (null == a || null == b) return false;

        if (a.length != b.length) {
            logger.info("length difference: " + a.length + " / " + b.length);
        }

        boolean equal = Arrays.equals(a, b);

        logger.info("arrays equal ? " + equal);

        return true;
    }

    private static void compareWithLast(byte[] data) {
        if (null != lastBuffer) {
            memcompare(data, lastBuffer, 0, data.length);
        }

        if (null == lastBuffer || lastBuffer.length != data.length) {
            lastBuffer = new byte[data.length];
        }

        System.arraycopy(data, 0, lastBuffer, 0, data.length);
    }
    */

    public byte[] load() {

        byte[] data = null;

        if (isZip()) {
            data = loadFromZip();
        } else {
            data = loadFromFile();
        }

        if (null == data) {
            return null;
        }

        //compareWithLast(data);

        return data;
    }

    private byte[] loadFromFile() {
        try {
            String filename = getUrl();

            File file = new File(filename);

            long fileSize = file.length();

            if (fileSize > DISK_IMAGE_SIZE) {
                logger.error("invalid disk image");
                return null;
            }

            int bufferSize = (int) fileSize;

            byte[] buffer = new byte[bufferSize];

            InputStream is = new FileInputStream(filename);

            int bytesRead = is.read(buffer, 0, bufferSize);
            if (bytesRead == bufferSize) {
                return buffer;
            }

        } catch (IOException e) {
            logger.error("failed to read disk image from storage");
        }

        return null;

    }

    private byte[] loadFromZip() {
        try {
            String filename = getUrl();

            ZipFile zipFile = new ZipFile(filename);

            ZipEntry entry = zipFile.getEntry(getArchivePath());
            if (null == entry) {
                logger.error("could not access zip file entry: " + entry.getName());
                return null;
            }

            long fileSize = entry.getSize();

            if (fileSize > DISK_IMAGE_SIZE) {
                logger.error("invalid disk image in zip file");
                return null;
            }

            int bufferSize = (int) fileSize;

            byte[] buffer = new byte[bufferSize];

            InputStream is = zipFile.getInputStream(entry);
            if (null == is) {
                logger.error("could not read from zip file");
                return null;
            }

            int bufferOffset = 0;
            int bytesToRead = bufferSize;

            while (bytesToRead > 0) {
                int bytesRead = is.read(buffer, bufferOffset, bytesToRead);
                if (bytesToRead < 1) break;
                bytesToRead -= bytesRead;
                bufferOffset += bytesRead;
            }

            if (0 == bytesToRead) {
                return buffer;
            } else {
                logger.error("invalid zip file content");
            }

        } catch (IOException e) {
            logger.error("failed to read disk image from zip file");
        }

        return null;
    }



}
