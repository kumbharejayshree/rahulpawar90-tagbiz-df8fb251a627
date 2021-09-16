package com.tagloy.tagbiz.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.utils.AppConfig;
import com.tagloy.tagbiz.utils.BackgroundClass;
import com.tagloy.tagbiz.utils.PreferenceHelper;


public class LoginActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 101;
    BackgroundClass backgroundClass;
    EditText usernameEdit, passwordEdit;
    Button loginButton;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Firebase Crashlytics configuration
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        usernameEdit = findViewById(R.id.username_edit);
        passwordEdit = findViewById(R.id.password_edit);
        loginButton = findViewById(R.id.login_button);
        mContext = this;
        backgroundClass = new BackgroundClass(this);
        checkPermission();
        if (backgroundClass.checkLogin()){
            Intent intent = new Intent(this, MainNavActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else {
            String token;
            try {
                token = PreferenceHelper.getValueString(mContext, AppConfig.USER_TOKEN);
                if (!token.isEmpty()){
                    String username = PreferenceHelper.getValueString(mContext,AppConfig.USERNAME);
                    String pass = PreferenceHelper.getValueString(mContext,AppConfig.PASSWORD);
                    backgroundClass.loginUser(username, pass, 0);
                }
            }catch (NullPointerException ne){
                ne.printStackTrace();
            }
//            PreferenceHelper.clearSharedPreference(this);
        }
        loginButton.setOnClickListener(v -> login());
    }

    private void checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.CAMERA)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},PERMISSION_REQUEST);
                }
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },PERMISSION_REQUEST);
                }
            }else if(ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                    },PERMISSION_REQUEST);
                }
            } else {
                Toast.makeText(mContext,"Camera and Storage permissions granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void login(){
        if (backgroundClass.isNetworkConnected()){
            if (TextUtils.isEmpty(usernameEdit.getText())){
                Toast.makeText(mContext,"Please enter username", Toast.LENGTH_SHORT).show();
            }else if (TextUtils.isEmpty(passwordEdit.getText())){
                Toast.makeText(mContext,"Please enter password", Toast.LENGTH_SHORT).show();
            }else {
                String username = usernameEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                PreferenceHelper.setValueString(mContext, AppConfig.HASH_TAG,"");
                backgroundClass.loginUser(username,password,0);
            }
        }else {
            Toast.makeText(mContext,"Please check network!", Toast.LENGTH_SHORT).show();
        }
    }
}
