package com.podevs.android.poAndroid.battle.gl;


import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.ClampToEdge;
import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;

public class StreamedTextureAtlasData {
    static final String[] tuple = new String[4];

    final Array<TextureAtlas.TextureAtlasData.Page> pages = new Array();
    final Array<TextureAtlas.TextureAtlasData.Region> regions = new Array();

    public StreamedTextureAtlasData(InputStream packStream, String debug) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(packStream), 64);
        try {
            TextureAtlas.TextureAtlasData.Page pageImage = null;
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                if (line.trim().length() == 0)
                    pageImage = null;
                else if (pageImage == null) {
                    //FileHandle file = imagesDir.child(line);

                    float width = 0, height = 0;
                    if (readTuple(reader) == 2) { // size is only optional for an atlas packed with an old TexturePacker.
                        width = Integer.parseInt(tuple[0]);
                        height = Integer.parseInt(tuple[1]);
                        readTuple(reader);
                    }
                    Pixmap.Format format = Pixmap.Format.valueOf(tuple[0]);

                    readTuple(reader);
                    Texture.TextureFilter min = Texture.TextureFilter.valueOf(tuple[0]);
                    Texture.TextureFilter max = Texture.TextureFilter.valueOf(tuple[1]);

                    String direction = readValue(reader);
                    Texture.TextureWrap repeatX = ClampToEdge;
                    Texture.TextureWrap repeatY = ClampToEdge;
                    if (direction.equals("x"))
                        repeatX = Repeat;
                    else if (direction.equals("y"))
                        repeatY = Repeat;
                    else if (direction.equals("xy")) {
                        repeatX = Repeat;
                        repeatY = Repeat;
                    }

                    pageImage = new TextureAtlas.TextureAtlasData.Page(null, width, height, min.isMipMap(), format, min, max, repeatX, repeatY);
                    pages.add(pageImage);
                } else {
                    boolean rotate = Boolean.valueOf(readValue(reader));

                    readTuple(reader);
                    int left = Integer.parseInt(tuple[0]);
                    int top = Integer.parseInt(tuple[1]);

                    readTuple(reader);
                    int width = Integer.parseInt(tuple[0]);
                    int height = Integer.parseInt(tuple[1]);

                    TextureAtlas.TextureAtlasData.Region region = new TextureAtlas.TextureAtlasData.Region();
                    region.page = pageImage;
                    region.left = left;
                    region.top = top;
                    region.width = width;
                    region.height = height;
                    region.name = line;
                    region.rotate = rotate;

                    if (readTuple(reader) == 4) { // split is optional
                        region.splits = new int[] {Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
                                Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};

                        if (readTuple(reader) == 4) { // pad is optional, but only present with splits
                            region.pads = new int[] {Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
                                    Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};

                            readTuple(reader);
                        }
                    }

                    region.originalWidth = Integer.parseInt(tuple[0]);
                    region.originalHeight = Integer.parseInt(tuple[1]);

                    readTuple(reader);
                    region.offsetX = Integer.parseInt(tuple[0]);
                    region.offsetY = Integer.parseInt(tuple[1]);

                    region.index = Integer.parseInt(readValue(reader));

                    regions.add(region);
                }
            }
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error reading pack file: " + debug, ex);
        } finally {
            StreamUtils.closeQuietly(reader);
        }

        regions.sort(indexComparator);
    }


    static int readTuple (BufferedReader reader) throws IOException {
        String line = reader.readLine();
        int colon = line.indexOf(':');
        if (colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
        int i = 0, lastMatch = colon + 1;
        for (i = 0; i < 3; i++) {
            int comma = line.indexOf(',', lastMatch);
            if (comma == -1) break;
            tuple[i] = line.substring(lastMatch, comma).trim();
            lastMatch = comma + 1;
        }
        tuple[i] = line.substring(lastMatch).trim();
        return i + 1;
    }

    static String readValue (BufferedReader reader) throws IOException {
        String line = reader.readLine();
        int colon = line.indexOf(':');
        if (colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
        return line.substring(colon + 1).trim();
    }

    static final Comparator<TextureAtlas.TextureAtlasData.Region> indexComparator = new Comparator<TextureAtlas.TextureAtlasData.Region>() {
        public int compare (TextureAtlas.TextureAtlasData.Region region1, TextureAtlas.TextureAtlasData.Region region2) {
            int i1 = region1.index;
            if (i1 == -1) i1 = Integer.MAX_VALUE;
            int i2 = region2.index;
            if (i2 == -1) i2 = Integer.MAX_VALUE;
            return i1 - i2;
        }
    };

    public Array<TextureAtlas.TextureAtlasData.Page> getPages() {
        return pages;
    }

    public Array<TextureAtlas.TextureAtlasData.Region> getRegions() {
        return regions;
    }
}
