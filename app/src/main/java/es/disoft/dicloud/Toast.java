package es.disoft.dicloud;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

public class Toast {

    private static android.widget.Toast toast;
    private static Context context;

    @SuppressLint("ShowToast")
    private static android.widget.Toast getInstance(Context ctx) {
        if (toast == null) {
            context = ctx;
            toast   = android.widget.Toast.makeText(context, "", android.widget.Toast.LENGTH_SHORT);
            centerText(toast);
        }
        return toast;
    }

    private static void centerText(android.widget.Toast toast) {
        TextView textView = toast.getView().findViewById(android.R.id.message);
        if (textView != null) textView.setGravity(Gravity.CENTER);
    }

    public static android.widget.Toast setText(Context ctx, int resID) {
        getInstance(ctx).setText(resID);
        return toast;
    }

    public static android.widget.Toast setText(Context ctx, CharSequence s) {
        getInstance(ctx).setText(s);
        return toast;
    }

    public static void show() {
        getInstance(context).cancel();
        getInstance(context).show();
    }
}
