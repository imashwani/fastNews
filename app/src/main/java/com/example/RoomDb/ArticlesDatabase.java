package com.example.RoomDb;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.util.Log;

import com.example.Models.Article;

@Database(entities = {Article.class}, version = 1, exportSchema = false)
@TypeConverters({SourceConverter.class})
public abstract class ArticlesDatabase extends RoomDatabase {
    private static final String LOG_TAG = ArticlesDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "articlelist";
    private static ArticlesDatabase sInstance;

    public static ArticlesDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        ArticlesDatabase.class, ArticlesDatabase.DATABASE_NAME)
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract ArticleDao articlesDao();
}
