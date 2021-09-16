package com.tagloy.tagbiz.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.fragment.HighlightsFragment;
import com.tagloy.tagbiz.fragment.PublishersFragment;
import com.tagloy.tagbiz.fragment.FeedsFragment;
import com.tagloy.tagbiz.receiver.ConnectionReceiver;
import com.tagloy.tagbiz.utils.AppConfig;
import com.tagloy.tagbiz.utils.BackgroundClass;
import com.tagloy.tagbiz.utils.PreferenceHelper;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainNavActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Context mContext;
    FragmentManager fragmentManager;
    DrawerLayout drawer;
    BackgroundClass backgroundClass;
    NavigationView navigationView;
    ConnectionReceiver connectionReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this;
        connectionReceiver = new ConnectionReceiver();
        backgroundClass = new BackgroundClass(mContext);
        fragmentManager = getSupportFragmentManager();
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        View view = navigationView.getHeaderView(0);
        TextView headerText = view.findViewById(R.id.headerTextView);
        ImageView thirdPartyIcon = view.findViewById(R.id.thirdPartyLogo);
        try{
            String org_name = PreferenceHelper.getValueString(mContext, AppConfig.ORG_NAME);
            String hash_tag = PreferenceHelper.getValueString(mContext, AppConfig.HASH_TAG);
            boolean thirdParty = PreferenceHelper.getValueBoolean(mContext, AppConfig.THIRD_PARTY);
            if (thirdParty){
                String orgIcon = PreferenceHelper.getValueString(mContext, AppConfig.ORG_ICON);
                if (!orgIcon.isEmpty()) {
                    thirdPartyIcon.setVisibility(View.VISIBLE);
                    Glide.with(mContext)
                            .load(orgIcon)
                            .into(thirdPartyIcon);
                }
            }
            if (org_name.equals("") && hash_tag.equals("")){
                headerText.setText(R.string.app_name);
            }else if (!org_name.equals("") && hash_tag.equals("")){
                headerText.setText(org_name);
            }else {
                headerText.setText(String.format("%s(%s)", org_name, hash_tag));
            }
        }catch (NullPointerException ne){
            ne.printStackTrace();
        }
        if (savedInstanceState == null){
            FeedsFragment feedsFragment = new FeedsFragment();
            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment,feedsFragment).commit();
            navigationView.setCheckedItem(R.id.nav_feeds);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(connectionReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectionReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            if (backgroundClass.checkLogin()){
                AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                alert.setTitle("Exit!");
                alert.setMessage("Do you want to exit Tagbiz?");
                alert.setPositiveButton("OK", (dialog, which) -> finishAffinity());
                alert.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                alert.show();
            }
        }
    }

    public void logout(){
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle("Logout!");
        alert.setMessage("Do you want to logout?");
        alert.setPositiveButton("OK", (dialog, which) -> {
            PreferenceHelper.clearSharedPreference(mContext);
            Intent intent = new Intent(mContext,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        alert.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        alert.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        boolean hasMultiple = PreferenceHelper.getValueBoolean(mContext,AppConfig.HAS_MULTIPLE);

        switch (menuItem.getItemId()){
            case R.id.nav_feeds:
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new FeedsFragment()).commit();
                break;
            case R.id.nav_publisher:
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new PublishersFragment()).commit();
                break;
            case R.id.nav_highlight:
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new HighlightsFragment()).commit();
                break;
            case R.id.nav_outlets:
                if (hasMultiple){
                    Intent intent = new Intent(mContext,OutletActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(mContext,"You have access to only one outlet!", Toast.LENGTH_LONG).show();
                }

                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
