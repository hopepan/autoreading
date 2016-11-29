package com.hopearena.autoreading.service.impl;

import android.content.Context;

import com.hopearena.autoreading.model.ArticleDetail;
import com.hopearena.autoreading.model.ArticleListItem;
import com.hopearena.autoreading.service.ArticleService;

import java.util.ArrayList;
import java.util.List;


public class ArticleServiceImpl implements ArticleService {

    public static final int COUNT = 20;

    @Override
    public List<ArticleListItem> getArticleListItems(final Context context) {
        List<ArticleListItem> items = new ArrayList<>();
        for (int i = 0; i<COUNT; i++) {
            ArticleListItem item = new ArticleListItem();
            item.setId(""+i);
            item.setTitle("title"+i);
            item.setDesc("desc"+i);
            item.setPicId(""+i);
            item.setDuration("");
            items.add(item);
        }
        return items;
    }

    @Override
    public ArticleDetail getArticleListDetails(final Context context, String itemId) {
        return new ArticleDetail();
    }
}
