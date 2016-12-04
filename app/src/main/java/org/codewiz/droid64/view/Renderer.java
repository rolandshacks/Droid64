package org.codewiz.droid64.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import org.codewiz.droid64.emu.EmuControl;
import org.codewiz.droid64.emu.EmuPrefs;
import org.codewiz.droid64.emu.Statistics;
import org.codewiz.droid64.util.LogManager;
import org.codewiz.droid64.util.Logger;
import org.codewiz.droid64.util.Util;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by roland on 04.12.2016.
 */

public class Renderer implements GLSurfaceView.Renderer {

    private final static Logger logger = LogManager.getLogger(Renderer.class.getName());

    public static final int BLEND_MODE_NORMAL = 0x1;
    public static final int BLEND_MODE_ADDITIVE = 0x2;

    private boolean resourcesLoaded;
    private boolean statisticsEnabled;
    private Statistics renderStatistics = new Statistics();
    private long statisticsOutputTime;

    private int width;
    private int height;

    protected Context context;
    protected GL10 gl; // current GL context

    public Renderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {

        this.gl = gl;

        if (resourcesLoaded) {
            resourcesLoaded = false;
            freeResources();
        }

        logger.info("Renderer: surface created");

        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glDisable(GL10.GL_DITHER);
        gl.glDisable(GL10.GL_CULL_FACE);
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_TEXTURE_2D);

        setBlendMode(BLEND_MODE_NORMAL);

        init();

        loadResources();
        resourcesLoaded = true;

        renderStatistics.reset();

        this.gl = null;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        logger.info("Renderer: surface changed");

        this.gl = gl;

        this.width = width;
        this.height = height;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode (GL10.GL_PROJECTION);
        gl.glLoadIdentity ();

        gl.glOrthof (0, width, height, 0, -1f, 1f);

        gl.glMatrixMode (GL10.GL_MODELVIEW);
        gl.glLoadIdentity ();

        resize(width, height);

        this.gl = null;

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        //long currentTime = System.currentTimeMillis();

        this.gl = gl;

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        setBlendMode(BLEND_MODE_NORMAL);

        {
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            draw();
        }

        {
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            gl.glEnable(GL10.GL_BLEND);

            drawOverlay();

            gl.glDisable(GL10.GL_BLEND);
        }

        if (statisticsEnabled) {

            long currentTime = System.currentTimeMillis();

            if (currentTime - statisticsOutputTime > 1000) {
                statisticsOutputTime = currentTime;
                outputStatistics(renderStatistics);
            }

        }

        renderStatistics.update();

        /*
        long elapsed = System.currentTimeMillis() - currentTime;

        if (elapsed < 1000) {
            try {
                Thread.sleep(1000-elapsed);
            } catch (InterruptedException e) {
                ;
            }
        }
        */

        this.gl = null;
    }

    protected void setBlendMode(int mode) {
        if (BLEND_MODE_ADDITIVE == mode) {
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
        } else {
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    protected int loadTexture(String name) {

        Bitmap bitmap = Util.getBitmap(context, name);

        if (null == bitmap) {
            logger.error("Failed to load texture: " + name);
            return 0;
        }

        int texture = allocTexture();
        if (0 == texture) {
            return 0;
        }

        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);

        // Set filtering
        //gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        //gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle();

        return texture;

    }


    protected int allocTexture() {

        IntBuffer textures = IntBuffer.allocate(2);
        gl.glGenTextures(1, textures);
        return textures.get(0);

    }

    protected void freeTexture(int texture) {

        IntBuffer textures = IntBuffer.allocate(2);
        textures.put(0, texture);
        gl.glDeleteTextures(1, textures);

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    protected void init() {
        ;
    }


    protected void resize(int width, int height) {
        ;
    }

    protected void loadResources() {
        ;
    }

    protected void freeResources() {
        ;
    }

    protected void draw() {
        ;
    }

    protected void drawOverlay() {
        ;
    }

    protected void outputStatistics(Statistics statistics) {
        ;
    }

}
