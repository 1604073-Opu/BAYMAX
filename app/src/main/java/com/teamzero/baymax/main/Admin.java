package com.teamzero.baymax.main;

import com.android.volley.Request;
import com.teamzero.baymax.extra.Webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.teamzero.baymax.initial.R;
import com.teamzero.baymax.volley.CustomJsonArrayRequest;
import com.teamzero.baymax.volley.SingletonRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Admin extends AppCompatActivity {


    public class RequestInfo {
        private int type, id;
        private String info, text, email;

        public void create(int Id, String Email, String Info, String Text, int Type) {
            info = Info;
            text = Text;
            type = Type;
            email = Email;
            id = Id;
        }
    }

    private SwipeRefreshLayout refresh;
    private RequestInfo requestInfo[] = new RequestInfo[20];
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Context context;

    public Admin(Context context) {
        this.context = context;
    }


    public void startActivity() {
        Button webButton = ((Activity) context).findViewById(R.id.webbutton);
        webButton.setOnClickListener(e -> {
            ((Activity) context).startActivity(new Intent(((Activity) context), Webview.class));
        });
        refresh = ((Activity) context).findViewById(R.id.swipe_refresh_admin);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startAdminActivity();
            }
        });
        mRecyclerView = ((Activity) context).findViewById(R.id.adminview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        startAdminActivity();
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, this.mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        if (requestInfo[position].type == 1) {
                            acceptAppointment(requestInfo[position].email, requestInfo[position].info, Integer.toString(requestInfo[position].id));
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );
    }

    private void acceptAppointment(String email, String docNo, String id) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.accept_app_layout, null);
        alertDialogBuilder.setView(view);
        TextView sNo, time, date, details;
        details = view.findViewById(R.id.acc_details);
        sNo = (TextView) view.findViewById(R.id.serial_no);
        date = (TextView) view.findViewById(R.id.acc_date);
        time = (TextView) view.findViewById(R.id.acc_time);
        Button accept = view.findViewById(R.id.acc_btn);
        details.setText(email + "\n Doctor No: " + docNo);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        accept.setOnClickListener(e -> {
            Map map = new HashMap<String, String>();
            map.put("query", "INSERT INTO treated_by(Date,Time,SNo,Email,DNo) VALUES(STR_TO_DATE('" + date.getText() + "','%d-%m-%Y'),'" + time.getText() + "','" + sNo.getText() + "','" + email + "','" + docNo + "')");
            String url = "http://zero.ourcuet.com/BAYMAX/runQuery.php";
            CustomJsonArrayRequest runQuery = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {
                map.clear();
                map.put("query", "DELETE FROM Requests WHERE Email='" + email + "' AND type=1 AND ID='" + id + "'");
                CustomJsonArrayRequest runQuery1 = new CustomJsonArrayRequest(Request.Method.POST, url, map, response1 -> {
                    Toast.makeText(context, "Successful", Toast.LENGTH_SHORT).show();
                }, error -> {
                });
                SingletonRequestQueue.getInstance(this).addToRequestQueue(runQuery1);
            }, error -> {
            });
            SingletonRequestQueue.getInstance(this).addToRequestQueue(runQuery);
            startAdminActivity();
            alertDialog.cancel();
        });
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

    private void startAdminActivity() {
        Map map = new HashMap<String, String>();
        map.put("query", "SELECT * FROM Requests");
        getData("admin", map);
    }

    private void getData(String type, Map map) {
        refresh.setRefreshing(true);
        String url = "http://zero.ourcuet.com/BAYMAX/runQuery.php";
        CustomJsonArrayRequest runQuery = new CustomJsonArrayRequest(Request.Method.POST, url, map, response -> {
            mAdapter = new MyAdapter(type, response);
            mRecyclerView.setAdapter(mAdapter);
            refresh.setRefreshing(false);
            for (int i = 0; i < response.length(); i++) {
                requestInfo[i] = new RequestInfo();
                try {
                    JSONObject jsonObject = response.getJSONObject(i);
                    requestInfo[i].create(jsonObject.getInt("ID"), jsonObject.getString("Email"), jsonObject.getString("Info"), jsonObject.getString("Text"), jsonObject.getInt("Type"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, error -> {
            refresh.setRefreshing(false);
        });
        SingletonRequestQueue.getInstance(this).addToRequestQueue(runQuery);
    }
}
