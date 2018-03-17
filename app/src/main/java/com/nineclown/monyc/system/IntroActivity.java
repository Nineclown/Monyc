package com.nineclown.monyc.system;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.nineclown.monyc.R;
import com.nineclown.monyc.join.JoinActivity;
import com.nineclown.monyc.main.MainActivity;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("액티비티", "IntroActivity OnCreate");
        setContentView(R.layout.activity_intro);
        Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
        startActivity(intent);
        finish();
        /*

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("액티비티", "IntroActivity onDestroy");
    }
}
