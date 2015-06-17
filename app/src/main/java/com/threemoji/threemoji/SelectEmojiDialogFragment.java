package com.threemoji.threemoji;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;


public class SelectEmojiDialogFragment extends DialogFragment {

    public interface SelectEmojiDialogListener {
        void onEmojiClick(int position, int scrollPosition);
    }

    private SelectEmojiDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SelectEmojiDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                                         + " must implement SelectEmojiDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int scrollPosition = getArguments().getInt("scrollPosition");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final GridView gridView = (GridView) inflater.inflate(R.layout.dialog_select_emoji, null);

        gridView.setAdapter(new SelectEmojiAdapter(getActivity()));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                mListener.onEmojiClick(position, gridView.getFirstVisiblePosition());
                dismiss();
            }
        });

        gridView.setSelection(scrollPosition);

        builder.setView(gridView);
        return builder.create();

    }
}
