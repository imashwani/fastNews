package com.example.RoomDb;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.Models.Article;

import java.util.List;

@Dao
public interface ArticleDao {

    @Query("SELECT * FROM articles")
    LiveData<List<Article>> getArticleList();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArticle(Article article);

    @Delete
    void deleteArticle(Article article);

}
