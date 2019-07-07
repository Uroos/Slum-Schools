package com.uroos.myapplication;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.uroos.myapplication.model.School;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.uroos.myapplication.utils.Utils.isOnline;

public class InputActivity extends AppCompatActivity {
    private String userId;
    private double latitude;
    private double longitude;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    SharedPreferences sharedPref;
    String chosenCountry = "Pakistan";

    @BindView(R.id.btnInputSave)
    Button btnSave;
    @BindView(R.id.imgbtnTimeStart)
    ImageButton imageButtonStart;
    @BindView(R.id.imgbtnTimeEnd)
    ImageButton imageButtonEnd;
    @BindView(R.id.edInputName)
    EditText editTextName;
    @BindView(R.id.edInputAddress)
    EditText editTextAddress;
    @BindView(R.id.edInputEmail)
    EditText editTextEmail;
    @BindView(R.id.edStartTime)
    EditText editTextStartTime;
    @BindView(R.id.edEndTime)
    EditText editTextEndTime;
    @BindView(R.id.edPhone)
    EditText editTextPhone;
    Spinner spinnerFrom;
    Spinner spinnerTo;

    private String name;
    private String address;
    private String email;
    private String startTime;
    private String endTime;
    private String phone;
    private int hour, minute;
    private String strHrsToShow = "";
    private String dayFrom;
    private String dayTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        ButterKnife.bind(InputActivity.this);

        //sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //chosenCountry = sharedPref.getString("key_country", null);
        //Toast.makeText(this, "saved country input activity is: " + chosenCountry, Toast.LENGTH_SHORT).show();

        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.days_array, android.R.layout.simple_spinner_dropdown_item);
        // Drop down layout style - list view with radio button
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dayFrom = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dayTo = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // if(savedInstanceState==null) {
        if (getIntent() != null && getIntent().hasExtra("school")) {
            userId = getIntent().getStringExtra(getString(R.string.key_uid));
            latitude = getIntent().getDoubleExtra(getString(R.string.key_latitude), 0);
            longitude = getIntent().getDoubleExtra(getString(R.string.key_longitude), 0);
            Bundle data = getIntent().getExtras();
            School school = data.getParcelable(getString(R.string.key_bundle_school));

            editTextName.setText(school.getName());
            editTextAddress.setText(school.getAddress());
            editTextEmail.setText(school.getEmail());
            editTextStartTime.setText(school.getStart_time());
            editTextEndTime.setText(school.getEnd_time());
            editTextPhone.setText(school.getPhone_no());
            spinnerFrom.setSelection(adapter.getPosition(school.getDayFrom()));
            spinnerTo.setSelection(adapter.getPosition(school.getDayTo()));

        } else if (getIntent() != null) {
            userId = getIntent().getStringExtra(getString(R.string.key_uid));
            latitude = getIntent().getDoubleExtra(getString(R.string.key_latitude), 0);
            longitude = getIntent().getDoubleExtra(getString(R.string.key_longitude), 0);
        }

        // The toolbar appears in the layout but it's not functioning as the app bar.
        // To apply the toolbar as the app bar, call setSupportActionBar() and pass the Toolbar object from your layout.
        Toolbar toolbar = findViewById(R.id.toolbarInput);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(toolbar);
        // Now add the button that opens the navigation drawer.
        // Enable the app bar's "home" button by calling setDisplayHomeAsUpEnabled(true)
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);

        // To resolve the error of not getting instance of database
        // Solution from: https://stackoverflow.com/questions/40081539/default-firebaseapp-is-not-initialized
        // Version com.google.gms:google-services:4.1.0 was crashing but com.google.gms:google-services:4.0.1 worked
        // so it was changed in the build.gradle file.
        FirebaseApp.initializeApp(this);
        // Main access point for the database
        firebaseDatabase = FirebaseDatabase.getInstance();
        // Using the access point, get access to a specific place in database
        //databaseReference = firebaseDatabase.getReference().child(chosenCountry).child(userId);
        databaseReference = firebaseDatabase.getReference().child(chosenCountry).child(userId);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getIntent() != null && getIntent().hasExtra("school")) {
            menu.findItem(R.id.delete).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete_menu, menu);
        // show the button when some condition is true
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.delete:
                createDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete the school data?")
                .setMessage("Going ahead will delete all the saved data about your school.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseReference.removeValue();
                        Toast.makeText(InputActivity.this, "Item deleted.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    /**
     * This mathod saves to cloud only if user is online.
     *
     * @param view
     */
    @OnClick(R.id.btnInputSave)
    public void save(View view) {
        name = editTextName.getText().toString();
        address = editTextAddress.getText().toString();
        email = editTextEmail.getText().toString();
        startTime = editTextStartTime.getText().toString();
        endTime = editTextEndTime.getText().toString();
        phone = editTextPhone.getText().toString();

        if (isOnline(this)) {
            if (!(TextUtils.isEmpty(name))
                    && !(TextUtils.isEmpty(address))
                    && !(TextUtils.isEmpty(email))
                    && !(TextUtils.isEmpty(startTime))
                    && !(TextUtils.isEmpty(endTime))
                    && !(TextUtils.isEmpty(phone))
                    && !(TextUtils.isEmpty(dayFrom))
                    && !TextUtils.isEmpty(dayTo)
            ) {
                School school = new School(name, address, email, startTime, endTime, phone, latitude, longitude, dayFrom, dayTo);
                databaseReference.setValue(school);
                Toast.makeText(InputActivity.this, "School location is saved!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "All fields must be filled.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "You need to be online to save.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.imgbtnTimeStart)
    public void timeStart() {
        // Get Current Time
        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String AM_PM = " AM";
                        String mm_precede = "";
                        if (hourOfDay >= 12) {
                            AM_PM = " PM";
                            if (hourOfDay >= 13 && hourOfDay < 24) {
                                hourOfDay -= 12;
                            } else {
                                hourOfDay = 12;
                            }
                        } else if (hourOfDay == 0) {
                            hourOfDay = 12;
                        }
                        if (minute < 10) {
                            mm_precede = "0";
                        }
                        strHrsToShow = hourOfDay + ":" + mm_precede + minute + AM_PM;
                        editTextStartTime.setText(strHrsToShow);
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }

    @OnClick(R.id.imgbtnTimeEnd)
    public void timeEnd() {
        // Get Current Time
        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String AM_PM = " AM";
                        String mm_precede = "";
                        if (hourOfDay >= 12) {
                            AM_PM = " PM";
                            if (hourOfDay >= 13 && hourOfDay < 24) {
                                hourOfDay -= 12;
                            } else {
                                hourOfDay = 12;
                            }
                        } else if (hourOfDay == 0) {
                            hourOfDay = 12;
                        }
                        if (minute < 10) {
                            mm_precede = "0";
                        }
                        strHrsToShow = hourOfDay + ":" + mm_precede + minute + AM_PM;
                        editTextEndTime.setText(strHrsToShow);
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }
}
