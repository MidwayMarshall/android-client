package com.podevs.android.poAndroid.registry;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.podevs.android.poAndroid.battle.gl.GdxGZipAssetLoader;
import com.podevs.android.poAndroid.battle.gl.SpriteAnimation;

public class LibgdxApplicationGame extends Game {

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
    public void render() {
        super.render();
    }

    @Override
    public void create() {
        setScreen(new LibgdxApplicationScreen());
    }
}

class LibgdxApplicationScreen implements Screen {

    AndroidApplication app;
    SpriteBatch batch;
    TextureAtlas atlas;
    SpriteAnimation animation;
    float elapsedTime = 0f;

    public LibgdxApplicationScreen() {
        boolean all = true;
        batch = new SpriteBatch();
        app = (AndroidApplication) Gdx.app;
        AndroidGraphics graphics = (AndroidGraphics) app.getGraphics();
        GLSurfaceView20 view = (GLSurfaceView20) graphics.getView();


        try {
            boolean debug = false;
            atlas = GdxGZipAssetLoader.loadTextureAtlas("384_1", debug);
            Array<TextureRegion> regions = new Array<TextureRegion>();
            if (atlas.findRegion("001") == null) {
                for (int i = 0; i < atlas.getRegions().size; i++) {
                    regions.add(atlas.findRegion("" + i));
                }
            } else {
                for (int i = 1; i < atlas.getRegions().size; i++) {
                    regions.add(atlas.findRegion(String.format("%03d", i)));
                }
            }
            animation = new SpriteAnimation(0.1f, regions, Animation.PlayMode.LOOP);
            Rectangle rect = new Rectangle(50, 50, 300, 300);
            animation.fitInRectangle(rect, debug);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {

    }

    TextureAtlas.AtlasRegion texture;
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        animation.draw(elapsedTime, batch);
        batch.end();
        elapsedTime += delta;
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
        dispose();
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        atlas.dispose();
        animation.dispose();
        batch.dispose();
        System.gc();
    }
}