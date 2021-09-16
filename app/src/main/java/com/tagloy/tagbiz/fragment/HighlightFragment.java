package com.tagloy.tagbiz.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.models.Creative;
import com.tagloy.tagbiz.utils.BackgroundClass;
import com.tagloy.tagbiz.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import id.zelory.compressor.Compressor;

public class HighlightFragment extends Fragment implements View.OnClickListener {

    ImageView selectedImageViewHighlight, galleryButton;
    EditText titleEdit, durationEdit;
    Spinner outletSpinner;
    VideoView selectedVideoViewHighlight;
    Context mContext;
    Button publishButton;
    BackgroundClass backgroundClass;
    LinearLayout imageViewLayout;
    RelativeLayout mainViewLayout;
    Creative creative;
    File selectedFile;
    ArrayList<String> orgIdList = new ArrayList<>();
    ArrayList<String> orgNameList = new ArrayList<>();
    public static final int GALLERY_REQUEST = 123;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_highlight, container, false);
        mContext = getActivity();
        backgroundClass = new BackgroundClass(mContext);
        mainViewLayout = view.findViewById(R.id.mainViewLayoutHighlight);
        imageViewLayout = view.findViewById(R.id.imageViewLayoutHighlight);
        galleryButton = view.findViewById(R.id.galleryButtonHighlight);
        selectedImageViewHighlight = view.findViewById(R.id.selectedImageHighlight);
        titleEdit = view.findViewById(R.id.titleEditHighlight);
        durationEdit = view.findViewById(R.id.durationEditHighlight);
        outletSpinner = view.findViewById(R.id.outletSpinnerHighlight);
        selectedVideoViewHighlight = view.findViewById(R.id.selectedVideoHighlight);
        publishButton = view.findViewById(R.id.publishButtonHighlight);
        backgroundClass.getOut();
        backgroundClass.setCreativeSpinner(outletSpinner);
        creative = new Creative();

        galleryButton.setOnClickListener(this);
        publishButton.setOnClickListener(this);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Highlight");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.galleryButtonHighlight:
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("image/* video/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
                break;
            case R.id.publishButtonHighlight:
                publishCreative();
                break;
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
                                selectedVideoViewHighlight.setVisibility(View.GONE);
                                selectedImageViewHighlight.setVisibility(View.VISIBLE);
                                File comp = new File(path[1]);
                                comp = new Compressor(mContext).compressToFile(comp);
                                long size1 = comp.length();
                                Glide.with(mContext).load(comp).into(selectedImageViewHighlight);
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
                                    selectedVideoViewHighlight.setVisibility(View.VISIBLE);
                                    selectedImageViewHighlight.setVisibility(View.GONE);
                                    selectedVideoViewHighlight.setVideoURI(Uri.fromFile(new File(path[1])));
                                    selectedVideoViewHighlight.canPause();
                                    selectedVideoViewHighlight.canSeekBackward();
                                    selectedVideoViewHighlight.canSeekForward();
                                    selectedVideoViewHighlight.start();
                                    durationEdit.setText(String.valueOf(duration));
                                    Log.d("duration",String.valueOf(duration));
                                    creative.setType("VIDEO");
                                    selectedFile = new File(path[1]);
                                }else {
                                    backgroundClass.openGallery();
                                    Toast.makeText(mContext,"Video duration can't be more than 60 seconds", Toast.LENGTH_LONG).show();
                                }
                            }else {
                                backgroundClass.openGallery();
                                Toast.makeText(mContext,"Video size can't be more than 20MB", Toast.LENGTH_LONG).show();
                            }
                        }else {
                            backgroundClass.openGallery();
                            Toast.makeText(mContext,"This media format is not supported", Toast.LENGTH_LONG).show();
                        }
                }
            }
        }
    }

    //Publish selected media as creative
    private void publishCreative(){
        for (int i = 0; i < backgroundClass.arrayList.size(); i++) {
            boolean isSelected = backgroundClass.arrayList.get(i).isSelected();
            if (isSelected) {
                String id = backgroundClass.arrayList.get(i).getId();
                orgIdList.add(id.toLowerCase());
                String name = backgroundClass.arrayList.get(i).getOrg_name();
                orgNameList.add(name);
            }
        }
        if (TextUtils.isEmpty(durationEdit.getText())){
            Toast.makeText(mContext,"Please enter duration",Toast.LENGTH_LONG).show();
        }else if(Integer.parseInt(durationEdit.getText().toString()) > 30){
            Toast.makeText(mContext,"Duration can't be more than 30sec",Toast.LENGTH_LONG).show();
        }else {
            if (backgroundClass.isNetworkConnected()){
                if (orgIdList.size() != 0 || orgNameList.size() != 0){
                    Gson gson = new Gson();
                    String ids = gson.toJson(orgIdList);
                    String names = gson.toJson(orgNameList);
                    Log.d("Id", ids);
                    Log.d("Names", names);
                    String title = titleEdit.getText().toString().trim();
                    String duration = durationEdit.getText().toString().trim();
                    creative.setTitle(title);
                    creative.setDuration(Integer.parseInt(duration));
                    creative.setScreen_type("HIGHLIGHT");
                    creative.setCreate_do(6);
                    creative.setChange("New Highlight");
                    creative.setOrganization_id(ids);
                    creative.setOrganization_name(names);
                    creative.setStartD("");
                    creative.setEndD("");
                    creative.setDays("");
                    creative.setDsTime("");
                    creative.setDeTime("");
                    creative.setAdType("");
                    backgroundClass.uploadCreative(creative, selectedFile);
                }else {
                    Toast.makeText(mContext,"Please select at least one outlet!",Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(mContext,"Please check network connection!",Toast.LENGTH_LONG).show();
            }
        }
    }
}
