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

import java.util.ArrayList;


public class CustomAdapterChat extends ArrayAdapter<ChatModel> {

    private ArrayList<ChatModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView tvMessage;
    }

    public CustomAdapterChat(ArrayList<ChatModel> data, Context context) {
        super(context, R.layout.list_item_for_sender_message, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(position).getType();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ChatModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        int type = getItemViewType(position);

        if (convertView == null) {

            viewHolder = new ViewHolder();

            if(type == 0){
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.list_item_for_sender_message, parent, false);
                viewHolder.tvMessage = (TextView) convertView.findViewById(R.id.tvMessage);

            }else if(type == 1){
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.list_item_for_receiver_message, parent, false);
                viewHolder.tvMessage = (TextView) convertView.findViewById(R.id.tvMessage);

            }

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        lastPosition = position;

        viewHolder.tvMessage.setText(""+dataModel.getMessage());
        return convertView;
    }
}