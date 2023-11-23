package com.example.contactlist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;


public class CustomContactAdapter extends ArrayAdapter<Contact> {

    private final Context context;
    private final ArrayList<Contact> values;



    public CustomContactAdapter(@NonNull Context context, @NonNull ArrayList<Contact> objects) {
        super(context, -1, objects);
        this.context = context;
        this.values = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @SuppressLint("ViewHolder")
        View rowView = inflater.inflate(R.layout.row_contact, parent, false); //get template

        //Access Elements
        TextView ContactName = rowView.findViewById(R.id.tvContactName);
        TextView ContactNumber = rowView.findViewById(R.id.tvContactNumber);
        ImageView ContactImage = rowView.findViewById(R.id.imageView);

        ContactName.setText(values.get(position).name);
        ContactNumber.setText(values.get(position).phoneHome);
        String imgString = values.get(position).imageString;

        byte[] bytes= Base64.decode(imgString,Base64.DEFAULT);
        // Initialize bitmap
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);

        // set bitmap on imageView
        ContactImage.setImageBitmap(bitmap);

        return rowView;
    }
}
