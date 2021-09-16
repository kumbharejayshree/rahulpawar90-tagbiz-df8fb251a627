package com.tagloy.tagbiz.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.utils.BackgroundClass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import id.zelory.compressor.Compressor;
import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView captureView, captureBackground;
    Context mContext;
    RelativeLayout previewRL;
    Spinner outletSpinner;
    Button publishButton;
    BackgroundClass backgroundClass;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final String FILE_AUTHORITY  = "com.tagloy.tagbiz.fileprovider";
    private static final int PERMISSION_REQUEST = 123;
    String mTempPhotoPath, savedImagePath;
    ArrayList<String> organizationArrayList = new ArrayList<>();
    public Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Firebase Crashlytics configuration
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        backgroundClass = new BackgroundClass(this);
        previewRL = findViewById(R.id.previewRL);
        captureView = findViewById(R.id.captureView);
        outletSpinner = findViewById(R.id.outletSpinner);
        publishButton = findViewById(R.id.publishButton);
        captureBackground = findViewById(R.id.imageViewBackground);
        mContext = this;
        checkPermission();
        publishButton.setOnClickListener(this);
        backgroundClass.getOutlets();
        backgroundClass.setSpinner(outletSpinner);
    }

    //Test force crash
    public void forceCrash(MenuItem menuItem) {
        throw new RuntimeException();
    }

    //Check if camera permission is granted and launch camera if it is
    private void checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST);
        } else {
            launchCamera(mContext);
        }
    }

    //Launch camera intent
    public void launchCamera(Context context) {
        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this, FILE_AUTHORITY, photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    //Creating image file for captured image with current timestamp
    private static File createImageFile() throws IOException{
        // Create an image file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"TagBiz");
        if (! storageDir.exists()){
            if (! storageDir.mkdirs()){
                System.out.println("Directory not created");
                return null;
            }
        }
        return new File(storageDir.getAbsolutePath() + File.separator +
                "JPEG_" + timeStamp + ".jpg");
    }


    //Delete temporary image file
    public boolean deleteImageFile(String imagePath) {

        // Get the file
        File imageFile = new File(imagePath);

        // Delete the image
        boolean deleted = imageFile.delete();

        // If there is an error deleting the file, show a Toast
        if (!deleted) {
            Log.d("Status", "deleteImageFile: Error");

        }
        return deleted;
    }

    //Get bitmap of the image file
    public Bitmap setPic(String imagePath) {

        // Get device screen size information
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);

        int targetH = metrics.heightPixels;
        int targetW = metrics.widthPixels;

        // Get the dimensions of the original bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(imagePath);
    }

    //Refresh gallery once image file is added
    public void refreshGallery(String filepath){
        MediaScannerConnection.scanFile(mContext,
                new String[] { filepath }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }

    public Bitmap imageRotation(String photoPath,Bitmap bitmap1){
        Bitmap rotatedBitmap = null;
        try{
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap1, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap1, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap1, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap1;
            }
        }catch (IOException ie){
            ie.printStackTrace();
        }
        return rotatedBitmap;
    }

    private Bitmap rotateImage(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void saveImage(Bitmap myBitmap){
        // Create the new file in the external storage
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"TagBiz");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        // Save the new Bitmap
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                refreshGallery(savedImagePath);
                FileOutputStream fOut = new FileOutputStream(imageFile);
                fOut.write(bytes.toByteArray());
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (mTempPhotoPath != null) {
                Log.d("Path", mTempPhotoPath);
                bitmap = setPic(mTempPhotoPath);
                bitmap = imageRotation(mTempPhotoPath, bitmap);
                deleteImageFile(mTempPhotoPath);
                saveImage(bitmap);
                previewRL.setVisibility(View.VISIBLE);
                String[] path = savedImagePath.split("/", 2);
                Glide.with(mContext)
                        .load(new File(path[1]))
                        .into(captureView);
                Glide.with(mContext).load(new File(path[1]))
                        .apply(bitmapTransform(new BlurTransformation(25)))
                        .into(captureBackground);
            }
        } else {
            backgroundClass.openGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            boolean allgranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }
            if(allgranted){
                launchCamera(mContext);
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.CAMERA)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},PERMISSION_REQUEST);
                }
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },PERMISSION_REQUEST);
                }
            } else {
                Toast.makeText(MainActivity.this,"Permission is mandatory, Try giving it from App Settings",Toast.LENGTH_LONG).show();
            }
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
        try {
            if (organizationArrayList.size() != 0){
                String[] path  = savedImagePath.split("/",2);
                publishButton.setClickable(false);
                String hashes = organizationArrayList.toString().replace(", ", " ").replaceAll("[\\[.\\]]", "");
                File comp = new File(path[1]);
                comp = new Compressor(mContext).compressToFile(comp);
                backgroundClass.uploadToS3(hashes,comp);
            }else {
                Toast.makeText(mContext,"Please select at least one outlet!",Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.publishButton){
            if (backgroundClass.isNetworkConnected()){
                publishPics();
            }else {
                Toast.makeText(mContext,"No network! Please connect to network and retry from Gallery",Toast.LENGTH_SHORT).show();
                backgroundClass.openGallery();
            }
        }
    }
}
