package com.hopearena.autoreading;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopearena.autoreading.dummy.DummyContent;
import com.hopearena.autoreading.model.ArticleListItem;
import com.hopearena.autoreading.service.ArticleService;
import com.hopearena.autoreading.service.impl.ArticleServiceImpl;

/**
 * A fragment representing a single Article detail screen.
 * This fragment is either contained in a {@link ArticleListActivity}
 * in two-pane mode (on tablets) or a {@link ArticleDetailActivity}
 * on handsets.
 */
public class ArticleDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The content this fragment is presenting.
     */
    private ArticleListItem mItem;

    private ArticleService articleService = new ArticleServiceImpl();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
//            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
            System.out.println("getArguments().getString(ARG_ITEM_ID)>>"+getArguments().getString(ARG_ITEM_ID));
            mItem = articleService.getArticleListDetails(this.getContext(), getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.getTitle());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.article_detail, container, false);

        // Show the content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.ariticle_detail)).setText(mItem.getDesc());
        }

        return rootView;
    }

    public void setArticleService(final ArticleService articleService) {
        this.articleService = articleService;
    }
}
