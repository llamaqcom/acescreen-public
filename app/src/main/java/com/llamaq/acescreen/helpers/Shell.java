package com.llamaq.acescreen.helpers;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import timber.log.Timber;

public class Shell {

    public static ShellResult run(@NonNull String exec, @NonNull String[] cmds) {
        ShellResult result = new ShellResult();
        Process process = null;

        try {
            process = Runtime.getRuntime().exec(exec);
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            String line;

            for (String cmd : cmds) {
                stdin.write((cmd + "\n").getBytes());
            }
            stdin.write(("exit $?\n").getBytes());
            stdin.flush();
            stdin.close();

            // stdout

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                result.addToStdout(line);
            }
            br.close();

            // stderr

            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                result.addToStderr(line);
            }
            br.close();

            process.waitFor();
            int exitCode = process.exitValue();
            result.setExitCode(exitCode);
        } catch (Exception | Error e) {
            Timber.d(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return result;
    }

    public static ShellResult runAsUser(String[] cmd) {
        return Shell.run("sh", cmd);
    }

    public static ShellResult runAsUser(String cmd) {
        return Shell.runAsUser(new String[]{cmd});
    }

}
