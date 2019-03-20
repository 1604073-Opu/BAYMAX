package com.teamzero.baymax.initial;

import com.teamzero.baymax.main.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.welcometune);
        mediaPlayer.start();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                startNextActivity();
            }
        }, 1000);
    }

    private void startNextActivity() {
        getUserData();
        if (Userinfo.getInstance().isActive() == true)
            startActivity(new Intent(this, Home.class));
        else
            startActivity(new Intent(this, Login.class));
    }

    private void getUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("com.teamzero.baymax", Context.MODE_PRIVATE);
        boolean active = sharedPreferences.getBoolean("status", false);
        String email = sharedPreferences.getString("email", "");
        boolean user=sharedPreferences.getBoolean("user",false);
        boolean admin=sharedPreferences.getBoolean("admin",false);
        boolean doctor=sharedPreferences.getBoolean("doctor",false);
        boolean pharmacy=sharedPreferences.getBoolean("pharmacy",false);
        boolean institute=sharedPreferences.getBoolean("institute",false);
        boolean driver=sharedPreferences.getBoolean("driver",false);
        String name=sharedPreferences.getString("name","");
        Userinfo.getInstance().setActive(email,name,active,user,admin,doctor,pharmacy,institute,doctor);
    }

}
