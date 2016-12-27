package ui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import emu.Control;
import system.Statistics;
import util.LogManager;
import util.Logger;
import util.Util;

public class EmuView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private final static Logger logger = LogManager.getLogger(EmuView.class.getName());

    private volatile boolean renderingEnabled;
    private SurfaceHolder surfaceHolder;
    private Thread renderThread;

    private Paint bitmapPaint;
    private Bitmap bitmap;
    private boolean bitmapReady;
    
    private Rect renderRect = new Rect(0, 0, 0, 0);
    private Rect bitmapSourceRect = new Rect(0, 0, Control.DISPLAY_X, Control.DISPLAY_Y);

    private Paint filteredPaint;
    private Paint textPaint;
    private Paint backgroundPaint;

    private DecimalFormat decimalFormat = new DecimalFormat("0.0");
    
    private volatile boolean viewDestroyed;
    
    private Statistics renderStatistics = new Statistics();

    public EmuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        
        bitmap = Bitmap.createBitmap(Control.DISPLAY_X, Control.DISPLAY_Y, Bitmap.Config.ARGB_8888);
        
        bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(false);
        bitmapPaint.setFilterBitmap(false);
        bitmapPaint.setDither(false);

        filteredPaint = new Paint();
        filteredPaint.setAntiAlias(true);
        filteredPaint.setFilterBitmap(true);
        
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setFilterBitmap(true);
        textPaint.setARGB(220, 255, 255, 255);

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(false);
        backgroundPaint.setARGB(255, 0, 0, 0);

        renderStatistics.reset();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	viewDestroyed = false;
        startRendering();
        Control.instance().resume();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	viewDestroyed = true;
        Control.instance().pause();
        stopRendering();
    }

    private void startRendering() {
        renderingEnabled = true;
        renderThread = new Thread(this);
        renderThread.start();
    }
    
    private void stopRendering() {
        renderingEnabled = false;
        if (null != renderThread) {
            renderThread.interrupt();
            renderThread = null;
        }
    }

    @Override
    public void run() {
        
        Canvas canvas;
        
        while (!Thread.interrupted() && !viewDestroyed) {
        
        	if (renderingEnabled) {
        		
        		canvas = surfaceHolder.lockCanvas(null);
                if (null != canvas) {
                    doRendering(canvas);
            		surfaceHolder.unlockCanvasAndPost(canvas);
                }
        	}

        }
    }
    
    private void updateBitmap() {
    	
        byte[] rawData = Control.instance().lockTextureData();
        if (null == rawData) {
            return;
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(rawData);
        buffer.order(ByteOrder.nativeOrder());
        buffer.position(0);
        bitmap.copyPixelsFromBuffer(buffer);
        buffer = null;
        bitmapReady = true;
        
        Control.instance().unlockTextureData();
        
    }
    
    private void doRendering(Canvas canvas) {
        
        renderStatistics.update();
        
    	try {
    		updateBitmap();
    	} catch (Exception e) {
    		logger.error("updateBitmap: exception: " + e);
    		return;
    	}
    	
        if (bitmapReady) {

            renderRect.set(Util.matchSize(canvas.getWidth(), canvas.getHeight()));

            if (renderRect.top > 0) {
                canvas.drawRect(0.0f, 0.0f, renderRect.right, renderRect.top-1, backgroundPaint);
            }

            if (renderRect.bottom < canvas.getHeight()) {
                canvas.drawRect(0.0f, renderRect.bottom, renderRect.right, canvas.getHeight(), backgroundPaint);
            }

        	canvas.drawBitmap(bitmap, bitmapSourceRect, renderRect, bitmapPaint);
        }

        drawOverlay(canvas);
    }

    private void drawOverlay(Canvas canvas) {
        
        double emuUpdatePerSec = Control.instance().getUpdatesPerSecond();
        double renderUpdatePerSec = renderStatistics.getUpdatesPerSecond();
        
        String text = "Statistics: " + decimalFormat.format(emuUpdatePerSec) + " ups / " + decimalFormat.format(renderUpdatePerSec) + " fps";
        canvas.drawText(text,  20.0f,  12.0f, textPaint);

        int stick = Control.instance().getStick();

        canvas.drawText("Stick: 0x" + Integer.toHexString(stick),  20.0f,  30.0f, textPaint);

    }

}
