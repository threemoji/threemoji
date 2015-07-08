package com.threemoji.threemoji;

import com.threemoji.threemoji.data.ChatContract;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class ChatListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ChatListFragment.class.getSimpleName();
    private ChatsRecyclerViewAdapter mAdapter;

    private final String[] CHAT_ITEM_PROJECTION = new String[]{
            ChatContract.PartnerEntry.COLUMN_UUID,
            ChatContract.PartnerEntry.COLUMN_EMOJI_1,
            ChatContract.PartnerEntry.COLUMN_EMOJI_2,
            ChatContract.PartnerEntry.COLUMN_EMOJI_3,
            ChatContract.PartnerEntry.COLUMN_GENDER,
            ChatContract.PartnerEntry.COLUMN_GENERATED_NAME,
            ChatContract.PartnerEntry.COLUMN_IS_ALIVE
    };

    // ================================================================
    // Methods for initialising the components of the chat list
    // ================================================================
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) inflater.inflate(
                R.layout.fragment_chat_list, container, false);
        setupRecyclerView(rv);
        return rv;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {

//        ContentValues testValues = new ContentValues();

//        testValues.put(ChatContract.PartnerEntry.COLUMN_UUID, "1d0982b4-4a73-41b1-b220-052667e223c2");
//        testValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_1, "emoji_1f604");
//        testValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_2, "emoji_1f603");
//        testValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_3, "emoji_1f600");
//        testValues.put(ChatContract.PartnerEntry.COLUMN_GENDER, "FEMALE");
//        testValues.put(ChatContract.PartnerEntry.COLUMN_GENERATED_NAME, "Weepy Xoni");

//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        testValues.put(ChatContract.PartnerEntry.COLUMN_UUID, prefs.getString(getString(R.string.profile_uid_key), ""));
//        testValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_1, prefs.getString(getString(R.string.profile_emoji_one_key), ""));
//        testValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_2, prefs.getString(getString(R.string.profile_emoji_two_key), ""));
//        testValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_3, prefs.getString(getString(R.string.profile_emoji_three_key), ""));
//        testValues.put(ChatContract.PartnerEntry.COLUMN_GENDER, prefs.getString(getString(R.string.profile_gender_key), ""));
//        testValues.put(ChatContract.PartnerEntry.COLUMN_GENERATED_NAME, prefs.getString(getString(R.string.profile_generated_name_key), ""));
//
//        Uri uri = getActivity().getContentResolver()
//                  .insert(ChatContract.PartnerEntry.CONTENT_URI, testValues);
//        Log.v(TAG, uri.toString());

//        int rowsDeleted = getActivity().getContentResolver().delete(ChatContract.PartnerEntry.CONTENT_URI,
//                                                                    ChatContract.PartnerEntry.COLUMN_GENERATED_NAME + " = ?",
//                                                                    new String[] {"Shiny Boubou"});
//        Log.v(TAG, rowsDeleted+"");

        Cursor cursor = getActivity().getContentResolver()
                                     .query(ChatContract.PartnerEntry.CONTENT_URI,
                                            CHAT_ITEM_PROJECTION, null, null, null);

        LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new ChatsRecyclerViewAdapter(getActivity(), cursor);
        recyclerView.setAdapter(mAdapter);

        getActivity().getSupportLoaderManager().initLoader(2, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), ChatContract.PartnerEntry.CONTENT_URI,
                                CHAT_ITEM_PROJECTION, null, null, null);
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
            final String uuid = mCursor.getString(0);
            final String emoji1 = mCursor.getString(1);
            final String emoji2 = mCursor.getString(2);
            final String emoji3 = mCursor.getString(3);
            final String gender = mCursor.getString(4);
            final String partnerName = mCursor.getString(5);
            final boolean isAlive = mCursor.getInt(6) > 0;
            final String lastActivity = getRandomTime();

            holder.emoji1.setImageResource(mContext.getResources().getIdentifier(emoji1, "drawable", mContext.getPackageName()));
            holder.emoji2.setImageResource(mContext.getResources().getIdentifier(emoji2, "drawable", mContext.getPackageName()));
            holder.emoji3.setImageResource(mContext.getResources().getIdentifier(emoji3, "drawable", mContext.getPackageName()));
            holder.partnerName.setText(partnerName);
            holder.lastActivity.setText(lastActivity);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("action", ChatActivity.Action.DISPLAY.name());
                    intent.putExtra("uuid", uuid);
                    intent.putExtra("emoji_1", emoji1);
                    intent.putExtra("emoji_2", emoji2);
                    intent.putExtra("emoji_3", emoji3);
                    intent.putExtra("gender", gender);
                    intent.putExtra("generated_name", partnerName);
                    intent.putExtra("isAlive", isAlive);
                    context.startActivity(intent);

                    Log.d(TAG, partnerName);
                }
            });
        }

        private String getRandomTime() {
            Random rand = new Random();
            return rand.nextInt(60) + " minutes ago";
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

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                emoji1 = (ImageView) view.findViewById(R.id.emoji1);
                emoji2 = (ImageView) view.findViewById(R.id.emoji2);
                emoji3 = (ImageView) view.findViewById(R.id.emoji3);
                partnerName = (TextView) view.findViewById(R.id.partnerName);
                lastActivity = (TextView) view.findViewById(R.id.lastActivity);
            }
        }
    }
}
