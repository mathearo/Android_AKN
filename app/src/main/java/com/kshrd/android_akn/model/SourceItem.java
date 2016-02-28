package com.kshrd.android_akn.model;

import com.kshrd.android_akn.util.Util;

/**
 * Created by Buth Mathearo on 1/9/2016.
 */
public class SourceItem {
    private int id;
    private String sourceName;
    private String url;
    private String logoUrl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = Util.BASE_LOGO_URL + logoUrl;
    }

}
