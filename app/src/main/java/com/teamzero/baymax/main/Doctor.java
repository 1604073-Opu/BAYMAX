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

public class Doctor extends AppCompatActivity {


    public class DoctorInfo {
        String RegistrationNo, Name, ContactNo, Address, INo, WorkingPlace, Specialty, Longitude, Latitude, Fee;

        public void create(String registrationNo, String name, String contactNo, String address, String workingPlace, String iNo, String specialty, String longitude, String latitude, String fee) {
            RegistrationNo = registrationNo;
            Name = name;
            ContactNo = contactNo;
            Address = address;
            INo = iNo;
            WorkingPlace = workingPlace;
            Specialty = specialty;
            Longitude = longitude;
            Latitude = latitude;
            Fee = fee;
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

        public String getINo() {
            return INo;
        }

        public String getWorkingPlace() {
            return WorkingPlace;
        }

        public String getSpecialty() {
            return Specialty;
        }

        public String getLongitude() {
            return Longitude;
        }

        public String getLatitude() {
            return Latitude;
        }

        public String getFee() {
            return Fee;
        }
    }

    private SwipeRefreshLayout refresh;
    private DoctorInfo doctorInfo[] = new DoctorInfo[20];
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Context context;

    public Doctor(Context context) {
        this.context = context;
    }


