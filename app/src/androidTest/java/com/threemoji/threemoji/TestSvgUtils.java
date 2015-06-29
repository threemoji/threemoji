package com.threemoji.threemoji;

import com.threemoji.threemoji.utility.SvgUtils;

import android.graphics.drawable.Drawable;
import android.test.AndroidTestCase;

import java.lang.reflect.Field;

public class TestSvgUtils extends AndroidTestCase {
    public static final String LOG_TAG = TestSvgUtils.class.getSimpleName();

    public void testConvertToSvg() {
        Class raw = R.raw.class;
        Field[] fields = raw.getFields();
        int size = 40;
        try {
            for (Field field : fields) {
                if (field.toString().contains("R$raw.emoji_")) {
                    int id = field.getInt(null);
                    Drawable svg = SvgUtils.svgToBitmapDrawable(getContext().getResources(), id,
                                                                size);
//                    Log.v(LOG_TAG, field.toString());
                    assertNotNull(field.toString(), svg);
                }
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
