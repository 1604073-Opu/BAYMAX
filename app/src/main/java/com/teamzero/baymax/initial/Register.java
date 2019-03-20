package com.teamzero.baymax.initial;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.teamzero.baymax.volley.*;
import com.teamzero.baymax.volley.SingletonRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Register extends Activity {

    String email, name, password1, password2, address, number, dob, gender = null;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EditText mail, nam, pass1, pass2, add, no;
        Button date;
        mail = findViewById(R.id.regemail);
        nam = findViewById(R.id.nam);
        pass1 = findViewById(R.id.password1);
        pass2 = findViewById(R.id.password2);
        add = findViewById(R.id.address);
        no = findViewById(R.id.number);
        date = findViewById(R.id.dob);
        setDateOfBirth(date);
        Button register = findViewById(R.id.register);
        CheckBox male = findViewById(R.id.male);
        CheckBox female = findViewById(R.id.female);
        male.setOnClickListener((View e) -> {
            gender = "Male";
            if (((CheckBox) female).isChecked()) ((CheckBox) female).setChecked(false);

        });
        female.setOnClickListener(e -> {
            gender = "Female";
            if (((CheckBox) male).isChecked()) ((CheckBox) male).setChecked(false);
        });
        register.setOnClickListener(e -> {
            email = mail.getText().toString();
            name = nam.getText().toString();
            password1 = pass1.getText().toString();
            password2 = pass2.getText().toString();
            address = add.getText().toString();
            number = no.getText().toString();
            dob = date.getText().toString();
            if (checkValidity()) {
                progressDialog = ProgressDialog.show(this, "", "Loading...", true);
                preRegister();
            }
        });
    }

    private boolean checkValidity() {
        if (email.length() == 0 || name.length() == 0 || password1.length() == 0 || password2.length() == 0 || address.length() == 0 || number.length() == 0 || dob.length() == 0 || gender == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password1.length() < 6) {
            Toast.makeText(this, "Password length must be greater or equals to 6", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password1.equals(password2)) {
            Toast.makeText(this, "Password must be same", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (number.length() < 11) {
            Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setDateOfBirth(Button dob) {
        dob.setInputType(InputType.TYPE_NULL);
        dob.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dpd = new DatePickerDialog(Register.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        if (year < mYear)
                            view.updateDate(mYear, mMonth, mDay);
                        if (monthOfYear < mMonth && year == mYear)
                            view.updateDate(mYear, mMonth, mDay);
                        if (dayOfMonth < mDay && year == mYear && monthOfYear == mMonth)
                            view.updateDate(mYear, mMonth, mDay);
                        dob.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    }, mYear, mMonth, mDay);
            dpd.show();
        });
    }

    private void preRegister() {
        Map map = new HashMap<String, String>();
        map.put("email", email);
        map.put("password", " ");
        String url = "http://zero.ourcuet.com/BAYMAX/login.php";
        CustomRequest loginRequest = new CustomRequest(Request.Method.POST, url, map, response -> {
            try {
                if (response.getBoolean("mailFound") == false) {
                    sendMail();
                } else {
                    progressDialog.cancel();
                    Toast.makeText(this, "Email Already Exists", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
        });
        SingletonRequestQueue.getInstance(this).addToRequestQueue(loginRequest);
    }

    private void sendMail() {
        Random random = new Random();
        int code = random.nextInt(99999 - 10000) + 10000;
        Map map = new HashMap<String, String>();
        String url = "http://zero.ourcuet.com/BAYMAX/sendmail.php";
        map.put("email", email);
        map.put("name", name);
        map.put("code", Integer.toString(code));
        CustomRequest mailRequest = new CustomRequest(Request.Method.POST, url, map, response -> {
        }, error -> {
        });
        SingletonRequestQueue.getInstance(this).addToRequestQueue(mailRequest);
        mailVerification(Integer.toString(code));
    }

    private void mailVerification(String sentCode) {
        progressDialog.cancel();
        setContentView(R.layout.verification);
        TextView note = findViewById(R.id.note);
        note.setText("A verification code is sent to " + email);
        EditText getcode = findViewById(R.id.code);
        Button verify = findViewById(R.id.verify);
        verify.setOnClickListener(e -> {
            String code = getcode.getText().toString();
            if (code.equals(sentCode)) {
                progressDialog = ProgressDialog.show(this, "", "Loading...", true);
                onRegister();
            } else if (!code.equals(sentCode))
                Toast.makeText(this, "Wrong verification code", Toast.LENGTH_SHORT).show();
        });
        Button resend = findViewById(R.id.resend);
        resend.setOnClickListener(e -> {
            progressDialog = ProgressDialog.show(this, "", "Loading...", true);
            Toast.makeText(this, "Code sent", Toast.LENGTH_SHORT).show();
            sendMail();
        });
    }

    private void onRegister() {
        Map map = new HashMap<String, String>();
        String url = "http://zero.ourcuet.com/BAYMAX/signup.php";
        map.put("email", email);
        map.put("name", name);
        map.put("password", password1);
        map.put("dob", dob);
        map.put("address", address);
        map.put("no", number);
        map.put("sex", gender);
        CustomRequest registerRequest = new CustomRequest(Request.Method.POST, url, map, response -> {
            progressDialog.cancel();
            try {
                Toast.makeText(this, response.getString("notice"), Toast.LENGTH_LONG).show();
                if (response.getBoolean("status") == true) {
                    progressDialog.cancel();
                    finish();
                    startActivity(new Intent(this, Login.class));
                } else progressDialog.cancel();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
        });
        SingletonRequestQueue.getInstance(this).addToRequestQueue(registerRequest);

    }
}
