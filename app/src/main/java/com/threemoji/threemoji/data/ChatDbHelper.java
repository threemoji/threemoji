package com.threemoji.threemoji.data;

import com.threemoji.threemoji.data.ChatContract.MessageEntry;
import com.threemoji.threemoji.data.ChatContract.PartnerEntry;
import com.threemoji.threemoji.data.ChatContract.PeopleNearbyEntry;

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
        createPartnersTable(db);
        createMessagesTable(db);
        createPeopleNearbyTable(db);
    }

    private void createPartnersTable(SQLiteDatabase db) {
        final String SQL_CREATE_PARTNERS_TABLE =
                "CREATE TABLE " + PartnerEntry.TABLE_NAME + " (" +
                PartnerEntry.COLUMN_UID + " TEXT PRIMARY KEY NOT NULL, " +
                PartnerEntry.COLUMN_EMOJI_1 + " TEXT NOT NULL, " +
                PartnerEntry.COLUMN_EMOJI_2 + " TEXT NOT NULL, " +
                PartnerEntry.COLUMN_EMOJI_3 + " TEXT NOT NULL, " +
                PartnerEntry.COLUMN_GENDER + " TEXT NOT NULL, " +
                PartnerEntry.COLUMN_GENERATED_NAME + " TEXT NOT NULL, " +
                PartnerEntry.COLUMN_IS_ALIVE + " INTEGER DEFAULT 1, " +
                PartnerEntry.COLUMN_IS_ARCHIVED + " INTEGER DEFAULT 0, " +
                PartnerEntry.COLUMN_LAST_ACTIVITY + " INTEGER DEFAULT 0, " +
                PartnerEntry.COLUMN_NUM_NEW_MESSAGES + " INTEGER DEFAULT 0, " +
                PartnerEntry.COLUMN_IS_MUTED + " INTEGER DEFAULT 0 " +
                " );";
        db.execSQL(SQL_CREATE_PARTNERS_TABLE);
    }

    private void createMessagesTable(SQLiteDatabase db) {
        final String SQL_CREATE_MESSAGES_TABLE =
                "CREATE TABLE " + MessageEntry.TABLE_NAME + " (" +
                MessageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MessageEntry.COLUMN_PARTNER_KEY + " INTEGER NOT NULL, " +
                MessageEntry.COLUMN_DATETIME + " INTEGER DEFAULT 0, " +
                MessageEntry.COLUMN_MESSAGE_TYPE + " TEXT NOT NULL, " +
                MessageEntry.COLUMN_MESSAGE_DATA + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + MessageEntry.COLUMN_PARTNER_KEY + ") REFERENCES " +
                PartnerEntry.TABLE_NAME + " (" + PartnerEntry.COLUMN_UID + ") " +
                " );";
        db.execSQL(SQL_CREATE_MESSAGES_TABLE);
    }

    private void createPeopleNearbyTable(SQLiteDatabase db) {
        final String SQL_CREATE_PEOPLE_NEARBY_TABLE =
                "CREATE TABLE " +
                PeopleNearbyEntry.TABLE_NAME + " (" +
                PeopleNearbyEntry.COLUMN_UID + " TEXT PRIMARY KEY NOT NULL, " +
                PeopleNearbyEntry.COLUMN_EMOJI_1 + " TEXT NOT NULL, " +
                PeopleNearbyEntry.COLUMN_EMOJI_2 + " TEXT NOT NULL, " +
                PeopleNearbyEntry.COLUMN_EMOJI_3 + " TEXT NOT NULL, " +
                PeopleNearbyEntry.COLUMN_GENDER + " TEXT NOT NULL, " +
                PeopleNearbyEntry.COLUMN_GENERATED_NAME + " TEXT NOT NULL, " +
                PeopleNearbyEntry.COLUMN_DISTANCE + " REAL NOT NULL " +
                " );";
        db.execSQL(SQL_CREATE_PEOPLE_NEARBY_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + PartnerEntry.TABLE_NAME);
//        db.execSQL("DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME);
//        db.execSQL("DROP TABLE IF EXISTS " + PeopleNearbyEntry.TABLE_NAME);
//        onCreate(db);
    }
}
