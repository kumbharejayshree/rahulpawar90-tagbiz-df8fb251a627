package com.tagloy.tagbiz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.models.Organization;
import java.util.ArrayList;
import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<Organization> {
    private Context context;
    private ArrayList<Organization> organizations;
    private SpinnerAdapter spinnerAdapter;
    private boolean isFromView = false;
    private Spinner spinner;

    public SpinnerAdapter(Context context, int resource, List<Organization> organizationList, Spinner spinner){
        super(context,resource,organizationList);
        this.context = context;
        this.organizations = (ArrayList<Organization>) organizationList;
        this.spinner = spinner;
        this.spinnerAdapter = this;
    }

    @Override
    public int getCount() {
        return organizations.size();
    }

    @Override
    public Organization getItem(int position) {
        return organizations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertview, ViewGroup parent){
        ViewHolder viewHolder;
         if (convertview == null){
             LayoutInflater layoutInflater = LayoutInflater.from(context);
             convertview = layoutInflater.inflate(R.layout.spinner_item,null);
             viewHolder = new ViewHolder();
             viewHolder.outletCheck = convertview.findViewById(R.id.outletCheck);
             viewHolder.outletNameText = convertview.findViewById(R.id.outletNameText);
             convertview.setTag(viewHolder);
         }else {
             viewHolder = (ViewHolder) convertview.getTag();
         }
         viewHolder.outletNameText.setText(organizations.get(position).hash_tag);
         isFromView = true;
         viewHolder.outletCheck.setChecked(organizations.get(position).isSelected());
         isFromView = false;
         viewHolder.outletCheck.setTag(position);
         viewHolder.outletCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 int position = (Integer) buttonView.getTag();
                 if (!isFromView){
                     organizations.get(position).setSelected(isChecked);
                 }
             }
         });
         viewHolder.outletNameText.setTag(position);
         viewHolder.outletNameText.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 spinner.performClick();
                 int pos = (Integer) v.getTag();
                 organizations.get(pos).setSelected(!organizations.get(pos).isSelected());
             }
         });
         return convertview;
    }

    private class ViewHolder{
        CheckBox outletCheck;
        TextView outletNameText;
    }
}
