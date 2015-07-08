package com.threemoji.threemoji.data;

import com.threemoji.threemoji.data.ChatContract.MessageEntry;
import com.threemoji.threemoji.data.ChatContract.PartnerEntry;
import com.threemoji.threemoji.data.ChatContract.PeopleNearbyEntry;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class ChatProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ChatDbHelper mChatDbHelper;

    static final int MESSAGES_WITH_PARTNER = 101;
    static final int PARTNERS = 200;
    static final int PARTNER = 201;
    static final int PEOPLE_NEARBY = 300;

    private static final SQLiteQueryBuilder sMessagesWithPartnerQueryBuilder;

    static {
        sMessagesWithPartnerQueryBuilder = new SQLiteQueryBuilder();

        // messages INNER JOIN partners ON messages.partner_id = partners.uuid
        sMessagesWithPartnerQueryBuilder.setTables(
                MessageEntry.TABLE_NAME + " INNER JOIN " +
                PartnerEntry.TABLE_NAME + " ON " +
                MessageEntry.TABLE_NAME + "." +
                MessageEntry.COLUMN_PARTNER_KEY + " = " +
                PartnerEntry.TABLE_NAME + "." +
                PartnerEntry.COLUMN_UUID
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ChatContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, ChatContract.PATH_MESSAGES + "/*", MESSAGES_WITH_PARTNER);
        matcher.addURI(authority, ChatContract.PATH_PARTNERS, PARTNERS);
        matcher.addURI(authority, ChatContract.PATH_PARTNERS + "/*", PARTNER);
        matcher.addURI(authority, ChatContract.PATH_PEOPLE_NEARBY, PEOPLE_NEARBY);

        return matcher;
    }

    private Cursor getMessagesWithPartner(Uri uri, String[] projection, String sortOrder) {
        String[] selectionArgs =
                new String[]{uri.getPathSegments().get(1)};
        String selection =
                PartnerEntry.TABLE_NAME + "." + PartnerEntry.COLUMN_UUID + " = ? ";

        return sMessagesWithPartnerQueryBuilder.query(mChatDbHelper.getReadableDatabase(),
                                                      projection,
                                                      selection,
                                                      selectionArgs,
                                                      null,
                                                      null,
                                                      sortOrder);
    }

    private Cursor getPartnerByUuid(Uri uri, String[] projection, String sortOrder) {
        String[] selectionArgs =
                new String[]{uri.getPathSegments().get(1)};
        String selection =
                PartnerEntry.TABLE_NAME + "." + PartnerEntry.COLUMN_UUID + " = ? ";

        return mChatDbHelper.getReadableDatabase().query(PartnerEntry.TABLE_NAME,
                                                         projection,
                                                         selection,
                                                         selectionArgs,
                                                         null,
                                                         null,
                                                         sortOrder);
    }

    @Override
    public boolean onCreate() {
        mChatDbHelper = new ChatDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor returnCursor;
        switch (sUriMatcher.match(uri)) {
            case MESSAGES_WITH_PARTNER:
                returnCursor = getMessagesWithPartner(uri, projection, sortOrder);
                break;
            case PARTNERS:
                returnCursor = mChatDbHelper.getReadableDatabase().query(
                        PartnerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case PARTNER:
                returnCursor = getPartnerByUuid(uri, projection, sortOrder);
                break;
            case PEOPLE_NEARBY:
                returnCursor = mChatDbHelper.getReadableDatabase().query(
                        PeopleNearbyEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case MESSAGES_WITH_PARTNER:
                return MessageEntry.CONTENT_TYPE;
            case PARTNERS:
                return PartnerEntry.CONTENT_TYPE;
            case PARTNER:
                return PartnerEntry.CONTENT_ITEM_TYPE;
            case PEOPLE_NEARBY:
                return PeopleNearbyEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mChatDbHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case MESSAGES_WITH_PARTNER: {
                long _id = db.insert(MessageEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = MessageEntry.buildMessageUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case PARTNERS: {
                long _id = db.insert(PartnerEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = PartnerEntry.buildPartnerUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case PEOPLE_NEARBY: {
                long _id = db.insert(PeopleNearbyEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = PeopleNearbyEntry.buildPeopleNearbyUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mChatDbHelper.getWritableDatabase();
        int rowsDeleted;
        if (selection == null) {
            selection = "1";
        }
        switch (sUriMatcher.match(uri)) {
            case MESSAGES_WITH_PARTNER:
                rowsDeleted = db.delete(MessageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PARTNERS:
                rowsDeleted = db.delete(PartnerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PEOPLE_NEARBY:
                rowsDeleted = db.delete(PeopleNearbyEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mChatDbHelper.getWritableDatabase();
        int rowsUpdated;
        if (selection == null) {
            selection = "1";
        }
        switch (sUriMatcher.match(uri)) {
            case MESSAGES_WITH_PARTNER:
                rowsUpdated = db.update(MessageEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PARTNERS:
                rowsUpdated = db.update(PartnerEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PEOPLE_NEARBY:
                rowsUpdated = db.update(PeopleNearbyEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mChatDbHelper.close();
        super.shutdown();
    }
}
