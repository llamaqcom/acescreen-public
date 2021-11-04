package com.llamaq.acescreen.models;

import static com.llamaq.acescreen.helpers.Bubble.showBubble;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.llamaq.acescreen.App;
import com.llamaq.acescreen.R;
import com.llamaq.acescreen.helpers.Prefs;
import com.llamaq.acescreen.helpers.Shell;
import com.llamaq.acescreen.helpers.ShellResult;

import timber.log.Timber;

/**
 * Helper class to execute limited number of commands with root privilege
 */

public class Su {
    private static final String CMD_GUESS_ROOT_PRESENT = "which su"; // `command -v` is unavailable
    private static final String CMD_DUMPSYS_POWER = "dumpsys power";
    private static final String CMD_PRESS_POWER_BUTTON = "input keyevent 26";
    private static final String PERMISSION_ROOT_GRANTED_KEY =
            App.get().getString(R.string.permission_root_granted_key);

    private volatile static Su sInstance;
    private final MutableLiveData<Boolean> mGranted = new MutableLiveData<>();
    private Boolean mRootFeature;

    public static Su getInstance() {
        if (sInstance == null) {
            synchronized (Su.class) {
                if (sInstance == null) {
                    sInstance = new Su();
                }
            }
        }
        return sInstance;
    }

    private Su() {
        mGranted.postValue(isGranted());
    }

    public boolean isGranted() {
        return Prefs.getBoolean(PERMISSION_ROOT_GRANTED_KEY, false);
    }

    public LiveData<Boolean> getGranted() { return mGranted; }

    /**
     * Getter for guessed value
     * @return true if rooted, false if not.
     */
    public boolean hasRootFeature() {
        return Boolean.TRUE.equals(mRootFeature);
    }

    /**
     * Try to guess if the device is rooted to show the user the relevant information
     */
    public void guess() {
        AsyncTask.execute(() -> {
            mRootFeature = (Shell.runAsUser(CMD_GUESS_ROOT_PRESENT)).getExitStatus();
            Timber.d("Device is rooted: %s", mRootFeature);
        });
    }

    /**
     * To request root rights, we use a command that does not perform active actions with
     * the user's screen, but which we will need in the future if the user wants to
     * keep WAKE_LOCKs by other apps.
     */
    public void request() {
        AsyncTask.execute(() -> {
            if (!runAsRoot(CMD_DUMPSYS_POWER).getExitStatus()) {
                showBubble(R.string.toast_failed_to_get_root_privileges);
            }
        });
    }

    private void setGranted(boolean value) {
        if (isGranted() != value) {
            Prefs.putBoolean(PERMISSION_ROOT_GRANTED_KEY, value);
            mGranted.postValue(value);
        }
    }

    /**
     * Immediately turns off display.
     * @return Whether the action was successfully performed.
     */
    public boolean lockNow() {
        ShellResult shellResult = runAsRoot(CMD_PRESS_POWER_BUTTON);
        boolean exitStatus = shellResult.getExitStatus();
        Timber.d("CMD_PRESS_POWER_BUTTON succeeded? %s", exitStatus);
        return exitStatus;
    }

    public boolean isWakeLockedByOtherApps() {
        ShellResult shellResult = runAsRoot(CMD_DUMPSYS_POWER);
        if (! shellResult.getExitStatus()) {
            // let's assume that there are wakelocks if `dumpsys power` fails for some reason
            return true;
        }

        boolean deviceWakeLocked = shellResult.containsAny(shellResult.getStdout(), new String[] {
                "FULL_WAKE_LOCK",
                "SCREEN_BRIGHT_WAKE_LOCK",
                "SCREEN_DIM_WAKE_LOCK"
        });

        Timber.d("deviceWakeLocked=%s", deviceWakeLocked);
        return deviceWakeLocked;
    }

    private ShellResult runAsRoot(String cmd) {
        return runAsRoot(new String[]{cmd});
    }

    private ShellResult runAsRoot(String[] cmd) {
        ShellResult shellResult = Shell.run("su", cmd);
        setGranted(shellResult.getExitStatus());
        return shellResult;
    }

//    public boolean hasRootAccess() {
//        try {
//            java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(new String[]{"/system/bin/su","-c","cd / && ls"}).getInputStream()).useDelimiter("\\A");
//            return !(s.hasNext() ? s.next() : "").equals("");
//        } catch (IOException e) {
//            Timber.e(e);
//        }
//        return false;
//    }

}
