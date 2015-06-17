package com.threemoji.threemoji;

import com.threemoji.threemoji.service.RegistrationIntentService;

import android.test.AndroidTestCase;

public class TestRegistrationIntentService extends AndroidTestCase {
    public static final String LOG_TAG = TestRegistrationIntentService.class.getSimpleName();

    public void testGetNextMsgId() {
        String token = "d9gdez4BpZo:APA91bEMAiAOZgi6HYtVVPlrS1dLrknh7gN9JdUMnGe9zkBKZ_SX2kyG8OTngxrKtUBbJuzBAIJ5dzWSVICk0ShokuoditzYqJQCCYt7IlG3juA8I7HIBOc";

        RegistrationIntentService service = new RegistrationIntentService();
        String msgId = service.getNextMsgId(token);
        assertEquals(msgId.substring(0, 5), "HIBOc");
        assertEquals(msgId.length(), 18);
    }
}
