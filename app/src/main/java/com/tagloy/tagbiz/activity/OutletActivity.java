package com.tagloy.tagbiz.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tagloy.tagbiz.adapter.OutletsAdapter;
import com.tagloy.tagbiz.models.Organization;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.utils.AppConfig;
import com.tagloy.tagbiz.utils.BackgroundClass;
import com.tagloy.tagbiz.utils.PreferenceHelper;
import com.tagloy.tagbiz.utils.RecyclerTouchListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OutletActivity extends AppCompatActivity {

    RecyclerView outLetRecyclerView;
    ArrayList<Organization> organizationList = new ArrayList<>();
    Context mContext;
    ProgressBar progressBar;
    OutletsAdapter organizationArrayAdapter;
    BackgroundClass backgroundClass;
    SearchView outletSearchView;
    public String ParseJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outlet);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar);
        View view = getSupportActionBar().getCustomView();
        mContext = this;
        outletSearchView = view.findViewById(R.id.outletSearch);
        outLetRecyclerView = findViewById(R.id.outletRecyclerView);
        progressBar = findViewById(R.id.outletProgress);
        backgroundClass = new BackgroundClass(mContext);
        final String username = PreferenceHelper.getValueString(mContext,AppConfig.USERNAME);
        final String pass = PreferenceHelper.getValueString(mContext,AppConfig.PASSWORD);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        outLetRecyclerView.setLayoutManager(mLayoutManager);
        outLetRecyclerView.setItemAnimator(new DefaultItemAnimator());
        outLetRecyclerView.addItemDecoration(new DividerItemDecoration(this,LinearLayoutManager.VERTICAL));
        getOrganization(username, pass, 0);
    }

    public void getOrganization(String username, String password, int org_id ){
        try{
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", username);
            jsonObject.put("password", password);
            if (org_id != 0)
                jsonObject.put("organization_id", org_id);
            RequestQueue queue = Volley.newRequestQueue(mContext);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.LOGIN_URL,
                    response -> Log.d("Response"," True"),
                    error -> {
                Log.d("Login API","Failure");
                if (error == null || error.networkResponse == null) {
                    Toast.makeText(mContext, "Network error! Please try again in some time", Toast.LENGTH_SHORT).show();
                    Log.d("Login API","Fail");
                    return;
                }
                String body;
                try {
                    int statusCode = error.networkResponse.statusCode;
                    Log.d("Status",String.valueOf(statusCode));
                    if (statusCode == 400) {
                        body = new String(error.networkResponse.data, "UTF-8");
                        JSONObject resultJson = new JSONObject(body);
                        String result = resultJson.getString("result");
                        ParseJson = result;
                        new LoadOutlets(mContext).execute();
                    } else if (statusCode == 401) {
                        Toast.makeText(mContext, "Please check credentials!", Toast.LENGTH_SHORT).show();
                    }
                } catch (UnsupportedEncodingException | JSONException ue) {
                    ue.printStackTrace();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("Content-Type", "application/json");
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
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(stringRequest);
        }catch (JSONException je){
            je.printStackTrace();
        }
    }

    public class LoadOutlets extends AsyncTask<Void,Void,Void> {
        Context context;
        final String username = PreferenceHelper.getValueString(mContext,AppConfig.USERNAME);
        final String pass = PreferenceHelper.getValueString(mContext,AppConfig.PASSWORD);

        public LoadOutlets(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                Organization organization;
                JSONArray resultArray = new JSONArray(ParseJson);
                for (int i=0; i< resultArray.length(); i++){
                    organization = new Organization();
                    JSONObject resultObject = resultArray.getJSONObject(i);
                    String org_id = resultObject.getString("organization_id");
                    String org_name = resultObject.getString("organization_name");
                    if (resultObject.has("address")){
                        if (org_name.equals("Tagloy")){
                            organization.setArea("t");
                            organization.setCity("Pune");
                        }else {
                            String address = resultObject.getString("address");
                            JSONObject addressObject = new JSONObject(address);
                            String city = addressObject.getString("city");
                            String area = addressObject.getString("area");
                            organization.setCity(city);
                            organization.setArea(area);
                        }
                    }else {
                        organization.setCity("t");
                        organization.setArea("t");
                    }
                    organization.setId(org_id);
                    organization.setOrg_name(org_name);
                    organizationList.add(organization);
                }
            }catch (JSONException je){
                je.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (organizationList.size() > 0){
                progressBar.setVisibility(View.GONE);
                organizationArrayAdapter = new OutletsAdapter(mContext,organizationList);
                outLetRecyclerView.setAdapter(organizationArrayAdapter);
                organizationArrayAdapter.notifyDataSetChanged();
                outLetRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, outLetRecyclerView, new RecyclerTouchListener.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        Organization organization = organizationList.get(position);
                        int org_id = Integer.parseInt(organization.getId());
                        String org_name = organization.getOrg_name();
                        PreferenceHelper.setValueString(context,AppConfig.ORG_ID,String.valueOf(org_id));
                        PreferenceHelper.setValueString(context,AppConfig.ORG_NAME,org_name);
                        backgroundClass.loginUser(username,pass,org_id);
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));
                if (organizationList.size() > 5){
                    outletSearchView.setVisibility(View.VISIBLE);
                    outletSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String s) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String s) {
                            organizationArrayAdapter.filter(s);
                            return false;
                        }
                    });
                }else {
                    outletSearchView.setVisibility(View.GONE);
                }
            }else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(mContext, "No outlets available...", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        organizationList.clear();
    }
}
