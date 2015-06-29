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

    public static final class PartnerEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                                                              .appendPath(PATH_PARTNERS)
                                                              .build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                PATH_PARTNERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
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
    }

    public static final class MessageEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                                                              .appendPath(PATH_MESSAGES)
                                                              .build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                PATH_MESSAGES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                PATH_MESSAGES;

        public static final String TABLE_NAME = "messages";

        public static final String COLUMN_PARTNER_KEY = "partner_id";
        public static final String COLUMN_DATETIME = "date_time";
        public static final String COLUMN_SENT_OR_RECEIVED = "sent_or_received";
        public static final String COLUMN_MESSAGE_DATA = "message_data";

        public static Uri buildMessagesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMessagesWithPartner(String uuid, String emoji_1, String emoji_2,
                                                   String emoji_3, String gender,
                                                   String generated_name) {
            return CONTENT_URI.buildUpon()
                              .appendQueryParameter(PartnerEntry.COLUMN_UUID, uuid)
                              .appendQueryParameter(PartnerEntry.COLUMN_EMOJI_1, emoji_1)
                              .appendQueryParameter(PartnerEntry.COLUMN_EMOJI_2, emoji_2)
                              .appendQueryParameter(PartnerEntry.COLUMN_EMOJI_3, emoji_3)
                              .appendQueryParameter(PartnerEntry.COLUMN_GENDER, gender)
                              .appendQueryParameter(PartnerEntry.COLUMN_GENERATED_NAME,
                                                    generated_name).build();
        }

        public static String[] getQueryParamsFromUri(Uri uri) {
            return new String[]{uri.getQueryParameter(PartnerEntry.COLUMN_UUID),
                                uri.getQueryParameter(PartnerEntry.COLUMN_EMOJI_1),
                                uri.getQueryParameter(PartnerEntry.COLUMN_EMOJI_2),
                                uri.getQueryParameter(PartnerEntry.COLUMN_EMOJI_3),
                                uri.getQueryParameter(PartnerEntry.COLUMN_GENDER),
                                uri.getQueryParameter(PartnerEntry.COLUMN_GENERATED_NAME)};
        }
    }
}
