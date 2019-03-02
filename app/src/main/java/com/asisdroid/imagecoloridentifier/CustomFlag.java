package com.asisdroid.imagecoloridentifier;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.skydoves.multicolorpicker.ColorEnvelope;
import com.skydoves.multicolorpicker.FlagView;

import org.w3c.dom.Text;

public class CustomFlag extends FlagView {

    private TextView txtView;
    private View view;

    CustomFlag(Context context, int layout){
        super(context,layout);
        txtView = (TextView)findViewById(R.id.flag_color_code);
        view = (View) findViewById(R.id.flag_color_layout);
    }

    @Override
    public void onRefresh(ColorEnvelope colorEnvelope) {
        txtView.setText("#"+colorEnvelope.getHtmlCode());
        view.setBackgroundColor(colorEnvelope.getColor());
    }
}
