
        package com.cikoapps.deezeralarm.activities;

        import android.app.AlertDialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.graphics.Typeface;
        import android.os.Bundle;
        import android.os.CountDownTimer;
        import android.support.v7.widget.Toolbar;
        import android.text.format.DateFormat;
        import android.view.View;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.RadioButton;
        import android.widget.TextView;
        import android.widget.TimePicker;

        import com.cikoapps.deezeralarm.R;
        import com.cikoapps.deezeralarm.helpers.AlarmManagerHelper;
        import com.cikoapps.deezeralarm.helpers.HelperClass;
        import com.cikoapps.deezeralarm.models.Alarm;

        import org.apache.commons.lang.StringEscapeUtils;


        public class AddAlarmActivity extends DeezerBase {

            public static final String RESTART_ACTIVITY = "restartActivity";
            private static final String TAG = "AddAlarmActivity";
            private final String elements[] = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            private boolean[] selected = {false, false, false, false, false, false, false};
            private Context context;
            private int type;
            private String uri = null;
            private long deezerRingtoneId;
            private String ringtoneName = "";
            private String artist = "";
            private boolean[] tempSelection;


            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                context = getApplicationContext();
                /* Layout of activity */
                setContentView(R.layout.add_alarm_activity_layout);
                Toolbar toolbar = (Toolbar) findViewById(R.id.appBar);
                setSupportActionBar(toolbar);
                /* Repeated Days dialog listener initialization */
                repeatNoButtonClick();
                repeatYesButtonClick();
                /* Cancel alarm button on click listener initialization */
                cancelAddingAlarm();
                /* Add alarm button on click listener initialization */
                addAlarm();
                /* Alarm Ringtone select button on click listener initialization */
                ringtoneEdit();
                /* Application bar functionality */
                appBarActions();
                /* Set time picker to use appropriate time settings */
                TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
                boolean fullTimeClock = DateFormat.is24HourFormat(context);
                timePicker.setIs24HourView(fullTimeClock);
             }

            // Navigates back to MainActivity
            @Override
            public void onBackPressed() {
                super.onBackPressed();
                finish();
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }

            // To initialize application bar on click actions
            void appBarActions() {
                ImageButton backButton = (ImageButton) findViewById(R.id.app_bar_back_btn);
                ImageButton settingsButton = (ImageButton) findViewById(R.id.app_bar_settings);
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        Intent intent = new Intent(context, MainActivity.class);
                        startActivity(intent);
                    }
                });
                settingsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, SettingsActivity.class);
                        startActivityForResult(intent, 2);
                    }
                });
            }

            // Clears repeated day selection array
            void repeatNoButtonClick() {
                findViewById(R.id.radioButtonNo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < selected.length; i++) {
                            selected[i] = false;
                        }
                    }
                });
            }

            // To select repeated days for alarm
            void repeatYesButtonClick() {
                findViewById(R.id.radioButtonYes).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (((RadioButton) v).isChecked()) {
                            AlertDialog.Builder repeatingDaysDialog = new AlertDialog.Builder(AddAlarmActivity.this);
                            tempSelection = selected.clone();
                            repeatingDaysDialog.setMultiChoiceItems(elements, tempSelection, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                                }
                            });
                            repeatingDaysDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selected = tempSelection.clone();
                                    // If none repeated day selected then set No radio button to checked
                                    // and disable Yes radio button
                                    if (HelperClass.allFalse(selected)) {
                                        ((RadioButton) findViewById(R.id.radioButtonNo)).setChecked(true);
                                        ((RadioButton) findViewById(R.id.radioButtonYes)).setChecked(false);
                                    }
                                }
                            });
                            repeatingDaysDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    tempSelection = selected.clone();
                                    dialog.cancel();
                                }
                            });
                            repeatingDaysDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    if (  HelperClass.allFalse(selected)) {
                                        ((RadioButton) findViewById(R.id.radioButtonNo)).setChecked(true);
                                        ((RadioButton) findViewById(R.id.radioButtonYes)).setChecked(false);
                                    }
                                }
                            });
                            AlertDialog dialog = repeatingDaysDialog.create();
                            dialog.setCanceledOnTouchOutside(true);
                            dialog.show();
                        }
                    }

                });
            }

            // To navigate back to MainActivity on cancel button click
            void cancelAddingAlarm() {
                findViewById(R.id.cancelAlarmAdd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        finish();
                        startActivity(intent);
                    }
                });
            }

            // To save created alarm object to database
            void addAlarm() {
                findViewById(R.id.confirmAlarmAdd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HelperClass helperClass = new HelperClass(getApplicationContext());
                        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
                        timePicker.clearFocus();
                        int hour = timePicker.getCurrentHour();
                        int minute = timePicker.getCurrentMinute();
                        EditText title = (EditText) findViewById(R.id.alarmTitleTextView);
                        String alarmTitleString = title.getText().toString();
                        boolean repeatWeekly = helperClass.oneOrMoreTrue(selected);
                        // If no title created use default value
                        if (alarmTitleString.trim().length() < 1) {
                            alarmTitleString = "My Alarm";
                        }
                        if (artist == null) artist = "";

                        Alarm alarm = new Alarm(StringEscapeUtils.escapeSql(alarmTitleString), hour, minute, true, selected,
                                repeatWeekly, uri, deezerRingtoneId, type, ringtoneName, artist);
                        AlarmManagerHelper.cancelAlarms(context);
                        alarm.insertIntoDataBase(context);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        finish();
                        AlarmManagerHelper.setAlarms(context);
                        // Navigate back to MainActivity
                        startActivity(intent);
                    }
                }) ;
            }

            // Alarm ringtone chooser
            void ringtoneEdit() {
                findViewById(R.id.editRingtoneButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // To enable Ripple animation
                        new CountDownTimer(350, 80) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                Intent intent = new Intent(AddAlarmActivity.this, RingtoneActivity.class);
                                startActivityForResult(intent, 1);
                            }
                        }.start();

                    }
                });
            }

            protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                // RequestCode is 1 for RingtoneActivity
                if (requestCode == 1) {
                    if (resultCode == RESULT_OK) {
                        try {
                            // Used when Deezer session was not active when activity was started first time
                            if (data.getStringExtra(RESTART_ACTIVITY).equalsIgnoreCase("true")) {
                                Intent intent = new Intent(AddAlarmActivity.this, RingtoneActivity.class);
                                startActivityForResult(intent, 1);
                            }
                        } catch (NullPointerException ignored) {
                        }
                        type = data.getIntExtra(RingtoneActivity.RINGTONE_TYPE, -1);
                        uri = data.getStringExtra(RingtoneActivity.RINGTONE_URI);
                        deezerRingtoneId = data.getLongExtra(RingtoneActivity.RINGTONE_ID_STRING, -1);
                        ringtoneName = data.getStringExtra(RingtoneActivity.RINGTONE_NAME);
                        artist = data.getStringExtra(RingtoneActivity.RINGTONE_ARTIST);
                        if (artist == null) {
                            artist = "";
                        }
                        if (ringtoneName != null) {
                            setRingtoneName(ringtoneName);
                            ((TextView) findViewById(R.id.ringtone)).setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf"));
                        } else {
                             ringtoneName = "Default ringtone";
                            ((TextView) findViewById(R.id.ringtone)).setText(ringtoneName);
                        }
                    }
                }
            }

            private void setRingtoneName(String alarmToneName) {
                String ringtoneName = alarmToneName;
                if (type == 0) {
                    ringtoneName = ringtoneName.concat(" Ringtone");
                } else if (type == 1) {
                    ringtoneName = ringtoneName.concat(" Playlist");
                } else if (type == 3) {
                    ringtoneName = ringtoneName.concat(" Artist Radio");
                } else if (type == 4) {
                    ringtoneName = ringtoneName.concat(" Radio");
                } else if (type == 2) {
                    if (artist != null)
                        ringtoneName = ringtoneName.concat(" by " + artist);
                }
                if (alarmToneName.length() <2) ringtoneName = "Default ringtone";
                ((TextView) findViewById(R.id.ringtone)).setText(ringtoneName);
                ((TextView) findViewById(R.id.ringtone)).setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf"));

            }
        }


