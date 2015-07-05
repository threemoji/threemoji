package com.threemoji.threemoji.data;

import com.threemoji.threemoji.data.ChatContract.MessageEntry;
import com.threemoji.threemoji.data.ChatContract.PartnerEntry;
import com.threemoji.threemoji.data.ChatContract.PeopleNearbyEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "chat.db";

    public ChatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PARTNER_TABLE =
                "CREATE TABLE " + PartnerEntry.TABLE_NAME + " (" +
                PartnerEntry._ID + " INTEGER, " +
                PartnerEntry.COLUMN_UUID + " TEXT PRIMARY KEY NOT NULL, " +
                PartnerEntry.COLUMN_EMOJI_1 + " TEXT NOT NULL, " +
                PartnerEntry.COLUMN_EMOJI_2 + " TEXT NOT NULL, " +
                PartnerEntry.COLUMN_EMOJI_3 + " TEXT NOT NULL, " +
                PartnerEntry.COLUMN_GENDER + " TEXT NOT NULL, " +
                PartnerEntry.COLUMN_GENERATED_NAME + " TEXT NOT NULL " +
                " );";

        final String SQL_CREATE_MESSAGE_TABLE =
                "CREATE TABLE " + MessageEntry.TABLE_NAME + " (" +
                MessageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MessageEntry.COLUMN_PARTNER_KEY + " INTEGER NOT NULL, " +
                MessageEntry.COLUMN_DATETIME + " BIGINT NOT NULL, " +
                MessageEntry.COLUMN_MESSAGE_TYPE + " TEXT NOT NULL, " +
                MessageEntry.COLUMN_MESSAGE_DATA + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + MessageEntry.COLUMN_PARTNER_KEY + ") REFERENCES " +
                PartnerEntry.TABLE_NAME + " (" + PartnerEntry.COLUMN_UUID + ") " +
                " );";

        final String SQL_CREATE_PEOPLE_NEARBY_TABLE =
                "CREATE TABLE " +
                PeopleNearbyEntry.TABLE_NAME + " (" +
                PeopleNearbyEntry._ID + " INTEGER, " +
                PeopleNearbyEntry.COLUMN_UUID + " TEXT PRIMARY KEY NOT NULL, " +
                PeopleNearbyEntry.COLUMN_EMOJI_1 + " TEXT NOT NULL, " +
                PeopleNearbyEntry.COLUMN_EMOJI_2 + " TEXT NOT NULL, " +
                PeopleNearbyEntry.COLUMN_EMOJI_3 + " TEXT NOT NULL, " +
                PeopleNearbyEntry.COLUMN_GENDER + " TEXT NOT NULL, " +
                PeopleNearbyEntry.COLUMN_GENERATED_NAME + " TEXT NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_PARTNER_TABLE);
        db.execSQL(SQL_CREATE_MESSAGE_TABLE);
        db.execSQL(SQL_CREATE_PEOPLE_NEARBY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PartnerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PeopleNearbyEntry.TABLE_NAME);
        onCreate(db);
    }
}
