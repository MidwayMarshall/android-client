package com.podevs.android.poAndroid.battle.gl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.podevs.android.poAndroid.battle.BattleActivityBaked2;
import com.podevs.android.poAndroid.battle.SpectatingBattle;

public class GameFragment extends AndroidFragmentApplication implements ContinuousGameFrame.CallBacks2 {
    public BattleActivityBaked2 activity;
    public SpectatingBattle battle;
    ContinuousGameFrame frame2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useCompass = false;
        config.useAccelerometer = false;
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            config.useGLSurfaceView20API18 = true;
        }
        config.numSamples = 2;
        Log.e("Fragment", "Initializing view");
        return initializeForView(new ContinuousGameFrame(this));
    }

    private BattleActivityBaked2 getAct() {
        return (BattleActivityBaked2) getActivity();
    }

    @Override
    public BattleActivityBaked2 hook() {
        return getAct();
    }

    @Override
    public void callForward(ContinuousGameFrame frame) {
        this.frame2 = frame;
        activity = getAct();
    }
}