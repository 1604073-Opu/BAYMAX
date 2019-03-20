package com.teamzero.baymax.initial;

import com.android.volley.Request;
import com.teamzero.baymax.main.*;
import com.teamzero.baymax.volley.CustomRequest;
import com.teamzero.baymax.volley.SingletonRequestQueue;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Login extends Activity {

    private Button login, forgot, signup;
    ProgressDialog progressDialog;
    private int backCount = 0;
    private boolean toExit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.login);
        forgot = findViewById(R.id.forgot);
        signup = findViewById(R.id.signup);
        buttonClicks();
    }

    private void buttonClicks() {
        login.setOnClickListener(e -> {
            toExit = false;
            EditText mail = findViewById(R.id.logemail);
            EditText pass = findViewById(R.id.logpassword);
            String email = mail.getText().toString();
            String password = pass.getText().toString();
            if (email.length() != 0 && password.length() != 0 && email.contains("@") && email.contains(".")) {
                progressDialog = ProgressDialog.show(this, "", "Loading...", true);
                onLogin(email, password);
            } else if (email.length() == 0 || password.length() == 0) {
                makeToast("Please fill in all fields");
            } else if (!email.contains("@") || !email.contains(".")) {
                makeToast("Enter a valid Email");
            }
        });

        forgot.setOnClickListener(e -> {
            toExit = false;
            getMail();
        });

        signup.setOnClickListener(e -> {
            toExit = false;
            finish();
            startActivity(new Intent(this, Register.class));
        });
    }

    private void getMail() {
        setContentView(R.layout.prerecovery);
        EditText mail = findViewById(R.id.setmail);
        Button go = findViewById(R.id.go);
        go.setOnClickListener(e1 -> {
            String temp = mail.getText().toString();
            if (temp.length() > 0 && temp.contains("@") && temp.contains(".")) {
                progressDialog = ProgressDialog.show(this, "", "Loading...");
                Map map = new HashMap<String, String>();
                map.put("email", temp);
                map.put("password", " ");
                String url = "http://zero.ourcuet.com/BAYMAX/login.php";
                CustomRequest loginRequest = new CustomRequest(Request.Method.POST, url, map, response -> {
                    try {
                        if (response.getBoolean("mailFound")) {
                            sendMail(temp);
                        } else {
                            progressDialog.cancel();
                            Toast.makeText(this, "Can not find this email in our server", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    progressDialog.cancel();
                    makeToast("Internal Error");
                });
                SingletonRequestQueue.getInstance(this).addToRequestQueue(loginRequest);
            } else Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
        });
    }

    private void sendMail(String email) {
        Random random = new Random();
        int code = random.nextInt(99999 - 10000) + 10000;
        Map map = new HashMap<String, String>();
        String url = "http://zero.ourcuet.com/BAYMAX/sendmail.php";
        map.put("email", email);
        map.put("name", " ");
        map.put("code", Integer.toString(code));
        CustomRequest mailRequest = new CustomRequest(Request.Method.POST, url, map, response -> {
        }, error -> {
        });
        SingletonRequestQueue.getInstance(this).addToRequestQueue(mailRequest);
        mailVerification(email, Integer.toString(code));
    }

    private void mailVerification(String email, String sentCode) {
        progressDialog.cancel();
        setContentView(R.layout.verification);
        TextView note = findViewById(R.id.note);
        note.setText("A verification code is sent to " + email);
        EditText getcode = findViewById(R.id.code);
        Button verify = findViewById(R.id.verify);
        verify.setOnClickListener(e -> {
            String code = getcode.getText().toString();
            if (code.equals(sentCode)) {
                changePassword(email);
            } else if (!code.equals(sentCode))
                Toast.makeText(this, "Wrong verification code", Toast.LENGTH_SHORT).show();
        });
        Button resend = findViewById(R.id.resend);
        resend.setOnClickListener(e -> {
            progressDialog = ProgressDialog.show(this, "", "Loading...", true);
            Toast.makeText(this, "Code sent", Toast.LENGTH_SHORT).show();
            sendMail(email);
        });
    }

    private void changePassword(String email) {
        progressDialog.cancel();
        setContentView(R.layout.recovery);
        EditText p1, p2;
        p1 = findViewById(R.id.newpass);
        p2 = findViewById(R.id.confirmnewpass);
        Button submit = findViewById(R.id.submit);
        submit.setOnClickListener(e -> {
            String pass1, pass2;
            pass1 = p1.getText().toString();
            pass2 = p2.getText().toString();
            if (pass1.equals(pass2) && pass1.length() > 5) {
                progressDialog = ProgressDialog.show(this, "", "Loading...", true);
                updatePassword(email, pass1);
            } else if (!pass1.equals(pass2)) {
                Toast.makeText(this, "Password didn't match", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Password length must be greater or equal to 6", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePassword(String email, String password) {
        Map map = new HashMap<String, String>();
        map.put("query", "UPDATE LOGIN SET Password='" + password + "' WHERE email='" + email + "'");
        String url = "http://zero.ourcuet.com/BAYMAX/nullReturnQuery.php";
        CustomRequest customRequest = new CustomRequest(Request.Method.POST, url, map, response -> {
            progressDialog.cancel();
            Toast.makeText(this, "Password Changed. PLease Login", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(this, Login.class));
        }, error -> {
            progressDialog.cancel();
            Toast.makeText(this, "Please try again later", Toast.LENGTH_SHORT).show();
        });
        SingletonRequestQueue.getInstance(this).addToRequestQueue(customRequest);
    }

    private void onLogin(String email, String password) {
        Map map = new HashMap<String, String>();
        map.put("email", email);
        map.put("password", password);
        String url = "http://zero.ourcuet.com/BAYMAX/login.php";
        CustomRequest loginRequest = new CustomRequest(Request.Method.POST, url, map, response -> {
            try {
                if (response.getBoolean("mailFound") && response.getBoolean("passFound")) {
                    progressDialog.cancel();
                    finish();
                    saveUserData(email, response);
                    startNextActivity(Home.class);
                } else progressDialog.cancel();
                makeToast(response.getString("notice"));
            } catch (JSONException e) {
                progressDialog.cancel();
                makeToast("Internal Error");
            }
        }, error -> {
            progressDialog.cancel();
            makeToast("Internal Error");
        });
        SingletonRequestQueue.getInstance(this).addToRequestQueue(loginRequest);
    }

    private void saveUserData(String email, JSONObject response) {

        SharedPreferences sharedPreferences = getSharedPreferences("com.teamzero.baymax", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        sharedPreferences.edit().clear().commit();
        editor.putString("email", email);
        editor.putBoolean("status", true);
        try {
            editor.putString("name", response.getString("name"));
            editor.putBoolean("user", response.getBoolean("user"));
            editor.putBoolean("admin", response.getBoolean("admin"));
            editor.putBoolean("doctor", response.getBoolean("doctor"));
            editor.putBoolean("pharmacy", response.getBoolean("pharmacy"));
            editor.putBoolean("institute", response.getBoolean("institute"));
            editor.putBoolean("driver", response.getBoolean("driver"));
            Userinfo.getInstance().setActive(email, response.getString("name"), true, response.getBoolean("user")
                    , response.getBoolean("admin"), response.getBoolean("doctor"), response.getBoolean("doctor")
                    , response.getBoolean("institute"), response.getBoolean("driver"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        editor.apply();
    }

    private void makeToast(String notice) {
        Toast.makeText(this, notice, Toast.LENGTH_LONG).show();
    }

    private void startNextActivity(Class<Home> homeClass) {
        startActivity(new Intent(this, homeClass));
    }

    @Override
    public void onBackPressed() {
        if (backCount == 0 && toExit) {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            backCount++;
        } else if (toExit) {
            finishAffinity();
            System.exit(0);
        } else {
            toExit = true;
            super.onBackPressed();
        }
    }
}

