package es.disoft.disoft;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

public class Toast extends android.widget.Toast {


    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */

    public Toast(Context context) {
        super(context);
    }

    public static android.widget.Toast makeText(Context context, int resID) {
        return make(context, resID);
    }

    public static android.widget.Toast makeText(Context context, CharSequence text) {
        return make(context, text);
    }

    private static android.widget.Toast make(Context context, int resID) {
        @SuppressLint("ShowToast")
        android.widget.Toast toast = android.widget.Toast.makeText(context, resID, android.widget.Toast.LENGTH_SHORT);
        centerText(toast);
        return toast;
    }

    private static android.widget.Toast make(Context context, CharSequence text) {
        @SuppressLint("ShowToast")
        android.widget.Toast toast = android.widget.Toast.makeText(context, text, android.widget.Toast.LENGTH_SHORT);
        centerText(toast);
        return toast;
    }

    private static void centerText(android.widget.Toast toast) {
        TextView textView = toast.getView().findViewById(android.R.id.message);
        if (textView != null) textView.setGravity(Gravity.CENTER);
    }
}
