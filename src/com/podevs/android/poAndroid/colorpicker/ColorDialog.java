package com.podevs.android.poAndroid.colorpicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import com.podevs.android.poAndroid.R;

public class ColorDialog extends DialogFragment {
    ImageView graph;
    ColorGraph colors;
    ImageView zoomed;
    AlertDialog alertDialog;

    public ColorDialog() {}

    public static ColorDialog newInstance() {
        ColorDialog colorDialog = new ColorDialog();
        colorDialog.initialize();
        colorDialog.colors = new ColorGraph();
        return colorDialog;
    }

    private void initialize() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();

        View view = LayoutInflater.from(activity).inflate(R.layout.color_dialog, null);
        graph = (ImageView) view.findViewById(R.id.color_graph);
        if (colors != null) {
            graph.setImageBitmap(colors.bitmap);
        }
        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("HI", "Clicky Clicky");
                colors.v += 30;
                colors.repaint();
                graph.setImageBitmap(colors.bitmap);
            }
        });

        alertDialog = new AlertDialog.Builder(activity)
                .setTitle("test")
                .setView(view)
                .create();

        return alertDialog;
    }
}
