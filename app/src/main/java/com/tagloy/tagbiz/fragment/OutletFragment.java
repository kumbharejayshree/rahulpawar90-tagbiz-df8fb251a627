package com.tagloy.tagbiz.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.activity.MainNavActivity;
import com.tagloy.tagbiz.models.Organization;
import com.tagloy.tagbiz.utils.AppConfig;
import com.tagloy.tagbiz.utils.BackgroundClass;
import com.tagloy.tagbiz.utils.PreferenceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutletFragment extends Fragment {

    ListView outLetListView;
    List<Organization> organizationList = new ArrayList<>();
    Context mContext;
    String ParseJson;
    ProgressBar progressBar;
    ArrayAdapter<String> organizationArrayAdapter;
    BackgroundClass backgroundClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_outlet, container, false);

        mContext = getActivity();
        outLetListView = view.findViewById(R.id.outletListView);
        progressBar = view.findViewById(R.id.outletProgress);
        backgroundClass = new BackgroundClass(mContext);
        if (backgroundClass.isNetworkConnected()) {
            getOutlets();
        }else {
            Toast.makeText(mContext, "Check network connection!", Toast.LENGTH_LONG).show();
        }
        outLetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String org_id = getItem(position).getId();
                String org_name = getItem(position).getOrg_name();
                String hash = getItem(position).getHash_tag();
                PreferenceHelper.setValueString(mContext, AppConfig.ORG_ID,org_id);
                PreferenceHelper.setValueString(mContext,AppConfig.ORG_NAME,org_name);
                PreferenceHelper.setValueString(mContext,AppConfig.HASH_TAG,hash);
                Intent intent = new Intent(mContext,MainNavActivity.class);
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Organizations");
    }

    public Organization getItem(int position) {
        return organizationList.get(position);
    }

    public void getOutlets() {
        try {
            final JSONObject jsonObject = new JSONObject();
            String user_id = PreferenceHelper.getValueString(mContext,AppConfig.USER_ID);
            String role_id = PreferenceHelper.getValueString(mContext,AppConfig.ROLE_ID);
            String org_id = PreferenceHelper.getValueString(mContext,AppConfig.ORG_ID);
            final String token = PreferenceHelper.getValueString(mContext,AppConfig.USER_TOKEN);
            jsonObject.put("user_id", user_id);
            jsonObject.put("role_id", role_id);
            jsonObject.put("organization_id", org_id);
            RequestQueue queue = Volley.newRequestQueue(mContext);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.GET_OUTLET_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    ParseJson = response;
                    new LoadOutlets(mContext).execute();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("Content-Type", "application/json");
                    parameter.put("Authorization", token);
                    return parameter;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    final String request = jsonObject.toString();
                    try {
                        return request.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", request, "utf-8");
                        return null;
                    }
                }
            };
            queue.add(stringRequest);
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    public class LoadOutlets extends AsyncTask<Void,Void,Void> {
        Context context;
        List<String> outlets = new ArrayList<>();

        public LoadOutlets(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Organization organization;
                JSONObject stringResponse = new JSONObject(ParseJson);
                boolean success = stringResponse.getBoolean("is_success");
                String requestCode = stringResponse.getString("status_code");
                if (success) {
                    if (requestCode.equals("200")) {
                        String result = stringResponse.getString("result");
                        JSONArray resultArray = new JSONArray(result);
                        for (int i = 0; i < resultArray.length(); i++) {
                            organization = new Organization();
                            JSONObject resultObject = resultArray.getJSONObject(i);
                            organization.id = resultObject.getString("organization_id");
                            organization.org_name = resultObject.getString("organization_name");
                            organization.hash_tag = "#" + resultObject.getString("hash_tag");
                            organization.setSelected(true);
                            organizationList.add(organization);
                        }
                    }
                } else {
                    Toast.makeText(mContext, "Please Re-login!", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException je) {
                je.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (organizationList != null){
                progressBar.setVisibility(View.GONE);
                for (int i=0; i < organizationList.size();i++){
                    outlets.add(organizationList.get(i).org_name);
                }
                organizationArrayAdapter = new ArrayAdapter<>(mContext,android.R.layout.simple_list_item_1,outlets);
                outLetListView.setAdapter(organizationArrayAdapter);
                organizationArrayAdapter.notifyDataSetChanged();
            }else {
                Toast.makeText(mContext, "No outlets available...", Toast.LENGTH_LONG).show();
            }
        }
    }
}
