package com.podevs.android.poAndroid.battle.gl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Animation {

    private TextureRegion[] keyFrames;
    private float frameDuration = 0f;

    public Animation (float frameDuration, Array<? extends TextureRegion> keyFrames) {
        this.frameDuration = frameDuration;
        this.keyFrames = new TextureRegion[keyFrames.size];
        for (int i = 0, n = keyFrames.size; i < n; i++) {
            this.keyFrames[i] = keyFrames.get(i);
        }
    }

    public Animation (float frameDuration, TextureRegion... keyFrames) {
        this.frameDuration = frameDuration;
        this.keyFrames = keyFrames;
    }

    public TextureRegion getKeyFrame (float stateTime) {
        int frameNumber = getKeyFrameIndex(stateTime);
        return keyFrames[frameNumber];
    }

    public int getKeyFrameIndex (float stateTime) {
        if (keyFrames.length == 1) return 0;

        int frameNumber = (int)(stateTime / frameDuration);
        frameNumber = frameNumber % keyFrames.length;

        return frameNumber;
    }

    public void setKeyFrames(Array<? extends TextureRegion> keyFrames) {
        this.keyFrames = new TextureRegion[keyFrames.size];
        for (int i = 0, n = keyFrames.size; i < n; i++) {
            this.keyFrames[i] = keyFrames.get(i);
        }
    }

    public void setFrameDuration (float frameDuration) {
        this.frameDuration = frameDuration;
    }
}
