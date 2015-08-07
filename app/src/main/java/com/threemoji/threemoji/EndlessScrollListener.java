package com.threemoji.threemoji;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {
    public static final String TAG = EndlessScrollListener.class.getSimpleName();
    private int mPreviousTotal = 0; // The total number of items in the dataset after the last load
    private boolean mLoading = true; // True if we are still waiting for the last set of data to load.
    private int mVisibleThreshold = 50; // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private int mCurrentPage = 1;

    private LinearLayoutManager mLinearLayoutManager;

    public EndlessScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        if (mLoading && (totalItemCount > mPreviousTotal)) {
            mLoading = false;
            mPreviousTotal = totalItemCount;
            mCurrentPage++;

        }
        if (!mLoading && (totalItemCount - visibleItemCount)
                         <= (firstVisibleItem + mVisibleThreshold)) {
            onLoadMore(mCurrentPage + 1);
            mLoading = true;
        }
    }

    public abstract void onLoadMore(int current_page);
}