package com.jds.matomemobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;

import java.io.Serializable;


public class ManagerActivity extends ActionBarActivity {

    // --Commented out by Inspection (5/8/15, 9:53 AM):final ActionBar actionBar = getSupportActionBar();
    private AlertDialog dialog;
    //define main fragment
    private BrowserFragment browserFragment = new BrowserFragment();
    FrameLayout frameLayout;

    public String MAIN_URL = "";
    public String CURRENT_URL = "";
    public String IS_WIFI = "";

//    private CustomWebView.OnClickListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setWindowAnimations(R.anim.abc_fade_in);
        setContentView(R.layout.activity_manager);
        frameLayout = (FrameLayout) findViewById(R.id.fragment_container);




        if (savedInstanceState != null) {
            browserFragment.is_resume = true;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, browserFragment, "main")
                    .commit();
        } else {
            getSupportFragmentManager().executePendingTransactions();
            browserFragment = (BrowserFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSupportFragmentManager().findFragmentById(R.id.fragment_container).setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//        getSupportFragmentManager().putFragment(outState, "ff", browserFragment);
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putSerializable("ff", (Serializable) browserFragment);
    }

    private boolean isInternet() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public void setProgressBar(int progress) {
        browserFragment.progressBar.setProgress(progress);

        if (progress >= 100) {
            browserFragment.progressBar.setVisibility(View.INVISIBLE);
        } else {
            browserFragment.progressBar.setVisibility(View.VISIBLE);
        }
    }


    public void CheckInternet() {
        if (!isInternet()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Internet Connection");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                }
            });
            dialog = builder.create();
            dialog.show();
        }
    }


    @Override
    public void onBackPressed() {
        if (dialog != null) {
            Log.d("dialog", "dialog not null");
        } else if (browserFragment.searchShown()) {
            browserFragment.hideSearchIn();
        } else if (browserFragment.canGoBack()) {
            browserFragment.goBack();
        } else {
            AppExit();
        }
    }


    private void AppExit() {
        this.finish();
        browserFragment.getWebView().destroy();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
