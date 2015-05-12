package com.jds.matomemobile.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by windows 7 on 18/03/2015.
 */
public class ImageUrlLoader {
    private Bitmap bitmap, bitmapCompressed;
    private ImageView imageView;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public ImageUrlLoader(String url) {
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public void saveBitmap(String savePath, String fileName) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmapCompressed = bitmap;
            bitmapCompressed.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            File f = new File(savePath);
            if(!f.exists()){
                f.mkdirs();
            }
            f = new File(savePath, fileName);
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    public Bitmap loadBitmap(String filePath) {
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL("file://" + filePath).getContent());
        } catch (IOException e) {
//            e.printStackTrace();
        }
        return bitmap;
    }
}
