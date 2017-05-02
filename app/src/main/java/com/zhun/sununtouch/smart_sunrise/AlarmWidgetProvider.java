package com.zhun.sununtouch.smart_sunrise;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.zhun.sununtouch.smart_sunrise.Configuration.AlarmConfigurationList;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sunny on 19.02.2017.
 * The Alarm Widget Class
 */

public class AlarmWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        AlarmConfigurationList alarms = new AlarmConfigurationList(context, false);
        for(int widgetId = 0; widgetId < appWidgetIds.length; ++widgetId )
            appWidgetManager.updateAppWidget(appWidgetIds[widgetId], getRemoteView(context, appWidgetManager, widgetId, alarms));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        appWidgetManager.updateAppWidget(appWidgetId, getRemoteView(context, appWidgetManager, appWidgetId, new AlarmConfigurationList(context, false)));
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    private RemoteViews getRemoteView(Context context, AppWidgetManager appWidgetManager, int appWidgetId, AlarmConfigurationList alarms){

        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        final int rows = getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));
        final int columns = getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));

        RemoteViews widgetView;
        switch (columns){
            case 1: widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_alarm_time_1x1); break;
            case 2: widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_alarm_time_1x2); break;
            case 3: widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_alarm_time_1x3); break;
            case 4: widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_alarm_time_1x4); break;
            default: widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_alarm_time_1x4);
        }

        if(alarms.size() == 0 || !alarms.isAlarmSet())
            widgetView.setTextViewText(R.id.alarm_widget_text_view, context.getString(R.string.wakeup_no_alarm));
        else
        {
            switch (columns){
                case 1:
                case 2: {
                    Date date = new Date(alarms.getNextSetAlarm().getTimeInMillis());
                    widgetView.setTextViewText(R.id.alarm_widget_text_view, SimpleDateFormat.getTimeInstance().format(date));
                    widgetView.setTextViewText(R.id.alarm_widget_text_view_Before, SimpleDateFormat.getDateInstance().format(date));

                } break;
                default: {
                    widgetView.setTextViewText(R.id.alarm_widget_text_view, SimpleDateFormat.getDateTimeInstance().format(new Date(alarms.getNextSetAlarm().getTimeInMillis())));
                    widgetView.setTextViewText(R.id.alarm_widget_text_view_Before, "Screen: "  + alarms.getNextSetAlarm().getScreenStartTime() +
                                                                                   "m | LED: " + alarms.getNextSetAlarm().getLEDStartTime() + "m" );
                }
            }
        }

        Intent alarmIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent =  PendingIntent.getActivity(context, 0, alarmIntent, 0);

        widgetView.setOnClickPendingIntent(R.id.alarm_widget_layout, pendingIntent);
        return  widgetView;
    }

    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }
}
