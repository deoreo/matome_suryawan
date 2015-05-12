package com.jds.matomemobile.plugin;

public class WebData {
    public static final String NO_URL = "http://hhask.as453mmck0gfd/";
    public static final String MAIN_URL = "http://m.matomeindo.com";
    public static final String MAIN_NO_HTTP = "m.matomeindo.com";
    public static final String MAIN_SEARCH = "http://m.matome.id/search/";
    private String CURRENT_URL = "";
    private Integer CURRENT_PROGRESS_BAR = 0;
    private boolean IS_LOAD_RESOURCES = false;

    public static final String YOUTUBE_URL = "http://www.youtube.com/watch?v=";
    public static final String CACHE_DIR = "/matome/cache/";
    public static final String DATA_DIR = "/matome/data/";
    public static final String SAVED_DIR = "/matome/saved/";
    public static final String SAVED_IMG_DIR = "/matome/saved/img/";

    public static final String WEB_ARCHIVE_EXT = ".mhtml";
    public static final String IMAGE_EXT = ".jpg";
    public static final String CACHE_FILENAME = "cache" + WEB_ARCHIVE_EXT;

    public static final String SAVE_PROGRESS_MESSAGE = "Saving Article...";
    public static final String FAILED_PROGRESS_MESSAGE = "Failed!";

    public static final int MAX_ARTICLE = 100;
    public static final int REQUEST_TIMEOUT = 500;
    public static final int MAX_RETRY_CONNECTION = 5;
    public static final int WARN_RETRY_CONNECTION = 2;

    public static final int START_SAVE_ARTICLE = 1;
    public static final int START_SAVE_BG_ARTICLE = 2;
    public static final int CANCEL_SAVE_ARTICLE = 3;
    public static final int HIDE_PROGRESS_DIALOG = 4;

    public static final int ARTICLE_SAVE_SUCCESS = 5;
    // --Commented out by Inspection (5/8/15, 9:51 AM):public static final int ARTICLE_SAVE_FAILED = 6;
    public static final int ARTICLE_CAPACITY_IS_FULL = 7;
    public static final int ARTICLE_CAPACITY_NOT_FULL = 8;
    public static final int ARTICLE_FAIL_MHT = 9;
    public static final int ARTICLE_FAIL_OFFLINE = 10;
    public static final int ARTICLE_FAIL_JSON = 11;
    public static final int ARTICLE_FAIL_IO = 12;
    public static final int ARTICLE_FAIL_RTO = 13;
    public static final int ARTICLE_SUCCESS_API = 14;
    public static final int ARTICLE_WARN_RETRY = 15;
    public static final int SET_SAVED_ARTICLE_HEIGHT = 16;
    public static final int SAVED_ARTICLE_UPDATE = 17;
    public static final int SHOW_HIDE_SAVED_ARTICLE = 18;
    // --Commented out by Inspection (5/8/15, 9:51 AM):public static final int FINISH_PAGE_LOAD = 19;

    //OUR GCM SERVER CONFIG
    private static final String MATOME_GCM_SERVER = "http://192.168.200.222:8000/matomemobileserver/";
    public static final String MATOME_GCM_SERVER_REGISTRATION = MATOME_GCM_SERVER + "gcm/register_db.php";
    public static final String MATOME_SERVER_TOKEN = "ec0c9b5e08a233ed70631f46b4e36f1c560dc13a";

    public WebData() {
    }

// --Commented out by Inspection START (5/8/15, 9:51 AM):
//    public static String getMatomeGcmServerRegistration(String reg_id, String device_id, String device_name) {
//        return MATOME_GCM_SERVER_REGISTRATION + "?reg_id=" + reg_id + "&device_id=" + device_id + "&device_name=" + device_name + "&salt=" + MATOME_SERVER_TOKEN;
//    }
// --Commented out by Inspection STOP (5/8/15, 9:51 AM)

    public void setURL(String url) {
        CURRENT_URL = url;
    }

// --Commented out by Inspection START (5/8/15, 9:51 AM):
//    public String getURL() {
//        return CURRENT_URL;
//    }
// --Commented out by Inspection STOP (5/8/15, 9:51 AM)

    public void setProgress(int progress) {
        CURRENT_PROGRESS_BAR = progress;
    }

// --Commented out by Inspection START (5/8/15, 9:51 AM):
//    public Integer getProgress() {
//        return CURRENT_PROGRESS_BAR;
//    }
// --Commented out by Inspection STOP (5/8/15, 9:51 AM)

    public void setLoadResources(boolean isLoad) {
        IS_LOAD_RESOURCES = isLoad;
    }

    public boolean getLoadResources() {
        return IS_LOAD_RESOURCES;
    }

}
