package com.tagloy.tagbiz.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.tagloy.tagbiz.activity.MainNavActivity;
import com.tagloy.tagbiz.activity.OutletActivity;
import com.tagloy.tagbiz.adapter.CreativeSpinnerAdapter;
import com.tagloy.tagbiz.adapter.SpinnerAdapter;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
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
import com.tagloy.tagbiz.models.Creative;
import com.tagloy.tagbiz.models.Organization;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import me.drakeet.support.toast.ToastCompat;

//Class for all the background functions in the app
public class BackgroundClass {
    private Context context;
    public ArrayList<Organization> arrayList = new ArrayList<>();
    public List<Organization> outletsArrayList = new ArrayList<>();
    String renameString ="";

    public BackgroundClass(Context context) {
        this.context = context;
    }

    //User login function
    public void loginUser(final String username, final String password, final int org_id) {
        try {
            PreferenceHelper.setValueString(context, AppConfig.USERNAME, username);
            PreferenceHelper.setValueString(context, AppConfig.PASSWORD, password);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", username);
            jsonObject.put("password", password);
            if (org_id != 0) {
                jsonObject.put("organization_id", org_id);
                PreferenceHelper.setValueBoolean(context, AppConfig.HAS_MULTIPLE, true);
            } else {
                PreferenceHelper.setValueBoolean(context, AppConfig.HAS_MULTIPLE, false);
            }
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.LOGIN_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject stringResponse = new JSONObject(response);
                        boolean success = stringResponse.getBoolean("is_success");
                        String requestCode = stringResponse.getString("status_code");
                        String message = stringResponse.getString("message");
                        if (success) {
                            if (requestCode.equals("200")) {
                                Log.d("Login", "Success");
                                Date date = new Date();
                                long time = (date.getTime() + TimeUnit.DAYS.toMillis(40)) / 1000;
                                String result = stringResponse.getString("result");
                                JSONObject resultResponse = new JSONObject(result);
                                String user_id = resultResponse.getString("id");
                                String first_name = resultResponse.getString("first_name");
                                String token = resultResponse.getString("token");
                                String role_id = resultResponse.getString("role_id");
                                String org_id = resultResponse.getString("organization_id");
                                String org_name = resultResponse.getString("organization_name");
                                String third_party = resultResponse.getString("is_third_party");
                                if (Integer.parseInt(third_party) == 1) {
                                    PreferenceHelper.setValueBoolean(context, AppConfig.THIRD_PARTY, true);
                                    String logo = resultResponse.getString("third_party_logo");
                                    PreferenceHelper.setValueString(context, AppConfig.ORG_ICON, logo);
                                } else {
                                    PreferenceHelper.setValueBoolean(context, AppConfig.THIRD_PARTY, false);
                                }
                                PreferenceHelper.setValueString(context, AppConfig.ORG_NAME, org_name);
                                PreferenceHelper.setValueString(context, AppConfig.FIRST_NAME, first_name);
                                PreferenceHelper.setValueString(context, AppConfig.USER_ID, user_id);
                                PreferenceHelper.setValueString(context, AppConfig.ORG_ID, org_id);
                                PreferenceHelper.setValueString(context, AppConfig.ROLE_ID, role_id);
                                PreferenceHelper.setValueString(context, AppConfig.USER_TOKEN, token);
                                PreferenceHelper.setValueString(context, AppConfig.LOGIN_TIME, String.valueOf(time));
                                Intent intent = new Intent(context, MainNavActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(intent);
                            }
                        } else {
                            Log.d("Login", "Failure");
                            if (requestCode.equals("401")) {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Login API", "Failure");
                    if (error == null || error.networkResponse == null) {
                        Toast.makeText(context, "Network error! Please try again in some time", Toast.LENGTH_SHORT).show();
                        Log.d("Login API", "Fail");
                        return;
                    }
                    String body;
                    try {
                        Organization organization;
                        int statusCode = error.networkResponse.statusCode;
                        Log.d("Status", String.valueOf(statusCode));
                        if (statusCode == 400) {
                            body = new String(error.networkResponse.data, "UTF-8");
                            JSONObject resultJson = new JSONObject(body);
                            Intent intent = new Intent(context, OutletActivity.class);
                            context.startActivity(intent);
                        } else if (statusCode == 401) {
                            Toast.makeText(context, "Please check credentials!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (UnsupportedEncodingException | JSONException ue) {
                        ue.printStackTrace();
                    }
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
            queue.add(stringRequest);
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    public void showMessage(String message) {
        if (android.os.Build.VERSION.SDK_INT == 25) {
            ToastCompat.makeText(context, message, Toast.LENGTH_SHORT)
                    .setBadTokenListener(toast -> {
                        Log.e("failed toast", message);
                    }).show();
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }


    //Check user token validity which is set to 12 Hours
    public boolean checkLogin() {
        try {
            String time = PreferenceHelper.getValueString(context, AppConfig.LOGIN_TIME);
            long savedTime = Long.parseLong(time);
            long currentTime = System.currentTimeMillis() / 1000;
            return savedTime > currentTime;
        } catch (NullPointerException ne) {
            ne.printStackTrace();
        } catch (NumberFormatException nr) {
            nr.printStackTrace();
        }
        return false;
    }

    //Get outlet list accessible to the user
    public void getOutlets() {
        try {
            String user_id = PreferenceHelper.getValueString(context, AppConfig.USER_ID);
            String role_id = PreferenceHelper.getValueString(context, AppConfig.ROLE_ID);
            String org_id = PreferenceHelper.getValueString(context, AppConfig.ORG_ID);
            final String token = PreferenceHelper.getValueString(context, AppConfig.USER_TOKEN);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", user_id);
            jsonObject.put("role_id", role_id);
            jsonObject.put("organization_id", org_id);
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.GET_OUTLET_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        Organization organization;
                        JSONObject stringResponse = new JSONObject(response);
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
                                    arrayList.add(organization);
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please Re-login!", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
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

    //Get outlets
    public void getOut() {
        try {
            String user_id = PreferenceHelper.getValueString(context, AppConfig.USER_ID);
            String role_id = PreferenceHelper.getValueString(context, AppConfig.ROLE_ID);
            String org_id = PreferenceHelper.getValueString(context, AppConfig.ORG_ID);
            final String token = PreferenceHelper.getValueString(context, AppConfig.USER_TOKEN);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", user_id);
            jsonObject.put("role_id", role_id);
            jsonObject.put("organization_id", org_id);
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.GET_OUTLET_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        Organization organization;
                        JSONObject stringResponse = new JSONObject(response);
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
                                    arrayList.add(organization);
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please Re-login!", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
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

    //Set outlets to spinner for feeds
    public void setSpinner(Spinner spinner) {
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(context, 0, arrayList, spinner);
        spinner.setAdapter(spinnerAdapter);
        spinnerAdapter.notifyDataSetChanged();
    }

    //Set outlets to spinner for creative
    public void setCreativeSpinner(Spinner spinner) {
        CreativeSpinnerAdapter creativeSpinnerAdapter = new CreativeSpinnerAdapter(context, 0, arrayList, spinner);
        spinner.setAdapter(creativeSpinnerAdapter);
        creativeSpinnerAdapter.notifyDataSetChanged();
    }

    //Function to convert dp to pixels
    public int dpToPx(int dp) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    //Upload feeds to AWS S3
    public void uploadToS3(String hashes, File file) {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                AppConfig.IDENTITY_POOL,
                Regions.AP_SOUTH_1
        );
        AmazonS3 amazonS3 = new AmazonS3Client(credentialsProvider);
        amazonS3.setRegion(Region.getRegion(Regions.AP_SOUTH_1));
        TransferUtility transferUtility = TransferUtility.builder().context(context)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(amazonS3).build();
        getNumber();
        String Name = file.getName();

        int i = Name.lastIndexOf('.');
        String fileExtension = Name.substring(i+1);
        Log.v("FILE EXTENSION: ", fileExtension);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        long mSec = calendar.getTimeInMillis();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dateTime = renameString +"-"+ String.valueOf(mSec)+ "-"+date;
        Log.e("TIME", dateTime);
        String newString = Name.replaceAll(Name,dateTime);
        String finalString= newString+"."+fileExtension;
        Log.e("MMMMMMMM", Name);
        Log.e("NNNNNNNNNN", renameString + "-" );
        final TransferObserver observer = transferUtility.upload(AppConfig.FEED_BUCKET, finalString, file, CannedAccessControlList.PublicRead);
        setTransferListener(hashes, observer);
    }

    //Transfer listener for feeds
    public void setTransferListener(final String hashes, final TransferObserver observer) {
        final ProgressBar progressBar = ((Activity) context).findViewById(R.id.publishLoader);
        final Button button = ((Activity) context).findViewById(R.id.publishButton);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    progressBar.setVisibility(View.GONE);
                    String url = "https://" + AppConfig.FEED_BUCKET + ".s3.amazonaws.com/" + observer.getKey();
                    Log.e("URL", url);
                    publishImage(hashes, url);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(int id, Exception ex) {
                button.setClickable(true);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "Unable to publish! Check network and Retry", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Approve particular feed
    public void approveFeed(int feed_id, String feed_type) {
        try {
            final String token = PreferenceHelper.getValueString(context, AppConfig.USER_TOKEN);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", feed_id);
            jsonObject.put("type", feed_type);
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.APPROVE_FEED_URL, response -> {
                try {
                    JSONObject stringResponse = new JSONObject(response);
                    boolean success = stringResponse.getBoolean("is_success");
                    String requestCode = stringResponse.getString("status_code");
                    if (success) {
                        if (requestCode.equals("200")) {
                            Toast.makeText(context, "Feed approved!", Toast.LENGTH_LONG).show();
//                                openGallery();
                        }
                    } else {
                        Toast.makeText(context, "Please Re-login!", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }, error -> Toast.makeText(context, "Error! Please try again.", Toast.LENGTH_LONG).show()) {
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

    //Reject particular feed
    public void rejectFeed(int feed_id, String feed_type) {
        try {
            final String token = PreferenceHelper.getValueString(context, AppConfig.USER_TOKEN);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", feed_id);
            jsonObject.put("type", feed_type);
            jsonObject.put("rejected_reason", "Rejected");
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.REJECT_FEED_URL, response -> {
                try {
                    JSONObject stringResponse = new JSONObject(response);
                    boolean success = stringResponse.getBoolean("is_success");
                    String requestCode = stringResponse.getString("status_code");
                    if (success) {
                        if (requestCode.equals("200")) {
                            Toast.makeText(context, "Feed rejected!", Toast.LENGTH_LONG).show();
//                            openGallery();
                        }
                    } else {
                        Toast.makeText(context, "Please Re-login!", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }, error -> Toast.makeText(context, "Error! Please try again.", Toast.LENGTH_LONG).show()) {
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

    //Upload creative to AWS S3
    public void uploadCreative(Creative creative, File file) {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                AppConfig.IDENTITY_POOL,
                Regions.AP_SOUTH_1
        );
        AmazonS3 amazonS3 = new AmazonS3Client(credentialsProvider);
        amazonS3.setRegion(Region.getRegion(Regions.AP_SOUTH_1));
        TransferUtility transferUtility = TransferUtility.builder().context(context)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(amazonS3).build();
        final TransferObserver observer = transferUtility.upload(AppConfig.BIZ_BUCKET, file.getName(), file, CannedAccessControlList.PublicRead);
        setCreativeTransferListener(creative, observer);
    }

    //Transfer listener for creative upload to S3
    public void setCreativeTransferListener(final Creative creative, final TransferObserver observer) {
        final ProgressBar progressBar = ((Activity) context).findViewById(R.id.creativeProgress);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    progressBar.setVisibility(View.GONE);
                    String url = "" + AppConfig.BIZ_BUCKET + ".s3.amazonaws.com/" + observer.getKey();
                    publishCreative(creative, url);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(int id, Exception ex) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "Unable to publish! Check network and Retry", Toast.LENGTH_SHORT).show();
                openGallery();
            }
        });
    }

    //Publish selected creative
    public void publishCreative(Creative creative, String media) {
        try {
            String user_id = PreferenceHelper.getValueString(context, AppConfig.USER_ID);
            final String token = PreferenceHelper.getValueString(context, AppConfig.USER_TOKEN);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("title", creative.getTitle());
            jsonObject.put("organization_id", creative.getOrganization_id());
            jsonObject.put("organization_name", creative.getOrganization_name());
            jsonObject.put("type", creative.getType());
            jsonObject.put("screen_type", creative.getScreen_type());
            jsonObject.put("duration", creative.getDuration());
            jsonObject.put("change", creative.getChange());
            jsonObject.put("do", creative.getCreate_do());
            jsonObject.put("media", media);
            jsonObject.put("extension", creative.getExtension());
            jsonObject.put("slot", "pending");
            jsonObject.put("uid", user_id);
            jsonObject.put("force_create", 0);
            if (!creative.getStartD().equals(""))
                jsonObject.put("start_datetime", creative.getStartD());
            if (!creative.getEndD().equals(""))
                jsonObject.put("end_datetime", creative.getEndD());
            if (!creative.getDays().equals(""))
                jsonObject.put("days", creative.getDays());
            if (!creative.getDsTime().equals(""))
                jsonObject.put("dstime", creative.getDsTime());
            if (!creative.getDeTime().equals(""))
                jsonObject.put("detime", creative.getDeTime());
            if (!creative.getAdType().equals(""))
                jsonObject.put("adtype", creative.getAdType());
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.ADD_CREATIVE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        boolean is_success = responseJson.getBoolean("is_success");
                        if (is_success) {
                            String status = responseJson.getString("status_code");
                            if (status.equals("200")) {
                                Toast.makeText(context, "Published successfully!", Toast.LENGTH_LONG).show();
                                openGallery();
                            }
                        }
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error == null || error.networkResponse == null) {
                        Toast.makeText(context, "Network error! Please try again in some time", Toast.LENGTH_SHORT).show();
                        Log.d("Login API", "Fail");
                        return;
                    }
                    String body;
                    try {
                        int statusCode = error.networkResponse.statusCode;
                        Log.d("Status", String.valueOf(statusCode));
                        if (statusCode == 500) {
                            body = new String(error.networkResponse.data, "UTF-8");
                            JSONObject resultJson = new JSONObject(body);
                            String message = resultJson.getString("message");
                            Log.d("Message", message);
                            String result = resultJson.getString("result");
                            Log.d("Result", result);
                        } else if (statusCode == 401) {
                            Toast.makeText(context, "Please check credentials!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (UnsupportedEncodingException | JSONException ue) {
                        ue.printStackTrace();
                    }
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
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(stringRequest);
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    //Delete the Live publisher
    public void deleteCreative(String id) {
        try {
            final String token = PreferenceHelper.getValueString(context, AppConfig.USER_TOKEN);
            String org_id = PreferenceHelper.getValueString(context, AppConfig.ORG_ID);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            jsonObject.put("organization_id", org_id);
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.DELETE_CREATIVE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject stringResponse = new JSONObject(response);
                        boolean success = stringResponse.getBoolean("is_success");
                        String requestCode = stringResponse.getString("status_code");
                        if (success) {
                            if (requestCode.equals("200")) {
                                Toast.makeText(context, "Publisher deleted!", Toast.LENGTH_LONG).show();
                                openGallery();
                            }
                        } else {
                            Toast.makeText(context, "Unauthorized!", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "Error! Please try again.", Toast.LENGTH_LONG).show();
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

    //Delete Live Highlight
    public void deleteHighlight(String id) {
        try {
            final String token = PreferenceHelper.getValueString(context, AppConfig.USER_TOKEN);
            String org_id = PreferenceHelper.getValueString(context, AppConfig.ORG_ID);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            jsonObject.put("organization_id", org_id);
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.DELETE_HIGHLIGHT_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject stringResponse = new JSONObject(response);
                        boolean success = stringResponse.getBoolean("is_success");
                        String requestCode = stringResponse.getString("status_code");
                        if (success) {
                            if (requestCode.equals("200")) {
                                Toast.makeText(context, "Highlight deleted!", Toast.LENGTH_LONG).show();
                                openGallery();
                            }
                        } else {
                            Toast.makeText(context, "Unauthorized!", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "Error! Please try again.", Toast.LENGTH_LONG).show();
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

    //Publish selected image as feed
    public void publishImage(String hash, String media) {
        try {
            long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
            String username = PreferenceHelper.getValueString(context, AppConfig.FIRST_NAME);
            final String token = PreferenceHelper.getValueString(context, AppConfig.USER_TOKEN);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("caption", hash);
            jsonObject.put("media_type", "image");
            jsonObject.put("like", "0");
            jsonObject.put("img_id", String.valueOf(number));
            jsonObject.put("username", username);
            jsonObject.put("media_url", media);
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.PUBLISH_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        boolean is_success = responseJson.getBoolean("is_success");
                        if (is_success) {
                            String message = responseJson.getString("message");
                            if (message.equals("SUCCESS")) {
                                Toast.makeText(context, "Image(s) published successfully!", Toast.LENGTH_LONG).show();
                                openGallery();
                            }
                        }
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "Publish unsuccessful, Please try again...", Toast.LENGTH_LONG).show();
                    openGallery();
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
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(stringRequest);
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    //Open mainNavActivity on completion of certain task
    public void openGallery() {
        Intent intent = new Intent(context, MainNavActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    //Check if device is connected to network
    public boolean isNetworkConnected() {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public void getNumber() {
        Random number = new Random();

        StringBuilder builder = new StringBuilder();
        for(
                int count = 0;
                count<=4;count++)

        {
            builder.append(number.nextInt(4));
        }

        renameString =builder.toString();
    }


}

