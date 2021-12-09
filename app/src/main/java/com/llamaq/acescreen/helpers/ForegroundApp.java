package com.llamaq.acescreen.helpers;

import static com.llamaq.acescreen.helpers.Bubble.showBubble;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.AsyncTask;

import com.llamaq.acescreen.BuildConfig;
import com.llamaq.acescreen.R;
import com.llamaq.acescreen.helpers.shell.Su;

import java.util.List;

import timber.log.Timber;

public class ForegroundApp {
    private static String sForegroundApp = "";

    public static void detectForegroundApp(final Context context) {
        if (Perms.isUsageAccessGranted()) {
            detectForegroundAppUsageStats(context);
        } else if (Su.getInstance().isGranted()) {
            AsyncTask.execute(() -> sForegroundApp = Su.getInstance().getForegroundApp());
        }
    }

    private static void detectForegroundAppUsageStats(final Context context)
    {
        final UsageStatsManager usageStatsManager =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, now - 1000 * 300, now);
        if (usageStatsList == null || usageStatsList.isEmpty()) return;

        String foregroundApp = "";
        long lastTimeUsed = 0;

        for (UsageStats usageStats : usageStatsList) {
            if (usageStats.getLastTimeUsed() > lastTimeUsed) {
                if ("android".equals(usageStats.getPackageName())) continue; // strange "phantom" package that sometimes pops up in usageStatsList.
                if ("com.android.launcher3".equals(usageStats.getPackageName())) continue; // a capricious launcher on Lineage OS that never reliably reports statistics about itself.
                foregroundApp = usageStats.getPackageName();
                lastTimeUsed = usageStats.getLastTimeUsed();
            }
        }

        if (BuildConfig.DEBUG) {
            if (! foregroundApp.equals(sForegroundApp)) {
                Timber.d("Foreground App changed from %s to %s", sForegroundApp, foregroundApp);
            }
        }

        sForegroundApp = foregroundApp;
    }

    public static String getForegroundApp() {
        return sForegroundApp;
    }
}
