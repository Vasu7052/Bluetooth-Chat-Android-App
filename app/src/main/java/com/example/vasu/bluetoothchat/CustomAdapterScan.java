package com.example.vasu.bluetoothchat;

/**
 * Created by Vasu on 14-01-2018.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class CustomAdapterScan extends ArrayAdapter<BlueToothDeviceModel> {

    private ArrayList<BlueToothDeviceModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView tvName;
        TextView tvAddress;
    }

    public CustomAdapterScan(ArrayList<BlueToothDeviceModel> data, Context context) {
        super(context, R.layout.list_item_for_scan_list, data);
        this.dataSet = data;
        this.mContext=context;

    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BlueToothDeviceModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();

            viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        lastPosition = position;

        viewHolder.tvName.setText(""+dataModel.getDevice().getName());
        viewHolder.tvAddress.setText(""+dataModel.getDevice().getAddress());
        return convertView;
    }
}