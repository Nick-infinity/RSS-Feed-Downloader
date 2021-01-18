package com.nickinfinity.toprssfeeddownloader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class FeedAdapter extends ArrayAdapter {
    private static final String TAG = "FeedAdapter";
    private final int layoutResource;
    private final LayoutInflater layoutInflater;
    private ArrayList<FeedEntry> applications;

    public FeedAdapter(@NonNull Context context, int resource, ArrayList<FeedEntry> applications) {
        super(context, resource);
        this.layoutResource = resource;
        this.layoutInflater = LayoutInflater.from(context);
        this.applications = applications;

    }

    @Override
    public int getCount() {
        return this.applications.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = layoutInflater.inflate(layoutResource,parent,false);

        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvArtist = view.findViewById(R.id.tvArtist);
        TextView tvSummary = view.findViewById(R.id.tvSummary);

        FeedEntry currentApplication = this.applications.get(position);

        tvName.setText(currentApplication.getName());
        tvArtist.setText(currentApplication.getArtist());
        tvSummary.setText(currentApplication.getSummary());

        return view;
    }


}
