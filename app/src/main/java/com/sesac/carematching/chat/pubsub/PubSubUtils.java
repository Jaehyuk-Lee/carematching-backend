package com.sesac.carematching.chat.pubsub;

public class PubSubUtils {
    private PubSubUtils() {}
    public static boolean isOwnOrigin(String origin, String instanceId) {
        return origin != null && origin.equals(instanceId);
    }
}
