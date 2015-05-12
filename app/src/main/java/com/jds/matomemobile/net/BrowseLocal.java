package com.jds.matomemobile.net;

import android.app.Activity;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by windows 7 on 13/03/2015.
 */
public class BrowseLocal {
    private File saveDir;
    private String latestSaveLoc;

    public BrowseLocal(String dir) {
        saveDir = new File(dir);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        latestSaveLoc = null;
    }

    public boolean saveLocal(WebView view, String fileName) {
        try {
            File file = new File(saveDir.getPath() + File.separator + fileName);
            File temp = new File(saveDir.getPath() + File.separator + "tmp_" + fileName);
            file.createNewFile();
            temp.createNewFile();
//            if (file.canWrite()) {
            view.saveWebArchive(temp.getPath());
            if (file.exists()) {
                long fileSize = file.length();
                long tempSize = temp.length();
                if (fileSize > tempSize) {
                    if (fileSize - tempSize < (fileSize * 0.5)) {
//                        file.delete();
                        view.saveWebArchive(file.getPath());
                        temp.delete();
                    }
                } else {
//                    file.delete();
                    view.saveWebArchive(file.getPath());
                    temp.delete();
                }
            } else {
                view.saveWebArchive(file.getPath());
            }
            //File f = new File(saveDir.getPath() + File.separator + fileName);
//            file.wait(1200);
//            if (!file.exists()) {
//                return false;
//            }
            latestSaveLoc = saveDir.getPath() + File.separator + fileName;
//            }
        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void loadLocal(WebView view, String fileName) {
//        if(new File(fileName).exists()) {
        Log.v("[ld fl]", fileName);
        view.loadUrl("file://" + fileName);
//        }
    }

    public boolean fileAvailable(String fileName) {
        File f = new File(saveDir.getPath() + File.separator + fileName);
        Log.v("[ld-cc]", "file size: " + f.length());
        if (f.length() == 0)
            return false;
        return f.exists();
    }

    public String getLatestSaveLoc() {
        return latestSaveLoc;
    }

    public String getDefaultSaveLoc() {
        return saveDir.getPath();
    }
}
