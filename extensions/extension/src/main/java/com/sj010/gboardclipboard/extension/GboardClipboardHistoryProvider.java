package com.sj010.gboardclipboard.extension;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.util.Log;

import java.util.List;

/**
 * Read-only ContentProvider exposing Gboard's clipboard history.
 * Authority: content://com.sj010.gboardclipboard.history/clips
 */
public final class GboardClipboardHistoryProvider extends ContentProvider {

    public static final String AUTHORITY = "com.sj010.gboardclipboard.history";
    public static final String PATH_CLIPS = "clips";    public static final String PATH_PRIMARY = "primary";
    public static final String QUERY_PARAM_TOKEN = "token";

    public static final String MIME_TYPE_CLIPS_DIR = "vnd.android.cursor.dir/clips";
    public static final String MIME_TYPE_CLIP_ITEM = "vnd.android.cursor.item/clips";
    public static final String MIME_TYPE_PRIMARY = "vnd.android.cursor.item/primary";

    private static final String TAG = "GboardClipboard";
    private static final int MATCH_CLIPS = 1;
    private static final int MATCH_CLIP_ITEM = 2;
    private static final int MATCH_PRIMARY = 3;

    private UriMatcher matcher;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        String authority = GboardClipboardHistorySettings.resolveAuthority(context);
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(authority, PATH_CLIPS, MATCH_CLIPS);
        matcher.addURI(authority, PATH_CLIPS + "/#", MATCH_CLIP_ITEM);
        matcher.addURI(authority, PATH_PRIMARY, MATCH_PRIMARY);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Context context = getContext();
        if (context == null || matcher == null) {
            return null;
        }
        try {
            enforceAuthorizedCaller(context, uri);
            int match = matcher.match(uri);
            if (match != MATCH_CLIPS && match != MATCH_CLIP_ITEM && match != MATCH_PRIMARY) {
                return null;
            }

            boolean includeSensitive = GboardClipboardHistorySettings.isSensitiveIncluded(context);

            if (match == MATCH_PRIMARY) {
                return queryPrimaryClip(context, includeSensitive);
            }

            List<GboardClipboardHistoryRepository.ClipboardEntry> entries =
                    GboardClipboardHistoryRepository.loadHistory(
                            context, includeSensitive, GboardClipboardHistoryRepository.MAX_LIMIT);

            MatrixCursor cursor = new MatrixCursor(GboardClipboardHistoryRepository.ALL_COLUMNS);
            if (match == MATCH_CLIP_ITEM) {
                long requestedId = parseId(uri);
                for (GboardClipboardHistoryRepository.ClipboardEntry entry : entries) {
                    if (entry.getId() == requestedId) {
                        appendRow(cursor, entry);
                        break;
                    }
                }
                return cursor;
            }

            for (GboardClipboardHistoryRepository.ClipboardEntry entry : entries) {
                appendRow(cursor, entry);
            }
            return cursor;
        } catch (SecurityException se) {
            throw se;
        } catch (Throwable t) {
            Log.w(TAG, "Failed to serve clipboard query", t);
            return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        if (matcher == null) return null;
        int match = matcher.match(uri);
        if (match == MATCH_CLIPS) return MIME_TYPE_CLIPS_DIR;
        if (match == MATCH_CLIP_ITEM) return MIME_TYPE_CLIP_ITEM;
        if (match == MATCH_PRIMARY) return MIME_TYPE_PRIMARY;
        return null;
    }

    private Cursor queryPrimaryClip(Context context, boolean includeSensitive) {
        MatrixCursor cursor = new MatrixCursor(GboardClipboardHistoryRepository.ALL_COLUMNS);
        try {
            GboardClipboardBackfill.PrimaryClip primary = GboardClipboardBackfill.readPrimaryClip(context);
            if (primary == null) {
                return cursor;
            }
            if (primary.sensitive && !includeSensitive) {
                return cursor;
            }
            cursor.addRow(new Object[]{
                    0L,
                    primary.text,
                    primary.htmlText,
                    0,
                    0,
                    primary.timestamp,
                    primary.sensitive ? 1 : 0,
                    primary.uri,
                    null
            });
        } catch (Throwable t) {
            Log.w(TAG, "Failed reading primary clip", t);
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Clipboard history is read-only");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Clipboard history is read-only");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Clipboard history is read-only");
    }

    private void enforceAuthorizedCaller(Context context, Uri uri) {
        if (!GboardClipboardHistorySettings.isExternalAccessEnabled(context)) {
            throw new SecurityException("Clipboard history external access is disabled in settings.");
        }
        int callingUid = Binder.getCallingUid();
        if (callingUid == Process.myUid()) {
            return;
        }
        if (context.checkPermission(GboardClipboardHistorySettings.resolvePermission(context),
                Binder.getCallingPid(), callingUid) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String token = uri == null ? null : uri.getQueryParameter(QUERY_PARAM_TOKEN);
        if (GboardClipboardHistorySettings.isAccessTokenValid(context, token)) {
            return;
        }
        throw new SecurityException("Unauthorized. Requires permission "
                + GboardClipboardHistorySettings.resolvePermission(context)
                + " or valid query parameter '?token=...'");
    }

    private static void appendRow(MatrixCursor cursor, GboardClipboardHistoryRepository.ClipboardEntry entry) {
        cursor.addRow(new Object[]{
                entry.getId(),
                entry.getText(),
                entry.getHtmlText(),
                entry.getItemType(),
                entry.getEntityType(),
                entry.getTimestamp(),
                entry.isSensitive() ? 1 : 0,
                entry.getUri(),
                entry.getGroupId()
        });
    }

    private static long parseId(Uri uri) {
        try {
            String seg = uri.getLastPathSegment();
            return seg == null ? -1L : Long.parseLong(seg);
        } catch (Throwable ignored) {
            return -1L;
        }
    }
}