    public void startActivity() {
        refresh = ((Activity) context).findViewById(R.id.swipe_refresh_doctor);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startDoctorActivity();
            }
        });
        mRecyclerView = ((Activity) context).findViewById(R.id.doctorview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        startDoctorActivity();
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, this.mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        showInfoDialog(doctorInfo[position].getRegistrationNo(), doctorInfo[position].getName(), doctorInfo[position].getSpecialty(), doctorInfo[position].getContactNo(),
                                doctorInfo[position].getAddress(), doctorInfo[position].getINo(), doctorInfo[position].getWorkingPlace(), doctorInfo[position].getFee(), doctorInfo[position].getLongitude(), doctorInfo[position].getLatitude());
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );
    }

    public void startSearchActivity(Map map) {
        getData("doctor", "http://zero.ourcuet.com/BAYMAX/searchQuery.php", map);
    }

    public void showInfoDialog(String dNo, String docName, String docSpec, String docCont, String docAdd, String iNo, String docPlace, String docFee, String lng, String lat) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.doctor_dialog, null);
        alertDialogBuilder.setView(view);
        TextView name, spec, cont, add, place, fee;
        name = (TextView) view.findViewById(R.id.doc_dialog_name);
        spec = (TextView) view.findViewById(R.id.doc_dialog_spec);
        cont = (TextView) view.findViewById(R.id.doc_dialog_cont);
        add = (TextView) view.findViewById(R.id.doc_dialog_add);
        place = (TextView) view.findViewById(R.id.doc_dialog_place);
        fee = (TextView) view.findViewById(R.id.doc_dialog_fee);
        name.setText(docName);
        spec.setText(docSpec);
        cont.setText(docCont);
        cont.setOnClickListener(e -> {
            dialNumber(docCont);
        });
        add.setText(docAdd);
        place.setText(docPlace);
        place.setOnClickListener(e -> {
            Map map = new HashMap<String, String>();
            map.put("query", "SELECT * FROM INSTITUTE WHERE RegistrationNo='" + iNo + "'");
            String url = "http://zero.ourcuet.com/BAYMAX/runQuery.php";
            CustomJsonArrayRequest runQuery = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {
                JSONObject jsonObject = null;
                try {
                    jsonObject = response.getJSONObject(0);
                    Institute institute = new Institute(context);
                    institute.showInfoDialog(jsonObject.getString("RegistrationNo"), jsonObject.getString("Name"),
                            jsonObject.getString("ContactNo"), jsonObject.getString("Address"), jsonObject.getString("Longitude"), jsonObject.getString("Latitude"));

                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }, error -> {

            });
            SingletonRequestQueue.getInstance(this).addToRequestQueue(runQuery);
        });
        fee.setText("Fee: " + docFee + " Tk");
        add.setOnClickListener(e -> {
            Intent intent = new Intent((Activity) context, MapActivity.class);
            intent.putExtra("lng", lng);
            intent.putExtra("lat", lat);
            ((Activity) context).startActivity(intent);
        });
        alertDialogBuilder.setPositiveButton("Make Appointment", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Toast.makeText(context, "Request Processing...", Toast.LENGTH_LONG).show();
                check(dNo, docName);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void check(String dNo, String docName) {

        Map map = new HashMap<String, String>();
        map.put("query", "SELECT * FROM treated_by WHERE DNo='" + dNo + "' AND Email='" + Userinfo.getInstance().getEmail() + "'");
        String url = "http://zero.ourcuet.com/BAYMAX/runQuery.php";
        CustomJsonArrayRequest customRequest = new CustomJsonArrayRequest(Request.Method.POST, url, map, (JSONArray response) -> {
            System.out.println("treated by size " + response.length());
            if (response.length() == 0) {
                Map map1 = new HashMap<String, String>();
                map1.put("query", "SELECT * FROM Requests WHERE Info='" + dNo + "' AND Email='" + Userinfo.getInstance().getEmail() + "' AND Type=1");
                CustomJsonArrayRequest customRequest1 = new CustomJsonArrayRequest(Request.Method.POST, url, map1, (JSONArray response1) -> {
                    System.out.println("pending size " + response1.length());
                    if (response1.length() == 0) {
                        makeAppointment(dNo, docName);
                    } else confirm("Pending. Can not request new Appointment");
                }, (VolleyError error) -> {
                    System.out.println(error);
                    confirm("Sorry. Please Try again later");
                });
                SingletonRequestQueue.getInstance(context).addToRequestQueue(customRequest1);
            } else confirm("Pending. Can not request new Appointment");
        }, (VolleyError error) -> {
            System.out.println(error);
            confirm("Sorry. Please Try again later");
        });
        SingletonRequestQueue.getInstance(context).addToRequestQueue(customRequest);

    }

    private void makeAppointment(String dNo, String docName) {
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
        aBuilder.setTitle("Confirm Request");
        aBuilder.setMessage("Are you sure to request an appointment?");
        aBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        aBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "Request Processing...", Toast.LENGTH_SHORT).show();
                Map map = new HashMap<String, String>();
                map.put("query", "INSERT INTO Requests (Type,Info,Email,Text) VALUES (1,'" + dNo + "','" + Userinfo.getInstance().getEmail() + "','" + docName + "')");
                String url = "http://zero.ourcuet.com/BAYMAX/runQuery.php";
                CustomJsonArrayRequest customRequest = new CustomJsonArrayRequest(Request.Method.POST, url, map, (JSONArray response) -> {
                    confirm("Pending...\n We will notify you later");
                }, (VolleyError error) -> {
                    System.out.println(error);
                    confirm("Sorry. Please Try again later");
                });
                SingletonRequestQueue.getInstance(context).addToRequestQueue(customRequest);

                dialog.dismiss();
            }
        });
        AlertDialog requestAlertDialog = aBuilder.create();
        requestAlertDialog.show();
    }

    private void confirm(String s) {
        AlertDialog.Builder confirm = new AlertDialog.Builder(context);
        confirm.setMessage(s);
        confirm.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog confirmAlertDialog = confirm.create();
        confirmAlertDialog.show();
    }

    private void startDoctorActivity() {
        Map map = new HashMap<String, String>();
        map.put("query", "SELECT DOCTOR.RegistrationNo,DOCTOR.Name,DOCTOR.ContactNo,DOCTOR.Address,DOCTOR.INo,DOCTOR.Specialty,DOCTOR.Longitude,DOCTOR.Latitude,DOCTOR.Fee, INSTITUTE.Name AS IName FROM DOCTOR INNER JOIN INSTITUTE ON INSTITUTE.RegistrationNo=DOCTOR.INo");
        getData("doctor", "http://zero.ourcuet.com/BAYMAX/runQuery.php", map);
    }

    private void getData(String type, String url, Map map) {
        refresh.setRefreshing(true);
        CustomJsonArrayRequest runQuery = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {
            mAdapter = new MyAdapter(type, response);
            mRecyclerView.setAdapter(mAdapter);
            refresh.setRefreshing(false);
            System.out.println("Data size: " + response.length());
            for (int i = 0; i < response.length(); i++) {
                doctorInfo[i] = new DoctorInfo();
                try {
                    JSONObject jsonObject = response.getJSONObject(i);
                    doctorInfo[i].create(jsonObject.getString("RegistrationNo"), jsonObject.getString("Name"),
                            jsonObject.getString("ContactNo"), jsonObject.getString("Address"), jsonObject.getString("IName"), jsonObject.getString("INo")
                            , jsonObject.getString("Specialty"), jsonObject.getString("Longitude"), jsonObject.getString("Latitude"),
                            jsonObject.getString("Fee"));
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
