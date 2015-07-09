package com.threemoji.threemoji.utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateUtils {
    public static String getDate(long milliseconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return formatter.format(calendar.getTime());
    }

    public static String getDate(long milliseconds) {
        return getDate(milliseconds, "KK:mm ss.SSS a, dd LLL");
    }

    public static String getTimeAgo(long milliseconds) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - milliseconds;

        long diffDays = diff / (24 * 60 * 60 * 1000);
        if (diffDays > 0) {
            if (diffDays == 1) {
                return "1 day ago";
            }
            return diffDays + " days ago";
        }

        long diffHours = diff / (60 * 60 * 1000) % 24;
        if (diffHours > 0) {
            if (diffHours == 1) {
                return "1 hour ago";
            }
            return diffHours + " hours ago";
        }

        long diffMinutes = diff / (60 * 1000) % 60;
        if (diffMinutes > 0) {
            if (diffMinutes == 1) {
                return "1 minute ago";
            }
            return diffMinutes + " minutes ago";
        }

        long diffSeconds = diff / 1000 % 60;
        if (diffSeconds > 0) {
            if (diffSeconds == 1) {
                return "1 second ago";
            }
            return diffSeconds + " seconds ago";
        }

        return "";
    }
}
