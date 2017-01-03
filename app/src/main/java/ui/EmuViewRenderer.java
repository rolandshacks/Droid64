package ui;

import android.content.Context;
import android.graphics.Rect;

import emu.Control;
import system.Preferences;
import system.Statistics;
import util.LogManager;
import util.Logger;
import util.Util;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by roland on 17.08.2016.
 */

public class EmuViewRenderer extends Renderer {

    private final static Logger logger = LogManager.getLogger(EmuViewRenderer.class.getName());

    private Rect renderRect = new Rect(0, 0, 0, 0);

    private boolean packedTextures = false;
    private int texture;
    private int textureWidth = Control.DISPLAY_X;
    private int textureHeight = Control.DISPLAY_Y;

    private ByteBuffer textureUpdateBuffer = null;
    private int textureUpdateBufferSize = 0;

    private int markerTexture;

    private DecimalFormat statisticsOutputFormat = new DecimalFormat("0.0");

    public EmuViewRenderer(Context context) {
        super(context);
    }

    @Override
    protected void init() {
        packedTextures = Preferences.instance().isTextureCompressionEnabled();
    }

    @Override
    protected void resize(int width, int height) {
    }

    @Override
    protected void loadResources() {
        markerTexture = loadTexture("button.png");
    }

    private void updateTexture() {

        byte[] rawData = Control.instance().lockTextureData();
        if (null == rawData) {
            return;
        }

        if (textureUpdateBuffer == null || textureUpdateBufferSize != rawData.length) {
            textureUpdateBuffer = ByteBuffer.allocate(rawData.length);
            textureUpdateBufferSize = rawData.length;
        }

        textureUpdateBuffer.put(rawData);
        textureUpdateBuffer.position(0);

        boolean newTexture = false;

        if (0 == texture) {
            texture = allocTexture();
            newTexture = true;
        }

        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        gl.glPixelStorei(GL10.GL_UNPACK_ALIGNMENT, 1);
        gl.glPixelStorei(GL10.GL_PACK_ALIGNMENT, 1);


        if (!packedTextures) {

            int internalFormat = GL10.GL_RGBA;
            int pixelFormat = GL10.GL_RGBA;

            if (newTexture) {
                gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, internalFormat, textureWidth, textureHeight, 0, pixelFormat, GL10.GL_UNSIGNED_BYTE, textureUpdateBuffer);
            } else {
                gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, textureWidth, textureHeight, pixelFormat, GL10.GL_UNSIGNED_BYTE, textureUpdateBuffer);
            }

        } else {

            // 4-bit indexed texture (16 color palette)

            /*

            int bufferSize = buffer.capacity();

            int internalFormat = GL10.GL_PALETTE8_RGB8_OES;
            int pixelFormat = 0;

            if (newTexture) {

                gl.glCompressedTexImage2D(GL10.GL_TEXTURE_2D, 0, internalFormat, textureWidth, textureHeight, 0, bufferSize, buffer);
            } else {
                gl.glCompressedTexSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, textureWidth, textureHeight, 0, bufferSize, buffer);
            }

            */

        }

        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);

        Control.instance().unlockTextureData();

    }

    @Override
    protected void draw() {

        Control emuControl = Control.instance();
        if (null == emuControl) {
            return;
        }

        updateTexture();

        boolean textureFiltering = Preferences.instance().isAntialiasingEnabled();

        renderRect.set(Util.matchSize(getWidth(), getHeight()));

        Quad.draw(gl,
                texture,
                (float) renderRect.left,
                (float) renderRect.top,
                (float) renderRect.width(),
                (float) renderRect.height(),
                Color.WHITE,
                textureFiltering);
    }

    @Override
    protected void drawOverlay() {

        VirtualGamepad gamepad = VirtualGamepad.instance();

        float visibility = gamepad.getVisibility();
        if (visibility == 0.0f) {
            return;
        }

        Color c = new Color(1.0f, 1.0f, 1.0f, visibility);

        float x = gamepad.getStickX();
        float y = gamepad.getStickY();

        float w = gamepad.getStickXGap() / 2.0f;
        float h = gamepad.getStickYGap() / 2.0f;

        setBlendMode(Renderer.BLEND_MODE_ADDITIVE);

        Quad.draw(gl,
                markerTexture,
                x-w/2.0f, y-h/2.0f, w, h,
                c,
                true);

        setBlendMode(Renderer.BLEND_MODE_NORMAL);
    }

    @Override
    protected void outputStatistics(Statistics statistics) {

        double emuUpdatePerSec = Control.instance().getUpdatesPerSecond();
        double renderUpdatePerSec = statistics.getUpdatesPerSecond();

        String text = "Statistics: " + statisticsOutputFormat.format(emuUpdatePerSec) + " ups / " + statisticsOutputFormat.format(renderUpdatePerSec) + " fps";

        logger.info(text);

    }

}
