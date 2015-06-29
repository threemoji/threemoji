package com.threemoji.threemoji.data;

import com.threemoji.threemoji.data.ChatContract.PartnerEntry;
import com.threemoji.threemoji.data.ChatContract.MessageEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "chat.db";

    public ChatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PARTNER_TABLE =
                "CREATE_TABLE " + PartnerEntry.TABLE_NAME + " (" +
                PartnerEntry._ID + " INTEGER PRIMARY KEY, " +
                PartnerEntry.COLUMN_UUID + " TEXT NOT NULL, " +
                PartnerEntry.COLUMN_EMOJI_1 + " INTEGER NOT NULL, " +
                PartnerEntry.COLUMN_EMOJI_2 + " INTEGER NOT NULL, " +
                PartnerEntry.COLUMN_EMOJI_3 + " INTEGER NOT NULL, " +
                PartnerEntry.COLUMN_GENDER + " TEXT NOT NULL, " +
                PartnerEntry.COLUMN_GENERATED_NAME + " TEXT NOT NULL, " +
//                "PRIMARY KEY (" + PartnerEntry.COLUMN_UUID + ", " + PartnerEntry._ID + ") " +
                " );";

        final String SQL_CREATE_MESSAGE_TABLE =
                "CREATE_TABLE " + MessageEntry.TABLE_NAME + " (" +
                MessageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MessageEntry.COLUMN_PARTNER_KEY + " INTEGER NOT NULL, " +
                MessageEntry.COLUMN_DATETIME + " BIGINT NOT NULL, " +
                MessageEntry.COLUMN_SENT_OR_RECEIVED + " TEXT NOT NULL, " +
                MessageEntry.COLUMN_MESSAGE_DATA + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + MessageEntry.COLUMN_PARTNER_KEY + ") REFERENCES " +
                PartnerEntry.TABLE_NAME + " (" + PartnerEntry._ID + ") " +
                " );";

        db.execSQL(SQL_CREATE_PARTNER_TABLE);
        db.execSQL(SQL_CREATE_MESSAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
