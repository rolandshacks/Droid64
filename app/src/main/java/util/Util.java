package util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import emu.Control;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by roland on 19.08.2016.
 */

public class Util {

    public static Bitmap getBitmap(Context context, String filePath) {

        if (null == context || null == filePath) {
            return null;
        }

        AssetManager assetManager = context.getAssets();
        if (null == assetManager) {
            return null;
        }

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }

        return bitmap;
    }

    /*
    public static Rect matchSize(int partWidth, int partHeight, int maxWidth, int maxHeight) {
        int zoom_factor = Math.min(maxWidth / partWidth, maxHeight / partHeight);
        if (zoom_factor < 1)
        {
            zoom_factor = 1;
        }
        else if (zoom_factor > 32)
        {
            zoom_factor = 32;
        }

        int outW = partWidth  * zoom_factor;
        int outH = partHeight  * zoom_factor;
        int outX = (maxWidth  - outW) / 2;
        int outY = (maxHeight - outH) / 2;

        return new Rect(outX, outY, outX+outW, outY+outH);
    }
    */

    public static Rect matchSize(int width, int height) {
        int zoom_factor = Math.min(width / (Control.DISPLAY_X-44),
                                   height / (Control.DISPLAY_Y-80));
        if (zoom_factor < 1)
        {
            zoom_factor = 1;
        }
        else if (zoom_factor > 32)
        {
            zoom_factor = 32;
        }

        int outW = Control.DISPLAY_X  * zoom_factor;
        int outH = Control.DISPLAY_Y  * zoom_factor;
        int outX = (width - outW) / 2;
        int outY = (height - outH) / 2;

        return new Rect(outX, outY, outX+outW, outY+outH);
    }


}
