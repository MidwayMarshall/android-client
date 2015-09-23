package com.podevs.android.poAndroid.battle.gl;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;

import java.io.IOException;
import java.io.InputStream;

public class StreamedPixmap {
    private Pixmap pixmap;

    public StreamedPixmap(InputStream is) throws IOException {
        Gdx2DPixmap temp = new Gdx2DPixmap(is, Gdx2DPixmap.GDX2D_FORMAT_RGBA8888);
        this.pixmap = new Pixmap(temp);
    }

    public Pixmap getPixmap() {
        return pixmap;
    }
}
