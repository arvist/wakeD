package com.cikoapps.deezeralarm.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.models.DeviceRingtone;

import java.util.ArrayList;

@SuppressWarnings("SameParameterValue")
public class SettingsActivity extends DeezerBase {
    public final static String SELECTED_INTERVAL = "selectedInterval";
    public final static String SELECTED_RINGTONE = "selectedRingtone";
    private final static String SELECTED_RINGTONE_TITLE = "selectedRingtoneTitle";
    public final static String SELECTED_RINGTONE_URI = "selectedRingtoneUri";
    public final static String WIND_MILES_BOOLEAN = "windMilesBool";
    public final static String TEMP_FAHRENHEIT_BOOLEAN = "tempFBool";
    public final static String ONLY_WIFI_SELECTED = "wifiSelected";
    public final static String MAX_ALARM_VOLUME = "maxVolume";
    public final static String WEATHER_SERVICE = "weatherService";
    private static final String TAG = "SettingsActivity";
    private final String[] elements = {"5 minutes ", "10 minutes", "15 minutes ", "20 minutes", "25 minutes", "30 minutes",
            "35 minutes", "40 minutes", "45 minutes", "50 minutes", "55 minutes", "60 minutes", "Do not refresh automatically"};
    private Context context;
    private RadioButton tempRadioButton;
    private RadioButton windRadioButton;
    private RadioButton weatherButton;
    private RadioButton useOnlyWiFiButton;
    private ImageButton editDefaultRingtoneButton;
    private ImageButton refreshWeatherDataEditButton;
    private TextView disconnectDeezerAccountTextView;
    private TextView textTimeSelected;
    private TextView textRingtoneInfo;
    private SharedPreferences.Editor sharedPreferencesEditor;
    private boolean windMilesRadioButtonSelected = false;
    private boolean weatherRadioButtonSelected = false;
    private boolean tempFahrenheitRadioButtonSelected = false;
    private boolean gettingRingtoneListFinished = false;
    private boolean wifiSelected;
    private int refreshTime = -1;
    private int selectedRingtone = -1;
    private String[] ringtoneElements;
    private ArrayList<DeviceRingtone> ringtoneList;
    private Dialog refreshRateDialog;
    private Dialog ringtoneDialog;
    private RingtoneAcquire ringtoneAcquire;
    private ProgressBar ringtoneProgress;
    private ProgressBar refreshProgress;
    private RelativeLayout aboutLayout;
    private SeekBar volumeSeekBar;
    private int selectedVolume = 0;
    private Typeface robotoRegular;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*Set activity layout */
        setContentView(R.layout.settings_activity_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);

        /* Initialize global variables */
        this.context = this;
        robotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
        initializeActivityViews();
        SharedPreferences sharedPreferences;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferencesEditor = sharedPreferences.edit();
        if (ringtoneAcquire == null) {
            ringtoneAcquire = new RingtoneAcquire();
            ringtoneAcquire.execute();
        }

        /*Set on click listeners */
        initializeAppBarActions();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    private void initializeActivityViews() {
        tempRadioButton = (RadioButton) findViewById(R.id.layoutTemp).findViewById(R.id.radioButtonTemp);
        windRadioButton = (RadioButton) findViewById(R.id.layoutWind).findViewById(R.id.radioButtonWind);
        useOnlyWiFiButton = (RadioButton) findViewById(R.id.layoutWiFi).findViewById(R.id.radioButtonWifi);
        weatherButton = (RadioButton) findViewById(R.id.layoutWeather).findViewById(R.id.radioButtonWeather);
        disconnectDeezerAccountTextView = (TextView) findViewById(R.id.layoutAccount).findViewById(R.id.disconnect);
        disconnectDeezerAccountTextView.setTypeface(robotoRegular);
        textTimeSelected = (TextView) findViewById(R.id.layoutRefresh).findViewById(R.id.textTimeSelected);
        textTimeSelected.setTypeface(robotoRegular);
        textRingtoneInfo = (TextView) findViewById(R.id.layoutRingtone).findViewById(R.id.textRingtoneInfo);
        textRingtoneInfo.setTypeface(robotoRegular);
        editDefaultRingtoneButton = (ImageButton) findViewById(R.id.layoutRingtone).findViewById(R.id.buttonRingtone);
        ringtoneProgress = (ProgressBar) findViewById(R.id.layoutRingtone).findViewById(R.id.cover_progress);
        ringtoneProgress.setVisibility(View.GONE);
        editDefaultRingtoneButton.setVisibility(View.VISIBLE);
        refreshWeatherDataEditButton = (ImageButton) findViewById(R.id.layoutRefresh).findViewById(R.id.buttonRate);
        refreshProgress = (ProgressBar) findViewById(R.id.layoutRefresh).findViewById(R.id.cover_progress_refresh);
        aboutLayout = (RelativeLayout) findViewById(R.id.about);
        volumeSeekBar = (SeekBar) findViewById(R.id.layoutVolume).findViewById(R.id.volumeSeekBar);
        refreshProgress.setVisibility(View.GONE);
        setSavedValuesFromSharedPreferences();
        createOnClickListeners();
    }

