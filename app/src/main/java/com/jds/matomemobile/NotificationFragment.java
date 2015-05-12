package com.jds.matomemobile;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.jds.matomemobile.plugin.DBHelper;

import java.sql.SQLException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends android.support.v4.app.Fragment {

    // --Commented out by Inspection (5/8/15, 9:43 AM):BrowserFragment mainFragment;
    private Button notification_close;
    private ListView notification_list;
    private ProgressBar notification_list_loader;

    private final DBHelper dbHelper = new DBHelper(parent());
    private String[] values;
    private ArrayList<ArrayList<Object>> object;
    private ArrayAdapter<String> arrayAdapter;
    private NotificationAdapter notificationAdapter;
    private ArrayList<ArrayList<Object>> dataArray;
    private int count = 0;

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.notification_dialog, container, false);

        rootView.setFocusable(true);

        notification_close = (Button) rootView.findViewById(R.id.notification_close);

        RelativeLayout notification_frame = (RelativeLayout) rootView.findViewById(R.id.notification_frame);
        notification_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseNotification();

            }
        });

        //setup listview in notificaiton area
        notification_list = (ListView) rootView.findViewById(R.id.notification_list);
        notification_list.setClickable(true);
        notification_list_loader = (ProgressBar) rootView.findViewById(R.id.notification_list_loader);

        notification_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadListView();
    }


    private void loadListView(){
        new AsyncTask<Void, Void, Boolean>(){

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    dbHelper.openDatabase();
                    count = dbHelper.countData();
                    dataArray = dbHelper.getAllData();

                    dbHelper.closeDatabase();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if(count > 0){
                    object = dataArray;
                    //Log.d("data", array[0].toString());
/*
            String data = new String();

            for (int i=0; i<object.size(); i++){
                Log.d("data ", String.valueOf(i) + ": " + object.get(i).get(1).toString());
                if(i > 0) {
                    data = data + "," + object.get(i).get(1).toString();
                } else {
                    data = object.get(i).get(1).toString();
                }
            }
            String value = "";

            values = data.split(",");
            arrayAdapter = new ArrayAdapter<String>(parent(), android.R.layout.simple_list_item_1, android.R.id.text1, values);
*/
                    for (int i=0; i< object.size(); i++){
                        Log.d("data", object.get(i).toString());
                    }
                    notificationAdapter = new NotificationAdapter(parent().getApplicationContext(), object);
                    return true;
                } else {
                    values = new String[]{"There is no new notifications"};
                    arrayAdapter = new ArrayAdapter<>(parent(), android.R.layout.simple_list_item_1, android.R.id.text1, values);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);

                notification_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CloseNotification();
                    }
                });

                if(aBoolean){
                    notification_list.setAdapter(notificationAdapter);
                } else {
                    notification_list.setAdapter(arrayAdapter);
                }

                notification_list.setVisibility(View.VISIBLE);
                notification_list_loader.setVisibility(View.GONE);
            }
        }.execute();
    }

    private void CloseNotification(){
        parent().getSupportFragmentManager()
                .beginTransaction()
                .remove(parent().getSupportFragmentManager().findFragmentByTag("notification"))
                .commit();

    }
    private ManagerActivity parent() {
        return ((ManagerActivity) getActivity());
    }

}
