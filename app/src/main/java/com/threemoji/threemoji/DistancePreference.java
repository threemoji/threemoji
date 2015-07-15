package com.threemoji.threemoji;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.threemoji.threemoji.service.RegistrationIntentService;

/**
 * This class is for the custom seekbar preference item seen in settings
 * http://stackoverflow.com/questions/16108609/android-creating-custom-preference
 * http://www.codeproject.com/Articles/163541/SeekBar-Preference
 * http://stackoverflow.com/questions/8956218/android-seekbar-setonseekbarchangelistener
 */
public class DistancePreference extends Preference {
    private final int minValue = Integer.parseInt(
            getContext().getString(R.string.pref_max_distance_min));

    private final int maxValue =
            Integer.parseInt(getContext().getString(R.string.pref_max_distance_max)) - minValue;

    private final String distanceKey = getContext().getString(R.string.pref_max_distance_key);
    private final String distanceDefault = getContext().getString(
            R.string.pref_max_distance_default);

    public DistancePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DistancePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(R.layout.preference_seekbar, parent, false);

        final TextView summary = (TextView) view.findViewById(R.id.distanceSummary);
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.distanceSeekBar);

        seekBar.setMax(maxValue);

        int currentValue = Integer.parseInt(getPrefs().getString(distanceKey, distanceDefault));
        seekBar.setProgress(currentValue);
        summary.setText(Integer.toString(currentValue));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int currentValue = progress + minValue;
                summary.setText(Integer.toString(currentValue));
                getPrefs().edit().putString(distanceKey, Integer.toString(currentValue)).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Context context = seekBar.getContext();
                context.startService(RegistrationIntentService.createIntent(context,
                        RegistrationIntentService.Action.UPDATE_PROFILE));
            }
        });

        return view;
    }

    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

}
