package de.gasthof_schnau.gasthofschnau.lib;

import android.content.Context;
import android.util.TypedValue;
import android.widget.Toast;

public class Util {

    public static int convertToDip(int dip, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getApplicationContext().getResources().getDisplayMetrics());
    }

    public static void makeToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void makeToast(Context context, String text, int length) {
        Toast.makeText(context, text, length).show();
    }

}
