package com.tagloy.tagbiz.fragment;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.adapter.PublisherGridAdapter;
import com.tagloy.tagbiz.models.Creative;
import com.tagloy.tagbiz.utils.AppConfig;
import com.tagloy.tagbiz.utils.BackgroundClass;
import com.tagloy.tagbiz.utils.GridSpacingItemDecoration;
import com.tagloy.tagbiz.utils.PreferenceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 */
public class CompletedPublisherFragment extends Fragment {

    Context mContext;
    ArrayList<Creative> creativeArrayList = new ArrayList<>();
    ProgressBar progressBar;
    RecyclerView publisherRecyclerView;
    PublisherGridAdapter publisherGridAdapter;
    TextView emptyText;
    BackgroundClass backgroundClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_completed_publisher, container, false);
        mContext = getActivity();
        progressBar = view.findViewById(R.id.completedPublisherProgress);
        publisherRecyclerView = view.findViewById(R.id.completePublisherRecyclerView);
        emptyText = view.findViewById(R.id.publisherEmptyText);
        backgroundClass = new BackgroundClass(mContext);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 2);
        publisherRecyclerView.setLayoutManager(layoutManager);
        publisherRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, backgroundClass.dpToPx(5), true));
        publisherRecyclerView.setItemAnimator(new DefaultItemAnimator());
        if (backgroundClass.isNetworkConnected()) {
            getScheduledPublisher();
            getRejectedPublisher();
        }else {
            Toast.makeText(mContext, "Check network connection!", Toast.LENGTH_LONG).show();
        }
        return view;
    }

    private void getScheduledPublisher() {
        try {
            String org_id = PreferenceHelper.getValueString(mContext, AppConfig.ORG_ID);
            final String token = PreferenceHelper.getValueString(mContext, AppConfig.USER_TOKEN);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("organization_id", org_id);
            jsonObject.put("screen_type","TAGPUBLISH");
            jsonObject.put("Live",0);
            jsonObject.put("Archived",1);
            RequestQueue queue = Volley.newRequestQueue(mContext);
            progressBar.setVisibility(View.VISIBLE);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.LIVE_CREATIVE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("Volley","Success");
                    try {
                        Creative creative;
                        JSONObject stringResponse = new JSONObject(response);
                        boolean success = stringResponse.getBoolean("is_success");
                        String requestCode = stringResponse.getString("status_code");
                        if (success) {
                            if (requestCode.equals("200")) {
                                Log.d("Get publisher","Success");
                                String result = stringResponse.getString("result");
                                JSONArray resultArray = new JSONArray(result);
                                for (int i = 0; i < resultArray.length(); i++) {
                                    creative = new Creative();
                                    JSONObject resultObject = resultArray.getJSONObject(i);
                                    String slot = resultObject.getString("slot");
                                    creative.setDuration(resultObject.getInt("duration"));
                                    creative.setType(resultObject.getString("type"));
                                    creative.setMedia("https://" + resultObject.getString("media_url"));
                                    String sDate = resultObject.getString("start_datetime");
                                    String eDate = resultObject.getString("end_datetime");
                                    SimpleDateFormat original = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US);
                                    original.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    format.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
                                    Date startDate = original.parse(sDate);
                                    Date endDate = original.parse(eDate);
                                    creative.setStartD(format.format(startDate));
                                    creative.setEndD(format.format(endDate));
                                    creative.setId(resultObject.getString("id"));
                                    if (resultObject.getInt("active") == 0 && resultObject.getInt("archive") == 1){
                                        creative.setStatus("Completed");
                                    }
                                    creativeArrayList.add(creative);
                                }
                            }else{
                                Log.d("Get publisher","Failure");
                            }
                        } else {
                            Log.d("Is success","False");
                            Toast.makeText(mContext, "Please Re-login!", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException | ParseException | NullPointerException je) {
                        je.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Get publisher volley","Failure");
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

    private void getRejectedPublisher() {
        try {
            String org_id = PreferenceHelper.getValueString(mContext, AppConfig.ORG_ID);
            final String token = PreferenceHelper.getValueString(mContext, AppConfig.USER_TOKEN);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("organization_id", org_id);
            jsonObject.put("screen_type","TAGPUBLISH");
            jsonObject.put("Live",0);
            jsonObject.put("Archived",1);
            RequestQueue queue = Volley.newRequestQueue(mContext);
            progressBar.setVisibility(View.VISIBLE);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.PENDING_CREATIVE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("Volley","Success");
                    try {
                        Creative creative;
                        JSONObject stringResponse = new JSONObject(response);
                        boolean success = stringResponse.getBoolean("is_success");
                        String requestCode = stringResponse.getString("status_code");
                        if (success) {
                            if (requestCode.equals("200")) {
                                Log.d("Get publisher","Success");
                                String result = stringResponse.getString("result");
                                JSONArray resultArray = new JSONArray(result);
                                for (int i = 0; i < resultArray.length(); i++) {
                                    creative = new Creative();
                                    JSONObject resultObject = resultArray.getJSONObject(i);
                                    creative.setDuration(resultObject.getInt("duration"));
                                    creative.setType(resultObject.getString("type"));
                                    creative.setMedia("https://" + resultObject.getString("media_url"));
                                    String sDate = resultObject.getString("start_datetime");
                                    String eDate = resultObject.getString("end_datetime");
                                    SimpleDateFormat original = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US);
                                    original.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    format.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
                                    Date startDate = original.parse(sDate);
                                    Date endDate = original.parse(eDate);
                                    creative.setStartD(format.format(startDate));
                                    creative.setEndD(format.format(endDate));
                                    creative.setId(resultObject.getString("Id"));
                                    creative.setStatus("Rejected");
                                    creativeArrayList.add(creative);
                                }
                            }else{
                                Log.d("Get publisher","Failure");
                            }
                        } else {
                            Log.d("Is success","False");
                            Toast.makeText(mContext, "Please Re-login!", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException | ParseException | NullPointerException je) {
                        je.printStackTrace();
                    }
                    new LoadCreative(mContext).execute();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Get publisher volley","Failure");
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
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(stringRequest);
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }


    public class LoadCreative extends AsyncTask<Void,Void,Void> {
        Context context;

        private LoadCreative(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (creativeArrayList.size() > 0){
                progressBar.setVisibility(View.GONE);
                publisherGridAdapter = new PublisherGridAdapter(context,creativeArrayList);
                publisherGridAdapter.notifyDataSetChanged();
                publisherRecyclerView.setAdapter(publisherGridAdapter);
            }else{
                progressBar.setVisibility(View.GONE);
                emptyText.setVisibility(View.VISIBLE);
            }
        }
    }
}
