package com.podevs.android.poAndroid.colorpicker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.util.ArrayList;

public class ColorFragment extends AndroidFragmentApplication implements callback {
    public ColorActivity activity;
    public ColorGame game;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useCompass = false;
        config.useAccelerometer = false;
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            config.useGLSurfaceView20API18 = true;
        }
        config.a = 1;
        config.r = 5;
        config.g = 6;
        config.b = 5;
        config.numSamples = 1;

        return initializeForView(new ColorGame(this));
    }

    @Override
    public ColorActivity hook() {
        return (ColorActivity) getActivity();
    }

    @Override
    public void callForward(ColorGame frame) {
        this.game = frame;
        activity = hook();
    }
}

interface callback {
    ColorActivity hook();
    void callForward(ColorGame frame);
}

class ColorGame extends Game implements InputProcessor {
    protected callback callback;
    protected ColorActivity activity;
    protected Pixmap map;
    protected Texture texture;
    protected Batch batch;
    protected ShaderProgram shader;
    protected float width, height;
    protected ArrayList<Integer> FPS = new ArrayList<Integer>();

    @Override
    public void create() {
        callback.callForward(this);
        activity = callback.hook();
        activity.callFoward(this);

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        map = new Pixmap((int) width, (int) height, Pixmap.Format.RGB565);

        texture = new Texture(map);
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(Gdx.files.internal("data/graph.vsh"), Gdx.files.internal("data/graph.fsh"));
        if (!shader.isCompiled()) Log.e("SHADER", shader.getLog());
        batch = new SpriteBatch(1, shader);
        Gdx.input.setInputProcessor(this);

        shader.begin();
        shader.setUniformf("u_size", width, height);
        shader.setUniformf("u_value", v / 255f);
        shader.end();
    }

    float v = 130f;

    private void refreshValue() {
        paused = true;
        shader.begin();
        shader.setUniformf("u_value", v / 255f);
        shader.end();
        paused = false;
    }

    void setValue(final int v) {
        this.v = v / 2f;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                refreshValue();
            }
        });
    }

    //com.badlogic.gdx.graphics.Color color1;
    /*
    protected void paint() {
        color1 = new com.badlogic.gdx.graphics.Color(1f, 0f, 1f, 1);
        int x = 0;
        for (h = 360; h > 0; h--) {
            int y = 0;
            for (s = 255; s > 0; s--) {
                int color = android.graphics.Color.HSVToColor(new float[]{h, s / 255f, v / 255f});
                if (PickerUtils.isValidColor(color)) {
                    int green = PickerUtils.green(color);
                    int red = PickerUtils.red(color);
                    int blue = PickerUtils.blue(color);
                    color1.r = red / 255f;
                    color1.g = green / 255f;
                    color1.b = blue / 255f;
                    map.setColor(color1);
                } else {
                    map.setColor(com.badlogic.gdx.graphics.Color.BLACK);
                }
                map.drawRectangle(x*2, y*3, 2, 3);
                y++;
            }
            x++;
        }
        if (texture != null) {
            texture.draw(map, 0, 0);
        }
    }
    */

    public ColorGame(callback call) {
        this.callback = call;
    }

    private final static int FRAME_TARGET = 1000/32;
    private int sleepTime;
    private long beginTime;
    boolean paused = false;
    @Override
    public void render() {
        if (!paused) {
            beginTime = System.currentTimeMillis();
            Gdx.gl.glClearColor(0, 0, 0.2f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.begin();
            batch.draw(texture, 0, 0);
            batch.end();

            long timeDiff = System.currentTimeMillis() - beginTime;
            sleepTime = (int) (FRAME_TARGET - timeDiff);
            if (sleepTime > 0 && Gdx.graphics.getFramesPerSecond() > 25) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {

                }
            }
            FPS.add(Gdx.graphics.getFramesPerSecond());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    boolean alternate;
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        getHSVfromXY(screenX, (int) height - screenY);
        return true;
    }

    void getHSVfromXY(int x, int y) {
        float h = Math.round((x / width)*360f);
        float s = Math.round((y / height)*255f);
        float v = this.v;
        if (activity != null) {
            activity.sendHS(h, s);
        }
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}