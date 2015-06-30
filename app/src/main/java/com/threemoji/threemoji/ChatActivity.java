package com.threemoji.threemoji;

import com.threemoji.threemoji.utility.SvgUtils;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

public class ChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        initActionBar(intent.getStringExtra("generated_name"),
                      intent.getIntExtra("emoji_1", 0),
                      intent.getIntExtra("emoji_2", 0),
                      intent.getIntExtra("emoji_3", 0));
    }

    private void initActionBar(String title, int emoji_1, int emoji_2, int emoji_3) {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                                                ActionBar.DISPLAY_HOME_AS_UP |
                                                ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setTitle(title);
        Drawable emoji_1_drawable = SvgUtils.getSvgDrawable(emoji_1, 24, getPackageName());
        Drawable emoji_2_drawable = SvgUtils.getSvgDrawable(emoji_2, 24, getPackageName());
        Drawable emoji_3_drawable = SvgUtils.getSvgDrawable(emoji_3, 24, getPackageName());
        ((ImageView) findViewById(R.id.title_emoji_1)).setImageDrawable(emoji_1_drawable);
        ((ImageView) findViewById(R.id.title_emoji_2)).setImageDrawable(emoji_2_drawable);
        ((ImageView) findViewById(R.id.title_emoji_3)).setImageDrawable(emoji_3_drawable);
    }
}
