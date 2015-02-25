package com.cikoapps.deezeralarm.models;

import android.content.Context;

import com.cikoapps.deezeralarm.HelperClasses.AlarmDBHelper;

/**
 * Created by arvis.taurenis on 2/10/2015.
 */
public class Alarm implements java.io.Serializable {
    public String title;
    public int id = 0;
    public int hour;
    public int minute;
    public boolean enabled;
    public boolean[] repeatingDays;
    public boolean repeatWeekly;
    public String alarmTone;
    public long alarmid = 0;
    public int type;


    String repeatDays;
    public String alarmToneName;

    public static final int SUNDAY = 0;
    public static final int MONDAY = 1;
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY = 4;
    public static final int FRIDAY = 5;
    public static final int SATURDAY = 6;

    public Alarm() {
        repeatingDays = new boolean[7];
    }

    public void setRepeatingDay(int dayOfWeek, boolean value) {
        repeatingDays[dayOfWeek] = value;
    }

    public boolean getRepeatingDay(int dayOfWeek) {
        return repeatingDays[dayOfWeek];
    }


    public void insertIntoDataBase(Context context) {
        AlarmDBHelper alarmDBHelper = new AlarmDBHelper(context);
        this.repeatDays = repeatingDaysToString();
        alarmDBHelper.insertAlarm(title, hour, minute, repeatDays, repeatWeekly, alarmToneName, alarmTone,
                alarmid, type, enabled);
        alarmDBHelper.close();

    }

    public Alarm(String title, int id, int hour, int minute, boolean enabled, boolean[] repeatingDays,
                 boolean repeatWeekly, String alarmTone, long alarmid, int type, String alarmToneName) {
        this.title = title;
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
        this.repeatingDays = repeatingDays;
        this.repeatWeekly = repeatWeekly;
        this.alarmTone = alarmTone;
        this.alarmid = alarmid;

        this.type = type;
        this.alarmToneName = alarmToneName;
    }

    public Alarm(String title, int hour, int minute, boolean enabled, boolean[] repeatingDays,
                 boolean repeatWeekly, String alarmTone, long alarmid, int type, String alarmToneName) {
        this.title = title;
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
        this.repeatingDays = repeatingDays;
        this.repeatWeekly = repeatWeekly;
        this.alarmTone = alarmTone;
        this.alarmid = alarmid;
        this.type = type;
        this.alarmToneName = alarmToneName;
    }

    private String repeatingDaysToString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (boolean b : repeatingDays) {
            stringBuilder.append(b);
            stringBuilder.append(",");
        }
        return stringBuilder.toString();

    }
}
