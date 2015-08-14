package com.podevs.android.poAndroid.battle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.android.launcher.DragController;
import com.android.launcher.DragLayer;
import com.android.launcher.PokeDragIcon;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.podevs.android.poAndroid.Command;
import com.podevs.android.poAndroid.NetworkService;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.battle.gl.BattleInfoHUD;
import com.podevs.android.poAndroid.battle.gl.GameFrame;
import com.podevs.android.poAndroid.battle.gl.SpriteAnimation;
import com.podevs.android.poAndroid.chat.ChatActivity;
import com.podevs.android.poAndroid.poke.PokeEnums;
import com.podevs.android.poAndroid.poke.ShallowBattlePoke;
import com.podevs.android.poAndroid.poke.ShallowShownPoke;
import com.podevs.android.poAndroid.poke.UniqueID;
import com.podevs.android.poAndroid.pokeinfo.InfoConfig;
import com.podevs.android.poAndroid.pokeinfo.MoveInfo;
import com.podevs.android.poAndroid.pokeinfo.PokemonInfo;
import com.podevs.android.poAndroid.pokeinfo.TypeInfo;
import com.podevs.android.utilities.Baos;

public class BattleActivityBaked extends BattleActivity implements MyResultReceiver.Receiver, AndroidFragmentApplication.Callbacks {
    private MyResultReceiver mRecvr;
    private static ComponentName servName = new ComponentName("com.podevs.android.pokemonresources", "com.podevs.android.pokemonresources.SpriteService");

    /*
    public enum BattleDialog {
        RearrangeTeam,
        ConfirmForfeit,
        OppDynamicInfo,
        MyDynamicInfo,
        MoveInfo
    }*/

    // public final static int SWIPE_TIME_THRESHOLD = 100;
    private static final String TAG = "BattleBake";

    //DragLayer mDragLayer;

    //ViewPager realViewSwitcher;
    //RelativeLayout battleView;
    //TextProgressBar[] hpBars = new TextProgressBar[2];
    //TextView[] currentPokeNames = new TextView[2];
    //TextView[] currentPokeLevels = new TextView[2];
    //ImageView[] currentPokeGenders = new ImageView[2];
    //ImageView[] currentPokeStatuses = new ImageView[2];

    //TextView[] attackNames = new TextView[4];
    //TextView[] attackPPs = new TextView[4];
    //RelativeLayout[] attackLayouts = new RelativeLayout[4];

    //TextView[] timers = new TextView[2];

    //PokeDragIcon[] myArrangePokeIcons = new PokeDragIcon[6];
    //ImageView[] oppArrangePokeIcons = new ImageView[6];

    //ListedPokemon pokeList[] = new ListedPokemon[6];

    //TextView infoView;
    //ScrollView infoScroll;
    //TextView[] names = new TextView[2];
    //ImageView[][] pokeballs = new ImageView[2][6];

    //RelativeLayout struggleLayout;
    //LinearLayout attackRow1;
    //LinearLayout attackRow2;

    //SpectatingBattle battle = null;
    //public Battle activeBattle = null;

    //boolean useAnimSprites = true;
    //boolean megaClicked = false;
    //BattleMove lastClickedMove;

    //Resources resources;
    //public NetworkService netServ = null;
    //int me, opp;

