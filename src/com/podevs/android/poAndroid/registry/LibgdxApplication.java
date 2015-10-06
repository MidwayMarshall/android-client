package com.podevs.android.poAndroid.registry;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.podevs.android.poAndroid.battle.gl.ContinuousGameFrame;

public class LibgdxApplication extends AndroidApplication {
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.numSamples = 2;
        config.useCompass = false;
        config.useAccelerometer = false;
        initialize(new ContinuousGameFrame(null), config);
    }
}
