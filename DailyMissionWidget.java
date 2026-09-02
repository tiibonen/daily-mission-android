package com.dailymission.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DailyMissionWidget extends AppWidgetProvider {
    private static final String ACTION_TOGGLE = "com.dailymission.widget.TOGGLE";
    private static final String PREFS = "mission_prefs";

    private static final String[] AFFIRMATIONS = {
        "Keep going. The results will come, even if you can't see them yet.",
        "You don't have to feel like doing it. Just start.",
        "The version of you you're working toward is built by what you do today.",
        "Don't quit because the results aren't visible yet.",
        "You will be glad you kept going.",
        "One good choice won't change everything. But repeated good choices will.",
        "You don't need motivation. You need one small action.",
        "Keep showing up. That's how things change.",
        "The discomfort of saying no is temporary. The satisfaction of keeping your promise lasts longer.",
        "You are not starting from zero. You're starting from experience.",
        "Don't let one urge decide what you want long-term.",
        "You can do something even when you don't feel like it.",
        "Future you is counting on today's you.",
        "Progress is happening even when you can't see it in the mirror.",
        "You don't need a perfect day. You need another day of trying.",
        "Stay patient. Your body needs time to catch up with your effort.",
        "This feeling will pass. You don't have to act on it.",
        "Keep the promise you made to yourself today.",
        "You can want change without hating who you are now.",
        "Don't give up just because today feels hard.",
        "Do it tired. Do it unmotivated. Just keep moving forward.",
        "Every time you choose your goal over an impulse, you're practicing who you want to become.",
        "You won't regret continuing.",
        "A few weeks from now, you'll wish you'd started today.",
        "Keep going. You haven't seen the full result yet."
    };

    private static final String[] EXERCISES = {
        "20 jumping jacks", "20 bodyweight squats", "10 wall push-ups", "30-second plank",
        "20 high knees", "10 reverse lunges each leg", "20 glute bridges", "15 calf raises",
        "10 incline push-ups", "30-second wall sit", "20 alternating toe touches", "10 bird-dogs each side",
        "20 marching knee raises", "10 chair squats"
    };

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateWidget(context, manager, id);
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_TOGGLE.equals(intent.getAction())) {
            int widgetId = intent.getIntExtra("widgetId", AppWidgetManager.INVALID_APPWIDGET_ID);
            int item = intent.getIntExtra("item", -1);
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID && item >= 0) {
                SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                String key = todayKey() + "_" + item;
                p.edit().putBoolean(key, !p.getBoolean(key, false)).apply();
                updateWidget(context, AppWidgetManager.getInstance(context), widgetId);
            }
        } else if (Intent.ACTION_DATE_CHANGED.equals(intent.getAction()) || Intent.ACTION_TIME_CHANGED.equals(intent.getAction()) || Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            for (int id : manager.getAppWidgetIds(new android.content.ComponentName(context, DailyMissionWidget.class))) updateWidget(context, manager, id);
        }
    }

    private static String todayKey() { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime()); }

    private static int dayNumber() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.DAY_OF_YEAR) + c.get(Calendar.YEAR) * 3;
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_daily_mission);
        int d = dayNumber();
        v.setTextViewText(R.id.affirmation, AFFIRMATIONS[Math.floorMod(d, AFFIRMATIONS.length)]);
        v.setTextViewText(R.id.text3, EXERCISES[Math.floorMod(d, EXERCISES.length)]);

        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String date = todayKey();
        int done = 0;
        int[] checks = {R.id.check1,R.id.check2,R.id.check3,R.id.check4,R.id.check5};
        int[] rows = {R.id.row1,R.id.row2,R.id.row3,R.id.row4,R.id.row5};
        for (int i=0;i<5;i++) {
            boolean checked = p.getBoolean(date + "_" + i, false);
            if (checked) done++;
            v.setTextViewText(checks[i], checked ? "☑" : "☐");
            Intent in = new Intent(context, DailyMissionWidget.class).setAction(ACTION_TOGGLE)
                .putExtra("widgetId", widgetId).putExtra("item", i);
            PendingIntent pi = PendingIntent.getBroadcast(context, widgetId * 10 + i, in, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            v.setOnClickPendingIntent(rows[i], pi);
        }
        v.setTextViewText(R.id.progress, done + " / 5 complete");
        manager.updateAppWidget(widgetId, v);
    }
}
