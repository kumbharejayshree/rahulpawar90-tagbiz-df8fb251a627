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
import com.tagloy.tagbiz.adapter.HighlightGridAdapter;
import com.tagloy.tagbiz.models.Creative;
import com.tagloy.tagbiz.utils.AppConfig;
import com.tagloy.tagbiz.utils.BackgroundClass;
import com.tagloy.tagbiz.utils.GridSpacingItemDecoration;
import com.tagloy.tagbiz.utils.PreferenceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class LiveHighlightFragment extends Fragment {

    Context mContext;
    ArrayList<Creative> creativeArrayList = new ArrayList<>();
    ProgressBar progressBar;
    RecyclerView highlightRecyclerView;
    HighlightGridAdapter highlightGridAdapter;
    TextView emptyText;
    BackgroundClass backgroundClass;
    boolean isReview = false;
    Creative creative;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_live_highlight, container, false);
        mContext = getActivity();
        progressBar = view.findViewById(R.id.highlightProgress);
        highlightRecyclerView = view.findViewById(R.id.liveHighlightRecyclerView);
        emptyText = view.findViewById(R.id.highlightEmptyText);
        backgroundClass = new BackgroundClass(mContext);
        creative = new Creative();

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 2);
        highlightRecyclerView.setLayoutManager(layoutManager);
        highlightRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, backgroundClass.dpToPx(5), true));
        highlightRecyclerView.setItemAnimator(new DefaultItemAnimator());
        if (backgroundClass.isNetworkConnected()) {
            getLiveHighlight();
            getReviewHighlight();
        }else {
            Toast.makeText(mContext, "Check network connection!", Toast.LENGTH_LONG).show();
        }
        return view;
    }

    private void getLiveHighlight() {
        try {
            isReview = false;
            String org_id = PreferenceHelper.getValueString(mContext, AppConfig.ORG_ID);
            final String token = PreferenceHelper.getValueString(mContext, AppConfig.USER_TOKEN);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("organization_id", org_id);
            jsonObject.put("screen_type","HIGHLIGHT");
            jsonObject.put("Live",1);
            jsonObject.put("Archived",0);
            RequestQueue queue = Volley.newRequestQueue(mContext);
            progressBar.setVisibility(View.VISIBLE);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.LIVE_HIGHLIGHT_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONObject stringResponse = new JSONObject(response);
                        boolean success = stringResponse.getBoolean("is_success");
                        String requestCode = stringResponse.getString("status_code");
                        if (success) {
                            if (requestCode.equals("200")) {
                                Log.d("Get live highlight","Success");
                                String result = stringResponse.getString("result");
                                JSONArray resultArray = new JSONArray(result);
                                if (resultArray.length()>0){
                                    for (int i = 0; i < resultArray.length(); i++) {
                                        creative = new Creative();
                                        JSONObject resultObject = resultArray.getJSONObject(i);
                                        creative.setDuration(resultObject.getInt("duration"));
                                        creative.setType(resultObject.getString("type"));
                                        creative.setMedia("https://" + resultObject.getString("media_url"));
                                        creative.setId(resultObject.getString("id"));
                                        creative.setStatus("Live");
                                        creativeArrayList.add(creative);
                                    }
                                }
                            }else{
                                Log.d("Get live highlight","Failure");
                            }
                        } else {
                            Log.d("Is success","False");
                            Toast.makeText(mContext, "Please Re-login!", Toast.LENGTH_LONG).show();
                        }
                    }catch (JSONException | NullPointerException je){
                        je.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Get highlight volley","Failure");
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

    private void getReviewHighlight() {
        try {
            String org_id = PreferenceHelper.getValueString(mContext, AppConfig.ORG_ID);
            final String token = PreferenceHelper.getValueString(mContext, AppConfig.USER_TOKEN);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("organization_id", org_id);
            jsonObject.put("screen_type","HIGHLIGHT");
            jsonObject.put("Live",1);
            jsonObject.put("Archived",0);
            RequestQueue queue = Volley.newRequestQueue(mContext);
            progressBar.setVisibility(View.VISIBLE);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.PENDING_CREATIVE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONObject stringResponse1 = new JSONObject(response);
                        boolean success1 = stringResponse1.getBoolean("is_success");
                        String requestCode1 = stringResponse1.getString("status_code");
                        if (success1) {
                            if (requestCode1.equals("200")) {
                                Log.d("Get live highlight", "Success");
                                String result = stringResponse1.getString("result");
                                JSONArray resultArray = new JSONArray(result);
                                if (resultArray.length() > 0) {
                                    for (int i = 0; i < resultArray.length(); i++) {
                                        creative = new Creative();
                                        JSONObject resultObject = resultArray.getJSONObject(i);
                                        creative.setDuration(resultObject.getInt("duration"));
                                        creative.setType(resultObject.getString("type"));
                                        creative.setMedia("https://" + resultObject.getString("media_url"));
                                        creative.setId(resultObject.getString("Id"));
                                        creative.setStatus("Review");
                                        creativeArrayList.add(creative);
                                    }
                                }
                            } else {
                                Log.d("Get live highlight", "Failure");
                            }
                        }else {
                            Log.d("Is success","False");
                            Toast.makeText(mContext, "Please Re-login!", Toast.LENGTH_LONG).show();
                        }
                    }catch (JSONException | NullPointerException je){
                        je.printStackTrace();
                    }
                    new LoadCreative(mContext).execute();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Get highlight volley","Failure");
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
                highlightGridAdapter = new HighlightGridAdapter(context,creativeArrayList);
                highlightGridAdapter.notifyDataSetChanged();
                highlightRecyclerView.setAdapter(highlightGridAdapter);
            }else{
                progressBar.setVisibility(View.GONE);
                emptyText.setVisibility(View.VISIBLE);
            }
        }
    }
}
