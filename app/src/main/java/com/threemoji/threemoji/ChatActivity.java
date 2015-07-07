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
import android.util.TypedValue;
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
            ChatContract.MessageEntry.COLUMN_MESSAGE_TYPE,
            ChatContract.MessageEntry.COLUMN_MESSAGE_DATA};
    private static String mSortOrder =
            ChatContract.MessageEntry.TABLE_NAME + "." + ChatContract.MessageEntry._ID + " DESC";
    private String mEmoji1;
    private String mEmoji2;
    private String mEmoji3;
    private String mGender;
    private String mGeneratedName;

    public static enum Action {
        NEW, DISPLAY
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        Action action = Action.valueOf(intent.getStringExtra("action"));
        mUuid = intent.getStringExtra("uuid");
        mEmoji1 = intent.getStringExtra("emoji_1");
        mEmoji2 = intent.getStringExtra("emoji_2");
        mEmoji3 = intent.getStringExtra("emoji_3");
        mGender = intent.getStringExtra("gender");
        mGeneratedName = intent.getStringExtra("generated_name");
//        mGeneratedName = "aaaaaaaaa aaaaaaaaa";

        if (action == Action.NEW && !personIsAlreadyPartner(mUuid)) {
            addNewPartnerToDb(mUuid, mEmoji1, mEmoji2, mEmoji3, mGender, mGeneratedName);
        }
//        boolean hasPartnerChangedProfile = hasPartnerChangedProfile();
        initActionBar(mEmoji1, mEmoji2, mEmoji3, mGeneratedName);
        initMessages(mUuid);
    }

    private boolean personIsAlreadyPartner(String uuid) {
        Cursor cursor =
                getContentResolver().query(ChatContract.PartnerEntry.CONTENT_URI,
                                           new String[]{
                                                   ChatContract.PartnerEntry.COLUMN_GENERATED_NAME},
                                           ChatContract.PartnerEntry.COLUMN_UUID + " = ?",
                                           new String[]{uuid},
                                           null);
        return cursor.getCount() > 0;
    }

    private void addNewPartnerToDb(String uuid, String emoji1, String emoji2, String emoji3,
                                   String gender, String generatedName) {
        ContentValues newValues = new ContentValues();
        newValues.put(ChatContract.PartnerEntry.COLUMN_UUID, uuid);
        newValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_1, emoji1);
        newValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_2, emoji2);
        newValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_3, emoji3);
        newValues.put(ChatContract.PartnerEntry.COLUMN_GENDER, gender);
        newValues.put(ChatContract.PartnerEntry.COLUMN_GENERATED_NAME, generatedName);

        Uri uri = getContentResolver().insert(ChatContract.PartnerEntry.CONTENT_URI, newValues);
        Log.v(TAG, uri.toString());
    }

//    private boolean hasPartnerChangedProfile() {
//        Intent intent = new Intent(this, ChatIntentService.class);
//        intent.putExtra("action", ChatIntentService.Action.LOOKUP_UUID.name());
//        return false;
//    }

    private void initActionBar(String emoji1, String emoji2, String emoji3, String title) {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                                                ActionBar.DISPLAY_HOME_AS_UP );
        ((TextView) findViewById(R.id.title_name)).setText(title);
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
            dummyValues.put(ChatContract.MessageEntry.COLUMN_MESSAGE_TYPE,
                            ChatContract.MessageEntry.MessageType.SENT.name());
            dummyValues.put(ChatContract.MessageEntry.COLUMN_MESSAGE_DATA, userMessage.trim());
            uri = getContentResolver().insert(
                    ChatContract.MessageEntry.buildMessagesWithPartnerUri(mUuid), dummyValues);
            Log.v(TAG, uri.toString());

//            //Testing the alerts
//            dummyValues = new ContentValues();
//            dummyValues.put(ChatContract.MessageEntry.COLUMN_PARTNER_KEY, mUuid);
//            dummyValues.put(ChatContract.MessageEntry.COLUMN_DATETIME, "124");
//            dummyValues.put(ChatContract.MessageEntry.COLUMN_MESSAGE_TYPE,
//                            ChatContract.MessageEntry.MessageType.ALERT.name());
//            dummyValues.put(ChatContract.MessageEntry.COLUMN_MESSAGE_DATA,
//                            mGeneratedName + " updated his profile");
//            uri = getContentResolver().insert(
//                    ChatContract.MessageEntry.buildMessagesWithPartnerUri(mUuid), dummyValues);
//            Log.v(TAG, uri.toString());
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
            extends RecyclerViewCursorAdapter<MessagesRecyclerViewAdapter.ViewHolder> {

        private RecyclerView rv;

        public MessagesRecyclerViewAdapter(Context context, Cursor cursor) {
            super(context, cursor);
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
            super.onBindViewHolder(holder, position);

            String message = mCursor.getString(2);
            holder.messageData.setText(message);

            FrameLayout wrapper = (FrameLayout) holder.messageData.getParent();
            LinearLayout parent = (LinearLayout) wrapper.getParent();

            String messageType = mCursor.getString(1);

            if (messageType.equals(ChatContract.MessageEntry.MessageType.SENT.name())) {
                parent.setGravity(Gravity.RIGHT);
                wrapper.setBackgroundResource(R.drawable.chat_box_sent);
                modifyParamsForMessages(holder, parent);

            } else if (messageType.equals(ChatContract.MessageEntry.MessageType.RECEIVED.name())) {
                parent.setGravity(Gravity.LEFT);
                wrapper.setBackgroundResource(R.drawable.chat_box_received);
                modifyParamsForMessages(holder, parent);

            } else if (messageType.equals(ChatContract.MessageEntry.MessageType.ALERT.name())) {
                parent.setGravity(Gravity.CENTER);
                wrapper.setBackgroundResource(R.drawable.chat_box_alert);
                modifyParamsForAlerts(holder, parent);
            }
        }

        private void modifyParamsForMessages(ViewHolder holder, LinearLayout parent) {
            holder.messageData.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    mContext.getResources().getDimension(R.dimen.chat_box_message_text_size));
            int leftRight = mContext.getResources()
                                    .getDimensionPixelSize(
                                            R.dimen.chat_box_message_wrapper_padding_left_right);
            int topBottom = mContext.getResources()
                                    .getDimensionPixelSize(
                                            R.dimen.chat_box_message_wrapper_padding_top_bottom);
            parent.setPadding(leftRight, topBottom, leftRight, topBottom);
        }

        private void modifyParamsForAlerts(ViewHolder holder,
                                           LinearLayout parent) {
            holder.messageData.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    mContext.getResources().getDimension(R.dimen.chat_box_alert_text_size));
            int leftRight = mContext.getResources()
                                    .getDimensionPixelSize(
                                            R.dimen.chat_box_alert_wrapper_padding_left_right);
            int topBottom = mContext.getResources()
                                    .getDimensionPixelSize(
                                            R.dimen.chat_box_alert_wrapper_padding_top_bottom);
            parent.setPadding(leftRight, topBottom, leftRight, topBottom);
        }

        public void moveToEnd() {
            if (rv != null) {
                rv.scrollToPosition(0);
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
