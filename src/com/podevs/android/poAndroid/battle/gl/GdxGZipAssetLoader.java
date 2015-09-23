package com.podevs.android.poAndroid.battle.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.zip.GZIPInputStream;

public class GdxGZipAssetLoader {
    public static TextureAtlas loadTextureAtlas(String key, boolean side) {
        StreamedTextureAtlas atlas = null;
        StreamedTextureAtlasData data = null;
        StreamedPixmap pixmap = null;
        try {
            {
                FileHandle zip = Gdx.files.internal("data/sheets/" + (!side ? "front/atlas/" : "back/atlas/") + key + ".zz");
                GZIPInputStream is = new GZIPInputStream(zip.read());

                data = new StreamedTextureAtlasData(is, key);
                is.close();
            }

            {
                FileHandle zip = Gdx.files.internal("data/sheets/" + (!side ? "front/texture/" : "back/texture/") + key + ".zz");
                GZIPInputStream is = new GZIPInputStream(zip.read());

                pixmap = new StreamedPixmap(is);
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        atlas = new StreamedTextureAtlas(data, pixmap);

        return atlas;
    }

    /*public class GdxZipAssetLoader {
    public static TextureAtlas loadTextureAtlas(String file, String key) throws IOException {
        StreamedTextureAtlas atlas;
        StreamedTextureAtlasData data = null;
        StreamedPixmap pixmap = null;
        boolean loadedTexture = false;
        boolean loadedData = false;

        FileHandle zip = Gdx.files.internal(file);
        ZipInputStream stream = new ZipInputStream(zip.read());
        ZipEntry entry;
        try {
            while ((entry = stream.getNextEntry()) != null) {
                //s = String.format("%s size %d", entry.getName(), entry.getSize());
                //if (all && s.contains("png")) {
                if (entry.getName().contains(key)) {
                    if (entry.getName().contains(".png")) {
                        pixmap = new StreamedPixmap(stream);
                        loadedTexture = true;
                    } else if (entry.getName().contains(".atlas")) {
                        data = new StreamedTextureAtlasData(stream, key);
                        loadedData = true;
                    }
                }
                if (loadedTexture && loadedData) break;
            }
        } catch (Exception e) {
        }
        if (loadedTexture && loadedData) {
            atlas = new StreamedTextureAtlas(data, pixmap);
        } else {
            throw new IOException("Incomplete Data");
        }

        return atlas;
    }
}
*/
}