    //View mainLayout, teamLayout;
    /*
    private class MyAdapter extends PagerAdapter {
        public MyAdapter() {
            super();
        }

        @Override
        public int getCount() {
            return isSpectating() ? 1 : 2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            switch (position) {
                case 0: container.addView(mainLayout);return mainLayout;
                case 1: container.addView(teamLayout);return teamLayout;
            }
            return null;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return (Object)arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }
    }
    */

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.w(TAG, "Battle id: " + getIntent().getIntExtra("battleId", -1));
        super.onCreate(savedInstanceState);
        mRecvr = new MyResultReceiver(new Handler());
        mRecvr.setReceiver(this);
        try {
            getPackageManager().getApplicationInfo("com.podevs.android.pokemonresources", 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("BattleActivity", "Animated sprites not found");
            useAnimSprites = false;
        }

        bindService(new Intent(BattleActivityBaked.this, NetworkService.class), connection,
                Context.BIND_AUTO_CREATE);

        resources = getResources();
        realViewSwitcher = (ViewPager) new ViewPager(this);
        mainLayout = getLayoutInflater().inflate(R.layout.battle_mainscreen_baked, null);


//        if (mainLayout.findViewById(R.id.smallBattleWindow) != null) {
//			/* Small screen, set full screen otherwise pokemon are cropped */
//            requestWindowFeature(Window.FEATURE_NO_TITLE);
//        }

        realViewSwitcher.setAdapter(new MyAdapter());
        setContentView(realViewSwitcher);

        infoView = (TextView)mainLayout.findViewById(R.id.infoWindow);
        infoScroll = (ScrollView)mainLayout.findViewById(R.id.infoScroll);
        //battleView = (RelativeLayout)mainLayout.findViewById(R.id.battleScreen);

        struggleLayout = (RelativeLayout)mainLayout.findViewById(R.id.struggleLayout);
        attackRow1 = (LinearLayout)mainLayout.findViewById(R.id.attackRow1);
        attackRow2 = (LinearLayout)mainLayout.findViewById(R.id.attackRow2);

        struggleLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                netServ.socket.sendMessage(activeBattle.constructAttack((byte) -1, megaClicked), Command.BattleMessage); // This is how you struggle
            }
        });
    }

    private Handler handler = new Handler();

    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            for(int i = 0; i < 2; i++) {
                int seconds;
                if (battle.ticking[i]) {
                    long millis = SystemClock.uptimeMillis()
                            - battle.startingTime[i];
                    seconds = battle.remainingTime[i] - (int) (millis / 1000);
                }
                else
                    seconds = battle.remainingTime[i];

                if(seconds < 0) seconds = 0;
                else if(seconds > 300) seconds = 300;

                int minutes = (seconds / 60);
                seconds = seconds % 60;
                timers[i].setText(String.format("%02d:%02d", minutes, seconds));
            }
            handler.postDelayed(this, 200);
        }
    };

    public void setHpBarTo(final int i, final int goal) {
        //hpAnimator.setGoal(i, goal);
        //hpAnimator.setHpBarToGoal();
    }

    @Override
    public void animateHpBarTo(final int i, final int goal, final int change) {
        final byte goal2 = (byte) goal;
        final boolean side = i == me;
        //hpAnimator.setGoal(i, goal);
        //new Thread(hpAnimator).start();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isSpectating() && side) {
                    Log.e(TAG, "sending setHPAnimatedNotSpectating " + goal2 +  " " + activeBattle.myTeam.pokes[0].currentHP);
                    battleScreen.setHPAnimatedNotSpectating(goal2, activeBattle.myTeam.pokes[0].currentHP);
                } else {
                    Log.e(TAG, "sending setHP " + goal2 + " " + side);
                    battleScreen.setHP(goal2, side);
                }
            }
        });
    }

    public void updateBattleInfo(boolean scroll) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (battle == null)
                    return;
                synchronized (battle.histDelta) {
                    infoView.append(battle.histDelta);
                    if (battle.histDelta.length() != 0 || true) {
                        infoScroll.post(new Runnable() {
                            public void run() {
                                infoScroll.smoothScrollTo(0, infoView.getMeasuredHeight());
                            }
                        });
                    }
                    infoScroll.invalidate();
                    battle.hist.append(battle.histDelta);
                    battle.histDelta.clear();
                }
            }
        });
    }

    public void updatePokes(byte player) {
        Log.e(TAG, PokemonInfo.cacheStatus());
        if (player == me)
            updateMyPoke();
        else
            updateOppPoke(player);
    }

    public int statusTint(int status) {
        switch (status) {
            case 0:
                return 1;
            case 31:
                return 0x7D000000;
            case 1:
                return 0x7DF8D030;
            case 2:
                return 0x7D888888;
            case 3:
                return 0x7D98D8D8;
            case 4:
                return 0x7DF08030;
            case 5:
                return 0x7DA040A0;
            case 6:
                return 0x7DC8C8C8;
            default:
                return 0;
        }
    }

    public void updatePokeballs() {
        runOnUiThread(new Runnable() {
            public void run() {
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 6; j++) {
                        pokeballs[i][j].setImageDrawable((
                                battle.pokes[i][j].uID.pokeNum != 0
                                        ? PokemonInfo.iconDrawableCache(battle.pokes[i][j].uID)
                                        : PokemonInfo.iconDrawablePokeballStatus()
                        ));
                        pokeballs[i][j].setColorFilter(statusTint(battle.pokes[i][j].status()));
                    }
                }
                /// PorterDuff.Mode.MULTIPLY;
            }
        });
    }


    private String getAnimSprite(ShallowBattlePoke poke, boolean front) {
        String res;
        UniqueID uID;
        if (poke.specialSprites.isEmpty())
            uID = poke.uID;
        else
            uID = poke.specialSprites.peek();

        if (poke.uID.pokeNum < 0)
            res = null;
        else {
            res = String.format("anim%03d", uID.pokeNum) + (uID.subNum == 0 ? "" : "_" + uID.subNum) +
                    (poke.gender == PokeEnums.Gender.Female.ordinal() ? "f" : "") + (front ? "_front" : "_back") + (poke.shiny ? "s" : "") + ".gif";
        }
        return res;
    }

    public void updateCurrentPokeListEntry() {
        runOnUiThread(new Runnable() {
            public void run() {
                synchronized (battle) {
                    BattlePoke battlePoke = activeBattle.myTeam.pokes[0];
                    pokeList[0].hp.setText(battlePoke.currentHP +
                            "/" + battlePoke.totalHP);
                }
                // TODO: Status ailments and stuff
            }
        });
    }

    public void updateMovePP(final int moveNum) {
        runOnUiThread(new Runnable() {
            public void run() {
                BattleMove move = activeBattle.displayedMoves[moveNum];
                attackPPs[moveNum].setText("PP " + move.currentPP + "/" + move.totalPP);
            }
        });
    }

    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode != Activity.RESULT_OK)
            return;
        String path = resultData.getString("path");
        /*
        if (resultData.getBoolean("me")) {
            pokeSprites[me].loadDataWithBaseURL(path, "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + resultData.getString("sprite") + "');}</style><body></body>", "text/html", "utf-8", null);
        } else {
            pokeSprites[opp].loadDataWithBaseURL(path, "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + resultData.getString("sprite") + "');}</style><body></body>", "text/html", "utf-8", null);
        }
        */
    }

    public boolean isSpectating() {
        return activeBattle == null;
    }

    public void updateMyPoke() {
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
                    //battleScreen.setHPNotAnimatedNotSpectating(poke.lifePercent, activeBattle.myTeam.pokes[0].currentHP);
                    if (HUDs[me] == null) {
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {

                        }
                    }
                    HUDs[me].setHPNonAnimated(poke.lifePercent);
                    HUDs[me].updateRealHealth(activeBattle.myTeam.pokes[0].currentHP);

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
                        String sprite = "hellO!";
                        /*
                        if (battle.shouldShowPreview || poke.status() == PokeEnums.Status.Koed.poValue()) {
                            sprite = "empty_sprite.png";
                        } else if (poke.sub) {
                            sprite = "sub_back.png";
                        } else {
                            if (useAnimSprites) {
                                Intent data = new Intent();
                                data.setComponent(servName);
                                data.putExtra("me", true);
                                data.putExtra("sprite", getAnimSprite(poke, false));
                                data.putExtra("cb", mRecvr);
                                startService(data);
                            } else {
                                sprite = PokemonInfo.sprite(poke, false);
                            }
                        }*/

                        if (sprite != null) {
                            Log.e(TAG, "Sending updateHUDNonSpectating " + activeBattle.myTeam.pokes[0].toString() + " " + true);
                            Log.e(TAG, "Sending updateSprite " + true + " " + poke.uID.toString());
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    battleScreen.updateHUDNonSpectating(activeBattle.myTeam.pokes[0], System.currentTimeMillis());
                                    battleScreen.updateSprite(true, poke.uID.toString(), System.currentTimeMillis());
                                    //battleScreen.updateSprite(true, String.format("%03d", poke.uID.pokeNum));
                                }
                            });
                            //sprites[me] = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("data/gif/back/" + String.format("%03d", poke.uID.pokeNum) + ".gif").read());;
                            //sprites[me].fitInRectangle(Rectangle.tmp, true);
                            //pokeSprites[me].loadDataWithBaseURL("file:///android_res/drawable/", "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + sprite + "');}</style><body></body>", "text/html", "utf-8", null);
                        }
                    } else {
                        samePokes[me] = true;
                        Log.e(TAG, "Sending setHPNonSpectating " + activeBattle.myTeam.pokes[0].currentHP);
                        Log.e(TAG, "Sending updateHUDStatus " + poke.status() + " " + (opp == me));
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                battleScreen.setHPNonSpectating(activeBattle.myTeam.pokes[0].currentHP, System.currentTimeMillis());
                                battleScreen.updateHUDStatus(poke.status(), true, System.currentTimeMillis());
                            }
                        });
                    }
                }
            }
        });
        updateTeam();
    }

    public boolean samePokes[] = new boolean[2];

    public void updateOppPoke(final int opp) {
        runOnUiThread(new Runnable() {
            public void run() {
                final ShallowBattlePoke poke = battle.currentPoke(opp);
                // Load correct moveset and name
                if(poke != null) {
                    Log.e(TAG, "Sending setHPNotAnimated " + poke.lifePercent + " " + (opp == me));
                    battleScreen.setHPNotAnimated(poke.lifePercent, opp == me);

                    if (!samePokes[opp]) {
                        String sprite = "hello";
                        /*
                        if (battle.shouldShowPreview || poke.status() == PokeEnums.Status.Koed.poValue()) {
                            sprite = "empty_sprite.png";
                        } else if (poke.sub) {
                            sprite = opp == me ? "sub_back.png" : "sub_front.png";
                        } else {
                            if (useAnimSprites) {
                                Intent data = new Intent();
                                data.setComponent(servName);
                                data.putExtra("me", false);
                                data.putExtra("sprite", getAnimSprite(poke, opp != me));
                                data.putExtra("cb", mRecvr);
                                startService(data);
                            } else {
                                sprite = PokemonInfo.sprite(poke, opp != me);
                            }
                        }*/

                        if (sprite != null) {
                            if (opp == me) {
                                Log.e(TAG, "Sending updateHUD " + poke.toString() + " " + true);
                                Log.e(TAG, "Sending updateSprite " + true + " " + poke.uID.toString());
                                Gdx.app.postRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        battleScreen.updateHUD(poke, true, System.currentTimeMillis());
                                        battleScreen.updateSprite(true, poke.uID.toString(), System.currentTimeMillis());
                                        //battleScreen.updateSprite(true, String.format("%03d", poke.uID.pokeNum));
                                    }
                                });
                                //sprites[me] = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("data/gif/back/" + String.format("%03d", poke.uID.pokeNum) + ".gif").read());
                                //sprites[me].fitInRectangle(Rectangle.tmp, true);
                            } else {
                                Log.e(TAG, "Sending updateHUD " + poke.toString() + " " + false);
                                Log.e(TAG, "Sending updateSprite " + false + " " + poke.uID.toString());
                                Gdx.app.postRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        battleScreen.updateHUD(poke, false, System.currentTimeMillis());
                                        battleScreen.updateSprite(false, poke.uID.toString(), System.currentTimeMillis());
                                        //battleScreen.updateSprite(false, String.format("%03d", poke.uID.pokeNum));
                                    }
                                });
                                //sprites[opp] = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("data/gif/front/" + String.format("%03d", poke.uID.pokeNum) + ".gif").read());;
                                //sprites[opp].fitInRectangle(Rectangle.tmp2, false);
                            }
                            //pokeSprites[opp].loadDataWithBaseURL("file:///android_res/drawable/", "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + sprite + "');}</style><body></body>", "text/html", "utf-8", null);
                        }
                    } else {
                        samePokes[opp] = true;
                        Log.e(TAG, "Sending updateHUDStatus " + poke.status() + " " + (opp == me));
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                battleScreen.updateHUDStatus(poke.status(), opp == me, System.currentTimeMillis());
                            }
                        });
                    }
                }
            }
        });
    }

    public void updateButtons() {
        if (isSpectating()) {
            return;
        }
        runOnUiThread(new Runnable() {
            public void run() {
                if (!checkStruggle()) {
                    for (int i = 0; i < 4; i++) {
                        if (activeBattle.allowAttack && !activeBattle.clicked) {
                            setAttackButtonEnabled(i, activeBattle.allowAttacks[i]);
                        }
                        else {
                            setAttackButtonEnabled(i, false);
                        }
                    }
                }
                megaClicked = false;
                for(int i = 0; i < 6; i++) {
                    if (activeBattle.myTeam.pokes[i].status() != PokeEnums.Status.Koed.poValue() && !activeBattle.clicked)
                        pokeList[i].setEnabled(i, activeBattle.allowSwitch);
                    else
                        pokeList[i].setEnabled(i, false);
                }
            }
        });
    }

    public boolean checkStruggle() {
        // This method should hide moves, show the button if necessary and return whether it showed the button
        boolean struggle = activeBattle.shouldStruggle;
        if(struggle) {
            attackRow1.setVisibility(View.GONE);
            attackRow2.setVisibility(View.GONE);
            struggleLayout.setVisibility(View.VISIBLE);
        }
        else {
            attackRow1.setVisibility(View.VISIBLE);
            attackRow2.setVisibility(View.VISIBLE);
            struggleLayout.setVisibility(View.GONE);
        }
        return struggle;
    }

    public void updateTeam() {
        runOnUiThread(new Runnable() {

            public void run() {
                for (int i = 0; i < 6; i++) {
                    BattlePoke poke = activeBattle.myTeam.pokes[i];
                    pokeList[i].update(poke, activeBattle.allowSwitch && !activeBattle.clicked);
                }
            }
        });
    }

    public void updateMoves(short attack) {
        if (!isSpectating()) {
            activeBattle.pokes[opp][0].addMove(attack);
        }
    }

    public void switchToPokeViewer() {
        runOnUiThread(new Runnable() {
            public void run() {
                realViewSwitcher.setCurrentItem(1, true);
            }
        });
    }

    public void onResume() {
        // XXX we might want more stuff here
        super.onResume();
        if (battle != null)
            checkRearrangeTeamDialog();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(BattleActivityBaked.this, ChatActivity.class));
        finish();
    }

    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            netServ = ((NetworkService.LocalBinder)service).getService();

            int battleId = getIntent().getIntExtra("battleId", 0);
            battle = netServ.activeBattles.get(battleId);
            if (battle == null) {
                battle = netServ.spectatedBattles.get(battleId);
            }

            if (battle == null) {
                startActivity(new Intent(BattleActivityBaked.this, ChatActivity.class));
                finish();

                netServ.closeBattle(battleId); //remove the possibly ongoing notification
                return;
            }

			/* Is it a spectating battle or not? */
            try {
                activeBattle = (Battle) battle;
            } catch (ClassCastException ex) {

            }

            if (isSpectating()) {
				/* If it's a spectating battle, we remove the info view's bottom margin */
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)infoScroll.getLayoutParams();
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin,
                        ((RelativeLayout.LayoutParams)attackRow2.getLayoutParams()).bottomMargin);
                infoScroll.setLayoutParams(params);
            } else {
                teamLayout = getLayoutInflater().inflate(R.layout.battle_teamscreen, null);

                for(int i = 0; i < 4; i++) {
                    attackNames[i] = (TextView)mainLayout.findViewById(resources.getIdentifier("attack" + (i+1) + "Name", "id", InfoConfig.pkgName));
                    attackPPs[i] = (TextView)mainLayout.findViewById(resources.getIdentifier("attack" + (i+1) + "PP", "id", InfoConfig.pkgName));
                    attackLayouts[i] = (RelativeLayout)mainLayout.findViewById(resources.getIdentifier("attack" + (i+1) + "Layout", "id", InfoConfig.pkgName));
                    attackLayouts[i].setOnClickListener(battleListener);
                    attackLayouts[i].setOnLongClickListener(moveListener);
                }
                for(int i = 0; i < 6; i++) {
                    RelativeLayout whole = (RelativeLayout) teamLayout.findViewById(resources.getIdentifier("pokeViewLayout" + (i+1), "id", InfoConfig.pkgName));
                    pokeList[i] = new ListedPokemon(whole);
                    whole.setOnClickListener(battleListener);
                }

                //oppLayout = getLayoutInflater().inflate(R.layout.battle_oppteam, null);

                /* Well it helps you keep track what your opponent has seen!
				// Pre-load PokeBall and sprite info
				for (int i = 0; i < 6; i++) {
					battle.pokes[battle.me][i].uID = activeBattle.myTeam.pokes[i].uID;
				}
				*/

		        /* Changed to two pages */
                realViewSwitcher.getAdapter().notifyDataSetChanged();
            }

            //battleView.setBackgroundResource(resources.getIdentifier("bg" + battle.background, "drawable", InfoConfig.pkgName));

            // Set the UI to display the correct info
            me = battle.me;
            opp = battle.opp;
            // We don't know which timer is which until the battle starts,
            // so set them here.
            timers[me] = (TextView)mainLayout.findViewById(R.id.timerB);
            timers[opp] = (TextView)mainLayout.findViewById(R.id.timerA);

            names[me] = (TextView)mainLayout.findViewById(R.id.nameB);
            names[opp] = (TextView)mainLayout.findViewById(R.id.nameA);

            for (int i = 0; i < 6; i++) {
                pokeballs[me][i] = (ImageView)mainLayout.findViewById(resources.getIdentifier("pokeball" + (i + 1) + "B", "id", InfoConfig.pkgName));
                pokeballs[opp][i] = (ImageView)mainLayout.findViewById(resources.getIdentifier("pokeball" + (i + 1) + "A", "id", InfoConfig.pkgName));
            }
            updatePokeballs();

            names[me].setText(battle.players[me].nick());
            names[opp].setText(battle.players[opp].nick());

            infoView.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    final EditText input = new EditText(BattleActivityBaked.this);
                    new AlertDialog.Builder(BattleActivityBaked.this)
                            .setTitle("Battle Chat")
                            .setMessage("Send Battle Message")
                            .setView(input)
                            .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface i, int j) {
                                    String message = input.getText().toString();
                                    if (message.length() > 0) {
                                        Baos msg = new Baos();
                                        msg.putInt(BattleActivityBaked.this.battle.bID);
                                        msg.putString(message);
                                        if (activeBattle != null) {
                                            netServ.socket.sendMessage(msg, Command.BattleChat);
                                        } else {
                                            netServ.socket.sendMessage(msg, Command.SpectateBattleChat);
                                        }
                                    }
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    }).show();
                    return false;
                }
            });

            // Don't set battleActivity until after we've finished
            // getting UI elements. Otherwise there's a race condition if Battle
            // wants to update one of our UI elements we haven't gotten yet.
            synchronized(battle) {
                battle.activity = BattleActivityBaked.this;
            }

            // Load scrollback
            infoView.setText(battle.hist);
            updateBattleInfo(true);

            // Prompt a UI update of the pokemon
            updateMyPoke();
            updateOppPoke(opp);

            // Enable or disable buttons
            updateButtons();

            // Start timer updating
            handler.postDelayed(updateTimeTask, 100);

            checkRearrangeTeamDialog();

            if (battleScreen != null) {

            }
        }

        public void onServiceDisconnected(ComponentName className) {
            battle.activity = null;
            netServ = null;
        }
    };

    public void end() {
        runOnUiThread(new Runnable() {
            public void run() {
                BattleActivityBaked.this.finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    public View.OnTouchListener dialogListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent e) {
            int id = v.getId();
            for(int i = 0; i < 6; i++) {
                if(id == myArrangePokeIcons[i].getId() && e.getAction() == MotionEvent.ACTION_DOWN) {
                    Object dragInfo = v;
                    mDragLayer.startDrag(v, myArrangePokeIcons[i], dragInfo, DragController.DRAG_ACTION_MOVE);
                    break;
                }
            }
            return true;
        }
    };

    public View.OnClickListener battleListener = new View.OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            // Check to see if click was on attack button
            for(int i = 0; i < 4; i++)
                if(id == attackLayouts[i].getId())
                    netServ.socket.sendMessage(activeBattle.constructAttack((byte)i, megaClicked), Command.BattleMessage);
            // Check to see if click was on pokelist button
            for(int i = 0; i < 6; i++) {
                if(id == pokeList[i].whole.getId()) {
                    netServ.socket.sendMessage(activeBattle.constructSwitch((byte)i), Command.BattleMessage);
                    realViewSwitcher.setCurrentItem(0, true);
                }
            }
            activeBattle.clicked = true;
            updateButtons();
        }
    };


    public View.OnLongClickListener moveListener = new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            int id = v.getId();
            for(int i = 0; i < 4; i++)
                if(id == attackLayouts[i].getId() && !attackNames[i].equals("No Move")) {
                    lastClickedMove = activeBattle.displayedMoves[i];
                    showDialog(BattleDialog.MoveInfo.ordinal());
                    return true;
                }
            return false;
        }
    };

    /*
    public View.OnLongClickListener spriteListener = new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            if(v.getId() == pokeSprites[me].getId())
                showDialog(BattleDialog.MyDynamicInfo.ordinal());
            else
                showDialog(BattleDialog.OppDynamicInfo.ordinal());
            return true;
        }
    };
    */

	/*
    void setLayoutEnabled(ViewGroup v, boolean enabled) {
    	v.setEnabled(enabled);
    	v.getBackground().setAlpha(enabled ? 255 : 128);
    }

    void setTextViewEnabled(TextView v, boolean enabled) {
    	v.setEnabled(enabled);
    	v.setTextColor(v.getTextColors().withAlpha(enabled ? 255 : 128).getDefaultColor());
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(isSpectating() ? R.menu.spectatingbattleoptions : R.menu.battleoptions, menu);

        menu.findItem(R.id.sounds).setChecked(getSharedPreferences("battle", MODE_PRIVATE).getBoolean("pokemon_cries", true));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (!isSpectating()) {
            if (activeBattle.gotEnd) {
                menu.findItem(R.id.close).setVisible(true);
                menu.findItem(R.id.cancel).setVisible(false);
                menu.findItem(R.id.forfeit).setVisible(false);
                menu.findItem(R.id.draw).setVisible(false);
            } else {
    			/* No point in canceling if no action done */
                menu.findItem(R.id.close).setVisible(false);
                menu.findItem(R.id.cancel).setVisible(activeBattle.clicked);
            }
            menu.findItem(R.id.megavolve).setVisible(activeBattle.allowMega);
            menu.findItem(R.id.megavolve).setChecked(megaClicked);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.megavolve:
                item.setChecked(!item.isChecked());
                megaClicked = item.isChecked();
                break;
            case R.id.cancel:
                netServ.socket.sendMessage(activeBattle.constructCancel(), Command.BattleMessage);
                break;
            case R.id.forfeit:
                if (netServ != null && netServ.isBattling() && !battle.gotEnd)
                    showDialog(BattleDialog.ConfirmForfeit.ordinal());
                break;
            case R.id.close:
                if (isSpectating()) {
                    netServ.stopWatching(battle.bID);
                } else {
                    endBattle();
                }
                break;
            case R.id.draw:
                netServ.socket.sendMessage(activeBattle.constructDraw(), Command.BattleMessage);
                break;
            case R.id.sounds:
                item.setChecked(!item.isChecked());
                getSharedPreferences("battle", Context.MODE_PRIVATE).edit().putBoolean("pokemon_cries", item.isChecked()).commit();
                break;
        }
        return true;
    }

    public void notifyRearrangeTeamDialog() {
        runOnUiThread(new Runnable() {
            public void run() {
                checkRearrangeTeamDialog();
            }
        });
    }

    private void checkRearrangeTeamDialog() {
        if (netServ != null && netServ.isBattling() && battle.shouldShowPreview) {
            showDialog(BattleDialog.RearrangeTeam.ordinal());
        }
    }

    void endBattle() {
        if (netServ != null && netServ.socket != null && netServ.socket.isConnected()) {
            Baos bID = new Baos();
            bID.putInt(battle.bID);
            netServ.socket.sendMessage(bID, Command.BattleFinished);
        }
    }

    protected Dialog onCreateDialog(final int id) {
        int player = me;
        final AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        switch(BattleDialog.values()[id]) {
            case RearrangeTeam: {
                View layout = inflater.inflate(R.layout.rearrange_team_dialog, (RelativeLayout) findViewById(R.id.rearrange_team_dialog));
                builder.setView(layout)
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                netServ.socket.sendMessage(activeBattle.constructRearrange(), Command.BattleMessage);
                    /*
                    // re-pre-load PokeBall and sprite info
                    for (int i = 0; i < 6; i++) {
                        battle.pokes[battle.me][i].uID = activeBattle.myTeam.pokes[i].uID;
                    }
                    */
                                battle.shouldShowPreview = false;
                                removeDialog(id);
                            }
                        })
                        .setCancelable(false);
                dialog = builder.create();

                mDragLayer = (DragLayer)layout.findViewById(R.id.drag_my_poke);
                for(int i = 0; i < 6; i++){
                    BattlePoke poke = activeBattle.myTeam.pokes[i];
                    myArrangePokeIcons[i] = (PokeDragIcon)layout.findViewById(resources.getIdentifier("my_arrange_poke" + (i+1), "id", InfoConfig.pkgName));
                    myArrangePokeIcons[i].setOnTouchListener(dialogListener);
                    myArrangePokeIcons[i].setImageDrawable(PokemonInfo.iconDrawableCache(poke.uID));
                    myArrangePokeIcons[i].num = i;
                    myArrangePokeIcons[i].battleActivity = this;

                    ShallowShownPoke oppPoke = activeBattle.oppTeam.pokes[i];
                    oppArrangePokeIcons[i] = (ImageView)layout.findViewById(resources.getIdentifier("foe_arrange_poke" + (i+1), "id", InfoConfig.pkgName));
                    oppArrangePokeIcons[i].setImageDrawable(PokemonInfo.iconDrawableCache(oppPoke.uID));
                }
                return dialog;
            }
            case ConfirmForfeit:
                builder.setMessage("Really Forfeit?")
                        .setCancelable(true)
                        .setPositiveButton("Forfeit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                endBattle();
                            }
                        })
                        .setNegativeButton("Cancel", null);
                return builder.create();
            case OppDynamicInfo:
                player = opp;
            case MyDynamicInfo:
                if(netServ != null && battle != null && battle.dynamicInfo[player] != null) {
                    final Dialog simpleDialog = new Dialog(this);
                    simpleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    simpleDialog.setContentView(R.layout.dynamic_info_layout);

                    TextView t = (TextView)simpleDialog.findViewById(R.id.nameTypeView);
                    t.setText(( (player == me && !isSpectating()) ? activeBattle.myTeam.pokes[0] : battle.currentPoke(player)).nameAndType());
                    t = (TextView)simpleDialog.findViewById(R.id.statNamesView);
                    t.setText(battle.dynamicInfo[player].statsAndHazards());
                    t = (TextView)simpleDialog.findViewById(R.id.statNumsView);
                    if (player == me && !isSpectating()) {
                        t.setText(activeBattle.myTeam.pokes[0].printStats());
                    }
                    else if (player != me && !isSpectating()) {
                        t.setText(activeBattle.currentPoke(player).statString(battle.dynamicInfo[player].boosts));
                        t = (TextView)simpleDialog.findViewById(R.id.moveString);
                        t.setText(activeBattle.currentPoke(opp).movesString());
                    }
                    //	t.setVisibility(View.GONE);
                    t = (TextView)simpleDialog.findViewById(R.id.statBoostView);
                    String s = battle.dynamicInfo[player].boosts();
                    if (!"\n\n\n\n".equals(s)) {
                        t.setText(s);
                    } else {
                        t.setVisibility(View.GONE);
                    }
                    simpleDialog.setCanceledOnTouchOutside(true);
                    simpleDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            removeDialog(id);
                        }
                    });
                    simpleDialog.findViewById(R.id.dynamic_info_layout).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            simpleDialog.cancel();
                        }
                    });
                    return simpleDialog;
                }
                return null;
            case MoveInfo:
                dialog = builder.setTitle(lastClickedMove.toString())
                        .setMessage(lastClickedMove.descAndEffects())
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                removeDialog(id);
                            }
                        })
                        .create();
                dialog.setCanceledOnTouchOutside(true);
                return dialog;
            default:
                return new Dialog(this);
        }
    }

    private GameFrame battleScreen;
    public SpriteAnimation[] sprites = new SpriteAnimation[2];
    public TextureAtlas[] spriteAtlas;
    public BattleInfoHUD[] HUDs = new BattleInfoHUD[2];
    public void callForward(GameFrame frame) {
        this.battleScreen = frame;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                battleScreen.setBackground(battle.background);
            }
        });
    }

    public SpectatingBattle getBattle() {
        return battle;
    }

    public Battle getActiveBattle() {
        return activeBattle;
    }

    @Override
    public void exit() {

    }
}
