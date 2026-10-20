package com.sj010.gboardclipboard.extension;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class GboardClipboardBackfill {

    static final String TAG = "GboardClipboard";
    private static final int RECENT_ENTRY_SCAN_LIMIT = 25;
    private static final int MAX_TEXT_LENGTH = 100000;

    private GboardClipboardBackfill() {
    }

    public static final class PrimaryClip {
        public final String text;
        public final String htmlText;
        public final boolean sensitive;
        public final String uri;
        public final long timestamp;

        PrimaryClip(String text, String htmlText, boolean sensitive, String uri, long timestamp) {
            this.text = text;
            this.htmlText = htmlText;
            this.sensitive = sensitive;
            this.uri = uri;
            this.timestamp = timestamp;
        }
    }

    public static PrimaryClip readPrimaryClip(Context context) {
        if (context == null) {
            return null;
        }
        try {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm == null || !cm.hasPrimaryClip()) {
                return null;
            }
            ClipData clip = cm.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) {
                return null;
            }
            ClipDescription desc = clip.getDescription();
            ClipData.Item item = clip.getItemAt(0);
            if (item == null) {
                return null;
            }

            boolean sensitive = desc != null && desc.getExtras() != null
                    && desc.getExtras().getBoolean("android.content.extra.IS_SENSITIVE", false);

            Uri uri = item.getUri();
            if (uri != null) {
                return new PrimaryClip(null, null, sensitive, uri.toString(), System.currentTimeMillis());
            }

            CharSequence text = item.getText();
            String textStr = text != null ? text.toString() : null;
            String htmlStr = item.getHtmlText();

            if ((textStr == null || textStr.isEmpty()) && (htmlStr == null || htmlStr.isEmpty())) {
                return null;
            }

            return new PrimaryClip(textStr, htmlStr, sensitive, null, System.currentTimeMillis());
        } catch (Throwable t) {
            Log.w(TAG, "Failed reading primary clip", t);
            return null;
        }
    }

    public static void backfillIfNecessary(Context context) {
        if (context == null) {
            return;
        }
        try {
            PrimaryClip primary = readPrimaryClip(context);
            if (primary == null || primary.text == null || primary.text.isEmpty()) {
                return;
            }
            if (primary.text.length() > MAX_TEXT_LENGTH) {
                return;
            }

            String marker = computeMarker(primary.text);
            String lastMarker = GboardClipboardHistorySettings.getBackfillMarker(context);
            if (marker.equals(lastMarker)) {
                return;
            }

            long rowId = GboardClipboardHistoryRepository.insertClipIfAbsent(
                    context, primary.text, primary.timestamp, primary.sensitive, RECENT_ENTRY_SCAN_LIMIT);

            if (rowId >= 0) {
                GboardClipboardHistorySettings.setBackfillMarker(context, marker);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed during clipboard backfill", t);
        }
    }

    private static String computeMarker(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Throwable ignored) {
            return String.valueOf(text.hashCode());
        }
    }
}
