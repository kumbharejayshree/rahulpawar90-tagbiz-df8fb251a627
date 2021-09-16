package com.tagloy.tagbiz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tagloy.tagbiz.models.Organization;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.utils.BackgroundClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OutletsAdapter extends RecyclerView.Adapter<OutletsAdapter.ViewHolder>{
    List<Organization> organizationList;
    Context context;
    List<Organization> list;
    BackgroundClass backgroundClass;

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView outletName, addressText;
        public ViewHolder(View view){
            super(view);
            outletName = view.findViewById(R.id.outletNameTextView);
            addressText = view.findViewById(R.id.addressTextView);
        }
    }


    public OutletsAdapter(Context context, List<Organization> organizationList){
        this.context = context;
        this.organizationList = organizationList;
        this.list = new ArrayList<>();
        this.list.addAll(organizationList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        backgroundClass = new BackgroundClass(context);
        final Organization organization = organizationList.get(position);
        viewHolder.outletName.setText(organization.getOrg_name());
        viewHolder.addressText.setText(organization.getCity() + ", " + organization.getArea());
        if (organization.getCity().equals("t") && organization.getArea().equals("t")){
            viewHolder.addressText.setVisibility(View.GONE);
        }else if (organization.getArea().equals("t")){
            viewHolder.addressText.setText(organization.getCity());
        }else {
            viewHolder.addressText.setText(organization.getCity() + ", " + organization.getArea());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return organizationList.size();
    }

    public void filter(String filterText){
        filterText = filterText.toLowerCase(Locale.getDefault());
        organizationList.clear();
        if (filterText.length() == 0){
            organizationList.addAll(list);
        }else {
            for (Organization organization: list){
                if (organization.getOrg_name().toLowerCase(Locale.getDefault()).contains(filterText)
                || organization.getArea().toLowerCase(Locale.getDefault()).contains(filterText)
                || organization.getCity().toLowerCase(Locale.getDefault()).contains(filterText)){
                    organizationList.add(organization);
                }
            }
        }
        notifyDataSetChanged();
    }
}
