package com.llamaq.acescreen.helpers.shell;

import static com.llamaq.acescreen.helpers.Bubble.showBubble;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.llamaq.acescreen.App;
import com.llamaq.acescreen.R;
import com.llamaq.acescreen.helpers.Prefs;

import timber.log.Timber;

/**
 * Helper class to execute limited number of commands with root privilege
 */

public class Su {
    private static final String PERMISSION_ROOT_GRANTED_KEY =
            App.get().getString(R.string.permission_root_granted_key);

    private static final String CMD_GUESS_ROOT_PRESENT =
            "which su"; // `command -v` is unavailable
    private static final String CMD_DUMPSYS_POWER =
            "dumpsys power";
    private static final String CMD_PRESS_POWER_BUTTON =
            "input keyevent 26";

    /**
     * Command to ensure min timeout to support wake locks by other apps as if device is wake locked
     * and the user wants us to respect wake locks by other apps, we temporarily delegate screen off
     * to the system to save power (as this situation can potentially last for hours).
     */
    private static final String CMD_SCREEN_OFF_TIMEOUT =
            "settings put system screen_off_timeout 15000";

    private volatile static Su sInstance;
    private final MutableLiveData<Boolean> mGranted = new MutableLiveData<>();
    private Boolean mFeaturePresent;

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
    public boolean isFeaturePresent() {
        return Boolean.TRUE.equals(mFeaturePresent);
    }

    /**
     * Try to guess if the device is rooted to show the user the relevant information
     */
    public void guess() {
        AsyncTask.execute(() -> {
            mFeaturePresent = (Shell.runAsUser(CMD_GUESS_ROOT_PRESENT)).getExitStatus();
            Timber.d("Device is rooted: %s", mFeaturePresent);
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
     * @param screenTimeoutEnforced Should we ignore other apps WAKE_LOCKs or not
     * @return Whether the action was successfully performed.
     */
    public boolean lockNow(boolean screenTimeoutEnforced) {
        if (!screenTimeoutEnforced) {
            if (isWakeLockedByOtherApps()) {
                runAsRoot(CMD_SCREEN_OFF_TIMEOUT);
                return false;
            }
        }

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
