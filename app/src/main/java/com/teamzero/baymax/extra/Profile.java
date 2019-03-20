package com.teamzero.baymax.extra;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.teamzero.baymax.initial.*;
import com.teamzero.baymax.volley.CustomJsonArrayRequest;
import com.teamzero.baymax.volley.SingletonRequestQueue;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    String name, password, address, dob, contc;
    ProgressDialog pd;
    boolean edit = false;
    EditText nam, pass, add, no;
    Button date, button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        pd = ProgressDialog.show(this, "Loading", "Please wait...", true);
        getUserProfile();
    }

    private void setValues() {
        nam = findViewById(R.id.profilename);
        nam.setText(name);
        pass = findViewById(R.id.profilepass);
        pass.setText(password);
        pass.setVisibility(View.INVISIBLE);
        add = findViewById(R.id.profileaddress);
        add.setText(address);
        no = findViewById(R.id.profilenum);
        no.setText(contc);
        date = findViewById(R.id.profiledob);
        date.setText(dob);
        setEditable(false);
        button = findViewById(R.id.profilebutton);
        button.setText("Edit");
        setDateOfBirth(date);
        button.setOnClickListener(e -> {
            if (!edit) {
                changeState();
            } else {
                name = nam.getText().toString();
                password = pass.getText().toString();
                address = add.getText().toString();
                contc = no.getText().toString();
                dob = date.getText().toString();
                if (password.length() < 6)
                    Toast.makeText(this, "Password length must be greater than 5", Toast.LENGTH_SHORT).show();
                else if (contc.length() < 10)
                    Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
                else if (name.length() > 0 && dob.length() > 0&&address.length()>0) {

                    pd = ProgressDialog.show(this, "Loading", "Please wait...", true);
                    updateData();
                }
                edit = false;
                setEditable(false);
                Snackbar.make(findViewById(R.id.profileview), "Profile Updated", Snackbar.LENGTH_LONG);
                pass.setVisibility(View.INVISIBLE);
                button.setText("Edit");
            }
        });
    }

    private void getUserProfile() {
        Map map = new HashMap<String, String>();
        map.put("query", "SELECT USER.Name,USER.DoB,USER.Address,USER.ContactNo,LOGIN.Password FROM USER,LOGIN WHERE USER.Email='" + Userinfo.getInstance().getEmail() + "' AND LOGIN.Email='" + Userinfo.getInstance().getEmail() + "'");
        String url = "http://zero.ourcuet.com/BAYMAX/runQuery.php";
        CustomJsonArrayRequest jsonArrayRequest = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {
            try {
                JSONObject jsonObject = response.getJSONObject(0);
                name = jsonObject.getString("Name");
                dob = jsonObject.getString("DoB");
                contc = jsonObject.getString("ContactNo");
                address = jsonObject.getString("Address");
                password = jsonObject.getString("Password");
                pd.cancel();
                setValues();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> {
        });
        SingletonRequestQueue.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

    private void updateData() {
        Map map = new HashMap<String, String>();
        map.put("query", "UPDATE USER,LOGIN SET LOGIN.Password='" + password + "',USER.Name='" + name + "',USER.DoB='" + dob + "',USER.ContactNo='" + contc + "',USER.Address='" + address + "' WHERE USER.Email='" + Userinfo.getInstance().getEmail() + "' AND LOGIN.Email='" + Userinfo.getInstance().getEmail() + "'");
        String url = "http://zero.ourcuet.com/BAYMAX/runQuery.php";
        CustomJsonArrayRequest jsonArrayRequest = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {
            pd.cancel();
            Snackbar.make(findViewById(R.id.profileview), "Profile Updated", Snackbar.LENGTH_LONG).show();
        }, error -> {
            Snackbar.make(findViewById(R.id.profileview), "Try again later", Snackbar.LENGTH_LONG).show();
        });
        SingletonRequestQueue.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

    private void setDateOfBirth(Button dob) {
        dob.setInputType(InputType.TYPE_NULL);
        dob.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dpd = new DatePickerDialog(this,
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

    private void setEditable(boolean flag) {
        nam.setEnabled(flag);
        add.setEnabled(flag);
        no.setEnabled(flag);
        date.setEnabled(flag);
    }

    private void changeState() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Your Password");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (password.equals(input.getText().toString())) {
                    button.setText("Save");
                    edit = true;
                    setEditable(true);
                    pass.setVisibility(View.VISIBLE);
                } else
                    Snackbar.make(findViewById(R.id.profileview), "Wrong Password", Snackbar.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
