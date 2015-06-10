package com.threemoji.threemoji;

import android.content.Context;
import android.content.res.Resources;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatListFragment extends Fragment {
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

        addDummyData(chats);

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
        String name = "emoji_" + (rand.nextInt(20) + 1);
        Resources resources = getActivity().getResources();
        int id = resources.getIdentifier(name, "drawable",
                                         getActivity().getPackageName());
        return id;
    }

    private String getRandomTime() {
        Random rand = new Random();
        return rand.nextInt(60) + " minutes ago";
    }

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

    public static class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private List<ChatItem> mItems;
        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;

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
            holder.emoji2.setImageResource(currentItem.emoji2);
            holder.emoji3.setImageResource(currentItem.emoji3);
            holder.partnerName.setText(currentItem.partnerName);
            holder.lastActivity.setText(currentItem.lastActivity);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Context context = v.getContext();
//                    Intent intent = new Intent(context, ChatActivity.class);
//                    intent.putExtra(ChatActivity.EXTRA_NAME, holder.mBoundString);
//
//                    context.startActivity(intent);
                    Log.d("onBindViewHolder", holder.partnerName.getText().toString());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }
}