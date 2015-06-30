package com.threemoji.threemoji;

import com.threemoji.threemoji.data.ChatContract;
import com.threemoji.threemoji.utility.NameGenerator;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatListFragment extends Fragment {

    private static final String TAG = ChatListFragment.class.getSimpleName();

    private final String[] CHAT_ITEM_PROJECTION = new String[]{
            ChatContract.PartnerEntry.COLUMN_EMOJI_1,
            ChatContract.PartnerEntry.COLUMN_EMOJI_2,
            ChatContract.PartnerEntry.COLUMN_EMOJI_3,
            ChatContract.PartnerEntry.COLUMN_GENERATED_NAME,
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
        ArrayList<ChatItem> chats = new ArrayList<ChatItem>();

//        Uri uri;
//        ContentValues testValues = new ContentValues();
//        testValues.put(ChatContract.PartnerEntry.COLUMN_UUID, "bc54eb07-03cb-48e8-8bf8-002eee94723b");
//        testValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_1, "2130838236");
//        testValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_2, "2130838233");
//        testValues.put(ChatContract.PartnerEntry.COLUMN_EMOJI_3, "2130838228");
//        testValues.put(ChatContract.PartnerEntry.COLUMN_GENDER, "FEMALE");
//        testValues.put(ChatContract.PartnerEntry.COLUMN_GENERATED_NAME, "Woeful Quagga");
//
//        uri = getActivity().getContentResolver().insert(ChatContract.PartnerEntry.CONTENT_URI, testValues);
//        Log.v(TAG, uri.toString());

//        int rowsDeleted = getActivity().getContentResolver().delete(ChatContract.PartnerEntry.CONTENT_URI,
//                                                                    ChatContract.PartnerEntry.COLUMN_GENERATED_NAME + " = ?",
//                                                                    new String[] {"Shiny Boubou"});
//        Log.v(TAG, rowsDeleted+"");

        Cursor cursor = getActivity().getContentResolver()
                                     .query(ChatContract.PartnerEntry.CONTENT_URI,
                                            CHAT_ITEM_PROJECTION, null, null, null);
//        addDummyData(chats);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                chats.add(
                        new ChatItem(
                                Integer.parseInt(cursor.getString(0)),
                                Integer.parseInt(cursor.getString(1)),
                                Integer.parseInt(cursor.getString(2)),
                                cursor.getString(3),
                                getRandomTime()));
            }
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new RecyclerViewAdapter(getActivity(), chats));
    }

    private void addDummyData(ArrayList<ChatItem> chats) {
        for (int i = 0; i < 20; i++) {
            chats.add(new ChatItem(getRandomEmoji(), getRandomEmoji(), getRandomEmoji(),
                                   NameGenerator.getName(), getRandomTime()));
        }
    }

    private int getRandomEmoji() {
        Random rand = new Random();
        Class drawable = R.drawable.class;
        Field[] fields = drawable.getFields();
        try {
            while (true) {
                Field field = fields[rand.nextInt(fields.length)];
                if (field.toString().contains("R$drawable.emoji_")) {
                    return field.getInt(null);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private String getRandomTime() {
        Random rand = new Random();
        return rand.nextInt(60) + " minutes ago";
    }


    // ================================================================
    // Inner class to represent each row of the chat list
    // ================================================================
    public class ChatItem {
        public int emoji1;
        public int emoji2;
        public int emoji3;
        public String partnerName;
        public String lastActivity;

        public ChatItem(int emoji1, int emoji2, int emoji3, String partnerName,
                        String lastActivity) {
            this.emoji1 = emoji1;
            this.emoji2 = emoji2;
            this.emoji3 = emoji3;
            this.partnerName = partnerName;
            this.lastActivity = lastActivity;
        }
    }


    // ================================================================
    // Inner class to handle the population of items in the list
    // ================================================================
    public static class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private List<ChatItem> mItems;
        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;

        public RecyclerViewAdapter(Context context, List<ChatItem> items) {
            // Initialises the animated background of the each list item.
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;

            mItems = items;
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
        public void onBindViewHolder(final ViewHolder holder, int position) {
            ChatItem currentItem = mItems.get(position);
            holder.emoji1.setImageResource(currentItem.emoji1);
            holder.emoji1.setTag(currentItem.emoji1);
            holder.emoji2.setImageResource(currentItem.emoji2);
            holder.emoji2.setTag(currentItem.emoji2);
            holder.emoji3.setImageResource(currentItem.emoji3);
            holder.emoji3.setTag(currentItem.emoji3);
            holder.partnerName.setText(currentItem.partnerName);
            holder.lastActivity.setText(currentItem.lastActivity);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("generated_name", holder.partnerName.getText());
                    intent.putExtra("emoji_1", (int) holder.emoji1.getTag());
                    intent.putExtra("emoji_2", (int) holder.emoji2.getTag());
                    intent.putExtra("emoji_3", (int) holder.emoji3.getTag());

                    context.startActivity(intent);
                    Log.d("onBindViewHolder", holder.partnerName.getText().toString());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
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
