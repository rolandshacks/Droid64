package org.codewiz.droid64.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import org.codewiz.droid64.emu.EmuControl;
import org.codewiz.droid64.util.LogManager;
import org.codewiz.droid64.util.Logger;

/**
 * Created by roland on 17.08.2016.
 */

public class GLEmuView extends GLSurfaceView {

    private final static Logger logger = LogManager.getLogger(GLEmuView.class.getName());

    private final EmuViewRenderer renderer;

    public GLEmuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        renderer = new EmuViewRenderer(context);
        setRenderer(renderer);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        logger.info("View: surface destroyed");
        super.surfaceDestroyed(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        logger.info("View: surface changed");
        super.surfaceChanged(holder, format, w, h);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        logger.info("View: surface created");
        super.surfaceCreated(holder);
    }
}
