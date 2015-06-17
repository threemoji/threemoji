package com.threemoji.threemoji;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;


public class StartPageActivity extends AppCompatActivity implements SelectEmojiDialogFragment.SelectEmojiDialogListener {

    private enum Gender {FEMALE, MALE}

    private ImageButton mCurrentEmojiButton;
    private int sizeOfEmojiIcon = 72;

    @Override
    public void onEmojiClick(int position) {
        int imageResource = EmojiList.allEmoji[position];
        Drawable drawable = SvgUtils.getSvgDrawable(imageResource, sizeOfEmojiIcon,
                                                    getPackageName());
        mCurrentEmojiButton.setBackgroundResource(0);
        mCurrentEmojiButton.setImageDrawable(drawable);
//        mCurrentEmojiButton.setBackgroundResource(imageResource);
        setProfileEmoji(mCurrentEmojiButton, imageResource);
        mCurrentEmojiButton = null;
    }

    private void setProfileEmoji(ImageButton currentEmojiButton, int imageResource) {
        int buttonId = currentEmojiButton.getId();
        String preferenceKey;
        switch (buttonId) {
            case R.id.start_page_emoji1:
                preferenceKey = getString(R.string.profile_emoji_one_key);
                break;
            case R.id.start_page_emoji2:
                preferenceKey = getString(R.string.profile_emoji_two_key);
                break;
            case R.id.start_page_emoji3:
                preferenceKey = getString(R.string.profile_emoji_three_key);
                break;
            default:
                preferenceKey = null;
        }

        assert preferenceKey != null;
        getPrefs().edit().putInt(preferenceKey, imageResource).apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);
        initEmojiButtons();
    }

    private void initEmojiButtons() {
        SharedPreferences prefs = getPrefs();
        int emoji1ImageResource = prefs.getInt(getString(R.string.profile_emoji_one_key), -1);
        int emoji2ImageResource = prefs.getInt(getString(R.string.profile_emoji_two_key), -1);
        int emoji3ImageResource = prefs.getInt(getString(R.string.profile_emoji_three_key), -1);

        if (emoji1ImageResource != -1) {
//            findViewById(R.id.start_page_emoji1).setBackgroundResource(emoji1ImageResource);
            Drawable emoji1DrawableResource = SvgUtils.getSvgDrawable(emoji1ImageResource,
                                                                      sizeOfEmojiIcon,
                                                                      getPackageName());
            ((ImageButton) findViewById(R.id.start_page_emoji1)).setImageDrawable(
                    emoji1DrawableResource);
        }
        if (emoji2ImageResource != -1) {
//            findViewById(R.id.start_page_emoji2).setBackgroundResource(emoji2ImageResource);
            Drawable emoji2DrawableResource = SvgUtils.getSvgDrawable(emoji2ImageResource,
                                                                      sizeOfEmojiIcon,
                                                                      getPackageName());
            ((ImageButton) findViewById(R.id.start_page_emoji2)).setImageDrawable(
                    emoji2DrawableResource);
        }
        if (emoji3ImageResource != -1) {
//            findViewById(R.id.start_page_emoji3).setBackgroundResource(emoji3ImageResource);
            Drawable emoji3DrawableResource = SvgUtils.getSvgDrawable(emoji3ImageResource,
                                                                      sizeOfEmojiIcon,
                                                                      getPackageName());
            ((ImageButton) findViewById(R.id.start_page_emoji3)).setImageDrawable(
                    emoji3DrawableResource);
        }
    }

    public void submitStartPage(View view) {
        setProfileGender();

        if (hasUserSelectedAllEmoji()) {
            getPrefs().edit()
                      .putBoolean(getString(R.string.pref_has_seen_start_page_key), true)
                      .apply();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Please select 3 emoji", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasUserSelectedAllEmoji() {
        SharedPreferences prefs = getPrefs();
        return prefs.getInt(getString(R.string.profile_emoji_one_key), -1) != -1 &&
               prefs.getInt(getString(R.string.profile_emoji_two_key), -1) != -1 &&
               prefs.getInt(getString(R.string.profile_emoji_three_key), -1) != -1;
    }

    private void setProfileGender() {
        RadioGroup genderButtonGroup = (RadioGroup) findViewById(R.id.radio_group_gender);
        int radioButtonID = genderButtonGroup.getCheckedRadioButtonId();
        View radioButton = genderButtonGroup.findViewById(radioButtonID);
        int genderIdx = genderButtonGroup.indexOfChild(radioButton);

        Gender gender;
        switch (genderIdx) {
            case 0:
                gender = Gender.FEMALE;
                break;
            case 1:
                gender = Gender.MALE;
                break;
            default:
                gender = null;
        }
        assert gender != null;
        getPrefs().edit()
                  .putString(getString(R.string.profile_gender_key), gender.toString())
                  .apply();
    }

    public void selectEmoji(View view) {
        mCurrentEmojiButton = (ImageButton) view;
        DialogFragment selectEmoji = new SelectEmojiDialogFragment();
        selectEmoji.show(getSupportFragmentManager(), "select emoji");
    }

    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

}
