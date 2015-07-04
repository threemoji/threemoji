package com.threemoji.threemoji;

import com.threemoji.threemoji.data.ChatContract;
import com.threemoji.threemoji.service.ChatIntentService;
import com.threemoji.threemoji.utility.SvgUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ChatActivity.class.getSimpleName();

    private MessagesRecyclerViewAdapter mAdapter;
    private String mUuid;
    private Uri mUri;
    private static final String[] projection = new String[]{
            ChatContract.MessageEntry.COLUMN_DATETIME,
            ChatContract.MessageEntry.COLUMN_SENT_OR_RECEIVED,
            ChatContract.MessageEntry.COLUMN_MESSAGE_DATA};
    private static String mSortOrder =
            ChatContract.MessageEntry.TABLE_NAME + "." + ChatContract.MessageEntry._ID + " DESC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        mUuid = intent.getStringExtra("uuid");
        String generatedName = intent.getStringExtra("generated_name");
//        String generatedName = "aaaaaaaaa aaaaaaaaa";
        int emoji1 = intent.getIntExtra("emoji_1", 0);
        int emoji2 = intent.getIntExtra("emoji_2", 0);
        int emoji3 = intent.getIntExtra("emoji_3", 0);

        initActionBar(emoji1, emoji2, emoji3, generatedName);

        initMessages(mUuid);
    }


    private void initActionBar(int emoji1, int emoji2, int emoji3, String title) {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                                                ActionBar.DISPLAY_HOME_AS_UP |
                                                ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setTitle(title);
        Drawable emoji1Drawable = SvgUtils.getSvgDrawable(emoji1, 24, getPackageName());
        Drawable emoji2Drawable = SvgUtils.getSvgDrawable(emoji2, 24, getPackageName());
        Drawable emoji3Drawable = SvgUtils.getSvgDrawable(emoji3, 24, getPackageName());
        ((ImageView) findViewById(R.id.title_emoji_1)).setImageDrawable(emoji1Drawable);
        ((ImageView) findViewById(R.id.title_emoji_2)).setImageDrawable(emoji2Drawable);
        ((ImageView) findViewById(R.id.title_emoji_3)).setImageDrawable(emoji3Drawable);
    }

    private void initMessages(String uuid) {
        mUri = ChatContract.MessageEntry.buildMessagesWithPartnerUri(uuid);

        Cursor cursor = getContentResolver().query(mUri, projection, null, null, mSortOrder);

        RecyclerView messagesView = (RecyclerView) findViewById(R.id.chat_messages);
        if (cursor != null) {
//            getContentResolver().delete(mUri, null, null);
            setupRecyclerView(messagesView, cursor);
        }
        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void setupRecyclerView(RecyclerView messagesView, Cursor cursor) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(messagesView.getContext());
//        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        layoutManager.scrollToPosition(0);

        messagesView.setLayoutManager(layoutManager);
        mAdapter = new MessagesRecyclerViewAdapter(this, cursor);
        messagesView.setAdapter(mAdapter);
    }

    public void sendMessage(View view) {
        EditText editText = (EditText) findViewById(R.id.user_message);
        String userMessage = editText.getText().toString();
        editText.setText("");

        if (userMessage.trim().length() > 0) {
            Intent intent = new Intent(this, ChatIntentService.class);
            intent.putExtra("uuid", mUuid);
            intent.putExtra("message", userMessage.trim());
            this.startService(intent);

            Uri uri;
            ContentValues dummyValues = new ContentValues();
            dummyValues.put(ChatContract.MessageEntry.COLUMN_PARTNER_KEY, mUuid);
            dummyValues.put(ChatContract.MessageEntry.COLUMN_DATETIME, "124");
            dummyValues.put(ChatContract.MessageEntry.COLUMN_SENT_OR_RECEIVED, "sent");
            dummyValues.put(ChatContract.MessageEntry.COLUMN_MESSAGE_DATA, userMessage.trim());
            uri = getContentResolver().insert(
                    ChatContract.MessageEntry.buildMessagesWithPartnerUri(mUuid), dummyValues);
            Log.v(TAG, uri.toString());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            return new CursorLoader(this, mUri, projection, null, null, mSortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
        mAdapter.moveToEnd();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public static class MessagesRecyclerViewAdapter
            extends RecyclerView.Adapter<MessagesRecyclerViewAdapter.ViewHolder> {

        private Cursor mCursor;
        private RecyclerView rv;

        public MessagesRecyclerViewAdapter(Context context, Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.item_chat_message, parent, false);
            rv = (RecyclerView) parent;
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder,
                                     int position) {
            mCursor.moveToPosition(position);

            String message = mCursor.getString(2);
            holder.messageData.setText(message);

            FrameLayout wrapper = (FrameLayout) holder.messageData.getParent();
            LinearLayout parent = (LinearLayout) holder.messageData.getParent();

            String sentOrReceived = mCursor.getString(1);
            if (sentOrReceived.equals("sent")) {
                parent.setGravity(Gravity.RIGHT);
                wrapper.setBackgroundResource(R.drawable.chat_box_sent);
            } else if (sentOrReceived.equals("received")) {
                parent.setGravity(Gravity.LEFT);
                wrapper.setBackgroundResource(R.drawable.chat_box_received);
            }
        }

        @Override
        public int getItemCount() {
            if (mCursor != null) {
                return mCursor.getCount();
            }
            return 0;
        }

        public void moveToEnd() {
            if (rv != null) {
                rv.scrollToPosition(0);
            }
        }

        public void changeCursor(Cursor cursor) {
            if (cursor != mCursor) {
                Cursor oldCursor = mCursor;
                mCursor = cursor;
                notifyDataSetChanged();
                oldCursor.close();
            }
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View view;
            public final TextView messageData;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                messageData = (TextView) view.findViewById(R.id.chat_message);
            }
        }
    }
}
