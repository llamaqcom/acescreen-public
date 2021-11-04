package com.llamaq.acescreen.helpers;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import timber.log.Timber;

public class ShellResult {
    private final ArrayList<String> stdout = new ArrayList<>();
    private final ArrayList<String> stderr = new ArrayList<>();
    private Boolean exitStatus;

    public ArrayList<String> getStdout() {
        return stdout;
    }

    @SuppressWarnings("unused")
    public ArrayList<String> getStderr() {
        return stderr;
    }

    public boolean getExitStatus() {
        return Boolean.TRUE.equals(exitStatus);
    }

    public boolean containsAny(@NonNull ArrayList<String> arr, @NonNull String[] needle) {
        for (String s : needle) {
            if (arr.stream().anyMatch(haystack -> haystack.contains(s))) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean addToStdout(String s) {
        try {
            if (stdout.add(s)) {
                return true;
            }
        } catch (Exception e) {
            Timber.d(e);
        }
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean addToStderr(String s) {
        try {
            if (stderr.add(s)) {
                return true;
            }
        } catch (Exception e) {
            Timber.d(e);
        }
        return false;
    }

    public void setExitCode(int exitCode) {
        this.exitStatus = (exitCode == 0);
    }
}
