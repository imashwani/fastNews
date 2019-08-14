package com.example.Models;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.example.RoomDb.SourceConverter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(tableName = "articles")
public class Article implements Serializable {

    @NonNull
    @PrimaryKey
    @SerializedName("url")
    @ColumnInfo(name = "url")
    @Expose
    private String url;

    @ColumnInfo(name = "source")
    @SerializedName("source")
    @Expose
    @TypeConverters({SourceConverter.class})
    private Source source;

    @ColumnInfo(name = "author")
    @SerializedName("author")
    @Expose
    private String author;

    @SerializedName("title")
    @ColumnInfo(name = "title")

    @Expose
    private String title;

    @SerializedName("description")
    @ColumnInfo(name = "description")
    @Expose
    private String description;


    @SerializedName("urlToImage")
    @ColumnInfo(name = "urlToImage")
    @Expose
    private String urlToImage;

    @SerializedName("publishedAt")
    @ColumnInfo(name = "publishedAt")
    @Expose
    private String publishedAt;

    @ColumnInfo(name = "issaved")
    private boolean isSaved = false;

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }
}
