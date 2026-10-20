package com.sj010.gboardclipboard.extension;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone Clipboard History Activity.
 * Launched directly from app drawer launcher icon or Gboard Settings -> Clipboard.
 */
public final class GboardClipboardHistoryActivity extends Activity {

    private static final int EXPORT_REQUEST_CODE = 1001;

    private List<GboardClipboardHistoryRepository.ClipboardEntry> allClips = new ArrayList<>();
    private final List<GboardClipboardHistoryRepository.ClipboardEntry> displayedClips = new ArrayList<>();
    private ClipListAdapter adapter;
    private TextView emptyView;
    private TextView countView;
    private EditText searchEditText;
    private Switch sensitiveSwitch;
    private Switch externalAccessSwitch;
    private TextView tokenTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Clipboard History");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF121212);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Top Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(16), dp(16), dp(16), dp(12));
        header.setBackgroundColor(0xFF1E1E1E);

        TextView titleView = new TextView(this);
        titleView.setText("Gboard Clipboard History");
        titleView.setTextColor(0xFFFFFFFF);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        header.addView(titleView);

        countView = new TextView(this);
        countView.setText("Loading clips...");
        countView.setTextColor(0xFFAAAAAA);
        countView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        countView.setPadding(0, dp(4), 0, dp(8));
        header.addView(countView);

