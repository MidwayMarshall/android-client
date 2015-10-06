package com.podevs.android.poAndroid.colorpicker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.podevs.android.poAndroid.R;

public class ColorActivity extends FragmentActivity implements AndroidFragmentApplication.Callbacks {
    private int selectedColor;
    private View main;
    private SeekBar valueBar;
    private TextView textH, textS, textV, textR, textG, textB;
    private Button btnOk, btnCancel;
    private ImageView zoomed;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedColor = getIntent().getIntExtra("QColor", 0xFFDD0000);

        main = getLayoutInflater().inflate(R.layout.color_picker, null);

        valueBar = (SeekBar) main.findViewById(R.id.valueBar);
        valueBar.setProgress(130);
        valueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sendValue(seekBar.getProgress());
                updateText();
                paintZoomed();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //paintZoomed();
            }
        });

        btnOk = (Button) main.findViewById(R.id.ok);
        btnCancel = (Button) main.findViewById(R.id.cancel);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                end(true);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                end(false);
            }
        });

        textH = (TextView) main.findViewById(R.id.textH);
        textS = (TextView) main.findViewById(R.id.textS);
        textV = (TextView) main.findViewById(R.id.textV);
        textR = (TextView) main.findViewById(R.id.textR);
        textG = (TextView) main.findViewById(R.id.textG);
        textB = (TextView) main.findViewById(R.id.textB);

        zoomed = (ImageView) main.findViewById(R.id.zoomed);

        bitmap = Bitmap.createBitmap(150, 180, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint.setStyle(Paint.Style.FILL);

        this.rgb = PickerUtils.RGBfromInt(selectedColor);
        float[] hsv = new float[3];
        Color.colorToHSV(selectedColor, hsv);
        this.h = hsv[0];
        this.s = hsv[1] * 255f;
        this.v = hsv[2] * 255f;

        valueBar.setProgress(Math.round(v * 2f));

        paintZoomed();

        setContentView(main);

        updateText();

        final int value = valueBar.getProgress();
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while (frame == null) {
                    try {
                        wait(500);
                    } catch (Exception e) {

                    }
                }
                sendValue(value);
            }
        })).start();
    }

    float h = 130f;
    float s = 130f;
    float v = 130f;
    int[] rgb = new int[] {63, 129, 74};

    private void updateText() {
        textH.setText("H: " + Math.round(h));
        textS.setText("S: " + Math.round(s));
        textV.setText("V: " + Math.round(v));
        textR.setText("R: " + rgb[0]);
        textG.setText("G: " + rgb[1]);
        textB.setText("B: " + rgb[2]);
    }

    void paintZoomed() {
        int color = Color.HSVToColor(new float[]{h, s / 255f, v / 255f});
        if (PickerUtils.isValidColor(color)) {
            selectedColor = color;
            paint.setColor(color);
        } else {
            selectedColor = Color.BLACK;
            paint.setColor(Color.BLACK);
        }
        this.rgb = PickerUtils.RGBfromInt(selectedColor);
        canvas.drawRect(0, 0, 150, 180, paint);
        zoomed.setImageBitmap(bitmap);
    }

    void sendHS(float h, float s) {
        this.h = h;
        this.s = s;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateText();
                paintZoomed();
            }
        });
    }

    private void sendValue(int value) {
        if (frame != null) {
            frame.setValue(value);
        }
        v = value / 2f;
    }

    private void end(boolean accept) {
        Bundle b = new Bundle();
        if (accept) {
            Intent i = getIntent();
            b.putInt("QColor", selectedColor);
            i.putExtras(b);
            setResult(Activity.RESULT_OK, i);
        } else {
            Intent i = getIntent();
            setResult(Activity.RESULT_CANCELED, i);
        }
        finish();
    }

    ColorGame frame;
    void callFoward(ColorGame game) {
        this.frame = game;
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    @Override
    public void exit() {
        setResult(Activity.RESULT_CANCELED, getIntent());
    }
}
