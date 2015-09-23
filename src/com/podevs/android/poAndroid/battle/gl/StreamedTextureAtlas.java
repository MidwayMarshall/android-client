package com.podevs.android.poAndroid.battle.gl;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.ObjectMap;
public class StreamedTextureAtlas extends TextureAtlas {
    private Texture masterTexture;

    public StreamedTextureAtlas(StreamedTextureAtlasData data, StreamedPixmap pixmap) {
        masterTexture = new Texture(pixmap.getPixmap(), data.getPages().get(0).format, data.getPages().get(0).useMipMaps);
        load(data);
    }

    private void load (StreamedTextureAtlasData data) {
            ObjectMap<TextureAtlasData.Page, Texture> pageToTexture = new ObjectMap<TextureAtlasData.Page, Texture>();
                for (TextureAtlasData.Page page : data.getPages()) {
                    Texture texture = null;
                    if (page.texture == null) {
                        //texture = new Texture(page.textureFile, page.format, page.useMipMaps);
                        texture = masterTexture;
                        texture.setFilter(page.minFilter, page.magFilter);
                        texture.setWrap(page.uWrap, page.vWrap);
                    } else {
                        texture = page.texture;
                texture.setFilter(page.minFilter, page.magFilter);
                texture.setWrap(page.uWrap, page.vWrap);
            }
            getTextures().add(texture);
            pageToTexture.put(page, texture);
        }

        for (TextureAtlasData.Region region : data.getRegions()) {
            int width = region.width;
            int height = region.height;
            AtlasRegion atlasRegion = new AtlasRegion(pageToTexture.get(region.page), region.left, region.top,
                    region.rotate ? height : width, region.rotate ? width : height);
            atlasRegion.index = region.index;
            atlasRegion.name = region.name;
            atlasRegion.offsetX = region.offsetX;
            atlasRegion.offsetY = region.offsetY;
            atlasRegion.originalHeight = region.originalHeight;
            atlasRegion.originalWidth = region.originalWidth;
            atlasRegion.rotate = region.rotate;
            atlasRegion.splits = region.splits;
            atlasRegion.pads = region.pads;
            if (region.flip) atlasRegion.flip(false, true);
            getRegions().add(atlasRegion);
        }
    }
}
