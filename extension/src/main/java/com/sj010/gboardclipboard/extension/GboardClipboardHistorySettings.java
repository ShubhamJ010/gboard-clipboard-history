package com.sj010.gboardclipboard.extension;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.security.SecureRandom;

public final class GboardClipboardHistorySettings {

    public static final String PERMISSION_READ_CLIPBOARD_HISTORY =
            "com.sj010.gboardclipboard.permission.READ_CLIPBOARD_HISTORY";

    static final String PREF_FILE_NAME = "gboard_clipboard_history_prefs";

    static final String PREF_KEY_EXTERNAL_ACCESS_ENABLED =
            "pref_clipboard_history_external_access_enabled";
    static final String PREF_KEY_INCLUDE_SENSITIVE =
            "pref_clipboard_history_include_sensitive";
    static final String PREF_KEY_ACCESS_TOKEN = "pref_clipboard_history_access_token";
    static final String PREF_KEY_BACKFILL_MARKER = "pref_clipboard_history_backfill_marker";

    private static final String TAG = "GboardClipboard";
    private static final int ACCESS_TOKEN_BYTES = 16;
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private GboardClipboardHistorySettings() {
    }

    public static boolean isExternalAccessEnabled(Context context) {
        SharedPreferences preferences = preferences(context);
        return preferences != null && preferences.getBoolean(PREF_KEY_EXTERNAL_ACCESS_ENABLED, false);
    }

    public static void setExternalAccessEnabled(Context context, boolean enabled) {
        SharedPreferences preferences = preferences(context);
        if (preferences != null) {
            preferences.edit().putBoolean(PREF_KEY_EXTERNAL_ACCESS_ENABLED, enabled).apply();
        }
    }

    public static boolean isSensitiveIncluded(Context context) {
        SharedPreferences preferences = preferences(context);
        return preferences != null && preferences.getBoolean(PREF_KEY_INCLUDE_SENSITIVE, false);
    }

    public static void setSensitiveIncluded(Context context, boolean included) {
        SharedPreferences preferences = preferences(context);
        if (preferences != null) {
            preferences.edit().putBoolean(PREF_KEY_INCLUDE_SENSITIVE, included).apply();
        }
    }

    public static String getAccessToken(Context context) {
        SharedPreferences preferences = preferences(context);
        if (preferences == null) {
            return "";
        }
        String token = preferences.getString(PREF_KEY_ACCESS_TOKEN, null);
        if (token != null && !token.isEmpty()) {
            return token;
        }
        String generated = generateAccessToken();
        preferences.edit().putString(PREF_KEY_ACCESS_TOKEN, generated).apply();
        return generated;
    }

    public static String regenerateAccessToken(Context context) {
        SharedPreferences preferences = preferences(context);
        if (preferences == null) {
            return "";
        }
        String generated = generateAccessToken();
        preferences.edit().putString(PREF_KEY_ACCESS_TOKEN, generated).apply();
        return generated;
    }

    public static String getBackfillMarker(Context context) {
        SharedPreferences preferences = preferences(context);
        return preferences != null ? preferences.getString(PREF_KEY_BACKFILL_MARKER, "") : "";
    }

    public static void setBackfillMarker(Context context, String marker) {
        SharedPreferences preferences = preferences(context);
        if (preferences != null) {
            if (marker == null || marker.isEmpty()) {
                preferences.edit().remove(PREF_KEY_BACKFILL_MARKER).apply();
            } else {
                preferences.edit().putString(PREF_KEY_BACKFILL_MARKER, marker).apply();
            }
        }
    }

    public static boolean isAccessTokenValid(Context context, String candidate) {
        if (candidate == null || candidate.isEmpty()) {
            return false;
        }
        String expected = getAccessToken(context);
        return !expected.isEmpty() && constantTimeEquals(expected, candidate);
    }

    static boolean constantTimeEquals(String expected, String candidate) {
        if (expected == null || candidate == null) {
            return false;
        }
        byte[] expectedBytes = expected.getBytes();
        byte[] candidateBytes = candidate.getBytes();
        int difference = expectedBytes.length ^ candidateBytes.length;
        for (int index = 0; index < expectedBytes.length && index < candidateBytes.length; index++) {
            difference |= expectedBytes[index] ^ candidateBytes[index];
        }
        return difference == 0;
    }

    static String generateAccessToken() {
        byte[] bytes = new byte[ACCESS_TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        char[] digits = new char[bytes.length * 2];
        for (int index = 0; index < bytes.length; index++) {
            int value = bytes[index] & 0xFF;
            digits[index * 2] = HEX_DIGITS[value >>> 4];
            digits[index * 2 + 1] = HEX_DIGITS[value & 0x0F];
        }
        return new String(digits);
    }

    private static SharedPreferences preferences(Context context) {
        if (context == null) {
            return null;
        }
        try {
            return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        } catch (Throwable t) {
            Log.w(TAG, "Failed reading clipboard settings prefs", t);
            return null;
        }
    }
}