        // Search box
        searchEditText = new EditText(this);
        searchEditText.setHint("Search copied clips...");
        searchEditText.setHintTextColor(0xFF777777);
        searchEditText.setTextColor(0xFFFFFFFF);
        searchEditText.setBackgroundColor(0xFF2C2C2C);
        searchEditText.setPadding(dp(12), dp(10), dp(12), dp(10));
        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClips(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        header.addView(searchEditText);

        // Action Buttons Row
        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setPadding(0, dp(10), 0, 0);

        Button exportBtn = createActionButton("Export (.txt)");
        exportBtn.setOnClickListener(v -> startExportHistory());
        actionRow.addView(exportBtn);

        Button settingsBtn = createActionButton("Settings / API");
        settingsBtn.setOnClickListener(v -> showSettingsDialog());
        actionRow.addView(settingsBtn);

        Button clearBtn = createActionButton("Clear All");
        clearBtn.setTextColor(0xFFFF6B6B);
        clearBtn.setOnClickListener(v -> confirmClearHistory());
        actionRow.addView(clearBtn);

        header.addView(actionRow);
        root.addView(header);

        // Clips List
        ListView listView = new ListView(this);
        listView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
        listView.setDivider(null);
        listView.setDividerHeight(dp(4));
        listView.setPadding(dp(12), dp(8), dp(12), dp(8));
        listView.setClipToPadding(false);

        emptyView = new TextView(this);
        emptyView.setText("No clipboard history found.\nCopied items will appear here.");
        emptyView.setTextColor(0xFF888888);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        emptyView.setPadding(dp(24), dp(48), dp(24), dp(24));
        listView.setEmptyView(emptyView);

        adapter = new ClipListAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < displayedClips.size()) {
                GboardClipboardHistoryRepository.ClipboardEntry clip = displayedClips.get(position);
                copyClip(clip);
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < displayedClips.size()) {
                showClipDetails(displayedClips.get(position));
                return true;
            }
            return false;
        });

        root.addView(emptyView);
        root.addView(listView);

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        boolean includeSensitive = GboardClipboardHistorySettings.isSensitiveIncluded(this);
        allClips = GboardClipboardHistoryRepository.loadHistory(
                this, includeSensitive, GboardClipboardHistoryRepository.MAX_LIMIT);
        filterClips(searchEditText != null ? searchEditText.getText().toString() : "");
    }

    private void filterClips(String query) {
        displayedClips.clear();
        if (TextUtils.isEmpty(query)) {
            displayedClips.addAll(allClips);
        } else {
            String lower = query.toLowerCase();
            for (GboardClipboardHistoryRepository.ClipboardEntry entry : allClips) {
                String text = entry.getPasteableText();
                if (text != null && text.toLowerCase().contains(lower)) {
                    displayedClips.add(entry);
                }
            }
        }
        if (countView != null) {
            countView.setText(allClips.size() + " clips captured in Gboard");
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void copyClip(GboardClipboardHistoryRepository.ClipboardEntry clip) {
        String text = clip.getPasteableText();
        if (!TextUtils.isEmpty(text)) {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("Copied Clip", text));
                Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
            }
        } else if (!TextUtils.isEmpty(clip.getUri())) {
            Toast.makeText(this, "Image clip: " + clip.getUri(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showClipDetails(GboardClipboardHistoryRepository.ClipboardEntry clip) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clipboard Clip Details");
        String info = "Timestamp: " + formatTimestamp(clip.getTimestamp()) + "\n"
                + "Sensitive: " + (clip.isSensitive() ? "Yes" : "No") + "\n\n"
                + "Content:\n" + clip.getPasteableText();
        builder.setMessage(info);
        builder.setPositiveButton("Copy", (dialog, which) -> copyClip(clip));
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void confirmClearHistory() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Clipboard History")
                .setMessage("Are you sure you want to permanently delete all Gboard clipboard history entries?")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    boolean success = GboardClipboardHistoryRepository.clearHistory(GboardClipboardHistoryActivity.this);
                    if (success) {
                        Toast.makeText(GboardClipboardHistoryActivity.this, "Clipboard history cleared", Toast.LENGTH_SHORT).show();
                        loadHistory();
                    } else {
                        Toast.makeText(GboardClipboardHistoryActivity.this, "Failed to clear history", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSettingsDialog() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(16), dp(20), dp(16));

        // Sensitive Switch
        sensitiveSwitch = new Switch(this);
        sensitiveSwitch.setText("Include sensitive clips (passwords)");
        sensitiveSwitch.setTextColor(0xFFFFFFFF);
        sensitiveSwitch.setChecked(GboardClipboardHistorySettings.isSensitiveIncluded(this));
        sensitiveSwitch.setOnCheckedChangeListener((btn, checked) -> {
            GboardClipboardHistorySettings.setSensitiveIncluded(this, checked);
            loadHistory();
        });
        layout.addView(sensitiveSwitch);

        // External Access Switch
        externalAccessSwitch = new Switch(this);
        externalAccessSwitch.setText("Allow companion apps (ContentProvider)");
        externalAccessSwitch.setTextColor(0xFFFFFFFF);
        externalAccessSwitch.setChecked(GboardClipboardHistorySettings.isExternalAccessEnabled(this));
        externalAccessSwitch.setPadding(0, dp(12), 0, dp(4));
        externalAccessSwitch.setOnCheckedChangeListener((btn, checked) -> {
            GboardClipboardHistorySettings.setExternalAccessEnabled(this, checked);
        });
        layout.addView(externalAccessSwitch);

        TextView apiDesc = new TextView(this);
        apiDesc.setText("Companion apps read history via:\ncontent://com.sj010.gboardclipboard.history/clips");
        apiDesc.setTextColor(0xFFAAAAAA);
        apiDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        apiDesc.setPadding(0, dp(8), 0, dp(8));
        layout.addView(apiDesc);

        // Token
        TextView tokenLabel = new TextView(this);
        tokenLabel.setText("Access Token:");
        tokenLabel.setTextColor(0xFFFFFFFF);
        tokenLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        layout.addView(tokenLabel);

        tokenTextView = new TextView(this);
        tokenTextView.setText(GboardClipboardHistorySettings.getAccessToken(this));
        tokenTextView.setTextColor(0xFF81D4FA);
        tokenTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tokenTextView.setTypeface(Typeface.MONOSPACE);
        tokenTextView.setPadding(dp(8), dp(8), dp(8), dp(8));
        tokenTextView.setBackgroundColor(0xFF222222);
        tokenTextView.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("API Token", tokenTextView.getText()));
                Toast.makeText(this, "API Token copied!", Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(tokenTextView);

        Button regenBtn = new Button(this);
        regenBtn.setText("Regenerate Token");
        regenBtn.setOnClickListener(v -> {
            String newToken = GboardClipboardHistorySettings.regenerateAccessToken(this);
            tokenTextView.setText(newToken);
            Toast.makeText(this, "New token generated", Toast.LENGTH_SHORT).show();
        });
        layout.addView(regenBtn);

        scrollView.addView(layout);

        new AlertDialog.Builder(this)
                .setTitle("Clipboard Settings & API")
                .setView(scrollView)
                .setPositiveButton("Done", null)
                .show();
    }

    private void startExportHistory() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "gboard_clipboard_export.txt");
        try {
            startActivityForResult(intent, EXPORT_REQUEST_CODE);
        } catch (Throwable t) {
            Toast.makeText(this, "File export not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXPORT_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            exportToFile(uri);
        }
    }

    private void exportToFile(Uri uri) {
        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
            if (os == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("=== Gboard Clipboard History Export ===\n");
            sb.append("Total Clips: ").append(allClips.size()).append("\n\n");
            for (GboardClipboardHistoryRepository.ClipboardEntry clip : allClips) {
                sb.append("[").append(formatTimestamp(clip.getTimestamp())).append("] ");
                if (clip.isSensitive()) sb.append("[SENSITIVE] ");
                sb.append(clip.getPasteableText()).append("\n----------------------------------------\n");
            }
            os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, "Exported successfully!", Toast.LENGTH_LONG).show();
        } catch (Throwable t) {
            Toast.makeText(this, "Export failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String formatTimestamp(long time) {
        if (time <= 0) return "Unknown";
        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
    }

    private Button createActionButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        b.setTextColor(0xFFFFFFFF);
        b.setBackgroundColor(0xFF333333);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        lp.setMargins(dp(3), 0, dp(3), 0);
        b.setLayoutParams(lp);
        return b;
    }

    private int dp(int val) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
    }

    private final class ClipListAdapter extends BaseAdapter {
        @Override public int getCount() { return displayedClips.size(); }
        @Override public Object getItem(int position) { return displayedClips.get(position); }
        @Override public long getItemId(int position) { return displayedClips.get(position).getId(); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout item;
            if (convertView instanceof LinearLayout) {
                item = (LinearLayout) convertView;
            } else {
                item = new LinearLayout(GboardClipboardHistoryActivity.this);
                item.setOrientation(LinearLayout.VERTICAL);
                item.setPadding(dp(14), dp(12), dp(14), dp(12));
                item.setBackgroundColor(0xFF1E1E1E);

                TextView text = new TextView(GboardClipboardHistoryActivity.this);
                text.setId(101);
                text.setTextColor(0xFFE0E0E0);
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                text.setMaxLines(4);
                text.setEllipsize(TextUtils.TruncateAt.END);
                item.addView(text);

                TextView meta = new TextView(GboardClipboardHistoryActivity.this);
                meta.setId(102);
                meta.setTextColor(0xFF888888);
                meta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                meta.setPadding(0, dp(4), 0, 0);
                item.addView(meta);
            }

            GboardClipboardHistoryRepository.ClipboardEntry clip = displayedClips.get(position);
            TextView text = item.findViewById(101);
            TextView meta = item.findViewById(102);

            String payload = clip.getPasteableText();
            if (TextUtils.isEmpty(payload) && !TextUtils.isEmpty(clip.getUri())) {
                payload = "[Image Clip: " + clip.getUri() + "]";
            }
            text.setText(payload);

            String timeStr = formatTimestamp(clip.getTimestamp());
            if (clip.isSensitive()) {
                timeStr += " • (Sensitive / Password)";
            }
            meta.setText(timeStr);

            return item;
        }
    }
}
