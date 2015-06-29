package com.threemoji.threemoji.utility;

import com.caverock.androidsvg.SVG; // https://code.google.com/p/androidsvg/
import com.caverock.androidsvg.SVGParseException;
import com.threemoji.threemoji.Threemoji;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;


public class SvgUtils {
    public static BitmapDrawable svgToBitmapDrawable(Resources res, int resource, int size) {
        Bitmap bmp = svgToBitmap(res, resource, size);
        if (bmp == null) {
            return null;
        }
        return new BitmapDrawable(res, bmp);
    }

    public static Bitmap svgToBitmap(Resources res, int resource, int size) {
        try {
            size = (int) (size * res.getDisplayMetrics().density);
            SVG svg = SVG.getFromResource(res, resource);

            Bitmap bmp;
            bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bmp);
            svg.renderToCanvas(canvas);

            return bmp;
        } catch (SVGParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Drawable getSvgDrawable(int imageResource, int size, String packageName) {
        Resources resources = Threemoji.getContext().getResources();
        String resourceName = resources.getResourceEntryName(imageResource);
        int rawResourceId = resources.getIdentifier(resourceName, "raw", packageName);
        return SvgUtils.svgToBitmapDrawable(resources, rawResourceId, size);
    }
}
