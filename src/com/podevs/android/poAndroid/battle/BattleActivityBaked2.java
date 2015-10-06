package com.podevs.android.poAndroid.battle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.podevs.android.poAndroid.Command;
import com.podevs.android.poAndroid.NetworkService;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.battle.gl.ContinuousGameFrame;
import com.podevs.android.poAndroid.battle.gl.tasks.Events;
import com.podevs.android.poAndroid.battle.gl.tasks.TaskService2;
import com.podevs.android.poAndroid.poke.ShallowBattlePoke;
import com.podevs.android.poAndroid.pokeinfo.InfoConfig;
import com.podevs.android.poAndroid.pokeinfo.MoveInfo;
import com.podevs.android.poAndroid.pokeinfo.TypeInfo;

;

public class BattleActivityBaked2 extends BattleActivity implements MyResultReceiver.Receiver, AndroidFragmentApplication.Callbacks {
    private MyResultReceiver mRecvr;
    private static ComponentName servName = new ComponentName("com.podevs.android.pokemonresources", "com.podevs.android.pokemonresources.SpriteService");
    private static final String TAG = "BattleBake";

    @Override
    public void animateHpBarTo(final int i, final int goal, final int change) {
        final byte goal2 = (byte) goal;
        final boolean side = i == me;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isSpectating() && side) {
                    taskService.offer(new Events.SetHPBattlingAnimated(goal2, activeBattle.myTeam.pokes[0].currentHP, (change*40f)/1000f));
                } else {
                    taskService.offer(new Events.SetHPAnimated(goal2, side, (change*40f)/1000f));
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.w(TAG, "Battle id: " + getIntent().getIntExtra("battleId", -1));
        super.onCreate(savedInstanceState);
        mRecvr = new MyResultReceiver(new Handler());
        mRecvr.setReceiver(this);

        bindService(new Intent(BattleActivityBaked2.this, NetworkService.class), connection,
                Context.BIND_AUTO_CREATE);

        resources = getResources();
        realViewSwitcher = (ViewPager) new ViewPager(this);
        mainLayout = getLayoutInflater().inflate(R.layout.battle_mainscreen_baked, null);

        realViewSwitcher.setAdapter(new MyAdapter());
        setContentView(realViewSwitcher);

        infoView = (TextView)mainLayout.findViewById(R.id.infoWindow);
        infoScroll = (ScrollView)mainLayout.findViewById(R.id.infoScroll);

        struggleLayout = (RelativeLayout)mainLayout.findViewById(R.id.struggleLayout);
        attackRow1 = (LinearLayout)mainLayout.findViewById(R.id.attackRow1);
        attackRow2 = (LinearLayout)mainLayout.findViewById(R.id.attackRow2);

        struggleLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                netServ.socket.sendMessage(activeBattle.constructAttack((byte) -1, megaClicked), Command.BattleMessage); // This is how you struggle
            }
        });
    }

    @Override
    public void updateMyPoke() {
        while (taskService == null) try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
        if (isSpectating()) {
            updateOppPoke(me);
            return;
        }
        runOnUiThread(new Runnable() {

            public void run() {
                final ShallowBattlePoke poke = battle.currentPoke(me);
                poke.shiny = activeBattle.myTeam.pokes[0].shiny; // This is a very stupid way to do it. ShallowBattleTeam never gives shiny correctly?
                // Load correct moveset and name
                if (poke != null) {
                    taskService.offer(new Events.SetHPBattling(poke.lifePercent, activeBattle.myTeam.pokes[0].currentHP));

                    for (int i = 0; i < 4; i++) {
                        BattleMove move = activeBattle.displayedMoves[i];
                        updateMovePP(i);
                        attackNames[i].setText(move.toString());
                        String type;
                        if (move.num == 237)
                            type = TypeInfo.name(activeBattle.myTeam.pokes[0].hiddenPowerType());
                        else
                            type = TypeInfo.name(MoveInfo.type(move.num()));
                        type = type.toLowerCase();
                        attackLayouts[i].setBackgroundResource(resources.getIdentifier(type + "_type_button", "drawable", InfoConfig.pkgName));
                    }

                    if (!samePokes[me]) {
                        taskService.offer(new Events.SpriteChange(true, poke.uID.toString(), poke.gender == 2));
                        taskService.offer(new Events.HUDChangeBattling(activeBattle.myTeam.pokes[0]));
                    } else {
                        samePokes[me] = true;
                        taskService.offer(new Events.StatusChange(true, poke.status()));
                    }
                }
            }
        });
        updateTeam();
    }

    @Override
    public void updateOppPoke(final int opp) {
        runOnUiThread(new Runnable() {
            public void run() {
                final ShallowBattlePoke poke = battle.currentPoke(opp);
                if (poke != null) {
                    taskService.offer(new Events.SetHP(poke.lifePercent, opp == me));
                    if (!samePokes[opp]) {
                        //Log.e(TAG, "Sending event" + true + " " + poke.uID.toString());
                        taskService.offer(new Events.SpriteChange(opp == me, poke.uID.toString(), poke.gender == 2));
                        taskService.offer(new Events.HUDChange(poke, opp == me));
                        //Log.e(TAG, "Sent event" + true + " " + poke.uID.toString())
                    } else {
                        samePokes[opp] = true;
                        taskService.offer(new Events.StatusChange(opp == me, poke.status()));
                    }
                }
            }
        });
    }

    @Override
    public void end() {
        runOnUiThread(new Runnable() {
            public void run() {
                BattleActivityBaked2.this.finish();
            }
        });
    }

    public void DialogLooper(final int id) {
        runOnUiThread(new Runnable() {
            public void run() {
                showDialog(id);
            }
        });
    }

    public TaskService2 taskService;
    private ContinuousGameFrame battleScreen;
    public void callForward(ContinuousGameFrame frame, TaskService2 service) {
        this.battleScreen = frame;
        this.taskService = service;
        service.offer(new Events.BackgroundChange(battle.background));
    }

    public SpectatingBattle getBattle() {
        return battle;
    }

    @Override
    public void exit() {

    }
}
