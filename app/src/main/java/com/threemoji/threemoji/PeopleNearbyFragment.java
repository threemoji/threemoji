package com.threemoji.threemoji;

import android.content.Context;
import android.content.Intent;
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

public class PeopleNearbyFragment extends Fragment {

    private static final String TAG = PeopleNearbyFragment.class.getSimpleName();

//    private final String[] PEOPLE_NEARBY_ITEM_PROJECTION = new String[]{
//    };

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
        ArrayList<PeopleNearbyItem> chats = new ArrayList<PeopleNearbyItem>();
//        Cursor cursor = getActivity().getContentResolver()
//                                     .query(ChatContract.PartnerEntry.CONTENT_URI,
//                                            CHAT_ITEM_PROJECTION, null, null, null);
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                chats.add(
//                        new PeopleNearbyItem(
//                                cursor.getString(0),
//                                cursor.getString(1),
//                                cursor.getString(2),
//                                cursor.getString(3),
//                                cursor.getString(4),
//                                cursor.getString(5)));
//            }
//        }

        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new RecyclerViewAdapter(getActivity(), chats));
    }


    // ================================================================
    // Inner class to represent each row of the people nearby list
    // ================================================================
    public class PeopleNearbyItem {
        public String uuid;
        public int emoji1;
        public int emoji2;
        public int emoji3;
        public String personName;
        public String distance;

        public PeopleNearbyItem(String uuid, String emoji1, String emoji2, String emoji3,
                        String partnerName,
                        String distance) {
            this.uuid = uuid;
            this.emoji1 = getResources().getIdentifier(emoji1, "drawable",
                                                       getActivity().getPackageName());
            this.emoji2 = getResources().getIdentifier(emoji2, "drawable",
                                                       getActivity().getPackageName());
            this.emoji3 = getResources().getIdentifier(emoji3, "drawable",
                                                       getActivity().getPackageName());
            this.personName = partnerName;
            this.distance = distance;
        }
    }


    // ================================================================
    // Inner class to handle the population of items in the list
    // ================================================================
    public static class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private List<PeopleNearbyItem> mItems;
        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;

        public RecyclerViewAdapter(Context context, List<PeopleNearbyItem> items) {
            // Initialises the animated background of the each list item.
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;

            mItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.item_people_nearby_list, parent, false);

            // Sets the animated background of each list item to show when item is touched.
            view.setBackgroundResource(mBackground);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final PeopleNearbyItem currentItem = mItems.get(position);
            holder.emoji1.setImageResource(currentItem.emoji1);
            holder.emoji2.setImageResource(currentItem.emoji2);
            holder.emoji3.setImageResource(currentItem.emoji3);
            holder.personName.setText(currentItem.personName);
            holder.distance.setText(currentItem.distance);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("uuid", currentItem.uuid);
                    intent.putExtra("emoji_1", currentItem.emoji1);
                    intent.putExtra("emoji_2", currentItem.emoji2);
                    intent.putExtra("emoji_3", currentItem.emoji3);
                    intent.putExtra("generated_name", holder.personName.getText());
                    context.startActivity(intent);

                    Log.d(TAG, holder.personName.getText().toString());
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
            public final TextView personName;
            public final TextView distance;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                emoji1 = (ImageView) view.findViewById(R.id.emoji1);
                emoji2 = (ImageView) view.findViewById(R.id.emoji2);
                emoji3 = (ImageView) view.findViewById(R.id.emoji3);
                personName = (TextView) view.findViewById(R.id.personName);
                distance = (TextView) view.findViewById(R.id.distance);
            }
        }
    }
}
