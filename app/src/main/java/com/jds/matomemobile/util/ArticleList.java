package com.jds.matomemobile.util;

import android.app.Activity;
import android.util.Log;

import com.jds.matomemobile.plugin.WebData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by windows 7 on 13/03/2015.
 */
public class ArticleList {
    private List<Article> articles;
    private File saveFile;
    private Activity activity;
    private Article currentArticle;
    private boolean bgSaving;

    public ArticleList(Activity activity, String path) {
        this.activity = activity;
        saveFile = new File(path);
        try {
            if (!saveFile.exists()) {
                saveFile.mkdirs();
            }
            saveFile = new File(path, "articles.json");
            saveFile.createNewFile();
        } catch (IOException e) {
//            e.printStackTrace();
        }
        articles = new ArrayList<>();
    }

    public List<Article> getArticles() {
        return articles;
    }

    public Article getArticle(String url) {
        int position = 0;
        for (int i = 0; i < articles.size(); i++) {
            if (articles.get(i).url == url) {
                return articles.get(position);
            }
        }
        return null;
    }

    public int addArticle(String id, String url, String saveLoc, String title) {
        boolean overwrite = false;
        int position = 0;
        for (int i = 0; i < articles.size(); i++) {
            if (articles.get(i).id.equals(id)) {
                overwrite = true;
                position = i;
                break;
            }
        }

        Article art = new Article(activity, id, url, saveLoc, title);

        if (!overwrite) {
            if(getArticleSize() < WebData.MAX_ARTICLE) {
                if(!art.getObtainFailed()) {
                    articles.add(art);
                    currentArticle = art;
                }
            } else {
                art.setObtainFailed(true);
                return WebData.ARTICLE_CAPACITY_IS_FULL;
            }
        } else {
            if(!art.getObtainFailed()) {
                articles.get(position).changeData(art, bgSaving);
                currentArticle = art;
            }
        }
        if(!art.getObtainFailed()) {
            sortArticle();
            saveArticles();
            return WebData.ARTICLE_CAPACITY_NOT_FULL;
        }

        return art.getArticleCreateState();
    }

    public void saveArticles() {
        String json = makeJson();
        try {
            saveFile.delete();
            saveFile.createNewFile();
            FileWriter writer = new FileWriter(saveFile.getAbsoluteFile());
            writer.write(json);
            writer.close();
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    private String makeJson() {
        JSONArray json = new JSONArray();
        try {
            for (int i = 0; i < articles.size(); i++) {
                JSONObject art = new JSONObject();
                art.put("id", articles.get(i).id);
                art.put("img", articles.get(i).img);
                art.put("key", articles.get(i).key);
                art.put("pv", articles.get(i).pv);
                art.put("saveLoc", articles.get(i).saveLoc);
                art.put("title", articles.get(i).title);
                art.put("url", articles.get(i).url);
                art.put("usr_nam", articles.get(i).usr_nam);
                art.put("mod_date", articles.get(i).getLastModifiedDate());
                json.put(i, art);
            }
        } catch (JSONException e) {
//            e.printStackTrace();
        }

        return json.toString();
    }

    public void loadArticles() {
        try {
            String jsonStr = Jsoup.parse(saveFile, "utf-8").body().text();
            JSONArray json = new JSONArray(jsonStr);
            articles.clear();
            for(int i = 0; i < json.length(); i++) {
                JSONObject jsonArt = json.getJSONObject(i);
                Article art = new Article(activity, jsonArt);
                art.loadImage();
                articles.add(art);
            }
        } catch (NullPointerException e) {
//            e.printStackTrace();
        } catch (JSONException e) {
//            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
        }
        sortArticle();
    }

    private void sortArticle() {
        Collections.sort(articles, new Comparator<Article>() {
            @Override
            public int compare(Article article, Article article2) {
                if(article != null && article2 != null) {
                    String modDate = article.getLastModifiedDate();
                    String modDate2 = article2.getLastModifiedDate();
                    return modDate2.compareTo(modDate);
                }
                return 0;
            }
        });
    }

    public void deleteAllArticles() {
        for(int i = 0; i < articles.toArray().length; i++) {
            articles.get(i).deleteData();
            articles.remove(i);
        }
        saveArticles();
        sortArticle();
    }

    public void deleteArticle(int position) {
        try{
            articles.get(position).deleteData();
            articles.remove(position);
            saveArticles();
            sortArticle();
        } catch (IndexOutOfBoundsException e) {
//            e.printStackTrace();
        }
    }

    public String getArticleSavedLoc(int id) {
        if (articles.get(id) != null) {
            return articles.get(id).saveLoc;
        }
        return "";
    }

    public int getArticleSize() {
        return articles.toArray().length;
    }

    public Article getCurrentArticle() {
        return currentArticle;
    }

    public void setBgSaving(boolean bgSaving) {
        this.bgSaving = bgSaving;
    }
}

