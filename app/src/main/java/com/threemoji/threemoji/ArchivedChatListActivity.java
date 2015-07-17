package com.threemoji.threemoji;

import com.threemoji.threemoji.data.ChatContract;
import com.threemoji.threemoji.utility.DateUtils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ArchivedChatListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = ArchivedChatListActivity.class.getSimpleName();

    public static final String[] CHAT_ITEM_PROJECTION = new String[]{
            ChatContract.PartnerEntry.COLUMN_UID,
            ChatContract.PartnerEntry.COLUMN_EMOJI_1,
            ChatContract.PartnerEntry.COLUMN_EMOJI_2,
            ChatContract.PartnerEntry.COLUMN_EMOJI_3,
            ChatContract.PartnerEntry.COLUMN_GENERATED_NAME,
            ChatContract.PartnerEntry.COLUMN_LAST_ACTIVITY
    };
    public static final String CHAT_ITEM_SORT_ORDER = ChatContract.PartnerEntry.TABLE_NAME + "." +
                                                      ChatContract.PartnerEntry.COLUMN_LAST_ACTIVITY +
                                                      " DESC";

    public static final String IS_ARCHIVE_SELECTION = ChatContract.PartnerEntry.TABLE_NAME + "." +
                                                      ChatContract.PartnerEntry.COLUMN_IS_ARCHIVED +
                                                      " = 1";

    private ArchivedChatsRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archived_chat_list);

        initActionBar();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.archivedChatList);
        setupRecyclerView(recyclerView);
    }

    private void initActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                                                ActionBar.DISPLAY_HOME_AS_UP |
                                                ActionBar.DISPLAY_SHOW_TITLE );
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        Cursor cursor = getContentResolver()
                                     .query(ChatContract.PartnerEntry.CONTENT_URI,
                                            CHAT_ITEM_PROJECTION, IS_ARCHIVE_SELECTION,
                                            null, CHAT_ITEM_SORT_ORDER);

        LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new ArchivedChatsRecyclerViewAdapter(this, cursor);
        recyclerView.setAdapter(mAdapter);

        this.getSupportLoaderManager().initLoader(4, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ChatContract.PartnerEntry.CONTENT_URI,
                                CHAT_ITEM_PROJECTION, IS_ARCHIVE_SELECTION,
                                null, CHAT_ITEM_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    // ================================================================
    // Inner class to handle the population of items in the list
    // ================================================================
    public static class ArchivedChatsRecyclerViewAdapter
            extends RecyclerViewCursorAdapter<ArchivedChatsRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;

        public ArchivedChatsRecyclerViewAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            // Initialises the animated background of the each list item.
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.item_chat_list, parent, false);

            // Sets the animated background of each list item to show when item is touched.
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            final String uid = mCursor.getString(0);
            final String emoji1 = mCursor.getString(1);
            final String emoji2 = mCursor.getString(2);
            final String emoji3 = mCursor.getString(3);
            final String partnerName = mCursor.getString(4);
            final long lastActivity = mCursor.getLong(5);

            holder.emoji1.setImageResource(mContext.getResources()
                                                   .getIdentifier(emoji1, "drawable",
                                                                  mContext.getPackageName()));
            holder.emoji2.setImageResource(mContext.getResources()
                                                   .getIdentifier(emoji2, "drawable",
                                                                  mContext.getPackageName()));
            holder.emoji3.setImageResource(mContext.getResources()
                                                   .getIdentifier(emoji3, "drawable",
                                                                  mContext.getPackageName()));
            holder.partnerName.setText(partnerName);
            holder.lastActivity.setText(DateUtils.getTimeAgo(lastActivity));

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("uid", uid);
                    context.startActivity(intent);

                    Log.d(TAG, partnerName);
                }
            });
        }


        // ================================================================
        // Inner class to represent ChatItems in actual views
        // ================================================================
        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View view;
            public final ImageView emoji1;
            public final ImageView emoji2;
            public final ImageView emoji3;
            public final TextView partnerName;
            public final TextView lastActivity;
            public final TextView numNewMessages;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                emoji1 = (ImageView) view.findViewById(R.id.emoji1);
                emoji2 = (ImageView) view.findViewById(R.id.emoji2);
                emoji3 = (ImageView) view.findViewById(R.id.emoji3);
                partnerName = (TextView) view.findViewById(R.id.partnerName);
                lastActivity = (TextView) view.findViewById(R.id.lastActivity);
                numNewMessages = (TextView) view.findViewById(R.id.numNewMessages);
                numNewMessages.setVisibility(View.GONE);
            }
        }
    }
}
