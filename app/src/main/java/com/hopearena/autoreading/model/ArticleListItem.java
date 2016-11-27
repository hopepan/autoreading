package com.hopearena.autoreading.model;


import java.io.Serializable;

public class ArticleListItem implements Serializable {

    private String id;
    private String title;
    private String desc;
    private String picId;

    public ArticleListItem() {
    }

    public ArticleListItem(final String picId, final String id, final String title, final String desc) {
        this.picId = picId;
        this.id = id;
        this.title = title;
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(final String desc) {
        this.desc = desc;
    }

    public String getPicId() {
        return picId;
    }

    public void setPicId(final String picId) {
        this.picId = picId;
    }

    @Override
    public String toString() {
        return "ArticleListItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", picId='" + picId + '\'' +
                '}';
    }
}
