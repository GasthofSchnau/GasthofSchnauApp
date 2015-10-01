package de.gasthof_schnau.gasthofschnau.lib;

import android.content.Context;
import android.graphics.Color;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class MyListPreference extends ListPreference {

    public MyListPreference(Context context) {
        super(context);
    }

    public MyListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTextColor(Color.BLACK);
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        summaryView.setTextColor(Color.BLACK);
    }
}
