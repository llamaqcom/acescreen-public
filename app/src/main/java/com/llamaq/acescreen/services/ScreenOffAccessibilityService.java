package com.llamaq.acescreen.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import timber.log.Timber;

public class ScreenOffAccessibilityService extends AccessibilityService {
    private static ScreenOffAccessibilityService sInstance;
    private static final MutableLiveData<Boolean> sEnabled = new MutableLiveData<>();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        sInstance = this;
        sEnabled.postValue(true);
        Timber.d("onServiceConnected");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        sInstance = null;
        sEnabled.postValue(false);
        Timber.d("onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Timber.d("onAccessibilityEvent: %s", event.toString());
    }

    @Override
    public void onInterrupt() {
        Timber.d("onInterrupt");
    }

    public static LiveData<Boolean> getEnabled() {
        return sEnabled;
    }

    public static boolean isEnabled() {
        return sInstance != null;
    }

    /**
     * Immediately turns off display.
     * @return Whether the action was successfully performed.
     */
    public static boolean lockNow() {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (sInstance != null) {
                try {
                    result = sInstance.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
                } catch (Exception | Error e) {
                    Timber.d(e);
                }
            }
        }
        Timber.d("lockNow: %s", result);
        return result;
    }
}
