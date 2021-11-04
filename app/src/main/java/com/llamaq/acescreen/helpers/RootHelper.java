package com.llamaq.acescreen.helpers;

import static com.llamaq.acescreen.helpers.Bubble.showBubble;

import android.os.AsyncTask;

import com.llamaq.acescreen.R;

import timber.log.Timber;

/**
 * Helper class to execute limited number of commands with root privilege
 */

public class RootHelper {
    // unfortunately, `command -v` is unavailable on Android shell variant
    private static final String CMD_GUESS_ROOT_PRESENT = "which su";
    private static final String CMD_DUMPSYS_POWER = "dumpsys power";
    private static final String CMD_PRESS_POWER_BUTTON = "input keyevent 26";

    private static volatile Boolean deviceRooted;

    /**
     * Try to guess if the device is rooted to show the user the relevant information
     */
    public static void guessIfDeviceRooted() {
        AsyncTask.execute(() -> {
            deviceRooted = (Shell.runAsUser(CMD_GUESS_ROOT_PRESENT)).getExitStatus();
            Timber.d("deviceRooted: %b", deviceRooted);
        });
    }

    /**
     * To request root rights, we use a command that does not perform active actions with
     * the user's screen, but which we will need in the future if the user wants to
     * keep WAKE_LOCKs by other apps.
     */
    public static void requestRootPermission() {
        AsyncTask.execute(() -> {
            RootHelper.runAsRoot(CMD_DUMPSYS_POWER);
            if (! Prefs.isRootGranted()) {
                showBubble(R.string.toast_failed_to_get_root_privileges);
            }
        });
    }

    /**
     * Getter for guessed value
     * @return true if rooted, false if not.
     */
    public static boolean isDeviceRooted() {
        return Boolean.TRUE.equals(deviceRooted);
    }

    /**
     * Immediately turns off display.
     * @return Whether the action was successfully performed.
     */
    public static boolean lockNow() {
        ShellResult shellResult = RootHelper.runAsRoot(CMD_PRESS_POWER_BUTTON);
        boolean exitStatus = shellResult.getExitStatus();
        Timber.d("CMD_PRESS_POWER_BUTTON succeeded? %s", exitStatus);
        return exitStatus;
    }

    public static boolean isWakeLockedByOtherApps() {
        ShellResult shellResult = RootHelper.runAsRoot(CMD_DUMPSYS_POWER);
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

    private static ShellResult runAsRoot(String cmd) {
        return RootHelper.runAsRoot(new String[]{cmd});
    }

    private static ShellResult runAsRoot(String[] cmd) {
        ShellResult shellResult = Shell.run("su", cmd);
        Prefs.setRootGranted(shellResult.getExitStatus());
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
