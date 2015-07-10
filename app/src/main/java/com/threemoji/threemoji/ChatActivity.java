package com.threemoji.threemoji;

import com.threemoji.threemoji.data.ChatContract;
import com.threemoji.threemoji.service.ChatIntentService;
import com.threemoji.threemoji.utility.DateUtils;
import com.threemoji.threemoji.utility.SvgUtils;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = ChatActivity.class.getSimpleName();
    public static final int PARTNER_LOADER = 0;
    public static final int MESSAGES_LOADER = 1;

    public static final String[] MESSAGES_PROJECTION = new String[]{
            ChatContract.MessageEntry.COLUMN_DATETIME,
            ChatContract.MessageEntry.COLUMN_MESSAGE_TYPE,
            ChatContract.MessageEntry.COLUMN_MESSAGE_DATA
    };
    public static final String MESSAGES_SORT_ORDER =
            ChatContract.MessageEntry.TABLE_NAME + "." + ChatContract.MessageEntry._ID + " DESC";

    public static final String[] PARTNER_PROJECTION = new String[]{
            ChatContract.PartnerEntry.COLUMN_EMOJI_1,
            ChatContract.PartnerEntry.COLUMN_EMOJI_2,
            ChatContract.PartnerEntry.COLUMN_EMOJI_3,
            ChatContract.PartnerEntry.COLUMN_GENDER,
            ChatContract.PartnerEntry.COLUMN_GENERATED_NAME,
            ChatContract.PartnerEntry.COLUMN_IS_ALIVE
    };
    public static final int FADE_IN_DURATION_MILLIS = 500;
    public static final int FADE_OUT_DURATION_MILLIS = 500;
    public static final int TIMESTAMP_DISPLAY_DURATION_MILLIS = 10000;

    private MessagesRecyclerViewAdapter mMessagesAdapter;

    private Uri mMessagesUri;
    private String mPartnerUid;
    private String mEmoji1;
    private String mEmoji2;
    private String mEmoji3;
    private String mGender;
    private String mGeneratedName;
    private boolean mIsAlive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initFields(getIntent());

        cancelNotifications();

        if (mIsAlive) {
            updatePartnerIfNeeded();
        } else {
            disableBottomBar();
        }

        initActionBar();
        initMessages();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.chat, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        switch (id) {
//            case R.id.action_mute_chat:
//                break;
//            case R.id.action_archive_chat:
//                break;
//            case R.id.action_block_chat:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void initFields(Intent intent) {
        mPartnerUid = intent.getStringExtra("uid");
        mMessagesUri = ChatContract.MessageEntry.buildMessagesByUidUri(mPartnerUid);

        Cursor cursor = getContentResolver().query(
                ChatContract.PartnerEntry.buildPartnerByUidUri(mPartnerUid), PARTNER_PROJECTION,
                null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            mEmoji1 = cursor.getString(0);
            mEmoji2 = cursor.getString(1);
            mEmoji3 = cursor.getString(2);
            mGender = cursor.getString(3);
            mGeneratedName = cursor.getString(4);
            mIsAlive = cursor.getInt(5) > 0;
        } else {
            mEmoji1 = intent.getStringExtra("emoji_1");
            mEmoji2 = intent.getStringExtra("emoji_2");
            mEmoji3 = intent.getStringExtra("emoji_3");
            mGender = intent.getStringExtra("gender");
            mGeneratedName = intent.getStringExtra("generated_name");
            mIsAlive = intent.getBooleanExtra("isAlive", true);
        }
    }

    private void cancelNotifications() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(getIntIDFromUid(mPartnerUid));
    }

    private int getIntIDFromUid(String fromUuid) {
        int result = 0;
        for (char c : fromUuid.toCharArray()) {
            int num = Character.getNumericValue(c);
            if (num >= 0) {
                result += num;
            }
        }
        return result;
    }

    private void updatePartnerIfNeeded() {
        Intent intent = new Intent(this, ChatIntentService.class);
        intent.putExtra("action", ChatIntentService.Action.LOOKUP_UID.name());
        intent.putExtra("uid", mPartnerUid);
        startService(intent);
    }

    private void disableBottomBar() {
        EditText editText = (EditText) findViewById(R.id.user_message);
        editText.setEnabled(false);
        editText.setText("Partner does not exist :(");
        findViewById(R.id.submit_button).setEnabled(false);
    }

    private void initActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                                                ActionBar.DISPLAY_HOME_AS_UP);
        setPartnerDetails(mEmoji1, mEmoji2, mEmoji3, mGeneratedName);
    }

    private void setPartnerDetails(String emoji1, String emoji2, String emoji3,
                                   String generatedName) {
        Drawable emoji1Drawable = SvgUtils.getSvgDrawable(emoji1, 24, getPackageName());
        Drawable emoji2Drawable = SvgUtils.getSvgDrawable(emoji2, 24, getPackageName());
        Drawable emoji3Drawable = SvgUtils.getSvgDrawable(emoji3, 24, getPackageName());
        ((ImageView) findViewById(R.id.title_emoji_1)).setImageDrawable(emoji1Drawable);
        ((ImageView) findViewById(R.id.title_emoji_2)).setImageDrawable(emoji2Drawable);
        ((ImageView) findViewById(R.id.title_emoji_3)).setImageDrawable(emoji3Drawable);
        ((TextView) findViewById(R.id.title_name)).setText(generatedName);
        mEmoji1 = emoji1;
        mEmoji2 = emoji2;
        mEmoji3 = emoji3;
        mGeneratedName = generatedName;
    }

    private void initMessages() {
        RecyclerView messagesView = (RecyclerView) findViewById(R.id.chat_messages);
        setupRecyclerView(messagesView);

        getSupportLoaderManager().initLoader(PARTNER_LOADER, null, this);
        getSupportLoaderManager().initLoader(MESSAGES_LOADER, null, this);
    }

    private void setupRecyclerView(RecyclerView messagesView) {
        Cursor cursor = getContentResolver().query(mMessagesUri, MESSAGES_PROJECTION, null, null,
                                                   MESSAGES_SORT_ORDER);

        LinearLayoutManager layoutManager = new LinearLayoutManager(messagesView.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.scrollToPosition(0);
        messagesView.setLayoutManager(layoutManager);

        mMessagesAdapter = new MessagesRecyclerViewAdapter(this, cursor);
        messagesView.setAdapter(mMessagesAdapter);
    }

    public void sendMessage(View view) {
        Log.d(TAG, "Send message button was pressed");
        EditText editText = (EditText) findViewById(R.id.user_message);
        String userMessage = editText.getText().toString();
        editText.setText("");

        if (userMessage.trim().length() > 0) {
            Intent intent = new Intent(this, ChatIntentService.class);
            intent.putExtra("uid", mPartnerUid);
            intent.putExtra("message", userMessage.trim());
            startService(intent);

            long currentTime = System.currentTimeMillis();

            ContentValues values = new ContentValues();
            values.put(ChatContract.MessageEntry.COLUMN_PARTNER_KEY, mPartnerUid);
            values.put(ChatContract.MessageEntry.COLUMN_DATETIME, currentTime);
            values.put(ChatContract.MessageEntry.COLUMN_MESSAGE_TYPE,
                       ChatContract.MessageEntry.MessageType.SENT.name());
            values.put(ChatContract.MessageEntry.COLUMN_MESSAGE_DATA, userMessage.trim());

            Uri uri = getContentResolver().insert(
                    ChatContract.MessageEntry.buildMessagesByUidUri(mPartnerUid),
                    values);
            Log.d(TAG, "Added message: " + uri.toString());

            values = new ContentValues();
            values.put(ChatContract.PartnerEntry.COLUMN_LAST_ACTIVITY, currentTime);
            getContentResolver().update(
                    ChatContract.PartnerEntry.buildPartnerByUidUri(mPartnerUid), values, null,
                    null);
        }
    }

    public void showMessageTime(View view) {
        final TextView messageTime = (TextView) view.findViewById(R.id.messageTime);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(FADE_IN_DURATION_MILLIS);

        final AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(FADE_OUT_DURATION_MILLIS);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                messageTime.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        messageTime.setVisibility(View.VISIBLE);
        messageTime.startAnimation(fadeIn);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                messageTime.startAnimation(fadeOut);
            }
        }, TIMESTAMP_DISPLAY_DURATION_MILLIS);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case PARTNER_LOADER:
                return new CursorLoader(this, ChatContract.PartnerEntry.buildPartnerByUidUri(
                        mPartnerUid), PARTNER_PROJECTION, null, null, null);
            case MESSAGES_LOADER:
                return new CursorLoader(this, mMessagesUri, MESSAGES_PROJECTION, null, null,
                                        MESSAGES_SORT_ORDER);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case PARTNER_LOADER:
                try {
                    data.moveToNext();
                    String newEmoji1 = data.getString(0);
                    String newEmoji2 = data.getString(1);
                    String newEmoji3 = data.getString(2);
                    String newName = data.getString(4);
                    boolean isAlive = data.getInt(5) > 0;

                    setPartnerDetails(newEmoji1, newEmoji2, newEmoji3, newName);

                    if (!isAlive) {
                        mIsAlive = false;
                        disableBottomBar();
                    }

                } catch (NullPointerException | CursorIndexOutOfBoundsException e) {
                    Log.e(TAG, e.getMessage());
                }
                break;
            case MESSAGES_LOADER:
                mMessagesAdapter.changeCursor(data);
                mMessagesAdapter.moveToEnd();
                break;
        }
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

            holder.messageTime.setText(DateUtils.getDate(mCursor.getLong(0)));

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
                parent.setOnClickListener(null);
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
            public final TextView messageTime;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                messageData = (TextView) view.findViewById(R.id.chat_message);
                messageTime = (TextView) view.findViewById(R.id.messageTime);
            }
        }
    }
}
