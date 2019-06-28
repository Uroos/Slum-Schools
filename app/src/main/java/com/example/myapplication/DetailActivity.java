package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.model.School;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.myapplication.utils.Utils.body;
import static com.example.myapplication.utils.Utils.subject;

public class DetailActivity extends AppCompatActivity {
    private String name;
    private String address;
    private String email;
    private String dayFrom;
    private String dayTo;
    private String startTime;
    private String endTime;
    private String phone;
    private AdView mAdView;

    @BindView(R.id.tvDetailName)
    TextView tvName;
    @BindView(R.id.tvDetailAddress)
    TextView tvAddress;
    @BindView(R.id.tvDetailEmail)
    TextView tvEmail;
    @BindView(R.id.tvDetailDayFrom)
    TextView tvDayFrom;
    @BindView(R.id.tvDetailDayTo)
    TextView tvDayTo;
    @BindView(R.id.tvDetailStartTime)
    TextView tvStartTime;
    @BindView(R.id.tvDetailEndTime)
    TextView tvEndTime;
    @BindView(R.id.tvDetailPhone)
    TextView tvPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        // Use this for testing purpose "ca-app-pub-3940256099942544/6300978111"
        // My personal id "ca-app-pub-7680180921711744~8820543312"
        // Change here and in xml and in manifest
        MobileAds.initialize(this, "ca-app-pub-7680180921711744~8820543312");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // The toolbar appears in the layout but it's not functioning as the app bar.
        // To apply the toolbar as the app bar, call setSupportActionBar() and pass the Toolbar object from the layout.
        Toolbar toolbar = findViewById(R.id.toolbarInput);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(toolbar);
        // Now add the back arrow.
        // Enable the app bar's "home" button by calling setDisplayHomeAsUpEnabled(true)
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);


        if (savedInstanceState == null) {
            if (getIntent() != null) {
                Bundle data = getIntent().getExtras();
                School school = data.getParcelable(getString(R.string.key_bundle_school));
                name = school.getName();
                address = school.getAddress();
                email = school.getEmail();
                dayFrom = school.getDayFrom();
                dayTo = school.getDayTo();
                startTime = school.getStart_time();
                endTime = school.getEnd_time();
                phone = school.getPhone_no();
                populateUI(name, address, email, dayFrom, dayTo, startTime, endTime, phone);
            } else {
                finish();
            }
        } else {
            populateUI(savedInstanceState.getString(getString(R.string.key_name)),
                    savedInstanceState.getString(getString(R.string.key_address)),
                    savedInstanceState.getString(getString(R.string.key_email)),
                    savedInstanceState.getString(getString(R.string.key_dayfrom)),
                    savedInstanceState.getString(getString(R.string.key_dayto)),
                    savedInstanceState.getString(getString(R.string.key_starttime)),
                    savedInstanceState.getString(getString(R.string.key_endtime)),
                    savedInstanceState.getString(getString(R.string.key_phone)));
        }
    }

    private void populateUI(String name, String address, String email, String dayFrom, String dayTo, String startTime, String endTime, String phone) {
        tvName.setText(name);
        tvAddress.setText(address);
        tvEmail.setText(email);
        tvDayFrom.setText(dayFrom);
        tvDayTo.setText(dayTo);
        tvStartTime.setText(startTime);
        tvEndTime.setText(endTime);
        tvPhone.setText(phone);
    }

    @OnClick(R.id.btnDetailCall)
    public void makeACall() {
        // ACTION_DIAL-Opens the dialer or phone app but user must explicitly make a call.
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @OnClick(R.id.btnDetailEmail)
    public void sendEmail() {
        // Start intent to send mail
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @OnClick(R.id.btnDetailLike)
    public void like() {
        // Service is started to update the widget.
        Toast.makeText(this, "Added to the Widget!", Toast.LENGTH_SHORT).show();
        School school = new School(name, address, email, startTime, endTime, phone, 0, 0, "", "");
        SchoolUpdateService.startSchoolUpdate(this, school);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String name = tvName.getText().toString();
        String address = tvAddress.getText().toString();
        String email = tvEmail.getText().toString();
        String dayFrom = tvDayFrom.getText().toString();
        String dayTo = tvDayTo.getText().toString();
        String startTime = tvStartTime.getText().toString();
        String endTime = tvEndTime.getText().toString();
        String phone = tvPhone.getText().toString();

        outState.putString(getString(R.string.key_name), name);
        outState.putString(getString(R.string.key_address), address);
        outState.putString(getString(R.string.key_email), email);
        outState.putString(getString(R.string.key_dayfrom), dayFrom);
        outState.putString(getString(R.string.key_dayto), dayTo);
        outState.putString(getString(R.string.key_starttime), startTime);
        outState.putString(getString(R.string.key_endtime), endTime);
        outState.putString(getString(R.string.key_phone), phone);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //This call requires minSDK of 21
                finishAndRemoveTask();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
