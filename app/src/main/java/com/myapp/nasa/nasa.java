package com.myapp.nasa;

import com.google.gson.annotations.SerializedName;

public class nasa {

    public String date;
    @SerializedName("explanation")
    public String body;
    @SerializedName("hdurl")
    public String hdlink;
    @SerializedName("url")
    public String link;
    @SerializedName("service_version")
    public String version;
    @SerializedName("mediatype")
    public String type;

    public String title;

    public String getDate() {
        return date;
    }

    public String getBody() {
        return body;
    }

    public String getHdlink() {
        return hdlink;
    }

    public String getLink() {
        return link;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }
}
