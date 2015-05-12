package com.jds.matomemobile.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jds.matomemobile.net.ApiLoader;
import com.jds.matomemobile.net.NetConnection;
import com.jds.matomemobile.plugin.WebData;

/**
 * Created by windows 7 on 13/03/2015.
 */
public class Article {
    protected String id, url, saveLoc, title, img, usr_nam, pv;
    protected int key;
    protected Bitmap imgBmp;
    private Activity activity;
    private String savedDir;
    private String savedImgDir;
    private String lastModifiedDate;
    private boolean doneParsing = false;
    private boolean finishLoading = false;
    private int connectRetry = 0;
    private int articleCreateState = 0;
    private boolean resetConnect = false;
    private boolean obtainFailed = false;

    protected Article(Activity activity, JSONObject jsonArt) {
        doneParsing = false;
        try {
            id = jsonArt.has("id") ? jsonArt.getString("id") : null;
            img = jsonArt.has("img") ? jsonArt.getString("img") : null;
            key = jsonArt.has("key") ? jsonArt.getInt("key") : 0;
            pv = jsonArt.has("pv") ? jsonArt.getString("pv") : null;
            saveLoc = jsonArt.has("saveLoc") ? jsonArt.getString("saveLoc") : null;
            title = jsonArt.has("title") ? jsonArt.getString("title") : null;
            url = jsonArt.has("url") ? jsonArt.getString("url").toLowerCase() : null;
            usr_nam = jsonArt.has("usr_nam") ? jsonArt.getString("usr_nam") : null;
            lastModifiedDate = jsonArt.has("mod_date") ? jsonArt.getString("mod_date") : null;
            setFilesPath(activity);
        } catch (JSONException e) {
//            e.printStackTrace();
        }
    }

    protected Article(Activity activity, String id, String url, String saveLoc, String title) {
        doneParsing = false;
        this.id = id;
        this.url = url.toLowerCase();
        this.saveLoc = saveLoc;
        this.title = title;
        this.activity = activity;
        lastModifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        setFilesPath(activity);

        obtainApiData();
    }

    protected void changeData(Article article, boolean bgSaving) {
        doneParsing = false;
        this.id = article.id;
        this.url = article.url.toLowerCase();
        this.saveLoc = article.saveLoc;
        this.title = article.title;
        this.activity = article.activity;
        if(!bgSaving) {
            lastModifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        }
        setFilesPath(activity);

        obtainApiData();
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    protected void setArticleCreateState(int articleCreateState) {
        this.articleCreateState = articleCreateState;
    }

    public int getArticleCreateState() {
        return articleCreateState;
    }

    private void setFilesPath(Activity activity) {
        savedDir = activity.getFilesDir().getPath() + WebData.SAVED_DIR;
        savedImgDir = activity.getFilesDir().getPath() + WebData.SAVED_IMG_DIR;
    }

    private void obtainApiData() {
        obtainFailed = false;
        ApiLoader loader = new ApiLoader();
        title = "Matome Home Page";
        pv = "-";
        usr_nam = "Matome";
        imgBmp = null;
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (url.contains("search")) {
            title = "Matome Search Page";
        }
        if (url.contains("topic")) {
            title = "Matome Topic Page";
        }
        if (url.contains("category")) {
            String[] urls = url.split("/");
            title = StringManipulation.toTitleCase("Matome Category " + urls[urls.length - 1]+ " Page");
        }
        if (!url.equals("http://m.matome.id/") && !url.contains("search") && !url.contains("topic") && !url.contains("category")) {
            NetConnection net = new NetConnection();
//            if (net.isOnline(cm)) {
                loader.setUrl(url);
                Thread thread = new Thread(null, loader, "API Loader");
                thread.start();

                synchronized (loader) {
                    while (!loader.finished()) {
                        finishLoading = loader.finished();
                        connectRetry = loader.getConnectionRetry();
                        if(resetConnect) {
                            thread.interrupt();
                            loader.setUrl(url);
                            thread.start();
                            resetConnect = false;
                        }
//                        try {
//                            loader.wait();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }
                }
                if (loader.getLoaderRunResult() == WebData.ARTICLE_SUCCESS_API) {
                    parseLoader(loader);
                } else {
                    articleCreateState = loader.getLoaderRunResult();
                    obtainFailed = true;
                }

                synchronized (this) {
                    this.notify();
                }
//            } else {
//                articleCreateState = WebData.ARTICLE_FAIL_OFFLINE;
//            }
        } else {
            doneParsing = true;
            articleCreateState = WebData.ARTICLE_SAVE_SUCCESS;
        }
    }

    public void loadImage() {
        try {
            if (key != 0) {
                imgBmp = BitmapFactory.decodeFile(savedImgDir + key + WebData.IMAGE_EXT);
            }
        } catch (OutOfMemoryError e) {
//            e.printStackTrace();
        }
    }

    protected void deleteData() {
        String cachePath = savedDir + key + WebData.WEB_ARCHIVE_EXT;
        String imgPath = savedImgDir + key + WebData.IMAGE_EXT;
        File f = new File(cachePath);
        f.delete();
        f = new File(imgPath);
        f.delete();
    }

    private void parseLoader(ApiLoader loader) {
        doneParsing = false;
        try {
            JSONObject jsonObject = loader.getJsonObject();
            if (loader.getHtmlResult() != "error") {
                if (jsonObject != null) {
                    key = jsonObject.has("key") ? jsonObject.getInt("key") : null;
                    img = jsonObject.has("img") ? jsonObject.getString("img") : null;
                    pv = jsonObject.has("pv") ? jsonObject.getString("pv") : "-";
                    title = jsonObject.has("ttl") ? jsonObject.getString("ttl") : null;
                    if (img != "") {
                        imgBmp = loader.getImage();
                        if (imgBmp != null) {
                            loader.saveImage(activity.getFilesDir().getPath() + WebData.SAVED_IMG_DIR, key + WebData.IMAGE_EXT);
                        }
                    }
                    JSONObject usr = jsonObject.getJSONObject("usr");
                    if (usr != null) {
                        usr_nam = usr.has("nam") ? usr.getString("nam") : null;
                    }
                    articleCreateState = WebData.ARTICLE_SAVE_SUCCESS;
                } else {
                    articleCreateState = WebData.ARTICLE_FAIL_JSON;
                }
            } else {
                title = "Matome Article";
            }
        } catch (JSONException e) {
//            e.printStackTrace();
            articleCreateState = WebData.ARTICLE_FAIL_JSON;
        }
        doneParsing = true;
    }

    public void setObtainFailed(boolean obtainFailed) {
        this.obtainFailed = obtainFailed;
    }

    public boolean getObtainFailed() {
        return obtainFailed;
    }

    public boolean getFinishedLoading() {
        return finishLoading;
    }

    public int getConnectionRetry() {
        return connectRetry;
    }

    public boolean finishedParsing() {
        return doneParsing;
    }

    public void sendRetry() {
        resetConnect = true;
    }
}

