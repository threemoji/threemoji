package com.threemoji.threemoji;

import com.threemoji.threemoji.data.ChatContract;
import com.threemoji.threemoji.service.BackgroundLocationService;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PeopleNearbyFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = PeopleNearbyFragment.class.getSimpleName();

    private boolean mIsVisibleToUser;

    private RecyclerView mRecyclerView;
    private TextView mNoDataText;
    private PeopleRecyclerViewAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private final String[] PEOPLE_NEARBY_ITEM_PROJECTION = new String[]{
            ChatContract.PeopleNearbyEntry.COLUMN_UID,
            ChatContract.PeopleNearbyEntry.COLUMN_EMOJI_1,
            ChatContract.PeopleNearbyEntry.COLUMN_EMOJI_2,
            ChatContract.PeopleNearbyEntry.COLUMN_EMOJI_3,
            ChatContract.PeopleNearbyEntry.COLUMN_GENDER,
            ChatContract.PeopleNearbyEntry.COLUMN_GENERATED_NAME,
            ChatContract.PeopleNearbyEntry.COLUMN_DISTANCE
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FrameLayout frameLayout = (FrameLayout) inflater.inflate(
                R.layout.fragment_people_nearby, container, false);

        mNoDataText = (TextView) frameLayout.findViewById(R.id.noDataText);
        mRecyclerView = (RecyclerView) frameLayout.findViewById(R.id.recyclerView);

        initSwipeRefresh();
        setupRecyclerView(mRecyclerView);

        if (mAdapter.getItemCount() == 0){
            showNoDataText();
            // Delay for 1s before finding people nearby;
            // this gives time for initial registration to succeed first
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getPeopleNearbyData();
                }
            }, 1000);
        } else {
            hideNoDataText();
        }
        return frameLayout;
    }

    private void initSwipeRefresh() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(
                R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_bright);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        Cursor cursor = getActivity().getContentResolver()
                                     .query(ChatContract.PeopleNearbyEntry.CONTENT_URI,
                                            PEOPLE_NEARBY_ITEM_PROJECTION, null, null, null);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new PeopleRecyclerViewAdapter(getActivity(), cursor);
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mSwipeRefreshLayout
                        .setEnabled(mIsVisibleToUser &&
                                    layoutManager.findFirstCompletelyVisibleItemPosition() == 0 &&
                                    !ViewCompat.canScrollVertically(recyclerView, -1));

            }
        });

        getActivity().getSupportLoaderManager().initLoader(1, null, this);
    }

    private void getPeopleNearbyData() {
        Toast.makeText(getActivity(), "Finding people nearby...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), BackgroundLocationService.class);
        intent.putExtra(getString(R.string.location_service_lookup_nearby), true);
        getActivity().startService(intent);
//        getActivity().startService(ChatIntentService.createIntent(getActivity(),
//                                                                  ChatIntentService.Action.LOOKUP_ALL));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), ChatContract.PeopleNearbyEntry.CONTENT_URI,
                                PEOPLE_NEARBY_ITEM_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.changeCursor(data);
        if (mAdapter.getItemCount() == 0) {
            showNoDataText();
        } else {
            hideNoDataText();
        }
    }

    private void showNoDataText() {
        mRecyclerView.setVisibility(View.GONE);
        mNoDataText.setVisibility(View.VISIBLE);
    }

    private void hideNoDataText() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mNoDataText.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    // Called when view is pulled down
    @Override
    public void onRefresh() {
        getPeopleNearbyData();
    }

    // Finds out if this fragment is in view
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    // ================================================================
    // Inner class to handle the population of items in the list
    // ================================================================
    public static class PeopleRecyclerViewAdapter
            extends RecyclerViewCursorAdapter<PeopleRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;

        public PeopleRecyclerViewAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            // Initialises the animated background of the each list item.
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
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
        public void onBindViewHolder(ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            final String uid = mCursor.getString(0);
            final String emoji1 = mCursor.getString(1);
            final String emoji2 = mCursor.getString(2);
            final String emoji3 = mCursor.getString(3);
            final String gender = mCursor.getString(4);
            final String personName = mCursor.getString(5);
            final String distance = mCursor.getString(6);

            holder.emoji1.setImageResource(mContext.getResources()
                                                   .getIdentifier(emoji1, "drawable",
                                                                  mContext.getPackageName()));
            holder.emoji2.setImageResource(mContext.getResources()
                                                   .getIdentifier(emoji2, "drawable",
                                                                  mContext.getPackageName()));
            holder.emoji3.setImageResource(mContext.getResources()
                                                   .getIdentifier(emoji3, "drawable",
                                                                  mContext.getPackageName()));
            holder.personName.setText(personName);
            holder.distance.setText(distance + "km");

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("uid", uid);
                    intent.putExtra("emoji_1", emoji1);
                    intent.putExtra("emoji_2", emoji2);
                    intent.putExtra("emoji_3", emoji3);
                    intent.putExtra("gender", gender);
                    intent.putExtra("generated_name", personName);
                    context.startActivity(intent);

                    Log.d(TAG, personName);
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
