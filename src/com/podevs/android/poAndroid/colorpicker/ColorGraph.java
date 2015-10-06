package com.podevs.android.poAndroid.colorpicker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ColorGraph {
    Bitmap bitmap;
    Canvas canvas;
    private float h;
    private float s;
    float v = 130f;

    Paint paint = new Paint();
    ColorGraph() {
        bitmap = Bitmap.createBitmap(360*2, 255*2, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint.setStyle(Paint.Style.FILL);
        repaint();
    }

    void repaint() {
        int x = 0;
        for (h = 360; h > 0; h--) {
            int y = 0;
            for (s = 255; s > 0; s--) {
                int color = Color.HSVToColor(new float[]{h, s / 255f, v / 255f});
                if (PickerUtils.isValidColor(color)) {
                    paint.setColor(color);
                } else {
                    paint.setColor(Color.TRANSPARENT);
                }
                //canvas.drawPoint(x*2, y*2, paint);
                canvas.drawPoints(new float[]{x*2, y*2, x*2, y*2 + 1, x*2 + 1, y*2, x*2 + 1, y*2 + 1}, paint);
                y++;
            }
            x++;
        }
    }

    int color;
    void graphColor(int x, int y) {
        color = bitmap.getPixel(x, y);
    }
}
