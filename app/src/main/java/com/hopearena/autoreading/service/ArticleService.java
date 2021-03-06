package com.hopearena.autoreading.service;


import android.content.Context;

import com.hopearena.autoreading.model.ArticleDetail;
import com.hopearena.autoreading.model.ArticleListItem;

import java.util.List;

public interface ArticleService {

    List<ArticleListItem> getArticleListItems(Context context);

    ArticleDetail getArticleListDetails(Context context, String itemId);
}
