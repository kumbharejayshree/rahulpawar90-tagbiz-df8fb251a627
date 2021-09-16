package com.tagloy.tagbiz.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.activity.MainActivity;
import com.tagloy.tagbiz.adapter.ImagePagerAdapter;
import com.tagloy.tagbiz.models.Captures;
import com.tagloy.tagbiz.utils.AppConfig;
import com.tagloy.tagbiz.utils.BackgroundClass;
import com.tagloy.tagbiz.utils.FileUtils;
import com.tagloy.tagbiz.utils.PreferenceHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import id.zelory.compressor.Compressor;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddFeedFragment extends Fragment implements View.OnClickListener {

    Context mContext;
    ImageView galleryButton, cameraButton, orgIconView;
    public static final int GALLERY_REQUEST = 12;
    private List<Captures> imagePathList;
    ViewPager viewPager;
    Spinner outSpinner;
    BackgroundClass backgroundClass;
    RelativeLayout resultView, mainView;
    Button publishBtn, reselectButton, homeButton;
    ArrayList<String> organizationArrayList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_feed, container, false);
        mContext = getActivity();
        backgroundClass = new BackgroundClass(mContext);
        mainView = view.findViewById(R.id.mainView);
        resultView = view.findViewById(R.id.resultView);
        publishBtn = view.findViewById(R.id.publishButton);
        orgIconView = view.findViewById(R.id.orgIconView);
        viewPager = view.findViewById(R.id.imageViewPager);
        galleryButton = view.findViewById(R.id.galleryButton);
        cameraButton = view.findViewById(R.id.cameraButton);
        homeButton = view.findViewById(R.id.homeButton);
        reselectButton = view.findViewById(R.id.reselectButton);
        outSpinner = view.findViewById(R.id.outSpinner);
        imagePathList = new ArrayList<>();
        galleryButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        publishBtn.setOnClickListener(this);
        homeButton.setOnClickListener(this);
        reselectButton.setOnClickListener(this);
        backgroundClass.getOutlets();
        backgroundClass.setSpinner(outSpinner);

        try {
            boolean thirdParty = PreferenceHelper.getValueBoolean(mContext, AppConfig.THIRD_PARTY);
            if (thirdParty){
                String orgIcon = PreferenceHelper.getValueString(mContext, AppConfig.ORG_ICON);
                if (!orgIcon.isEmpty()) {
                    orgIconView.setVisibility(View.VISIBLE);
                    Glide.with(mContext)
                            .load(Uri.parse(orgIcon)).centerInside()
                            .into(orgIconView);
                }
            }
        }catch (NullPointerException ne){
            ne.printStackTrace();
        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK && data!=null){
            ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(mContext,imagePathList);
            Captures captures;
            if (data.getClipData()!=null){
                int count = data.getClipData().getItemCount();
                for (int i=0;i<count;i++){
                    captures = new Captures();
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();

                    String basePath = FileUtils.getPath(mContext,imageUri);
                    String[] path  = new String[0];
                    if (basePath != null) {
                        path = basePath.split("/",2);
                    }
                    captures.imagePath =  path[1];
                    imagePathList.add(captures);
                    Log.d("Image",basePath);
                }
            }else if (data.getData()!=null){
                captures = new Captures();
                try{
                    Uri imageUri = data.getData();
                    String basePath = FileUtils.getPath(mContext,imageUri);
                    String[] path  = new String[0];
                    if (basePath != null) {
                        path = basePath.split("/",2);

                    }
                    captures.imagePath = path[1];
                    Log.e("Name", String.valueOf(captures));
                    imagePathList.add(captures);
                    Log.d("Image",basePath);
                }catch (NullPointerException ne){ne.printStackTrace();}
            }
            viewPager.setAdapter(imagePagerAdapter);
            mainView.setVisibility(View.GONE);
            resultView.setVisibility(View.VISIBLE);
        }
    }

    public void publishPics(){

        for (int i = 0; i < backgroundClass.arrayList.size(); i++) {
            boolean isSelected = backgroundClass.arrayList.get(i).isSelected();
            if (isSelected) {
                String tag = backgroundClass.arrayList.get(i).getHash_tag();
                organizationArrayList.add(tag.toLowerCase());
            }
        }
        if (organizationArrayList.size() != 0){
            publishBtn.setClickable(false);
            String hashes = organizationArrayList.toString().replace(", ", " ").replaceAll("[\\[.\\]]", "");
            Log.d("Hash", hashes);
            try {
                for (int i=0;i<imagePathList.size();i++){
                    String basePath = imagePathList.get(i).imagePath;

                    File comp = new File(basePath);
                    Log.e("Comp", String.valueOf(comp));
                    comp = new Compressor(mContext).compressToFile(comp);
                    backgroundClass.uploadToS3(hashes,comp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            Toast.makeText(mContext,"Please select at least one outlet!",Toast.LENGTH_LONG).show();
        }
    }

    public void openGalleryInt(){
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(galleryIntent,GALLERY_REQUEST);
    }

    //Test force crash
    public void forceCrash() {
        throw new RuntimeException("Test Crash");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.galleryButton:
                openGalleryInt();

                break;

            case R.id.cameraButton:
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;

            case R.id.publishButton:
                if (backgroundClass.isNetworkConnected()){
                    publishPics();
                }else {
                    Toast.makeText(mContext,"No network, please retry!",Toast.LENGTH_SHORT).show();
                    backgroundClass.openGallery();
                }
                break;
            case R.id.reselectButton:
                imagePathList.clear();
                openGalleryInt();
                break;

            case R.id.homeButton:
                backgroundClass.openGallery();
                break;
        }
    }
}
