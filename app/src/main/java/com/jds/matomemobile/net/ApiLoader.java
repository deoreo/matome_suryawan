package com.jds.matomemobile.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.jds.matomemobile.plugin.WebData;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Created by windows 7 on 18/03/2015.
 */
public class ApiLoader implements Runnable {
    private String Url, htmlResult;
    private JSONObject jsonObject;
    private boolean finish = false;
    private ImageUrlLoader imageUrlLoader;
    private int loaderRunResult = 0;
    private int connectionRetry = 0;

    public void setUrl(String url) {
        Url = url;
        htmlResult = null;
        connectionRetry = 0;
    }

    public String getHtmlResult() {
        if(htmlResult != null) {
            return htmlResult;
        }
        return "";
    }

    public JSONObject getJsonObject() {
        if(jsonObject != null) {
            return jsonObject;
        }
        return new JSONObject();
    }

    public Bitmap getImage() {
        if (imageUrlLoader != null) {
            return imageUrlLoader.getBitmap();
        }
        return BitmapFactory.decodeByteArray(new byte[0], 0, 0);
    }

    public int getLoaderRunResult() {
        return loaderRunResult;
    }

    public int getConnectionRetry() {
        return connectionRetry;
    }

    public void saveImage(String savePath, String fileName) {
        imageUrlLoader.saveBitmap(savePath, fileName);
    }

    public boolean finished() {
        return finish;
    }

    public void run() {
        connectionRetry++;
        finish = false;
        jsonObject = new JSONObject();
        try {
            String[] urlSplit = Url.split("/");
            String articleKey = urlSplit[urlSplit.length - 1];
            String apiUrl = "http://api.matome.id/1/article/" + articleKey;
            htmlResult = Jsoup.connect(apiUrl).timeout(WebData.REQUEST_TIMEOUT).ignoreContentType(true).execute().body();
            jsonObject = new JSONObject(htmlResult);
            htmlResult = jsonObject.toString(1);
            String img = jsonObject.has("img") ? jsonObject.getString("img") : null;
            imageUrlLoader = new ImageUrlLoader("http://api.matome.id/photo/" + img + "?w=200&h=200&c=fill");
            finish = true;
            loaderRunResult = WebData.ARTICLE_SUCCESS_API;
        } catch (SocketTimeoutException e) {
//            e.printStackTrace();
            htmlResult = "error";
            if(connectionRetry < WebData.MAX_RETRY_CONNECTION) {
                run();
            } else {
                finish = true;
                loaderRunResult = WebData.ARTICLE_FAIL_RTO;
            }
        } catch (IOException e) {
//            e.printStackTrace();
            htmlResult = "error";
            if(connectionRetry < WebData.MAX_RETRY_CONNECTION) {
                run();
            } else {
                finish = true;
                loaderRunResult = WebData.ARTICLE_FAIL_IO;
            }
        } catch (JSONException e) {
//            e.printStackTrace();
            htmlResult = "error";
            finish = true;
            loaderRunResult = WebData.ARTICLE_FAIL_JSON;
        }

        synchronized (this) {
            this.notify();
        }
    }
}