    /* Build and set listeners to RingtoneSelect & WeatherRefreshRate dialogs */
    private void buildDialogs() {
        AlertDialog.Builder refreshRateDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        refreshRateDialogBuilder.setSingleChoiceItems(elements, refreshTime, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                refreshTime = which;
            }
        });
        refreshRateDialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textTimeSelected.setText(elements[refreshTime]);
                sharedPreferencesEditor.putInt(SELECTED_INTERVAL, refreshTime);
            }
        });
        refreshRateDialog = refreshRateDialogBuilder.create();
        AlertDialog.Builder ringtoneDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        ringtoneElements = new String[ringtoneList.size()];
        int i = 0;
        for (DeviceRingtone dr : ringtoneList) {
            ringtoneElements[i] = dr.title;
            i++;
        }
        ringtoneDialogBuilder.setSingleChoiceItems(ringtoneElements, selectedRingtone, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedRingtone = which;
            }
        });
        ringtoneDialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textRingtoneInfo.setText(ringtoneElements[selectedRingtone]);
                sharedPreferencesEditor.putInt(SELECTED_RINGTONE, selectedRingtone);
                sharedPreferencesEditor.putString(SELECTED_RINGTONE_TITLE, ringtoneElements[selectedRingtone]);
                sharedPreferencesEditor.putString(SELECTED_RINGTONE_URI, ringtoneList.get(selectedRingtone).uri);
            }
        });
        ringtoneDialog = ringtoneDialogBuilder.create();
        ringtoneDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ringtoneProgress.setVisibility(View.GONE);
                editDefaultRingtoneButton.setVisibility(View.VISIBLE);
            }
        });
        refreshRateDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                refreshProgress.setVisibility(View.GONE);
                refreshWeatherDataEditButton.setVisibility(View.VISIBLE);
            }
        });
    }

    /* Apply saved selected values to all views */
    public void setSavedValuesFromSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean windMiles = preferences.getBoolean(WIND_MILES_BOOLEAN, false);
        boolean tempFBool = preferences.getBoolean(TEMP_FAHRENHEIT_BOOLEAN, false);
        boolean wifiBool = preferences.getBoolean(ONLY_WIFI_SELECTED, false);
        boolean weatherBool = preferences.getBoolean(WEATHER_SERVICE, false);
        windRadioButton.setChecked(windMiles);
        tempRadioButton.setChecked(tempFBool);
        useOnlyWiFiButton.setChecked(wifiBool);
        weatherButton.setChecked(weatherBool);
        windMilesRadioButtonSelected = windMiles;
        tempFahrenheitRadioButtonSelected = tempFBool;
        weatherRadioButtonSelected = weatherBool;
        wifiSelected = wifiBool;
        textTimeSelected.setText(elements[preferences.getInt(SELECTED_INTERVAL, 1)]);
        refreshTime = preferences.getInt(SELECTED_INTERVAL, 1);
        textRingtoneInfo.setText(preferences.getString(SELECTED_RINGTONE_TITLE, "Not Selected"));
        selectedRingtone = preferences.getInt(SELECTED_RINGTONE, 1);
        selectedVolume = preferences.getInt(MAX_ALARM_VOLUME, 8);
        volumeSeekBar.setMax(((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeSeekBar.setProgress(selectedVolume);
    }

    /* Create on click listeners for views that do not have dialogs */
    private void createOnClickListeners() {
        tempRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tempFahrenheitRadioButtonSelected) {
                    tempRadioButton.setChecked(false);
                    tempFahrenheitRadioButtonSelected = false;
                } else {
                    tempRadioButton.setChecked(true);
                    tempFahrenheitRadioButtonSelected = true;
                }
                if (tempFahrenheitRadioButtonSelected) {
                    sharedPreferencesEditor.putBoolean(TEMP_FAHRENHEIT_BOOLEAN, true);
                } else {
                    sharedPreferencesEditor.putBoolean(TEMP_FAHRENHEIT_BOOLEAN, false);
                }
            }
        });
        windRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (windMilesRadioButtonSelected) {
                    windRadioButton.setChecked(false);
                    windMilesRadioButtonSelected = false;
                } else {
                    windRadioButton.setChecked(true);
                    windMilesRadioButtonSelected = true;
                }
                if (windMilesRadioButtonSelected) {
                    sharedPreferencesEditor.putBoolean(WIND_MILES_BOOLEAN, true);
                } else {
                    sharedPreferencesEditor.putBoolean(WIND_MILES_BOOLEAN, false);
                }
            }
        });
        useOnlyWiFiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiSelected) {
                    useOnlyWiFiButton.setChecked(false);
                    wifiSelected = false;
                } else {
                    useOnlyWiFiButton.setChecked(true);
                    wifiSelected = true;
                }
                if (wifiSelected) {
                    sharedPreferencesEditor.putBoolean(ONLY_WIFI_SELECTED, true);
                } else {
                    sharedPreferencesEditor.putBoolean(ONLY_WIFI_SELECTED, false);
                }
            }
        });
        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (weatherRadioButtonSelected) {
                    weatherButton.setChecked(false);
                    weatherRadioButtonSelected = false;
                } else {
                    weatherButton.setChecked(true);
                    weatherRadioButtonSelected = true;
                }
                if (weatherRadioButtonSelected) {
                    sharedPreferencesEditor.putBoolean(WEATHER_SERVICE, true);
                } else {
                    sharedPreferencesEditor.putBoolean(WEATHER_SERVICE, false);
                }
            }
        });
        disconnectDeezerAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDeezer();
                Toast.makeText(context, "Your Deezer account is no longer connected to app", Toast.LENGTH_SHORT).show();
            }
        });
        aboutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
             /*
                Creates and shows loading animation
                until ringtone list fetched from database
                and can be shown as dialog
             */
        refreshWeatherDataEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gettingRingtoneListFinished) {
                    refreshProgress.setVisibility(View.VISIBLE);
                    refreshWeatherDataEditButton.setVisibility(View.GONE);
                }
                new CountDownTimer(250, 80) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        if (refreshRateDialog != null) {
                            if (!refreshRateDialog.isShowing() && !ringtoneDialog.isShowing()) {
                                refreshRateDialog.show();
                            }
                        } else {
                            new CountDownTimer(250, 80) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                }

                                @Override
                                public void onFinish() {
                                    refreshWeatherDataEditButton.callOnClick();
                                }
                            }.start();
                        }
                    }
                }.start();
            }
        });
            /*
                Creates and shows loading animation
                until ringtone list fetched from database
                and can be shown as dialog
             */
        editDefaultRingtoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gettingRingtoneListFinished) {
                    ringtoneProgress.setVisibility(View.VISIBLE);
                    editDefaultRingtoneButton.setVisibility(View.GONE);
                }
                new CountDownTimer(250, 80) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        if (ringtoneDialog != null) {
                            if (!ringtoneDialog.isShowing() && !refreshRateDialog.isShowing()) {
                                ringtoneDialog.show();
                            }
                        } else {
                            new CountDownTimer(250, 80) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                }

                                @Override
                                public void onFinish() {
                                    editDefaultRingtoneButton.callOnClick();
                                }
                            }.start();
                        }
                    }
                }.start();
            }
        });

        /*
            Puts selected volume value in shared preferences
         */
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedVolume = progress;
                sharedPreferencesEditor.putInt(MAX_ALARM_VOLUME, selectedVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /* Application bar action creation */
    private void initializeAppBarActions() {
        ImageButton backButton = (ImageButton) findViewById(R.id.app_bar_back_btn);
        ImageButton saveButton = (ImageButton) findViewById(R.id.app_bar_save);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferencesEditor.commit();
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    public SharedPreferences.Editor getSharedPreferencesEditor() {
        return sharedPreferencesEditor;
    }

    public void setSelectedVolume(int selectedVolume) {
        this.selectedVolume = selectedVolume;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    public void setSelectedRingtone(int selectedRingtone) {
        this.selectedRingtone = selectedRingtone;
    }

    public int getSelectedRingtone() {
        return selectedRingtone;
    }

    public int getRefreshTime() {
        return refreshTime;
    }

    public int getSelectedVolume() {
        return selectedVolume;
    }

    /* Aquires Device ringtone list in background  */
    class RingtoneAcquire extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... params) {
            getRingtones();
            return "Post execute";
        }

        public void getRingtones() {
            ringtoneList = new ArrayList<>();
            RingtoneManager ringtoneMgr = new RingtoneManager(context);
            ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
            Cursor alarmsCursor = ringtoneMgr.getCursor();
            if (alarmsCursor.moveToFirst()) {
                do {

                    int currentPosition = alarmsCursor.getPosition();
                    DeviceRingtone deviceRingtone = new DeviceRingtone(ringtoneMgr.getRingtoneUri(currentPosition).toString(), ringtoneMgr.getRingtone(currentPosition).getTitle(context), false);
                    ringtoneList.add(deviceRingtone);
                } while (alarmsCursor.moveToNext());
                alarmsCursor.close();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            buildDialogs();
            gettingRingtoneListFinished = true;
        }
    }
}