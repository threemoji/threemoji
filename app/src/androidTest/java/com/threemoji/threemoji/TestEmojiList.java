package com.threemoji.threemoji;

import com.threemoji.threemoji.utility.EmojiList;

import android.test.AndroidTestCase;

public class TestEmojiList extends AndroidTestCase {
    public void testLengthOfAllEmoji() {
        int totalNumberOfEmoji = EmojiList.emoticons.length +
                                 EmojiList.nature.length +
                                 EmojiList.objects.length +
                                 EmojiList.places.length +
                                 EmojiList.other.length;
        assertEquals(EmojiList.allEmoji.length, totalNumberOfEmoji);
    }
}
