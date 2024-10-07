package com.kbyai.idcardrecognition;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ResultAdapter extends ArrayAdapter<ResultItem> {
    public ResultAdapter(Context context, ArrayList<ResultItem> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ResultItem resultItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_result, parent, false);
        }
        // Lookup view for data population
        TextView tvKey = (TextView) convertView.findViewById(R.id.txt_key);
        TextView tvValue1 = (TextView) convertView.findViewById(R.id.txt_value1);
        TextView tvValue2 = (TextView) convertView.findViewById(R.id.txt_value2);
        TextView tvValue3 = (TextView) convertView.findViewById(R.id.txt_value3);
        TextView tvField1 = (TextView) convertView.findViewById(R.id.txt_field1);
        TextView tvField2 = (TextView) convertView.findViewById(R.id.txt_field2);
        TextView tvField3 = (TextView) convertView.findViewById(R.id.txt_field3);

        // Populate the data into the template view using the data object
        tvKey.setText(resultItem.key);

        if(resultItem.value1 == null || resultItem.value1.isEmpty()) {
            tvValue1.setVisibility(View.GONE);
        } else {
            tvValue1.setVisibility(View.VISIBLE);
            tvValue1.setText(resultItem.value1);
        }

        if(resultItem.value2 == null || resultItem.value2.isEmpty()) {
            tvValue2.setVisibility(View.GONE);
        } else {
            tvValue2.setVisibility(View.VISIBLE);
            tvValue2.setText(resultItem.value2);
        }

        if(resultItem.value3 == null || resultItem.value3.isEmpty()) {
            tvValue3.setVisibility(View.GONE);
        } else {
            tvValue3.setVisibility(View.VISIBLE);
            tvValue3.setText(resultItem.value3);
        }

        if(resultItem.field1 == null || resultItem.field1.isEmpty()) {
            tvField1.setVisibility(View.GONE);
        } else {
            tvField1.setVisibility(View.VISIBLE);
            tvField1.setText(resultItem.field1);
        }

        if(resultItem.field2 == null || resultItem.field2.isEmpty()) {
            tvField2.setVisibility(View.GONE);
        } else {
            tvField2.setVisibility(View.VISIBLE);
            tvField2.setText(resultItem.field2);
        }

        if(resultItem.field3 == null || resultItem.field3.isEmpty()) {
            tvField3.setVisibility(View.GONE);
        } else {
            tvField3.setVisibility(View.VISIBLE);
            tvField3.setText(resultItem.field3);
        }
        return convertView;
    }
}