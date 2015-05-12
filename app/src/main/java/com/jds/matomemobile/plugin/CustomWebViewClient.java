package com.jds.matomemobile.plugin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.jds.matomemobile.net.BrowseLocal;
import com.jds.matomemobile.net.NetConnection;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JDS on 3/19/15.
 */
public class CustomWebViewClient extends WebViewClient {

    WebData webData = new WebData();
    public String currentURL = "";
    public boolean ispagecompleted = true;

    Handler mHandler = new Handler();

    private View rootView;
    private CustomWebView customWebView;
    private int rWebViewId;
    private Activity activity;
    private ConnectivityManager cm;
    private boolean loadFromFile;
    private boolean fileLoadFromUrl;
    private boolean lastUrlFail;
    private String fileLoc, fileName, fileUrl, sourceProtocol;
    private BrowseLocal cached;
    private BrowseLocal savedList;
    private List<String> browseHistories;
    private boolean is_reload;
    private String lastURL;
    private boolean hasStarted;
    private boolean resume;

    public boolean getLastUrlFail() {
        return lastUrlFail;
    }

    public boolean isFileLoadFromUrl() {
        return fileLoadFromUrl;
    }

    public CustomWebViewClient(CustomWebView customWebView, Activity activity, boolean is_resume, View rootView, int rWebViewId) {
        this.activity = activity;
        this.rootView = rootView;
        this.rWebViewId = rWebViewId;
        this.customWebView = customWebView;
        cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        loadFromFile = false;
        fileLoadFromUrl = false;
        hasStarted = false;
        fileLoc = "";
        fileName = "";
        fileUrl = "";
        cached = new BrowseLocal(activity.getFilesDir().getPath() + WebData.CACHE_DIR);
        savedList = new BrowseLocal(activity.getFilesDir().getPath() + WebData.SAVED_DIR);
        sourceProtocol = "";
        browseHistories = new ArrayList<>();
        if (is_resume) {
            loadBrowseHistories();
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        lastUrlFail = false;
        clearCache(view);
        if (url.startsWith(webData.MAIN_URL)) {
            view.loadUrl(url);
        } else {
            try {
                if (isYouTubeUrl(url)) {
                    playYouTube(url);
                    view.stopLoading();
                } else {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
                view.stopLoading();
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity.getApplicationContext(), "No application can handle this request." +
                        "Please install a Web Browser application", Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (hasStarted == false && ispagecompleted == false) {
            customWebView.setVisibility(View.GONE);

            hasStarted = true;
            lastURL = null;
        }
        clearCache(view);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        currentURL = url;
        webData.setURL(url);
        System.gc();
    }


    @Override
    public void onLoadResource(WebView view, String url) {
        if(mHandler != null && loadResources != null) {
            loadResources.start = false;
            mHandler.removeCallbacks(loadResources);
        }
        if (!isYouTubeUrl(url)) {
            view = clearCache(view);
            if (hasStarted == false && ispagecompleted == false) {
                if (url.contains(WebData.MAIN_NO_HTTP)) {
                    customWebView.setVisibility(View.GONE);
                }

                hasStarted = true;
                lastURL = null;
            }
            try {
                addHistories(view);
                if (is_reload) {
                    if (browseHistories.size() > 0) {
                        view.loadUrl(browseHistories.get(browseHistories.size() - 1));
                    } else {
                        view.loadUrl(WebData.MAIN_URL);
                    }
                    is_reload = false;
                }
                if (getProtocol(view, url).equals("file:")) {
                    if (Build.VERSION.SDK_INT < 19) {
                        android422(view, url);
                    } else {
//                    notAndroid422(view, url);
                        android422(view, url);
                    }
                } else {
                    fileLoadFromUrl = false;
                }

                if (view.getUrl() != null) {
                    if (view.getUrl().equals("about:blank")) {
                        view.loadUrl(WebData.MAIN_URL);
                    }
                } else {
                    view.loadUrl(WebData.MAIN_URL);
                }

                if (ispagecompleted == true) ispagecompleted = false;
                webData.setLoadResources(ispagecompleted);
                loadResources.start = true;
                mHandler.postDelayed(loadResources, 1700);
            } catch (Exception e) {
//                e.printStackTrace();
            }
        } else {
            try {
                playYouTube(url);
                view.stopLoading();
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity.getApplicationContext(), "No application can handle this request." +
                        "Please install a YouTube application", Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
            }
        }
    }

    private boolean isYouTubeUrl(String url) {
        if (url.contains("www.youtube.com/get_video_info"))
            return true;
        if (url.contains("www.youtube.com/watch"))
            return true;
        return false;
    }

    private String getYouTubeVideo(String url) {
        String v_id = "";
        if (url.contains("video_id")) {
            Pattern pattern = Pattern.compile("(?<=video_id=)(.*?)(?=&)");
            Matcher matcher = pattern.matcher(url);
            while (matcher.find()) {
                v_id = matcher.group(1);
            }
        } else if (url.contains("v")) {
            Pattern pattern = Pattern.compile("(?<=v=)(.*?)(?=&)");
            Matcher matcher = pattern.matcher(url);
            while (matcher.find()) {
                v_id = matcher.group(1);
            }
        }
        return v_id;
    }

    private void playYouTube(String url) {
        String v_id = getYouTubeVideo(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WebData.YOUTUBE_URL + v_id));
        intent.createChooser(intent, "Play YouTube video via");
        activity.startActivity(intent);
    }

    private void android422(WebView view, String url) {
        Log.v("[ld fl]", "=== url: " + url);
        loadFromFile = true;
        fileLoadFromUrl = true;
        getFileLocation(view, url);
        getFileName(view, url);
        String dest = view.getOriginalUrl() != null ? view.getOriginalUrl() : url;
        String newUrl = WebData.MAIN_URL;

        if (!dest.contains("cache")) {
            getFileUrl(view, url);
            newUrl = fileUrl;
        } else {
            try {
                String read;
                String cacheUrl = "";
                BufferedReader br = new BufferedReader(new FileReader(cached.getDefaultSaveLoc() + "/visit.url"));

                while ((read = br.readLine()) != null) {
                    cacheUrl = read;
                }
                newUrl = cacheUrl;
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
        view.stopLoading();
        view.loadUrl(newUrl);
    }

    private void notAndroid422(WebView view, String url) {
        loadFromFile = true;
        fileLoadFromUrl = true;
        getFileLocation(view, url);
        getFileName(view, url);
        String dest = view.getOriginalUrl() != null ? view.getOriginalUrl() : url;
        if (new File(fileLoc).length() == 0)
            view.stopLoading();
        if (!dest.contains("cache")) {
            getFileUrl(view, url);
            view.stopLoading();
            view.loadUrl(fileUrl);
//                }
        } else if (new NetConnection().isOnline(cm)) {
            try {
                String read;
                String cacheUrl = "";
                BufferedReader br = new BufferedReader(new FileReader(cached.getDefaultSaveLoc() + "/visit.url"));

                while ((read = br.readLine()) != null) {
                    cacheUrl = read;
                }
                view.stopLoading();
                view.loadUrl(cacheUrl);
            } catch (IOException e) {
//                e.printStackTrace();
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String
            failingUrl) {
        view.loadData(
                "<html>\n" +
                        "<head>\n" +
                        "<style type=\"text/css\">\n" +
                        "html,body { font-size: 10pt; height: 100%; margin: 0px; padding: 0px; }\n" +
                        "#full {\n" +
                        "position: relative;\n" +
                        "background: #fff;\n" +
                        "width: 100%;\n" +
                        "top: 10%;\n" +
                        "text-align: center; vertical-align: middle;\n" +
                        "}\n" +
                        "a { font-size: 8pt; background: #fc9abc; padding-top: 7px; padding-bottom: 7px; padding-left: 14px; padding-right: 14px; border-radius: 14px; color: #fff; }\n" +
                        "a:link, a:visited, a:hover, a:active { text-decoration: none; }\n" +
                        "</style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<div id=\"full\">\n" +
                        "Internet connection is not available.<br>\n" +
                        "<span style=\"color: #aaa;\"><i>Pull down this page to retry.</i></span>\n" +
                        "<p>or click \"Retry\" button below\n" +
                        "<p><a href=\"" + view.getUrl() + "\">RETRY</a></p></p>\n" +
                        "</div>\n" +
                        "</body>\n" +
                        "</html>",
                "text/html", "");
        lastUrlFail = true;
    }

    private WebView clearCache(WebView webView) {
        String url = webView.getUrl();
        if (lastURL != null) {
            if (!lastURL.equals(url)) {
                customWebView.reload();
                lastURL = url;
            }
        } else {
            lastURL = url;
        }

        return webView;
    }


    private String getProtocol(WebView view, String url) {
        if (view.getProgress() < 50) {
            String[] urls = url.split("/");
            sourceProtocol = urls.length > 0 ? urls[0] : sourceProtocol;
        }
        return sourceProtocol;
    }

    private String getFileLocation(WebView view, String url) {
        if (view.getProgress() < 50) {
            String[] urls = url.split("file://");
            fileLoc = urls.length > 1 ? urls[1] : fileLoc;
        }
        return fileLoc;
    }

    private String getFileName(WebView view, String url) {
        if (view.getProgress() < 50) {
            String[] urls = url.split("/");
            fileName = urls.length > 0 ? urls[urls.length - 1] : fileName;
        }
        return fileName;
    }

    private String getFileUrl(WebView view, String url) {
        if (view.getProgress() < 50) {
            fileUrl = "http://m.matome.id/";
            if (!fileName.equals("home" + WebData.WEB_ARCHIVE_EXT)) {
                fileUrl += fileName.replace('_', '/');
                fileUrl = fileUrl.replace(WebData.WEB_ARCHIVE_EXT, "");
            }
        }

        return fileUrl;
    }

    public List<String> getBrowseHistories() {
        return browseHistories;
    }

    private void saveCache(WebView view) {
        try {
            if (!view.getUrl().equals(WebData.NO_URL)) {
                addHistories(view);
                if (view.getProgress() == 100) {
                    if (fileName.contains("cache"))
                        loadFromFile = false;
                    if (loadFromFile) {
                        savedList.saveLocal(view, fileName);
                    } else {
                        cached.saveLocal(view, WebData.CACHE_FILENAME);
                        if (!view.getUrl().contains("file")) {
                            try {
                                File f = new File(cached.getDefaultSaveLoc(), "visit.url");
                                if (!f.exists()) {
                                    f.createNewFile();
                                }
                                String data = view.getUrl();
                                PrintWriter p = new PrintWriter(f);
                                p.print(data);
                                p.close();
                            } catch (IOException e) {
//                        e.printStackTrace();
                            }
                        }

                    }
                    loadFromFile = false;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void addHistories(WebView view) {
        if (view.getUrl() != null) {
            if (!view.getUrl().contains("file:") && !view.getUrl().contains("data:") && !view.getUrl().contains("cache")) {
                if (browseHistories.size() > 0) {
                    if (!browseHistories.get(browseHistories.size() - 1).equals(view.getUrl())) {
                        browseHistories.add(view.getUrl());
                    }
                } else {
                    browseHistories.add(view.getUrl());
                }
                shiftBrowseHistories();
                saveBrowseHistoriesFile();
            }
        }
    }

    private void shiftBrowseHistories() {
        if (browseHistories.size() > 20) {
            List<String> temp = new ArrayList<>();
            for (int i = 1; i < browseHistories.size(); i++) {
                temp.add(browseHistories.get(i));
            }
            browseHistories = temp;
        }
    }

    private void saveBrowseHistoriesFile() {
        try {
            File f = new File(cached.getDefaultSaveLoc(), "histories.json");
            if (!f.exists()) {
                f.delete();
                f.createNewFile();
            }
            FileWriter writer = new FileWriter(f.getAbsoluteFile());
            writer.write(makeBrowseHistoriesJson());
            writer.close();
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    public void loadBrowseHistories() {
        try {
            File f = new File(cached.getDefaultSaveLoc(), "histories.json");
            String jsonStr = Jsoup.parse(f, "utf-8").body().text();

            JSONArray json = new JSONArray(jsonStr);
            browseHistories.clear();
            for (int i = 0; i < json.length(); i++) {
                browseHistories.add((String) json.get(i));
            }
        } catch (IOException e) {
//            e.printStackTrace();
        } catch (JSONException e) {
//            e.printStackTrace();
        }
    }

    private String makeBrowseHistoriesJson() {
        JSONArray json = new JSONArray();
        for (int i = 0; i < browseHistories.size(); i++) {
            json.put(browseHistories.get(i));
        }
        return json.toString();
    }

    private class CustomRunnable implements Runnable{
        public boolean start = false;
        @Override
        public void run() {
            if(start == true) {
                Log.v("[LR]", "Runn");
                if (ispagecompleted == false) {
                    ispagecompleted = true;
                    webData.setLoadResources(ispagecompleted);
                }

                if (ispagecompleted) {
                    if (webData.getLoadResources()) {
                        Log.d("[ld-dsp]", "finish loading resources");
                        customWebView.setVisibility(View.VISIBLE);
                        hasStarted = false;
                        if (!lastUrlFail) {
                            saveCache(customWebView.getWebView());
                        }
                        start = false;
                    }
                }
            }
        }
    }

    private CustomRunnable loadResources = new CustomRunnable();

    public void popHistories() {
        List<String> temp = new ArrayList<>();
        for (int i = 0; i < browseHistories.size() - 1; i++) {
            temp.add(browseHistories.get(i));
        }
        browseHistories = temp;
    }

    public String getBackUrl() {
        popHistories();
        return browseHistories.get(browseHistories.size() - 1);
    }

    public void setResume(boolean resume) {
        this.resume = resume;
    }

    public void setReload(boolean is_reload) {
        this.is_reload = is_reload;
    }
}
