package com.kshrd.android_akn.model;

import com.kshrd.android_akn.util.Util;

/**
 * Created by Buth Mathearo on 12/28/2015.
 */
public class Article {
    private int id;
    private String title;
    private String description;
    private String imageUrl;
    private String date;
    private int viewCount;
    private String url;
    private boolean status;
    private String content;
    private String siteLogo;
    private int siteId;
    private boolean isSaved;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSiteLogo() {
        return siteLogo;
    }

    public void setSiteLogo(String siteLogo) {
        this.siteLogo = Util.BASE_LOGO_URL + siteLogo;
    }

    public String getShortTitle() {
       /*return title.substring(0, title.length() > 70 ? 70 : title.length()) + "...";*/
        return title;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    // Works with Contains Method of Collection


    @Override
    public boolean equals(Object o) {
        boolean rs = false;
        if (o != null && o instanceof Article) {
            rs = ((Article) o).getId() == this.id;
        }
        return rs;
    }

    @Override
    public int hashCode() {
        return this.hashCode();
    }
}
