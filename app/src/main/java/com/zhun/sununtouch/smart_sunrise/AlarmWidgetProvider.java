package com.zhun.sununtouch.smart_sunrise;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.zhun.sununtouch.smart_sunrise.Configuration.AlarmConfigurationList;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Sunny on 19.02.2017.
 * The Alarm Widget Class
 */

public class AlarmWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        AlarmConfigurationList alarms = new AlarmConfigurationList(context);

        for(int widgetIdx = 0; widgetIdx < appWidgetIds.length; ++widgetIdx ){

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_alarm_time);

            if(alarms.size() == 0 || !alarms.isAlarmSet()){
                remoteViews.setTextViewText(R.id.alarm_widget_text_view, context.getString(R.string.wakeup_no_alarm));
                remoteViews.setTextViewText(R.id.alarm_widget_text_view_Before, "");
            }
            else
            {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(alarms.getNextSetAlarm().getTimeInMillis());
                remoteViews.setTextViewText(R.id.alarm_widget_text_view, SimpleDateFormat.getDateTimeInstance().format(calendar.getTime()));
                remoteViews.setTextViewText(R.id.alarm_widget_text_view_Before,
                        "Screen: "  + alarms.getNextSetAlarm().getScreenStartTime() +
                        "m | LED: " + alarms.getNextSetAlarm().getLEDStartTime() + "m" );
            }
            appWidgetManager.updateAppWidget(appWidgetIds[widgetIdx], remoteViews);
        }
    }
}
