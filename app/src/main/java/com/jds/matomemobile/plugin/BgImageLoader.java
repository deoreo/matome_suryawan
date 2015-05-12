package com.jds.matomemobile.plugin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.jds.matomemobile.R;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by JDS on 4/29/15.
 */
public class BgImageLoader extends AsyncTask<String, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewWeakReference;

    public BgImageLoader(ImageView imageView) {
        imageViewWeakReference = new WeakReference<ImageView>(imageView);
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        return downloadBitmap(strings[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(isCancelled()){
            bitmap = null;
        }

        if(imageViewWeakReference != null){
            ImageView imageView = imageViewWeakReference.get();
            if(imageView != null){
                if(bitmap != null){
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    private Bitmap downloadBitmap(String url){
        HttpURLConnection urlConnection = null;

        try {
            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();

            if(statusCode == HttpStatus.SC_OK){
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            if(inputStream != null){
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            urlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            urlConnection.disconnect();
        } finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
        }
        return null;
    }
}
