package com.threemoji.threemoji;

import com.threemoji.threemoji.data.ChatContract;
import com.threemoji.threemoji.utility.SvgUtils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        String uuid = intent.getStringExtra("uuid");
        String generatedName = intent.getStringExtra("generated_name");
        int emoji1 = intent.getIntExtra("emoji_1", 0);
        int emoji2 = intent.getIntExtra("emoji_2", 0);
        int emoji3 = intent.getIntExtra("emoji_3", 0);

        initActionBar(emoji1, emoji2, emoji3, generatedName);

        initMessages(uuid, emoji1, emoji2, emoji3, generatedName);
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

    private void initMessages(String uuid, int emoji1, int emoji2, int emoji3,
                              String generatedName) {
        ArrayList<Message> messages = new ArrayList<Message>();
        String[] projection = new String[]{ChatContract.MessageEntry.COLUMN_DATETIME,
                                           ChatContract.MessageEntry.COLUMN_SENT_OR_RECEIVED,
                                           ChatContract.MessageEntry.COLUMN_MESSAGE_DATA};

        addDummyMessages();

        Cursor cursor = getContentResolver().query(
                ChatContract.MessageEntry.buildMessagesWithPartner(uuid, emoji1 + "", emoji2 + "",
                                                                   emoji3 + "", generatedName),
                projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                messages.add(
                        new Message(cursor.getString(0),
                                    cursor.getString(1),
                                    cursor.getString(2)));
            }
        }

        RecyclerView messagesView = (RecyclerView) findViewById(R.id.chat_messages);
        setupRecyclerView(messagesView, messages);
    }

    private void addDummyMessages() {
//        int rowsDeleted = getContentResolver().delete(ChatContract.MessageEntry.CONTENT_URI,
//                                                      ChatContract.MessageEntry.COLUMN_PARTNER_KEY + " = 2", null);

//        Uri uri;
//        ContentValues dummyValues = new ContentValues();
//        dummyValues.put(ChatContract.MessageEntry.COLUMN_PARTNER_KEY, "2");
//        dummyValues.put(ChatContract.MessageEntry.COLUMN_DATETIME, "124");
//        dummyValues.put(ChatContract.MessageEntry.COLUMN_SENT_OR_RECEIVED, "received");
//        dummyValues.put(ChatContract.MessageEntry.COLUMN_MESSAGE_DATA, "Hello Shiny Boubou");
//        uri = getContentResolver().insert(ChatContract.MessageEntry.CONTENT_URI, dummyValues);
//        Log.v("", uri.toString());
    }

    private void setupRecyclerView(RecyclerView messagesView, ArrayList<Message> messages) {
        messagesView.setLayoutManager(new LinearLayoutManager(messagesView.getContext()));
        messagesView.setAdapter(new MessagesRecyclerViewAdapter(this, messages));
    }

    public class Message {
        public String dateTime;
        public String sentOrReceived;
        public String messageData;

        public Message(String dateTime, String sentOrReceived, String messageData) {
            this.dateTime = dateTime;
            this.sentOrReceived = sentOrReceived;
            this.messageData = messageData;
        }
    }

    public static class MessagesRecyclerViewAdapter
            extends RecyclerView.Adapter<MessagesRecyclerViewAdapter.ViewHolder> {

        List<Message> mMessages;

        public MessagesRecyclerViewAdapter(Context context, List<Message> items) {
            mMessages = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.item_chat_message, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder,
                                     int position) {
            Message currentMessage = mMessages.get(position);
            holder.messageData.setText(currentMessage.messageData);
            LinearLayout parent = (LinearLayout) holder.messageData.getParent();

            if (currentMessage.sentOrReceived.equals("sent")) {
                parent.setGravity(Gravity.RIGHT);
            } else if (currentMessage.sentOrReceived.equals("received")) {
                parent.setGravity(Gravity.LEFT);
            }
        }

        @Override
        public int getItemCount() {
            return mMessages.size();
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
