package com.example.fastnews;

import android.database.sqlite.SQLiteConstraintException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.Models.Article;
import com.example.RoomDb.AppExecutors;
import com.example.RoomDb.ArticlesDatabase;

public class PendingNotificationActivity extends AppCompatActivity {

    TextView authorTv, descriptionTv, sourceTv, titleTv, timeTv;
    ImageView newsImage;
    ProgressBar progressBar;
    ImageButton imageButtonDownload;
    Article article = null;
    ArticlesDatabase articlesDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_notification);
        initView();

        if (getIntent() != null) {
            article = (Article) getIntent().getSerializableExtra("data");
            initData();
            imageButtonDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveNews(article);
                }
            });
        }

        articlesDatabase = ArticlesDatabase.getInstance(getApplicationContext());
    }

    void initView() {

        authorTv = findViewById(R.id.pending_author);
        descriptionTv = findViewById(R.id.pending_item_description);
        sourceTv = findViewById(R.id.pending_source);
        titleTv = findViewById(R.id.pending_item_title);
        timeTv = findViewById(R.id.pending_time);
        newsImage = findViewById(R.id.pending_news_image);
        imageButtonDownload = findViewById(R.id.pending_save_news_imgbtn);
        progressBar = findViewById(R.id.pending_progress_load_photo);
    }

    void initData() {
        authorTv.setText(article.getAuthor());
        descriptionTv.setText(article.getDescription());
        sourceTv.setText(article.getSource().getName());
        titleTv.setText(article.getTitle());
        timeTv.setText(Util.getFormattedDate(article.getPublishedAt()));
        Glide.with(this).load(article.getUrlToImage())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .transition(DrawableTransitionOptions.withCrossFade()).into(newsImage);
    }

    void saveNews(Article article) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.d("sql", "run: inserting the data: " + article.getTitle());
                try {
                    article.setSaved(true);
                    articlesDatabase.articlesDao().insertArticle(article);
                } catch (SQLiteConstraintException sqlConstExct) {
                    sqlConstExct.printStackTrace();
                    Log.d("sql", "run: data already present" + article.getTitle());
                }
            }
        });
    }
}
