package com.threemoji.threemoji;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import com.threemoji.threemoji.service.RegistrationIntentService;
import com.threemoji.threemoji.utility.EmojiList;
import com.threemoji.threemoji.utility.NameGenerator;
import com.threemoji.threemoji.utility.SvgUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class StartPageActivity extends AppCompatActivity implements SelectEmojiDialogFragment.SelectEmojiDialogListener {

    private enum Gender {FEMALE, MALE}

    private static final String TAG = StartPageActivity.class.getSimpleName();

    private ImageButton mCurrentEmojiButton;
    private int mSizeOfEmojiIcon = 72;
    private int mScrollPositionInDialog = 0;

    // ================================================================
    // Methods to handle emoji selection from the popup dialog
    // ================================================================
    @Override
    public void onEmojiClick(int position, int scrollPosition) {
        mScrollPositionInDialog = scrollPosition;

        int imageResource = EmojiList.allEmoji[position];
        Drawable drawable = SvgUtils.getSvgDrawable(imageResource, mSizeOfEmojiIcon,
                                                    getPackageName());

        TextView label = (TextView)((FrameLayout) mCurrentEmojiButton.getParent()).findViewById(R.id.start_page_emoji_label);
        label.setText("");
        mCurrentEmojiButton.setBackgroundResource(0);
        mCurrentEmojiButton.setImageDrawable(drawable);

        mCurrentEmojiButton.setTag(getResources().getResourceEntryName(imageResource));
        mCurrentEmojiButton = null;
    }


    // ================================================================
    // Initiation methods when this activity is created
    // ================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Activity started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);
        initEmojiButtons();
        initGender();

        startService(RegistrationIntentService.createIntent(this,
                                                            RegistrationIntentService.Action.CREATE_TOKEN));
    }

    private void initEmojiButtons() {
        Log.v(TAG, "Initalising emoji buttons");
        SharedPreferences prefs = getPrefs();

        String emoji1ResourceName = prefs.getString(getString(R.string.profile_emoji_one_key),
                                                    null);
        String emoji2ResourceName = prefs.getString(getString(R.string.profile_emoji_two_key),
                                                    null);
        String emoji3ResourceName = prefs.getString(getString(R.string.profile_emoji_three_key),
                                                    null);

        if (emoji1ResourceName != null) {
            updateImageById(emoji1ResourceName, R.id.start_page_emoji1);
        }
        if (emoji2ResourceName != null) {
            updateImageById(emoji2ResourceName, R.id.start_page_emoji2);
        }
        if (emoji3ResourceName != null) {
            updateImageById(emoji3ResourceName, R.id.start_page_emoji3);
        }
    }

    private void updateImageById(String resourceName, int id) {
        Drawable drawable = SvgUtils.getSvgDrawable(resourceName, mSizeOfEmojiIcon, getPackageName());
        ImageButton imageButton = (ImageButton) findViewById(id);
        TextView label = (TextView)((FrameLayout) imageButton.getParent()).findViewById(
                R.id.start_page_emoji_label);
        label.setText("");
        imageButton.setBackgroundResource(0);
        imageButton.setImageDrawable(drawable);
        imageButton.setTag(resourceName);
    }

    private void initGender() {
        Log.v(TAG, "Initialising gender");
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


    // ================================================================
    // Methods for when the start page is submitted
    // ================================================================
    public void submitStartPage(View view) {
        setProfileGender();

        ImageButton emoji1 = (ImageButton) findViewById(R.id.start_page_emoji1);
        ImageButton emoji2 = (ImageButton) findViewById(R.id.start_page_emoji2);
        ImageButton emoji3 = (ImageButton) findViewById(R.id.start_page_emoji3);

        if (hasUserSelectedAllEmoji(emoji1, emoji2, emoji3)) {
            setProfileEmoji(emoji1, emoji2, emoji3);
            setProfileGeneratedName();

            if (getPrefs().getBoolean(getString(R.string.pref_has_seen_start_page_key), false)) {
                startService(RegistrationIntentService.createIntent(this,
                                                                    RegistrationIntentService.Action.UPDATE_PROFILE));
            } else {
                startService(RegistrationIntentService.createIntent(this,
                                                                    RegistrationIntentService.Action.CREATE_PROFILE));
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

    private boolean hasUserSelectedAllEmoji(ImageButton emoji1, ImageButton emoji2,
                                            ImageButton emoji3) {
        return emoji1.getBackground() == null &&
               emoji2.getBackground() == null &&
               emoji3.getBackground() == null;
    }

    private void setProfileEmoji(ImageButton emoji1, ImageButton emoji2, ImageButton emoji3) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(getString(R.string.profile_emoji_one_key), emoji1.getTag().toString());
        editor.putString(getString(R.string.profile_emoji_two_key), emoji2.getTag().toString());
        editor.putString(getString(R.string.profile_emoji_three_key), emoji3.getTag().toString());
        editor.apply();
    }

    private void setProfileGeneratedName() {
        getPrefs().edit()
                  .putString(getString(R.string.profile_generated_name_key),
                             NameGenerator.getName())
                  .apply();
    }

    private void uploadProfile(boolean update) {
        int timeToLive = 60 * 60; // one hour
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String token = getPrefs().getString(getString(R.string.pref_token_key), "");
        try {
            Bundle data = new Bundle();
            data.putString("action", update ? getString(
                    R.string.backend_action_update_profile_key) : getString(
                    R.string.backend_action_upload_profile_key));
            data.putString(getString(R.string.backend_uid_key),
                           getPrefs().getString(getString(R.string.profile_uid_key), ""));
            data.putString(getString(R.string.backend_password_key),
                           getPrefs().getString(getString(R.string.profile_password_key), ""));
            data.putString(getString(R.string.backend_token_key), token);
            data.putString(getString(R.string.backend_emoji_one_key),
                    getPrefs().getString(getString(R.string.profile_emoji_one_key), ""));
            data.putString(getString(R.string.backend_emoji_two_key),
                    getPrefs().getString(getString(R.string.profile_emoji_two_key), ""));
            data.putString(getString(R.string.backend_emoji_three_key),
                    getPrefs().getString(getString(R.string.profile_emoji_three_key), ""));
            data.putString(getString(R.string.backend_generated_name_key),
                           getPrefs().getString(getString(R.string.profile_generated_name_key),
                                                ""));
            data.putString(getString(R.string.backend_gender_key),
                           getPrefs().getString(getString(R.string.profile_gender_key), ""));
            data.putString(getString(R.string.backend_location_key), "LOCATION");
            data.putString(getString(R.string.backend_radius_key),
                           getPrefs().getString(getString(R.string.pref_max_distance_key),
                                                getString(R.string.pref_max_distance_default)));
            String msgId = getNextMsgId(token);
            gcm.send(getString(R.string.gcm_project_num) + "@gcm.googleapis.com", msgId,
                     timeToLive, data);
            Log.v(TAG, "profile uploaded");
        } catch (IOException e) {
            Log.e(TAG,
                  "IOException while uploading profile to backend...", e);
        }
    }

    public String getNextMsgId(String token) {
        return token.substring(token.length() - 5).concat("" + System.currentTimeMillis());
    }


    // ================================================================
    // Handler for button that creates the select-emoji-dialog
    // ================================================================
    public void selectEmoji(View view) {
        mCurrentEmojiButton = (ImageButton) view;
        DialogFragment selectEmoji = new SelectEmojiDialogFragment();

        Bundle args = new Bundle();
        args.putInt("scrollPosition", mScrollPositionInDialog);
        selectEmoji.setArguments(args);

        selectEmoji.show(getSupportFragmentManager(), "select emoji");
    }


    // ================================================================
    // Utility methods
    // ================================================================
    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}
