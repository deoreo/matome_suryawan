package com.jds.matomemobile.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.jds.matomemobile.R;
import com.jds.matomemobile.plugin.WebData;

/**
 * Created by windows 7 on 10/03/2015.
 */
public class ListViewAdapter extends ArrayAdapter<Article> {
    private final Context context;
    private final ArticleList articles;

    public ListViewAdapter(Context context, ArticleList articles) {
        super(context, R.layout.list_view_adapter, articles.getArticles());
        this.context = context;
        this.articles = articles;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_view_adapter, parent, false);

        ImageView img = (ImageView) rowView.findViewById(R.id.img);
        TextView title = (TextView) rowView.findViewById(R.id.title);
//        TextView creator = (TextView) rowView.findViewById(R.id.creator);
//        TextView viewed = (TextView) rowView.findViewById(R.id.viewed);
        ImageButton delete = (ImageButton) rowView.findViewById(R.id.deleteButon);

//        String titleText = articles.getArticles().get(position).title;
//        if(titleText.length() > 64) {
//            titleText = titleText.substring(0, 64) + "...";
//        }

        title.setText(articles.getArticles().get(position).title);
//        creator.setText(articles.getArticles().get(position).usr_nam);
//        viewed.setText(articles.getArticles().get(position).pv + " views");
        Bitmap imgBmp = articles.getArticles().get(position).imgBmp;
        if (imgBmp != null) {
            img.setImageBitmap(imgBmp);
        } else {
            img.setImageResource(R.mipmap.ic_launcher);
        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = articles.getArticles().get(position).title;
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(parent.getContext());

                alertDialog.setTitle("Delete Article");
                alertDialog.setIcon(R.mipmap.ic_delete);
                alertDialog.setMessage("You are about to delete an article mentioned below from Saved Article List.\n\"" + title + "\"\n\n" +
                        "Do you want to proceed?");
                alertDialog.setCancelable(true);

                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView savedListView = (ListView) parent.findViewById(R.id.savedListView);
                        TextView sizeArt = (TextView) ((View) parent.getParent().getParent()).findViewById(R.id.sizeArt);
                        articles.deleteArticle(position);
                        sizeArt.setText(articles.getArticleSize() + "/" + WebData.MAX_ARTICLE);
                        savedListView.setAdapter(new ListViewAdapter(context, articles));
                    }
                });

                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
            }
        });

        return rowView;
    }
}
