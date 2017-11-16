package com.example.android.capstone_project.http.apimodel;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Article implements Parcelable{

    @SerializedName("author")
    @Expose
    private final String author;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("description")
    @Expose
    private final String description;
    @SerializedName("url")
    @Expose
    private final String url;
    @SerializedName("urlToImage")
    @Expose
    private final String urlToImage;
    @SerializedName("publishedAt")
    @Expose
    private final String publishedAt;
    private String category;
    private String source;

    private Article(Parcel in){
        this.author = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.urlToImage = in.readString();
        this.url = in.readString();
        this.publishedAt = in.readString();
        this.category = in.readString();
        this.source = in.readString();
    }

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getAuthor());
        parcel.writeString(getTitle());
        parcel.writeString(getDescription());
        parcel.writeString(getUrlToImage());
        parcel.writeString(getUrl());
        parcel.writeString(getPublishedAt());
        parcel.writeString(getCategory());
    }

    public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {

        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }

        public Article[] newArray(int size) {
            return new Article[size];
        }
    };

    public String getAuthor() {
        return author;
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

    public String getUrl() {
        return url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory(){
        return this.category;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return this.source;
    }
}