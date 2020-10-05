package com.example.mynews;

public class NewsObject {
    private String mAuthor;
    private String mTitle;
    private String mDescription;
    private String mUrl;
    private String mUrlToImage;
    private String mPublishedAt;

    public NewsObject(String author, String title, String description, String url, String urlToImage, String publishedAt){
        mAuthor = author;
        mTitle = title;
        mDescription = description;
        mUrl = url;
        mUrlToImage = urlToImage;
        mPublishedAt = publishedAt;
    }


    public String getUrlToImage() {
        return mUrlToImage;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getPublishedAt() {
        return mPublishedAt;
    }
}
