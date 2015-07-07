package com.threemoji.threemoji.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class ChatContract {
    public static final String CONTENT_AUTHORITY = "com.threemoji.threemoji";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PARTNERS = "partners";
    public static final String PATH_MESSAGES = "messages";
    public static final String PATH_PEOPLE_NEARBY = "people_nearby";

    public static final class PartnerEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                                                              .appendPath(PATH_PARTNERS)
                                                              .build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" +
                PATH_PARTNERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" +
                PATH_PARTNERS;

        public static final String TABLE_NAME = "partners";

        public static final String COLUMN_UUID = "uuid";
        public static final String COLUMN_EMOJI_1 = "emoji_1";
        public static final String COLUMN_EMOJI_2 = "emoji_2";
        public static final String COLUMN_EMOJI_3 = "emoji_3";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_GENERATED_NAME = "generated_name";

        public static Uri buildPartnerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPartnerByUuidUri(String uuid) {
            return CONTENT_URI.buildUpon().appendPath(uuid).build();
        }
    }

    public static final class MessageEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                                                              .appendPath(PATH_MESSAGES)
                                                              .build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" +
                PATH_MESSAGES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" +
                PATH_MESSAGES;

        public static final String TABLE_NAME = "messages";

        public static final String COLUMN_PARTNER_KEY = "partner_id";
        public static final String COLUMN_DATETIME = "date_time";
        public static final String COLUMN_MESSAGE_TYPE = "message_type";
        public static final String COLUMN_MESSAGE_DATA = "message_data";

        public enum MessageType {
            SENT, RECEIVED, ALERT
        }

        public static Uri buildMessageUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMessagesWithPartnerUri(String uuid) {
            return CONTENT_URI.buildUpon().appendPath(uuid).build();
        }
    }

    public static final class PeopleNearbyEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                                                              .appendPath(PATH_PEOPLE_NEARBY)
                                                              .build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" +
                PATH_PEOPLE_NEARBY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" +
                PATH_PEOPLE_NEARBY;

        public static final String TABLE_NAME = "people";

        public static final String COLUMN_UUID = "uuid";
        public static final String COLUMN_EMOJI_1 = "emoji_1";
        public static final String COLUMN_EMOJI_2 = "emoji_2";
        public static final String COLUMN_EMOJI_3 = "emoji_3";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_GENERATED_NAME = "generated_name";
        public static final String COLUMN_DISTANCE = "distance";

        public static Uri buildPeopleNearbyUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
