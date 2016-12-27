package emu;

import util.LogManager;
import util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by roland on 14.08.2016.
 */

public class Image {

    private final static Logger logger = LogManager.getLogger(Image.class.getName());
    private static final int MAX_IMAGE_SIZE = 174848; // 174Kbytes max disk size

    public static final int TYPE_SNAPSHOT = 1;
    public static final int TYPE_DISK = 2;
    public static final int TYPE_TAPE = 3;
    public static final int TYPE_DIR = 4;

    private boolean zip;

    private String url;
    private String name;
    private String archiveElement;
    private int type;

    public Image(String url) {
        this.url = url;
        set(url);
        zip = false;
    }

    public Image(String archiveUrl, String archiveElement) {
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
            name = path.substring(pos + 1);
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

    public String getArchivePath() {
        return archiveElement;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return getName();
    }

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

            if (fileSize > MAX_IMAGE_SIZE) {
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

            if (fileSize > MAX_IMAGE_SIZE) {
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
