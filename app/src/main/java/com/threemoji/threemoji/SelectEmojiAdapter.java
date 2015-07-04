package com.threemoji.threemoji;

import com.threemoji.threemoji.utility.EmojiList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


public class SelectEmojiAdapter extends BaseAdapter {
    private Context mContext;
    private int emoji_size;

    public SelectEmojiAdapter(Context c) {
        mContext = c;
        emoji_size = (int) mContext.getResources().getDimension(R.dimen.emoji_selection_size);
    }

    @Override
    public int getCount() {
        return EmojiList.allEmoji.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(emoji_size, emoji_size));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(EmojiList.allEmoji[position]);
        return imageView;
    }
}
