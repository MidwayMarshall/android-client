package com.podevs.android.poAndroid.battle.gl.tasks;

import android.util.Log;
import com.podevs.android.poAndroid.battle.BattlePoke;
import com.podevs.android.poAndroid.battle.gl.ContinuousGameFrame;
import com.podevs.android.poAndroid.poke.ShallowBattlePoke;

public class Events {
    private Events() {}

    public static class SetHPAnimated implements Event {
        byte HP;
        boolean side;
        String log;
        long time;
        float duration;

        public SetHPAnimated(byte HP, boolean side, float duration) {
            this.HP = HP;
            this.side = side;
            this.duration = duration;
            log = Thread.currentThread().getName();
            time = System.currentTimeMillis();
        }

        @Override
        public void run(ContinuousGameFrame Frame) {
            Frame.HUDs[(side ? 0 : 1)].setHP(HP, duration);
            time = System.currentTimeMillis() - time;
     //       Log.e("Event", "SetHPAnimated " + log + " to " + Thread.currentThread().getName() + " took: " + time);
        }
    }

    public static class SetHP implements Event {
        byte HP;
        boolean side;

        long time;
        String log;

        public SetHP(byte HP, boolean side) {
            this.HP = HP;
            this.side = side;
            log = Thread.currentThread().getName();
            time = System.currentTimeMillis();
        }

        @Override
        public void run(ContinuousGameFrame Frame) {
            Frame.HUDs[(side ? 0 : 1)].setHPNonAnimated(HP);

            time = System.currentTimeMillis() - time;
 //           Log.e("Event", "SetHP " + log + " to " + Thread.currentThread().getName() + " took: " + time);
        }
    }

    public static class SetHPBattling implements Event {
        byte percent;
        short HP;
        String log;
        long time;

        public SetHPBattling(byte percent, short HP) {
            this.percent = percent;
            this.HP = HP;
            log = Thread.currentThread().getName();
            time = System.currentTimeMillis();
        }

        @Override
        public void run(ContinuousGameFrame Frame) {
            Frame.HUDs[0].updateRealHealth(percent, HP);
            time = System.currentTimeMillis() - time;
    //        Log.e("Event", "SetHPBattling " + log + " to " + Thread.currentThread().getName() + " took: " + time);
        }
    }

    public static class SetHPBattlingAnimated implements Event {
        byte percent;
        short HP;
        float duration;
        String log;
        long time;

        public SetHPBattlingAnimated(byte percent, short HP, float duration) {
            this.percent = percent;
            this.HP = HP;
            this.duration = duration;
            log = Thread.currentThread().getName();
            time = System.currentTimeMillis();
        }

        @Override
        public void run(ContinuousGameFrame Frame) {
            Frame.HUDs[0].setHP(percent, HP, duration);
            time = System.currentTimeMillis() - time;
 //          Log.e("Event", "SetHPBattlingAnimated " +  log + " to " + Thread.currentThread().getName() + " took: " + time);
        }
    }

    public static class HUDChangeBattling implements Event {
        String log;
        long time;
        BattlePoke poke;

        public HUDChangeBattling(BattlePoke poke) {
            this.poke = poke;
            log = Thread.currentThread().getName();
            time = System.currentTimeMillis();
        }

        @Override
        public void run(ContinuousGameFrame Frame) {
            Frame.HUDs[0].updatePokeNonSpectating(poke);
            if (poke.status() == 31) {
                Frame.sprites[0].paused = true;
            }
            time = System.currentTimeMillis() - time;
            //Log.e("Event", log + " to " + Thread.currentThread().getName() + " took: " + time);
        }
    }

    public static class HUDChange implements Event {
        String log;
        long time;
        ShallowBattlePoke poke;
        boolean side;

        public HUDChange(ShallowBattlePoke poke, boolean side) {
            this.poke = poke;
            this.side = side;
            log = Thread.currentThread().getName();
            time = System.currentTimeMillis();
        }

        @Override
        public void run(ContinuousGameFrame Frame) {
            Frame.HUDs[(side ? 0 : 1)].updatePoke(poke);
            if (poke.status() == 31) {
                Frame.sprites[(side ? 0 : 1)].paused = true;
            }
            time = System.currentTimeMillis() - time;
            //Log.e("Event", log + " to " + Thread.currentThread().getName() + " took: " + time);
        }
    }

    public static class SpriteChange implements Event {
        boolean side;
        String path;
        String log;
        long time;
        boolean female;

        public SpriteChange(boolean side, String path, boolean female) {
            this.side = side;
            this.path = path;
            this.female = female;
            log = Thread.currentThread().getName();
            time = System.currentTimeMillis();
        }

        @Override
        public void run(ContinuousGameFrame Frame) {
            time = System.currentTimeMillis() - time;
//            Log.e("Event", "SpriteChange " + log + " to " + Thread.currentThread().getName() + " took: " + time);
            Frame.updateSprite(side, path, female);
        }
    }

    public static class StatusChange implements Event {
        boolean side;
        int status;
        String log;
        long time;

        public StatusChange(boolean side, int status) {
            this.side = side;
            this.status = status;
            log = Thread.currentThread().getName();
            time = System.currentTimeMillis();
        }

        @Override
        public void run(ContinuousGameFrame Frame) {
            time = System.currentTimeMillis() - time;
            //Log.e("Event", log + " to " + Thread.currentThread().getName() + " took: " + time);
            Frame.HUDs[(side ? 1 : 0)].updateStatus(status);
            if (status == 31) {
                Frame.sprites[(side ? 1 : 0)].paused = true;
            }
        }
    }

    public static class BackgroundChange implements Event {
        int id;

        public BackgroundChange(int id) {
            this.id = id;
        }

        @Override
        public void run(ContinuousGameFrame Frame) {
            Frame.setBackground(id);
        }
    }
}
