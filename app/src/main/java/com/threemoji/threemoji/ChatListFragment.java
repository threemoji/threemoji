package com.threemoji.threemoji;

import com.threemoji.threemoji.data.ChatContract;
import com.threemoji.threemoji.utility.DateUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = ChatListFragment.class.getSimpleName();
    public static final String[] CHAT_ITEM_PROJECTION = new String[]{
            ChatContract.PartnerEntry.COLUMN_UID,
            ChatContract.PartnerEntry.COLUMN_EMOJI_1,
            ChatContract.PartnerEntry.COLUMN_EMOJI_2,
            ChatContract.PartnerEntry.COLUMN_EMOJI_3,
            ChatContract.PartnerEntry.COLUMN_GENERATED_NAME,
            ChatContract.PartnerEntry.COLUMN_LAST_ACTIVITY,
            ChatContract.PartnerEntry.COLUMN_NUM_NEW_MESSAGES
    };
    public static final String CHAT_ITEM_SORT_ORDER = ChatContract.PartnerEntry.TABLE_NAME + "." +
                                                      ChatContract.PartnerEntry.COLUMN_LAST_ACTIVITY +
                                                      " DESC";

    public static final String IS_ARCHIVE_SELECTION = ChatContract.PartnerEntry.TABLE_NAME + "." +
                                                      ChatContract.PartnerEntry.COLUMN_IS_ARCHIVED +
                                                      " = 0";

    private ChatsRecyclerViewAdapter mAdapter;


    // ================================================================
    // Methods for initialising the components of the chat list
    // ================================================================
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) inflater.inflate(
                R.layout.fragment_chat_list, container, false);
        archiveOldChats();
        setupRecyclerView(rv);
        if (mAdapter.getItemCount() == 0) {
            ((ViewPager) getActivity().findViewById(R.id.viewpager)).setCurrentItem(1);
        }
        return rv;
    }

    private void archiveOldChats() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                getActivity());
        int archiveDays = Integer.parseInt(
                preferences.getString(getString(R.string.pref_chat_archive_duration_key),
                                      getString(R.string.pref_chat_archive_duration_default)));
        long archiveMillis = (long) archiveDays * 24 * 60 * 60 * 1000;
        long currentTime = System.currentTimeMillis();
        String selection = ChatContract.PartnerEntry.TABLE_NAME + "." +
                           ChatContract.PartnerEntry.COLUMN_IS_ARCHIVED + " = ? AND (" +
                           ChatContract.PartnerEntry.TABLE_NAME + "." +
                           ChatContract.PartnerEntry.COLUMN_LAST_ACTIVITY + " < ? OR " +
                           ChatContract.PartnerEntry.COLUMN_IS_ALIVE + " = ?)";
        String[] selectionArgs = new String[]{"0", String.valueOf(currentTime - archiveMillis), "0"};

        ContentValues values = new ContentValues();
        values.put(ChatContract.PartnerEntry.COLUMN_IS_ARCHIVED, 1);
        int rowsUpdated = getActivity().getContentResolver()
                                       .update(ChatContract.PartnerEntry.CONTENT_URI,
                                               values, selection, selectionArgs);
        Log.i(TAG, rowsUpdated + " chats archived");
    }

    private void setupRecyclerView(RecyclerView recyclerView) {

//        // Add a chat with yourself
//        ContentValues testValues = new ContentValues();
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        testValues.put(ChatContract.PartnerEntry.COLUMN_UID, prefs.getString(getString(R.string.profile_uid_key), ""));
//        testValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_1, prefs.getString(getString(R.string.profile_emoji_one_key), ""));
//        testValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_2, prefs.getString(getString(R.string.profile_emoji_two_key), ""));
//        testValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_3, prefs.getString(getString(R.string.profile_emoji_three_key), ""));
//        testValues.put(ChatContract.PartnerEntry.COLUMN_GENDER, prefs.getString(getString(R.string.profile_gender_key), ""));
//        testValues.put(ChatContract.PartnerEntry.COLUMN_GENERATED_NAME, prefs.getString(getString(R.string.profile_generated_name_key), ""));
//
//        Uri uri = getActivity().getContentResolver()
//                  .insert(ChatContract.PartnerEntry.CONTENT_URI, testValues);
//        Log.v(TAG, uri.toString());

        Cursor cursor = getActivity().getContentResolver()
                                     .query(ChatContract.PartnerEntry.CONTENT_URI,
                                            CHAT_ITEM_PROJECTION, IS_ARCHIVE_SELECTION,
                                            null, CHAT_ITEM_SORT_ORDER);

        LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new ChatsRecyclerViewAdapter(getActivity(), cursor);
        recyclerView.setAdapter(mAdapter);

        getActivity().getSupportLoaderManager().initLoader(2, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), ChatContract.PartnerEntry.CONTENT_URI,
                                CHAT_ITEM_PROJECTION, IS_ARCHIVE_SELECTION,
                                null, CHAT_ITEM_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}


    // ================================================================
    // Inner class to handle the population of items in the list
    // ================================================================
    public static class ChatsRecyclerViewAdapter
            extends RecyclerViewCursorAdapter<ChatsRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;

        public ChatsRecyclerViewAdapter(Context context, Cursor cursor) {
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
            final int numNewMessages = mCursor.getInt(6);

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
            if (numNewMessages == 0) {
                holder.numNewMessages.setVisibility(View.GONE);
            } else {
                holder.numNewMessages.setVisibility(View.VISIBLE);
                holder.numNewMessages.setText(
                        numNewMessages > 99 ? "99+" : String.valueOf(numNewMessages));
            }

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
            }
        }
    }
}
