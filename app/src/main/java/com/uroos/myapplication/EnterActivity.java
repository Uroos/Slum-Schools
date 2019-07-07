package com.uroos.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

public class EnterActivity extends AppCompatActivity {
    CountryCodePicker ccp;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        ccp = findViewById(R.id.ccp);
        // Use getDefaultSharedPreferences() to get the default shared preference file for the entire app.
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();

        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                String country = ccp.getSelectedCountryName();
                Toast.makeText(EnterActivity.this, "Updated: " + country, Toast.LENGTH_SHORT).show();
                editor.putString("key_country", country);
            }
        });

    }

    public void enter(View view) {
        editor.commit();
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}
