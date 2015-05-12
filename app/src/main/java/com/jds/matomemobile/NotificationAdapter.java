package com.jds.matomemobile;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jds.matomemobile.plugin.DBHelper;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

public class NotificationAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<ArrayList<Object>> notificationList;
    private final DBHelper customDB;

    private ImageView image;

    public NotificationAdapter(Context context, ArrayList<ArrayList<Object>> list) {
        this.context = context;
        notificationList = list;
        customDB = new DBHelper(context);
    }

    @Override
    public int getCount() {
        return notificationList.size();
    }

    @Override
    public Object getItem(int position) {
        return notificationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.notification_adapter, null);

        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(notificationList.get(position).get(1).toString());
        TextView content = (TextView) view.findViewById(R.id.content);
        content.setText(notificationList.get(position).get(2).toString());

        image = (ImageView) view.findViewById(R.id.img);

        new AsyncTask<String, Void, Bitmap>(){

            @Override
            protected Bitmap doInBackground(String... strings) {
                try{
                    Log.d("url", "loading this url");
                    URL imageURL = new URL("http://192.168.200.222:8000/matomemobileserver/assets/thumbnail/201504091601475.jpg");
                    return BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                if (bitmap != null) {
                    image.setImageBitmap(bitmap);
                }
            }
        }.execute();

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                Log.d("motion event", event.toString());
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    Log.d("position", notificationList.get(position).toString());
                    view.setBackgroundColor(Color.LTGRAY);
                } else
                if(event.getAction() == MotionEvent.ACTION_UP){
                    view.setBackgroundColor(Color.WHITE);

                    ContentValues values = new ContentValues();
                    values.put("status", "1");
                    String[] data = new String[]{notificationList.get(position).get(0).toString()};

                    try {
                        customDB.openDatabase();
                        customDB.updateContent("storage", values, "id", data);
                        customDB.closeDatabase();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                } else
                if(event.getAction() == MotionEvent.ACTION_CANCEL){
                    view.setBackgroundColor(Color.WHITE);
                }

                return false;
            }
        });

        return view;
    }

}
