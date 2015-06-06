package com.threemoji.threemoji;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class ChatListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) inflater.inflate(
                R.layout.fragment_chat_list, container, false);
        setupRecyclerView(rv);
        return rv;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        String[] chatPartners = {"Flying Pig", "Jumping Spider", "Hopping Elephant",
                "Flying Pig", "Jumping Spider", "Hopping Elephant",
                "Flying Pig", "Jumping Spider", "Hopping Elephant",
                "Flying Pig", "Jumping Spider", "Hopping Elephant",
                "Flying Pig", "Jumping Spider", "Hopping Elephant",
                "Flying Pig", "Jumping Spider", "Hopping Elephant"};

        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new RecyclerViewAdapter(getActivity(), Arrays.asList(chatPartners)));
    }


    public static class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<String> mValues;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public String mBoundString;

            public final View mView;
            public final ImageView mImageView1;
            public final ImageView mImageView2;
            public final ImageView mImageView3;
            public final TextView mTextView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView1 = (ImageView) view.findViewById(R.id.emoji1);
                mImageView2 = (ImageView) view.findViewById(R.id.emoji2);
                mImageView3 = (ImageView) view.findViewById(R.id.emoji3);
                mTextView = (TextView) view.findViewById(R.id.partner_name);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTextView.getText();
            }
        }

        public String getValueAt(int position) {
            return mValues.get(position);
        }

        public RecyclerViewAdapter(Context context, List<String> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_list, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mBoundString = mValues.get(position);
            holder.mTextView.setText(mValues.get(position));

//            holder.mView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Context context = v.getContext();
//                    Intent intent = new Intent(context, ChatActivity.class);
//                    intent.putExtra(ChatActivity.EXTRA_NAME, holder.mBoundString);
//
//                    context.startActivity(intent);
//                }
//            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }
}
