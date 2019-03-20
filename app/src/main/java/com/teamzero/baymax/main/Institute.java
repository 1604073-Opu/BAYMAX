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

public class Institute extends AppCompatActivity {


    public class InstituteInfo {
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
    private InstituteInfo instituteInfo[] = new InstituteInfo[20];
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Context context;

    public Institute(Context context) {
        this.context = context;
    }


    public void startActivity() {
        refresh = ((Activity) context).findViewById(R.id.swipe_refresh_institute);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startInstituteActivity();
            }
        });
        mRecyclerView = ((Activity) context).findViewById(R.id.instituteview);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        startInstituteActivity();
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, this.mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        showInfoDialog(instituteInfo[position].getRegistrationNo(), instituteInfo[position].getName(), instituteInfo[position].getContactNo(), instituteInfo[position].getAddress(), instituteInfo[position].getLongitude(), instituteInfo[position].getLatitude());
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );
    }

    public void startSearchActivity(Map map) {
        getData("institute", "http://zero.ourcuet.com/BAYMAX/searchQuery.php", map);
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
        map.put("query", "SELECT * FROM INSTITUTE");
        getData("institute", "http://zero.ourcuet.com/BAYMAX/runQuery.php", map);
    }

    private void getData(String type, String url, Map map) {
        refresh.setRefreshing(true);
        CustomJsonArrayRequest runQuery = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {
            mAdapter = new MyAdapter(type, response);
            mRecyclerView.setAdapter(mAdapter);
            refresh.setRefreshing(false);
            for (int i = 0; i < response.length(); i++) {
                instituteInfo[i] = new InstituteInfo();
                try {
                    JSONObject jsonObject = response.getJSONObject(i);
                    instituteInfo[i].create(jsonObject.getString("RegistrationNo"), jsonObject.getString("Name"),
                            jsonObject.getString("ContactNo"), jsonObject.getString("Address"), jsonObject.getString("Longitude"), jsonObject.getString("Latitude"));
                    System.out.println(instituteInfo[i].Name);
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
