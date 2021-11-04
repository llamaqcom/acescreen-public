package com.llamaq.acescreen.helpers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.llamaq.acescreen.App;

public class Bubble {
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;

    public static void showBubble(CharSequence text,
                                  int duration) {
        Context context = App.get();
        if (context != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(context, text, duration).show());
        }
    }

    public static void showBubble(CharSequence text) {
        showBubble(text, LENGTH_SHORT);
    }

    public static void showBubble(@StringRes int resId,
                                  int duration) {
        Context context = App.get();
        String text = context.getString(resId);
        showBubble(text, duration);
    }

    public static void showBubble(@StringRes int resId) {
        showBubble(resId, LENGTH_SHORT);
    }
}
