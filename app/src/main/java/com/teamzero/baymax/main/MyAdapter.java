package com.teamzero.baymax.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.teamzero.baymax.initial.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private JSONArray mDataset;
    private String type = "";

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView first, second;

        public MyViewHolder(View v, String type) {
            super(v);
            first = v.findViewById(R.id.first);
            second = v.findViewById(R.id.second);
        }
    }

    public MyAdapter(String adaptertype, JSONArray myDataset) {
        type = adaptertype;
        mDataset = myDataset;
    }

    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);
        MyViewHolder vh = new MyViewHolder(v, type);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        try {
            if (type.equals("home")) {
                JSONObject jsonObject = mDataset.getJSONObject(position);
                holder.first.setText(jsonObject.getString("Name"));
                holder.second.setText(jsonObject.getString("Date"));
            } else if (type.equals("pending")) {
                JSONObject jsonObject = mDataset.getJSONObject(position);
                holder.first.setText(jsonObject.getString("Text"));
                holder.second.setText("Request Pending");
            } else if (type.equals("doctor")) {
                JSONObject jsonObject = mDataset.getJSONObject(position);
                holder.first.setText(jsonObject.getString("Name"));
                holder.second.setText(jsonObject.getString("Specialty"));
            } else if (type.equals("admin")) {
                JSONObject jsonObject = mDataset.getJSONObject(position);
                holder.first.setText(jsonObject.getString("Email"));
                holder.first.setTextSize(14);
                holder.second.setText("type: " + jsonObject.getString("Type") + " info: " + jsonObject.getString("Info"));
                holder.second.setTextSize(14);
            } else if (type.equals("institute")||type.equals("pharmacy")) {
                JSONObject jsonObject = mDataset.getJSONObject(position);
                holder.first.setText(jsonObject.getString("Name"));
                holder.second.setText(jsonObject.getString("Address"));
            }else if (type.equals("ambulance")) {
                JSONObject jsonObject = mDataset.getJSONObject(position);
                holder.first.setText(jsonObject.getString("Driver"));
                String s="Occupied";
                if(jsonObject.getString("Status").equals("0")) s="Available";
                holder.second.setText(s);
            }
            else if (type.equals("search")) {
                JSONObject jsonObject = mDataset.getJSONObject(position);
                if(jsonObject.getString("Name")==null)
                holder.first.setText(jsonObject.getString("Driver"));
                else holder.first.setText(jsonObject.getString("Name"));
                holder.second.setText("click to view details");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.length();
    }

}