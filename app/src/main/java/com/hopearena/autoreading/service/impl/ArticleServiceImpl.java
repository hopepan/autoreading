package com.hopearena.autoreading.service.impl;

import android.content.Context;

import com.hopearena.autoreading.model.ArticleListItem;
import com.hopearena.autoreading.service.ArticleService;

import java.util.ArrayList;
import java.util.List;


public class ArticleServiceImpl implements ArticleService {
    @Override
    public List<ArticleListItem> getArticleListItems(final Context context) {
        List<ArticleListItem> items = new ArrayList<>(10);
        for (int i = 0; i<items.size(); i++) {
            ArticleListItem item = new ArticleListItem(""+i, ""+i, "content"+i, "desc"+i);
            items.add(item);
        }
        System.out.println(items);
        return items;
    }
}
