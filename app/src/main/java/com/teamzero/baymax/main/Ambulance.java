package com.teamzero.baymax.main;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.teamzero.baymax.initial.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.teamzero.baymax.initial.R;
import com.teamzero.baymax.map.MapActivity;
import com.teamzero.baymax.volley.CustomJsonArrayRequest;
import com.teamzero.baymax.volley.CustomRequest;
import com.teamzero.baymax.volley.SingletonRequestQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Ambulance extends AppCompatActivity {


    public class AmbulanceInfo {
        String LicenseNo, Company, Driver, Status, dEmail, iNo, ContactNo, Longitude, Latitude;

        public void create(String licenseNo, String company, String name, String contactNo, String status, String longitude, String latitude, String email, String ino) {
            LicenseNo = licenseNo;
            Driver = name;
            Status = status;
            Company = company;
            ContactNo = contactNo;
            Status = status;
            Longitude = longitude;
            Latitude = latitude;
        }

        public String getLicenseNo() {
            return LicenseNo;
        }

        public String getDriver() {
            return Driver;
        }

        public String getCompany() {
            return Company;
        }

        public String getdEmail() {
            return dEmail;
        }

        public String getiNo() {
            return iNo;
        }

        public String getContactNo() {
            return ContactNo;
        }

        public String getStatus() {
            return Status;
        }

        public String getLongitude() {
            return Longitude;
        }

        public String getLatitude() {
            return Latitude;
        }
    }

    private SwipeRefreshLayout refresh;
    private AmbulanceInfo ambulanceInfo[] = new AmbulanceInfo[20];
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Context context;

    public Ambulance(Context context) {
        this.context = context;
    }


    public void startActivity() {
        refresh = ((Activity) context).findViewById(R.id.swipe_refresh_ambulance);
        refresh.setOnRefreshListener((SwipeRefreshLayout.OnRefreshListener) context);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startInstituteActivity();
            }
        });
        mRecyclerView = ((Activity) context).findViewById(R.id.ambulanceview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        startInstituteActivity();
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, this.mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        showInfoDialog(ambulanceInfo[position].getLicenseNo(), ambulanceInfo[position].getCompany(), ambulanceInfo[position].getDriver(), ambulanceInfo[position].getContactNo(), ambulanceInfo[position].getStatus(), ambulanceInfo[position].getLongitude(), ambulanceInfo[position].getLatitude(), ambulanceInfo[position].getdEmail(), ambulanceInfo[position].getiNo());
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );
    }

    private void showInfoDialog(String licenseNo, String company, String driver, String contactNo, String status, String longitude, String latitude, String email, String ino) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.doctor_dialog, null);
        alertDialogBuilder.setView(view);
        view.findViewById(R.id.doc4).setVisibility(View.GONE);
        view.findViewById(R.id.doc5).setVisibility(View.GONE);
        view.findViewById(R.id.doc6).setVisibility(View.GONE);
        TextView name, cont, add;
        name = (TextView) view.findViewById(R.id.doc_dialog_name);
        cont = (TextView) view.findViewById(R.id.doc_dialog_cont);
        add = (TextView) view.findViewById(R.id.doc_dialog_spec);
        name.setText(driver);
        cont.setText(contactNo);
        cont.setOnClickListener(e -> {
            dialNumber(contactNo);
        });
        if (status.equals("0")) status = "Available";
        else {
            status = "Occupied";
            add.setOnClickListener(e -> {
                Intent intent = new Intent((Activity) context, MapActivity.class);
                intent.putExtra("lng", longitude);
                intent.putExtra("lat", latitude);
                ((Activity) context).startActivity(intent);
            });
        }
        add.setText(status);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void startInstituteActivity() {
        Map map = new HashMap<String, String>();
        map.put("query", "SELECT * FROM AMBULANCE");
        getData("ambulance", "http://zero.ourcuet.com/BAYMAX/runQuery.php", map);
    }

    public void startSearchActivity(Map map) {
        getData("ambulance", "http://zero.ourcuet.com/BAYMAX/searchQuery.php", map);
    }

    private void getData(String type, String url, Map map) {
        refresh.setRefreshing(true);
        CustomJsonArrayRequest runQuery = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {
            mAdapter = new MyAdapter(type, response);
            mRecyclerView.setAdapter(mAdapter);
            refresh.setRefreshing(false);
            for (int i = 0; i < response.length(); i++) {
                ambulanceInfo[i] = new AmbulanceInfo();
                try {
                    JSONObject jsonObject = response.getJSONObject(i);
                    ambulanceInfo[i].create(jsonObject.getString("LicenseNo"), jsonObject.getString("Company"), jsonObject.getString("Driver"), jsonObject.getString("ContactNo"), jsonObject.getString("Status"),
                            jsonObject.getString("Longitude"), jsonObject.getString("Latitude"), jsonObject.getString("Email"), jsonObject.getString("INo"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }, error -> {
        });
        SingletonRequestQueue.getInstance(this).addToRequestQueue(runQuery);
    }

    private void dialNumber(String number) {
        String phoneNumber = String.format("tel: %s", number);
        // Create the intent.
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        // Set the data for the intent as the phone number.
        dialIntent.setData(Uri.parse(phoneNumber));
        // If package resolves to an app, send intent.
        if (dialIntent.resolveActivity(context.getPackageManager()) != null) {
            ((Activity)context).startActivity(dialIntent);
        } else {
            Toast.makeText(context, "Can not make call", Toast.LENGTH_SHORT).show();
        }
    }
}
