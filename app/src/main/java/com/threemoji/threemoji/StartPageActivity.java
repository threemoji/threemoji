package com.threemoji.threemoji;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.UUID;


public class StartPageActivity extends AppCompatActivity implements SelectEmojiDialogFragment.SelectEmojiDialogListener {

    private enum Gender {FEMALE, MALE}

    private static final String TAG = StartPageActivity.class.getSimpleName();
    private int timeToLive = 60 * 60; // one hour

    private ImageButton mCurrentEmojiButton;
    private int mSizeOfEmojiIcon = 72;
    private int mScrollPositionInDialog = 0;

    @Override
    public void onEmojiClick(int position, int scrollPosition) {
        mScrollPositionInDialog = scrollPosition;
        int imageResource = EmojiList.allEmoji[position];
        Drawable drawable = SvgUtils.getSvgDrawable(imageResource, mSizeOfEmojiIcon,
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
        initGender();
        initUid();
        initPassword();
    }

    private void initUid() {
        String uid = getPrefs().getString(getString(R.string.profile_uid_key), null);
        if (uid == null) {
            getPrefs().edit()
                      .putString(getString(R.string.profile_uid_key), UUID.randomUUID().toString())
                      .apply();
        }
    }

    private void initPassword() {
        String password = getPrefs().getString(getString(R.string.profile_password_key), null);
        if (password == null) {
            getPrefs().edit()
                    .putString(getString(R.string.profile_password_key), InstanceID.getInstance(this).getId())
                    .apply();
        }
    }

    private void initGender() {
        String genderPref = getPrefs().getString(getString(R.string.profile_gender_key), null);
        if (genderPref != null) {
            Gender gender = Gender.valueOf(genderPref);
            switch (gender) {
                case FEMALE:
                    ((RadioGroup) findViewById(R.id.radio_group_gender)).check(
                            R.id.radio_button_female);
                    break;
                case MALE:
                    ((RadioGroup) findViewById(R.id.radio_group_gender)).check(
                            R.id.radio_button_male);
                    break;
            }
        }
    }

    private void initEmojiButtons() {
        SharedPreferences prefs = getPrefs();
        int emoji1ImageResource = prefs.getInt(getString(R.string.profile_emoji_one_key), -1);
        int emoji2ImageResource = prefs.getInt(getString(R.string.profile_emoji_two_key), -1);
        int emoji3ImageResource = prefs.getInt(getString(R.string.profile_emoji_three_key), -1);

        if (emoji1ImageResource != -1) {
//            findViewById(R.id.start_page_emoji1).setBackgroundResource(emoji1ImageResource);
            Drawable emoji1DrawableResource = SvgUtils.getSvgDrawable(emoji1ImageResource,
                                                                      mSizeOfEmojiIcon,
                                                                      getPackageName());
            ImageButton emoji1 = (ImageButton) findViewById(R.id.start_page_emoji1);
            emoji1.setBackgroundResource(0);
            emoji1.setImageDrawable(emoji1DrawableResource);
        }
        if (emoji2ImageResource != -1) {
//            findViewById(R.id.start_page_emoji2).setBackgroundResource(emoji2ImageResource);
            Drawable emoji2DrawableResource = SvgUtils.getSvgDrawable(emoji2ImageResource,
                                                                      mSizeOfEmojiIcon,
                                                                      getPackageName());
            ImageButton emoji2 = (ImageButton) findViewById(R.id.start_page_emoji2);
            emoji2.setBackgroundResource(0);
            emoji2.setImageDrawable(emoji2DrawableResource);
        }
        if (emoji3ImageResource != -1) {
//            findViewById(R.id.start_page_emoji3).setBackgroundResource(emoji3ImageResource);
            Drawable emoji3DrawableResource = SvgUtils.getSvgDrawable(emoji3ImageResource,
                                                                      mSizeOfEmojiIcon,
                                                                      getPackageName());
            ImageButton emoji3 = (ImageButton) findViewById(R.id.start_page_emoji3);
            emoji3.setBackgroundResource(0);
            emoji3.setImageDrawable(emoji3DrawableResource);
        }
    }

    public void submitStartPage(View view) {
        setProfileGender();

        if (hasUserSelectedAllEmoji()) {
            setProfileGeneratedName();
            if (!getPrefs().getBoolean(getString(R.string.pref_has_seen_start_page_key), false)) {
                uploadProfile(false);
            } else {
                uploadProfile(true);
            }
            getPrefs().edit()
                    .putBoolean(getString(R.string.pref_has_seen_start_page_key), true)
                    .apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Please select 3 emoji", Toast.LENGTH_SHORT).show();
        }
    }

    private void setProfileGeneratedName() {
        String name = NameGenerator.getName();
        getPrefs().edit().putString(getString(R.string.profile_generated_name_key), name).apply();
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

        Bundle args = new Bundle();
        args.putInt("scrollPosition", mScrollPositionInDialog);
        selectEmoji.setArguments(args);

        selectEmoji.show(getSupportFragmentManager(), "select emoji");
    }

    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    public String getNextMsgId(String token) {
        return token.substring(token.length() - 5).concat("" + System.currentTimeMillis());
    }

    private void uploadProfile(boolean update) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String token = getPrefs().getString(getString(R.string.pref_token_key), "");
        try {
            Bundle data = new Bundle();
            data.putString("action", update ? getString(R.string.backend_action_update_profile_key) : getString(R.string.backend_action_upload_profile_key));
            data.putString(getString(R.string.backend_uid_key), getPrefs().getString(getString(R.string.profile_uid_key), ""));
            data.putString(getString(R.string.backend_password_key), getPrefs().getString(getString(R.string.profile_password_key), ""));
            data.putString(getString(R.string.backend_token_key), token);
            data.putString(getString(R.string.backend_emoji_one_key), String.valueOf(getPrefs().getInt(getString(R.string.profile_emoji_one_key), -1)));
            data.putString(getString(R.string.backend_emoji_two_key), String.valueOf(getPrefs().getInt(getString(R.string.profile_emoji_two_key), -1)));
            data.putString(getString(R.string.backend_emoji_three_key), String.valueOf(getPrefs().getInt(getString(R.string.profile_emoji_three_key), -1)));
            data.putString(getString(R.string.backend_generated_name_key), getPrefs().getString(getString(R.string.profile_generated_name_key), ""));
            data.putString(getString(R.string.backend_gender_key), getPrefs().getString(getString(R.string.profile_gender_key), ""));
            data.putString(getString(R.string.backend_location_key), "LOCATION");
            data.putString(getString(R.string.backend_radius_key), getPrefs().getString(getString(R.string.pref_max_distance_key), "10"));
            String msgId = getNextMsgId(token);
            gcm.send(getString(R.string.gcm_project_num) + "@gcm.googleapis.com", msgId,
                    timeToLive, data);
            Log.v(TAG, "profile uploaded");
        } catch (IOException e) {
            Log.e(TAG,
                    "IOException while uploading profile to backend...", e);
        }
    }

}
