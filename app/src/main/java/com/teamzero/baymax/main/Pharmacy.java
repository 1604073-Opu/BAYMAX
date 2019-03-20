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

public class Pharmacy extends AppCompatActivity {


    public class PharmacyInfo {
        String RegistrationNo, Name, ContactNo, Address, Longitude, Latitude;

        public void create(String registrationNo, String name, String contactNo, String address, String longitude, String latitude) {
            RegistrationNo = registrationNo;
            Name = name;
            ContactNo = contactNo;
            Address = address;
            Longitude = longitude;
            Latitude = latitude;
        }

        public String getRegistrationNo() {
            return RegistrationNo;
        }

        public String getName() {
            return Name;
        }

        public String getContactNo() {
            return ContactNo;
        }

        public String getAddress() {
            return Address;
        }

        public String getLongitude() {
            return Longitude;
        }

        public String getLatitude() {
            return Latitude;
        }
    }

    private SwipeRefreshLayout refresh;
    private PharmacyInfo pharmacyInfo[] = new PharmacyInfo[20];
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Context context;

    public Pharmacy(Context context) {
        this.context = context;
    }


    public void startActivity() {
        refresh = ((Activity) context).findViewById(R.id.swipe_refresh_pharmacy);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startInstituteActivity();
            }
        });
        mRecyclerView = ((Activity) context).findViewById(R.id.pharmacyview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        startInstituteActivity();
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, this.mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        showInfoDialog(pharmacyInfo[position].getRegistrationNo(), pharmacyInfo[position].getName(), pharmacyInfo[position].getContactNo(), pharmacyInfo[position].getAddress(), pharmacyInfo[position].getLongitude(), pharmacyInfo[position].getLatitude());
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );
    }


    public void showInfoDialog(String iNo, String iName, String iCont, String iAdd, String iLong, String iLat) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.doctor_dialog, null);
        alertDialogBuilder.setView(view);
        TextView name, cont, add;
        name = (TextView) view.findViewById(R.id.doc_dialog_name);
        cont = (TextView) view.findViewById(R.id.doc_dialog_cont);
        add = (TextView) view.findViewById(R.id.doc_dialog_add);
        view.findViewById(R.id.doc2).setVisibility(View.GONE);
        view.findViewById(R.id.doc5).setVisibility(View.GONE);
        view.findViewById(R.id.doc6).setVisibility(View.GONE);
        name.setText(iName);
        cont.setText(iCont);
        cont.setOnClickListener(e -> {
            dialNumber(iCont);
        });
        add.setText(iAdd);
        add.setOnClickListener(e -> {
            Intent intent = new Intent((Activity) context, MapActivity.class);
            intent.putExtra("lng", iLong);
            intent.putExtra("lat", iLat);
            ((Activity) context).startActivity(intent);
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    private void startInstituteActivity() {
        Map map = new HashMap<String, String>();
        map.put("query", "SELECT * FROM PHARMACY");
        getData("pharmacy", "http://zero.ourcuet.com/BAYMAX/runQuery.php", map);
    }

    public void startSearchActivity(Map map) {
        getData("pharmacy", "http://zero.ourcuet.com/BAYMAX/searchQuery.php", map);
    }

    private void getData(String type, String url, Map map) {
        refresh.setRefreshing(true);
        CustomJsonArrayRequest runQuery = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {
            mAdapter = new MyAdapter(type, response);
            mRecyclerView.setAdapter(mAdapter);
            refresh.setRefreshing(false);
            for (int i = 0; i < response.length(); i++) {
                pharmacyInfo[i] = new PharmacyInfo();
                try {
                    JSONObject jsonObject = response.getJSONObject(i);
                    pharmacyInfo[i].create(jsonObject.getString("RegistrationNo"), jsonObject.getString("Name"),
                            jsonObject.getString("ContactNo"), jsonObject.getString("Address"), jsonObject.getString("Longitude"), jsonObject.getString("Latitude"));
                    System.out.println(pharmacyInfo[i].Name);
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
            ((Activity) context).startActivity(dialIntent);
        } else {
            Toast.makeText(context, "Can not make call", Toast.LENGTH_SHORT).show();
        }
    }
}

