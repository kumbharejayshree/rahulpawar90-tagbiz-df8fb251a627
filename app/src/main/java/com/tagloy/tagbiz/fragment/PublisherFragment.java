package com.tagloy.tagbiz.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.models.Creative;
import com.tagloy.tagbiz.utils.BackgroundClass;
import com.tagloy.tagbiz.utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import id.zelory.compressor.Compressor;

public class PublisherFragment extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    Spinner outletSpinner;
    VideoView selectedVideoViewPublish;
    ImageView galleryButton, startImage, endImage, startTimeImage, endTimeImage, selectedImageViewPublish,
    sTimeImage, eTimeImage;
    EditText startEdit, endEdit, startTime, endTime, titleEdit, durationEdit, startTimeEdit, endTimeEdit;
    CheckBox suCheck, moCheck, tuCheck, weCheck, thCheck, frCheck, saCheck;
    Calendar myCalendar;
    Context mContext;
    LinearLayout dayWiseLayoutPublish, imageViewLayout;
    RelativeLayout mainViewLayout;
    RadioButton allDayRadio, dayWiseRadio;
    RadioGroup radioGroup;
    DatePickerDialog date, date1;
    TimePickerDialog time, time1, sTime, eTime;
    Button publishButton;
    BackgroundClass backgroundClass;
    public static final int GALLERY_REQUEST = 12;
    Creative creative;
    ArrayList<String> orgIdList = new ArrayList<>();
    ArrayList<String> orgNameList = new ArrayList<>();
    ArrayList<String> days = new ArrayList<>();
    File selectedFile;
    MediaController mediaController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publisher, container, false);
        mContext = getActivity();
        backgroundClass = new BackgroundClass(mContext);
        mediaController = new MediaController(mContext);
        mainViewLayout = view.findViewById(R.id.mainViewLayoutPublish);
        imageViewLayout = view.findViewById(R.id.imageViewLayoutPublish);
        selectedImageViewPublish = view.findViewById(R.id.selectedImagePublish);
        titleEdit = view.findViewById(R.id.titleEditPublish);
        durationEdit = view.findViewById(R.id.durationEditPublish);
        outletSpinner = view.findViewById(R.id.outletSpinnerPublish);
        selectedVideoViewPublish = view.findViewById(R.id.selectedVideoPublish);
        publishButton = view.findViewById(R.id.publishButtonPublish);
        dayWiseLayoutPublish = view.findViewById(R.id.dayWiseLayoutPublish);
        startTime = view.findViewById(R.id.startTimeEditPublish);
        endTime = view.findViewById(R.id.endTimeEditPublish);
        startTimeEdit = view.findViewById(R.id.sTimeEditPublish);
        endTimeEdit = view.findViewById(R.id.eTimeEditPublish);
        startImage = view.findViewById(R.id.startDateImagePublish);
        endImage = view.findViewById(R.id.endDateImagePublish);
        startTimeImage = view.findViewById(R.id.startTimeImagePublish);
        endTimeImage = view.findViewById(R.id.endTimeImagePublish);
        sTimeImage = view.findViewById(R.id.sTimeImagePublish);
        eTimeImage = view.findViewById(R.id.eTimeImagePublish);
        startEdit = view.findViewById(R.id.startDateEditPublish);
        endEdit = view.findViewById(R.id.endDateEditPublish);
        allDayRadio = view.findViewById(R.id.alldayRadio);
        dayWiseRadio = view.findViewById(R.id.daywiseRadio);
        radioGroup = view.findViewById(R.id.radioGroup);
        suCheck = view.findViewById(R.id.suCheck);
        moCheck = view.findViewById(R.id.moCheck);
        tuCheck = view.findViewById(R.id.tuCheck);
        weCheck = view.findViewById(R.id.weCheck);
        thCheck = view.findViewById(R.id.thCheck);
        frCheck = view.findViewById(R.id.frCheck);
        saCheck = view.findViewById(R.id.saCheck);
        galleryButton = view.findViewById(R.id.galleryButtonPublish);
        myCalendar = Calendar.getInstance();
        int hours = myCalendar.get(Calendar.HOUR_OF_DAY);
        int minutes = myCalendar.get(Calendar.MINUTE);
        int years = myCalendar.get(Calendar.YEAR);
        int months = myCalendar.get(Calendar.MONTH);
        int days = myCalendar.get(Calendar.DAY_OF_MONTH);
        creative = new Creative();

        //Set start date
        date = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                int mon = month + 1;
                startEdit.setText(year + "-" + mon + "-" + day);
                endEdit.getText().clear();
            }
        },years,months,days);
        date.getDatePicker().setMinDate(System.currentTimeMillis());

        //Set end date
        date1 = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                try{
                    int mon = month + 1;
                    String startDate = startEdit.getText().toString();
                    String endDate = year + "-" + mon + "-" + day;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date start = dateFormat.parse(startDate);
                    Date end = dateFormat.parse(endDate);
                    if (!end.before(start)){
                        endEdit.setText(year + "-" + mon + "-" + day);
                    }else {
                        Toast.makeText(mContext,"End date can not be before Start date", Toast.LENGTH_LONG).show();
                    }
                    startTimeEdit.getText().clear();
                    endTimeEdit.getText().clear();
                }catch (ParseException pe){
                    pe.printStackTrace();
                }
            }
        },years,months,days);
        date1.getDatePicker().setMinDate(System.currentTimeMillis());

        //Set start time
        sTime = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                if (minute>=10){
                    startTimeEdit.setText(hour + ":" + minute);
                }else {
                    startTimeEdit.setText(hour + ":0" + minute);
                }
            }
        },hours, minutes, true);

        //Set end time
        eTime = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                try{
                    String startDate = startEdit.getText().toString();
                    String endDate = endEdit.getText().toString();
                    String sTime = startTimeEdit.getText().toString();
                    String eTime = hour + ":" + minute;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                    Date start = dateFormat.parse(sTime);
                    Date end = dateFormat.parse(eTime);
                    if (startDate.equals(endDate) && !start.before(end)){
                        Toast.makeText(mContext,"End time can not be before Start time", Toast.LENGTH_LONG).show();
                    }else {
                        if (minute>=10){
                            endTimeEdit.setText(hour + ":" + minute);
                        }else {
                            endTimeEdit.setText(hour + ":0" + minute);
                        }
                    }
                }catch (ParseException | NullPointerException pe){
                    pe.printStackTrace();
                }
            }
        },hours, minutes, true);

        //Set daywise start time
        time = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                if (minute>=10){
                    startTime.setText(hour + ":" + minute);
                }else {
                    startTime.setText(hour + ":0" + minute);
                }
            }
        },hours, minutes, true);

        //Set daywise end time
        time1 = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                try{
                    String startDate = startEdit.getText().toString();
                    String endDate = endEdit.getText().toString();
                    String sTime = startTime.getText().toString();
                    String eTime = hour + ":" + minute;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                    Date start = dateFormat.parse(sTime);
                    Date end = dateFormat.parse(eTime);
                    if (startDate.equals(endDate) && !start.before(end)){
                        Toast.makeText(mContext,"End time can not be before Start time", Toast.LENGTH_LONG).show();
                    }else {
                        if (minute>=10){
                            endTime.setText(hour + ":" + minute);
                        }else {
                            endTime.setText(hour + ":0" + minute);
                        }
                    }
                }catch (ParseException | NullPointerException pe){
                    pe.printStackTrace();
                }
            }
        },hours, minutes, true);

        startImage.setOnClickListener(this);
        endImage.setOnClickListener(this);
        startTimeImage.setOnClickListener(this);
        endTimeImage.setOnClickListener(this);
        sTimeImage.setOnClickListener(this);
        eTimeImage.setOnClickListener(this);
        galleryButton.setOnClickListener(this);
        publishButton.setOnClickListener(this);
        startEdit.setOnClickListener(this);
        endEdit.setOnClickListener(this);
        startTime.setOnClickListener(this);
        endTime.setOnClickListener(this);
        startTimeEdit.setOnClickListener(this);
        endTimeEdit.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(this);
        backgroundClass.getOut();
        backgroundClass.setCreativeSpinner(outletSpinner);
        // Inflate the layout for this fragment
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Publisher");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.startDateEditPublish:
            case R.id.startDateImagePublish:
                date.show();
                break;
            case R.id.endDateEditPublish:
            case R.id.endDateImagePublish:
                if (TextUtils.isEmpty(startEdit.getText())){
                    Toast.makeText(mContext,"Please select start date", Toast.LENGTH_SHORT).show();
                }else {
                    date1.show();
                }
                break;
            case R.id.sTimeImagePublish:
            case R.id.sTimeEditPublish:
                sTime.setTitle("Set start time");
                sTime.show();
                break;
            case R.id.eTimeImagePublish:
            case R.id.eTimeEditPublish:
                if (TextUtils.isEmpty(startTimeEdit.getText())){
                    Toast.makeText(mContext,"Please select start time", Toast.LENGTH_SHORT).show();
                }else {
                    eTime.setTitle("Set end time");
                    eTime.show();
                }
                break;
            case R.id.startTimeImagePublish:
            case R.id.startTimeEditPublish:
                time.show();
                break;
            case R.id.endTimeImagePublish:
            case R.id.endTimeEditPublish:
                if (TextUtils.isEmpty(startTime.getText())){
                    Toast.makeText(mContext,"Please select start time", Toast.LENGTH_SHORT).show();
                }else {
                    time1.setTitle("Set end time");
                    time1.show();
                }
                break;
            case R.id.galleryButtonPublish:
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/* video/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
                break;
            case R.id.publishButtonPublish:
                publishCreative();
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        View radioButton = ((Activity)mContext).findViewById(checkedId);
        int index = radioGroup.indexOfChild(radioButton);
        if (index == 0){
            dayWiseLayoutPublish.setVisibility(View.GONE);
        }else if (index == 1){
            dayWiseLayoutPublish.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data!=null){
            if (requestCode == GALLERY_REQUEST){
                if (data.getData() != null){
                    mainViewLayout.setVisibility(View.GONE);
                    imageViewLayout.setVisibility(View.VISIBLE);
                    Uri mediaUri = data.getData();
                    String basePath = FileUtils.getPath(mContext,mediaUri);
                    String[] path  = basePath.split("/",2);
                    String type = mContext.getContentResolver().getType(mediaUri);
                    long size = new File(path[1]).length();
                    Log.d("Path", path[1]);
                    Log.d("type", type);
                    Log.d("size",String.valueOf(size));
                    creative.setExtension(type);
                    //If selected media is image
                    if (type.equals("image/jpeg")){
                        if ((size/1000)<=10240){
                            try{
                                selectedVideoViewPublish.setVisibility(View.GONE);
                                selectedImageViewPublish.setVisibility(View.VISIBLE);
                                File comp = new File(path[1]);
                                comp = new Compressor(mContext).compressToFile(comp);
                                long size1 = comp.length();
                                Glide.with(mContext).load(comp).into(selectedImageViewPublish);
                                Log.d("size1",String.valueOf(size1));
                                creative.setType("IMAGE");
                                selectedFile = comp;
                            }catch (IOException ie){
                                ie.printStackTrace();
                            }
                        }else {
                            backgroundClass.openGallery();
                            Toast.makeText(mContext,"Image size can't be more than 5MB", Toast.LENGTH_LONG).show();
                        }
                    }else
                        //Selected media is video
                        if (type.equals("video/mp4")){
                        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                        mediaMetadataRetriever.setDataSource(path[1]);
                        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        String height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                        String width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                        Log.d("Res", height + "*" + width);
                        long duration = Long.parseLong(time);
                        duration = duration / 1000;
                        if ((size/1000)<=20480){
                            if ((duration)<=60){
                                selectedVideoViewPublish.setVisibility(View.VISIBLE);
                                selectedImageViewPublish.setVisibility(View.GONE);
                                mediaController.setAnchorView(selectedVideoViewPublish);
                                selectedVideoViewPublish.setMediaController(mediaController);
                                selectedVideoViewPublish.setVideoURI(Uri.fromFile(new File(path[1])));
                                selectedVideoViewPublish.canPause();
                                selectedVideoViewPublish.canSeekBackward();
                                selectedVideoViewPublish.canSeekForward();
                                selectedVideoViewPublish.start();
                                durationEdit.setText(String.valueOf(duration));
                                creative.setType("VIDEO");
                                selectedFile = new File(path[1]);
                            }else {
                                backgroundClass.openGallery();
                                Toast.makeText(mContext,"Video duration shouldn't be more than 60 seconds", Toast.LENGTH_LONG).show();
                            }
                        }else {
                            backgroundClass.openGallery();
                            Toast.makeText(mContext,"Video size shouldn't be more than 20MB", Toast.LENGTH_LONG).show();
                        }
                    }else {
                        backgroundClass.openGallery();
                        Toast.makeText(mContext,"This media format is not supported", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    private void getDays(){
        if (suCheck.isChecked()){
            days.add("Sunday");
        }
        if (moCheck.isChecked()){
            days.add("Monday");
        }
        if (tuCheck.isChecked()){
            days.add("Tuesday");
        }
        if (weCheck.isChecked()){
            days.add("Wednesday");
        }
        if (thCheck.isChecked()){
            days.add("Thursday");
        }
        if (frCheck.isChecked()){
            days.add("Friday");
        }
        if (saCheck.isChecked()){
            days.add("Saturday");
        }
    }

    //Publish selected media as creative
    private void publishCreative(){
        Gson gson = new Gson();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObject1 = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONArray jsonArray1 = new JSONArray();
        for (int i = 0; i < backgroundClass.arrayList.size(); i++) {
            boolean isSelected = backgroundClass.arrayList.get(i).isSelected();
            if (isSelected) {
                String id = backgroundClass.arrayList.get(i).getId();
                orgIdList.add(id.toLowerCase());
                String name = backgroundClass.arrayList.get(i).getOrg_name();
                orgNameList.add(name);
            }
        }
        if (TextUtils.isEmpty(titleEdit.getText())){
            Toast.makeText(mContext,"Please enter title",Toast.LENGTH_LONG).show();
        }else if (TextUtils.isEmpty(durationEdit.getText())){
            Toast.makeText(mContext,"Please enter duration",Toast.LENGTH_LONG).show();
        }else if (TextUtils.isEmpty(startEdit.getText())){
            Toast.makeText(mContext,"Please enter start date",Toast.LENGTH_LONG).show();
        }else if (TextUtils.isEmpty(endEdit.getText())){
            Toast.makeText(mContext,"Please enter end date",Toast.LENGTH_LONG).show();
        }else if (TextUtils.isEmpty(startTimeEdit.getText())){
            Toast.makeText(mContext,"Please enter start time",Toast.LENGTH_LONG).show();
        }else if (TextUtils.isEmpty(endTimeEdit.getText())){
            Toast.makeText(mContext,"Please enter end time",Toast.LENGTH_LONG).show();
        }else if(Integer.parseInt(durationEdit.getText().toString()) > 30 && creative.getType().equals("IMAGE")){
            Toast.makeText(mContext,"Duration shouldn't be more than 30sec",Toast.LENGTH_LONG).show();
        }else{
            if (backgroundClass.isNetworkConnected()){
                if (orgIdList.size() != 0 || orgNameList.size() != 0){
                    String ids = gson.toJson(orgIdList);
                    String names = gson.toJson(orgNameList);
                    Log.d("Id", ids);
                    Log.d("Names", names);
                    String title = titleEdit.getText().toString().trim();
                    String duration = durationEdit.getText().toString().trim();
                    String sDate = startEdit.getText().toString();
                    String eDate = endEdit.getText().toString();
                    String sTime = startTimeEdit.getText().toString();
                    String eTime = endTimeEdit.getText().toString();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String dtStart = sDate + " " + sTime;
                    String dtEnd = eDate + " " + eTime;
                    try{
                        Date date = dateFormat.parse(dtStart);
                        Date date2 = dateFormat.parse(dtEnd);
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        dtStart = dateFormat.format(date);
                        dtEnd = dateFormat.format(date2);
                        creative.setStartD(dtStart);
                        creative.setEndD(dtEnd);
                        Log.d("UTC start", dtStart);
                        Log.d("UTC End", dtEnd);
                    }catch (ParseException pe){pe.printStackTrace();}
                    creative.setTitle(title);
                    creative.setDuration(Integer.parseInt(duration));
                    creative.setScreen_type("TAGPUBLISH");
                    creative.setCreate_do(5);
                    creative.setChange("New Tagpublish");
                    creative.setOrganization_id(ids);
                    creative.setOrganization_name(names);
                    if (allDayRadio.isChecked()){
                        creative.setAdType("Fulltime");
                        creative.setDays("");
                        creative.setDsTime("");
                        creative.setDeTime("");
                        backgroundClass.uploadCreative(creative, selectedFile);
                    }else if (dayWiseRadio.isChecked()){
                        creative.setAdType("Daywise");
                        if (!suCheck.isChecked() && !moCheck.isChecked() && !tuCheck.isChecked() && !weCheck.isChecked()
                                && !thCheck.isChecked() && !frCheck.isChecked() && !saCheck.isChecked()){
                            Toast.makeText(mContext,"Please select at least one day",Toast.LENGTH_LONG).show();
                        }else {
                            if (TextUtils.isEmpty(startTime.getText())){
                                Toast.makeText(mContext,"Please enter day wise start time",Toast.LENGTH_LONG).show();
                            }else if (TextUtils.isEmpty(endTime.getText())){
                                Toast.makeText(mContext,"Please enter day wise end time",Toast.LENGTH_LONG).show();
                            }else {
                                String start = startTime.getText().toString();
                                String end = endTime.getText().toString();
                                SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm");
                                try{
                                    Date date = dateFormat1.parse(start);
                                    Date date2 = dateFormat1.parse(end);
                                    dateFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    start = dateFormat1.format(date);
                                    end = dateFormat1.format(date2);
                                    creative.setStartD(dtStart);
                                    creative.setEndD(dtEnd);
                                }catch (ParseException pe){pe.printStackTrace();}
                                jsonArray.put(start);
                                jsonArray1.put(end);
                                getDays();
                                String dayyss = gson.toJson(days);
                                for (int i=0; i<days.size();i++){
                                    String d = days.get(i);
                                    try{
                                        jsonObject.put("S" + d,jsonArray);
                                        jsonObject1.put("E" + d,jsonArray1);
                                    }catch (JSONException je){
                                        je.printStackTrace();
                                    }
                                }
                                String sD = jsonObject.toString();
                                String eD = jsonObject1.toString();
                                creative.setDays(dayyss);
                                creative.setDsTime(sD);
                                creative.setDeTime(eD);
                                backgroundClass.uploadCreative(creative, selectedFile);
                            }
                        }
                    }else {
                        Toast.makeText(mContext,"Please select if creative to be shown all days or day wise",Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(mContext,"Please select at least one outlet!",Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(mContext,"Please check network connection!",Toast.LENGTH_LONG).show();
            }
        }
    }
}
