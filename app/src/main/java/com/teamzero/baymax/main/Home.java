package com.teamzero.baymax.main;

import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.teamzero.baymax.extra.Developer;
import com.teamzero.baymax.extra.HelpAndSupport;
import com.teamzero.baymax.extra.Profile;
import com.teamzero.baymax.initial.*;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.teamzero.baymax.initial.R;
import com.teamzero.baymax.volley.CustomJsonArrayRequest;
import com.teamzero.baymax.volley.CustomRequest;
import com.teamzero.baymax.volley.SingletonRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    class Appointment {
        String DocName, DNo, Date, Time, SNo, TrxID;

        public void makeAppointment(String docName, String dNo, String date, String time, String sNo, String trxID) {
            DocName = docName;
            DNo = dNo;
            Date = date;
            Time = time;
            SNo = sNo;
            TrxID = trxID;
        }

        public String getDocName() {
            return DocName;
        }

        public String getSNo() {
            return SNo;
        }

        public String getDate() {
            return Date;
        }

        public String getTime() {
            return Time;
        }

        public String getDNo() {
            return DNo;
        }

        public String getTrxID() {
            return TrxID;
        }
    }

    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Appointment appointment[] = new Appointment[20];
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private NavigationView navigationView;
    private int mode;
    private Doctor doctor = new Doctor(Home.this);
    private Institute institute = new Institute(this);
    private Pharmacy pharmacy = new Pharmacy(this);
    private Ambulance ambulance = new Ambulance(this);
    private boolean locationPermission = false;
    private double userLat, userLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setContentGone();
        findViewById(R.id.custom_home_layout).setVisibility(View.VISIBLE);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_home);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0);
        TextView name = (TextView) view.findViewById(R.id.nav_name);
        name.setText(Userinfo.getInstance().getName());
        TextView mail = (TextView) view.findViewById(R.id.nav_email);
        mail.setText(Userinfo.getInstance().getEmail());
        navigationView.setNavigationItemSelectedListener(this);
        findViewById(R.id.appointmentview).setVisibility(View.GONE);
        findViewById(R.id.noticelayout).setVisibility(View.VISIBLE);
        ImageView uInfo = view.findViewById(R.id.nav_photo);
        uInfo.setOnClickListener(e -> {
            startActivity(new Intent(this, Profile.class));
        });
        ImageButton emergency = findViewById(R.id.emergencycall);
        emergency.setOnClickListener(e -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            View view1 = LayoutInflater.from(this).inflate(R.layout.emergency_dialog, null);
            alertDialogBuilder.setView(view1);
            TextView nineninenine, cont;
            nineninenine = view1.findViewById(R.id.number1);
            cont = view1.findViewById(R.id.number2);
            cont.setOnClickListener(e1 -> {
                Snackbar.make(findViewById(R.id.app_bar), "Turn on LOCATION for this service", Snackbar.LENGTH_LONG).show();
                getLocationPermission();
                getDeviceLoaction();
            });
            nineninenine.setOnClickListener(e2 -> {
                dialNumber("999");
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        });
        startHomeActivity();
        ItemClick();
    }


    @Override
    public void onRefresh() {
        TextView tv = findViewById(R.id.header_home);
        if (tv.getText().equals(" Requested Appointments")) getRequestData();
        else startHomeActivity();
    }

    private void ItemClick() {
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        showInfoDialog(appointment[position].getDocName(), appointment[position].getDate(), appointment[position].getTime(), appointment[position].getSNo(), appointment[position].getTrxID(), appointment[position].getDNo());
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        showDelDialog(position);
                    }
                })
        );
    }

    private void showDelDialog(int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Delete Appointment");
        alertDialogBuilder.setMessage("Are you sure?");
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Map map = new HashMap<String, String>();
                if (mode == 1) {
                    map.put("query", "DELETE FROM treated_by WHERE Email='" + Userinfo.getInstance().getEmail() + "' AND Dno='" + appointment[position].getDNo() + "'");
                    String url = "http://zero.ourcuet.com/BAYMAX/runQuery.php";
                    CustomJsonArrayRequest runQuery = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {
                        Snackbar.make(findViewById(R.id.custom_home_layout), "Appointment Canceled! You will get refund (if any)", BaseTransientBottomBar.LENGTH_LONG).show();
                        startHomeActivity();
                    }, error -> {
                    });
                    SingletonRequestQueue.getInstance(Home.this).addToRequestQueue(runQuery);
                } else {
                    map.put("query", "DELETE FROM Requests WHERE Email='" + Userinfo.getInstance().getEmail() + "' AND Info='" + appointment[position].getDNo() + "'");
                    String url = "http://zero.ourcuet.com/BAYMAX/runQuery.php";
                    CustomJsonArrayRequest runQuery = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {
                        Snackbar.make(findViewById(R.id.custom_home_layout), "Appointment request canceled", BaseTransientBottomBar.LENGTH_LONG).show();
                        getRequestData();
                    }, error -> {
                    });
                    SingletonRequestQueue.getInstance(Home.this).addToRequestQueue(runQuery);
                }

            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void showInfoDialog(String appName, String appDate, String appTime, String appSno, String trxId, String dNo) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.appointment_dialog, null);
        alertDialogBuilder.setView(view);
        TextView name, sno, date, time;
        name = (TextView) view.findViewById(R.id.dialog_doc);
        sno = (TextView) view.findViewById(R.id.dialog_sno);
        date = (TextView) view.findViewById(R.id.dialog_date);
        time = (TextView) view.findViewById(R.id.dialog_time);
        name.setText(appName);
        sno.setText("Serial No: " + appSno);
        time.setText("Time: " + appTime);
        date.setText("Date: " + appDate);
        name.setOnClickListener(e -> {
            Map map = new HashMap<String, String>();
            map.put("query", "SELECT DOCTOR.RegistrationNo,DOCTOR.Name,DOCTOR.ContactNo,DOCTOR.Address,DOCTOR.INo,DOCTOR.Specialty,DOCTOR.Longitude,DOCTOR.Latitude,DOCTOR.Fee, INSTITUTE.Name AS IName FROM (DOCTOR INNER JOIN INSTITUTE ON INSTITUTE.RegistrationNo=DOCTOR.INo) WHERE DOCTOR.RegistrationNo='" + dNo + "'");
            String url = "http://zero.ourcuet.com/BAYMAX/runQuery.php";
            CustomJsonArrayRequest runQuery = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {
                JSONObject jsonObject = null;
                try {
                    jsonObject = response.getJSONObject(0);
                    doctor.showInfoDialog(jsonObject.getString("RegistrationNo"), jsonObject.getString("Name"),
                            jsonObject.getString("Specialty"), jsonObject.getString("ContactNo"), jsonObject.getString("Address"), jsonObject.getString("INo"), jsonObject.getString("IName")
                            , jsonObject.getString("Fee"), jsonObject.getString("Longitude"), jsonObject.getString("Latitude"));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }, error -> {

            });
            SingletonRequestQueue.getInstance(this).addToRequestQueue(runQuery);
        });
        if (mode == 1 && trxId.length() == 0) {
            alertDialogBuilder.setNegativeButton("Make Payment", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(Home.this, "Feature will be added later", Toast.LENGTH_SHORT).show();
                }
            });
        }

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void setContentGone() {
        findViewById(R.id.custom_home_layout).setVisibility(View.GONE);
        findViewById(R.id.custom_doctor_layout).setVisibility(View.GONE);
        findViewById(R.id.custom_institute_layout).setVisibility(View.GONE);
        findViewById(R.id.custom_pharmacy_layout).setVisibility(View.GONE);
        findViewById(R.id.custom_ambulance_layout).setVisibility(View.GONE);
        findViewById(R.id.custom_admin_layout).setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!toolbar.getTitle().equals("Home")) {
            toolbar.setTitle("Home");
            setContentGone();
            findViewById(R.id.custom_home_layout).setVisibility(View.VISIBLE);
            startHomeActivity();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
     if (id == R.id.request) {
            getRequestData();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getRequestData() {
        toolbar.setTitle("Home");
        setContentGone();
        findViewById(R.id.custom_home_layout).setVisibility(View.VISIBLE);
        navigationView.getMenu().getItem(0).setChecked(true);
        TextView tv = findViewById(R.id.header_home);
        tv.setText(" Requested Appointments");
        Map map = new HashMap<String, String>();
        map.put("query", "SELECT Info,Text FROM Requests WHERE Email='" + Userinfo.getInstance().getEmail() + "' AND Type=1");
        mRecyclerView = findViewById(R.id.appointmentview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        getData("pending", "http://zero.ourcuet.com/BAYMAX/runQuery.php", map);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            toolbar.setTitle("Home");
            setContentGone();
            findViewById(R.id.custom_home_layout).setVisibility(View.VISIBLE);
            startHomeActivity();
        } else if (id == R.id.doctor) {
            toolbar.setTitle("Doctor");
            setContentGone();
            findViewById(R.id.custom_doctor_layout).setVisibility(View.VISIBLE);
            startDoctorActivity();
        } else if (id == R.id.institute) {
            toolbar.setTitle("Health Institute");
            setContentGone();
            findViewById(R.id.custom_institute_layout).setVisibility(View.VISIBLE);
            startHospitalActivity();
        } else if (id == R.id.pharmacy) {
            toolbar.setTitle("Pharmacy");
            setContentGone();
            findViewById(R.id.custom_pharmacy_layout).setVisibility(View.VISIBLE);
            startPharmacyActivity();
        } else if (id == R.id.ambulance) {
            toolbar.setTitle("Ambulance");
            setContentGone();
            findViewById(R.id.custom_ambulance_layout).setVisibility(View.VISIBLE);
            startAmbulanceActivity();
        } else if (id == R.id.admin) {
            if (!Userinfo.getInstance().isAdmin()) {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setIcon(R.drawable.pending);
                alert.setTitle(" ");
                alert.setMessage("You are not an ADMIN!");
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog warning = alert.create();
                warning.show();
            } else {
                for (int i = 0; i <= 4; i++)
                    (navigationView.getMenu().getItem(i)).setChecked(false);
                startAdminActivity();
            }
        } else if (id == R.id.about) {
            startActivity(new Intent(this, Developer.class));
        }else if(id==R.id.help){
           startActivity(new Intent(this, HelpAndSupport.class));
        }
        else if (id == R.id.logout) {
            Toast.makeText(this, "Logged out from " + Userinfo.getInstance().getEmail(), Toast.LENGTH_LONG).show();
            clearUserData();
            startActivity(new Intent(this, Login.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startAmbulanceActivity() {
        setContentGone();
        findViewById(R.id.custom_ambulance_layout).setVisibility(View.VISIBLE);
        ambulance.startActivity();
    }

    private void startPharmacyActivity() {
        setContentGone();
        findViewById(R.id.custom_pharmacy_layout).setVisibility(View.VISIBLE);
        pharmacy.startActivity();
    }

    private void startHospitalActivity() {
        setContentGone();
        findViewById(R.id.custom_institute_layout).setVisibility(View.VISIBLE);
        institute.startActivity();
    }

    private void startDoctorActivity() {
        (navigationView.getMenu().getItem(1)).setChecked(true);
        setContentGone();
        findViewById(R.id.custom_doctor_layout).setVisibility(View.VISIBLE);
        doctor.startActivity();
    }

    private void startAdminActivity() {
        setContentGone();
        toolbar.setTitle("Admin");
        findViewById(R.id.custom_admin_layout).setVisibility(View.VISIBLE);
        Admin admin = new Admin(this);
        admin.startActivity();
    }

    private void startHomeActivity() {

        TextView tv = findViewById(R.id.header_home);
        tv.setText(" Appointments");
        Map map = new HashMap<String, String>();
        map.put("query", "SELECT D.Name,T.DNo,T.Date,T.Time,T.SNo,T.TrxID " +
                "FROM DOCTOR D,treated_by T " +
                "WHERE T.Email='" + Userinfo.getInstance().getEmail() + "' AND T.DNo=D.RegistrationNo ORDER BY T.Time DESC;");
        map.put("query,","SELECT * FROM INSTITUTE");
        ImageButton addNew = findViewById(R.id.addappointment);
        addNew.setOnClickListener(e -> {
            toolbar.setTitle("Doctor");
            setContentGone();
            findViewById(R.id.custom_doctor_layout).setVisibility(View.VISIBLE);
            startDoctorActivity();
        });
        mRecyclerView = findViewById(R.id.appointmentview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        getData("home", "http://zero.ourcuet.com/BAYMAX/runQuery.php", map);
    }

    private void clearUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("com.teamzero.baymax", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        sharedPreferences.edit().clear().commit();
        Userinfo.getInstance().refresh();
        finish();
        finishAffinity();
    }

    private void getData(String type, String url, Map map) {
        CustomJsonArrayRequest runQuery = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {

            swipeRefreshLayout.setRefreshing(false);
            if (response.length() <= 0) {
                findViewById(R.id.appointmentview).setVisibility(View.GONE);
                findViewById(R.id.noticelayout).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.appointmentview).setVisibility(View.VISIBLE);
                findViewById(R.id.noticelayout).setVisibility(View.GONE);
                mAdapter = new MyAdapter(type, response);
                mRecyclerView.setAdapter(mAdapter);
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        appointment[i] = new Appointment();
                        if (type.equals("home")) {
                            mode = 1;
                            appointment[i].makeAppointment(jsonObject.getString("Name"), jsonObject.getString("DNo"), jsonObject.getString("Date"), jsonObject.getString("Time"), jsonObject.getString("SNo"), jsonObject.getString("TrxID"));
                        } else if (type.equals("pending")) {
                            mode = 2;
                            appointment[i].makeAppointment(jsonObject.getString("Text"), jsonObject.getString("Info"), " ", " ", " ", " ");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, error -> {
            swipeRefreshLayout.setRefreshing(false);
        });
        SingletonRequestQueue.getInstance(this).addToRequestQueue(runQuery);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                runSearchQuery(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                runSearchQuery(newText);
                return false;
            }
        });
        return true;
    }

    private void runSearchQuery(String query) {
        if (toolbar.getTitle().equals("Home")) {
            Map map = new HashMap<String, String>();
            map.put("table", "home");
            map.put("key", query);
            map.put("email", Userinfo.getInstance().getEmail());
            getData("home", "http://zero.ourcuet.com/BAYMAX/searchQuery.php", map);
        } else if (toolbar.getTitle().equals("Doctor")) {
            Map map = new HashMap<String, String>();
            map.put("table", "DOCTOR");
            map.put("key", query);
            doctor.startSearchActivity(map);
        } else if (toolbar.getTitle().equals("Health Institute")) {
            Map map = new HashMap<String, String>();
            map.put("table", "institute");
            map.put("key", query);
            institute.startSearchActivity(map);
        } else if (toolbar.getTitle().equals("Pharmacy")) {
            Map map = new HashMap<String, String>();
            map.put("table", "pharmacy");
            map.put("key", query);
            pharmacy.startSearchActivity(map);
        } else if (toolbar.getTitle().equals("Ambulance")) {
            Map map = new HashMap<String, String>();
            map.put("table", "ambulance");
            map.put("key", query);
            ambulance.startSearchActivity(map);
        }

    }

    private void dialNumber(String number) {
        String phoneNumber = String.format("tel: %s", number);
        // Create the intent.
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        // Set the data for the intent as the phone number.
        dialIntent.setData(Uri.parse(phoneNumber));
        // If package resolves to an app, send intent.
        if (dialIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(dialIntent);
        } else {
            Toast.makeText(Home.this, "Can not make call", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermission = true;
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        1234);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    1234);
        }
    }

    private void getDeviceLoaction() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (locationPermission) {
                Task location = fusedLocationProviderClient.getLastLocation();
                Toast.makeText(this, "Getting Device Location", Toast.LENGTH_SHORT).show();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location currentLocation = (Location) task.getResult();
                            userLat = currentLocation.getLatitude();
                            userLng = currentLocation.getLongitude();
                            calculateData();
                        }
                    }
                });
            }
        } catch (SecurityException e) {

        }
    }

    private void calculateData() {

        Map map = new HashMap<String, String>();
        map.put("query", "SELECT Name,ContactNo,Longitude,Latitude FROM INSTITUTE ");
        String url = "http://zero.ourcuet.com/BAYMAX/runQuery.php";
        CustomJsonArrayRequest jsonArrayRequest = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {
            String instituteName, contNo;
            int distance = 6372;
            try {
                JSONObject jsonObject = response.getJSONObject(0);
                instituteName = jsonObject.getString("Name");
                contNo = jsonObject.getString("ContactNo");
                distance = calculateDistance(Double.parseDouble(jsonObject.getString("Latitude")), Double.parseDouble(jsonObject.getString("Longitude")));
                for (int i = 1; i < response.length(); i++) {
                    jsonObject = response.getJSONObject(i);
                    int temp = calculateDistance(Double.parseDouble(jsonObject.getString("Latitude")), Double.parseDouble(jsonObject.getString("Longitude")));
                    System.out.println(jsonObject.getString("Name")+" "+i+" :"+temp);
                    if (temp < distance) {
                        distance = temp;
                        instituteName = jsonObject.getString("Name");
                        contNo = jsonObject.getString("ContactNo");
                    }
                }
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Confirm Call !");
                alertDialogBuilder.setMessage("Calling " + instituteName + "\n" + contNo);
                String finalContNo = contNo;
                alertDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialNumber(finalContNo);
                    }
                });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } catch (Exception ex) {
            }
        }, error ->

        {
        });
        SingletonRequestQueue.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

    private int calculateDistance(double venueLat, double venueLng) {

        double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
    }
}

