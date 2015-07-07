package com.threemoji.threemoji;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

public abstract class RecyclerViewCursorAdapter<ViewHolder extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<ViewHolder> {

    protected Cursor mCursor;
    protected Context mContext;

    public RecyclerViewCursorAdapter(Context context, Cursor cursor) {
        mCursor = cursor;
        mContext = context;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,
                                 int position) {
        mCursor.moveToPosition(position);
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    public void changeCursor(Cursor cursor) {
        if (cursor != mCursor) {
            Cursor oldCursor = mCursor;
            mCursor = cursor;
            notifyDataSetChanged();
            oldCursor.close();
        }
    }
}
