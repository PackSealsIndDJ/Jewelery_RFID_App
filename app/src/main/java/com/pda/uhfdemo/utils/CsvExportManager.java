package com.pda.uhfdemo.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.pda.uhfdemo.entity.BindInfo;
import com.pda.uhfdemo.entity.TagInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CsvExportManager {
    private static final String TAG = "CsvExportManager";
    private static final String DOWNLOAD_DIRECTORY = "PackSealsUHF_G2";
    private static final String MIME_TYPE = "text/csv";
    private static final String[] BIND_HEADERS =
            {"New", "EPC", "Goods", "Total bound", "Date Time"};
    private static final String[] FIND_HEADERS =
            {"EPC", "Goods", "RSSI", "Status", "Last Seen Time"};
    private static final String[] TAG_HEADERS =
            {"Index", "Type", "EPC", "TID", "UserData", "Reserved", "TotalCount", "ReadTime"};
    private static final ExecutorService EXPORT_EXECUTOR =
            Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "csv-export"));

    private CsvExportManager() {
    }

    public interface Callback {
        void onSuccess(ExportedFile file, int recordCount);

        void onError(Exception error);
    }

    public static final class ExportedFile {
        public final Uri uri;
        public final String location;
        public final String displayName;

        private ExportedFile(Uri uri, String location, String displayName) {
            this.uri = uri;
            this.location = location;
            this.displayName = displayName;
        }
    }

    public static void exportBindDataToCsv(Context context, List<BindInfo> records,
                                           Callback callback) {
        List<BindInfo> snapshot = snapshot(records);
        String fileName = "Bind_Report_" + fileTimestamp() + ".csv";
        executeExport(context, fileName, BIND_HEADERS, () -> buildBindRows(snapshot), callback);
    }

    public static void exportFindDataToCsv(Context context, List<BindInfo> records,
                                           String foundStatus, Callback callback) {
        List<BindInfo> snapshot = snapshot(records);
        String fileName = "Find_Report_" + fileTimestamp() + ".csv";
        executeExport(context, fileName, FIND_HEADERS,
                () -> buildFindRows(snapshot, foundStatus), callback);
    }

    public static void exportTagDataToCsv(Context context, List<TagInfo> records,
                                          Callback callback) {
        List<TagInfo> snapshot = snapshotTags(records);
        String fileName = "Tag_Report_" + fileTimestamp() + ".csv";
        executeExport(context, fileName, TAG_HEADERS, () -> buildTagRows(snapshot), callback);
    }

    public static void writeCsvHeaders(Writer writer, String[] headers) throws IOException {
        writeCsvRow(writer, headers);
    }

    public static void writeCsvRows(Writer writer, List<String[]> rows) throws IOException {
        for (String[] row : rows) {
            writeCsvRow(writer, row);
        }
    }

    public static void writeCsv(OutputStream outputStream, String[] headers, List<String[]> rows,
                                String exportedAt, int recordCount) throws IOException {
        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.write('\uFEFF');
            writeCsvHeaders(writer, headers);
            writeCsvRows(writer, rows);
            writeCsvRows(writer, java.util.Collections.singletonList(
                    new String[]{"Export Time", exportedAt}));
            writeCsvRows(writer, java.util.Collections.singletonList(
                    new String[]{"Total Records", String.valueOf(recordCount)}));
        }
    }

    private static void executeExport(Context context, String fileName, String[] headers,
                                      RowsProvider rowsProvider, Callback callback) {
        if (context == null || callback == null) {
            throw new IllegalArgumentException("Context and callback are required.");
        }
        Context appContext = context.getApplicationContext();
        EXPORT_EXECUTOR.execute(() -> {
            CsvDestination destination = null;
            boolean committed = false;
            try {
                List<String[]> rows = rowsProvider.build();
                if (rows.isEmpty()) {
                    throw new IOException("No records to export.");
                }
                int recordCount = rows.size();
                String exportedAt = dateTime(System.currentTimeMillis());
                destination = createCsvFile(appContext, fileName);
                writeCsv(destination.outputStream, headers, rows, exportedAt, recordCount);
                destination.publish(appContext);
                committed = true;
                ExportedFile result = new ExportedFile(destination.uri, destination.location, fileName);
                int exportedRecordCount = recordCount;
                postToMainThread(() -> callback.onSuccess(result, exportedRecordCount));
            } catch (IOException | RuntimeException error) {
                Log.e(TAG, "CSV export failed", error);
                postError(callback, error);
            } finally {
                if (!committed && destination != null) {
                    destination.delete(appContext);
                }
            }
        });
    }

    private static CsvDestination createCsvFile(Context context, String fileName) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, MIME_TYPE);
            values.put(MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/" + DOWNLOAD_DIRECTORY);
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                throw new IOException("Could not create the CSV file in Downloads.");
            }
            OutputStream outputStream;
            try {
                outputStream = resolver.openOutputStream(uri, "wt");
            } catch (IOException | RuntimeException error) {
                deletePendingFile(resolver, uri);
                throw error;
            }
            if (outputStream == null) {
                deletePendingFile(resolver, uri);
                throw new IOException("Could not open the CSV file for writing.");
            }
            return new CsvDestination(uri, outputStream,
                    "Downloads/" + DOWNLOAD_DIRECTORY + "/" + fileName, null);
        }

        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File directory = new File(downloads, DOWNLOAD_DIRECTORY);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create the Downloads/PackSealsUHF_G2 folder.");
        }
        File file = new File(directory, fileName);
        OutputStream outputStream = new FileOutputStream(file, false);
        Uri uri;
        try {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        } catch (RuntimeException error) {
            outputStream.close();
            file.delete();
            throw error;
        }
        return new CsvDestination(uri, outputStream,
                "Downloads/" + DOWNLOAD_DIRECTORY + "/" + fileName, file);
    }

    private static void deletePendingFile(ContentResolver resolver, Uri uri) {
        try {
            resolver.delete(uri, null, null);
        } catch (RuntimeException error) {
            Log.w(TAG, "Could not remove incomplete CSV export", error);
        }
    }

    static List<String[]> buildBindRows(List<BindInfo> records) {
        List<String[]> rows = new ArrayList<>(records.size());
        int totalBound = records.size();
        for (int i = 0; i < totalBound; i++) {
            BindInfo record = records.get(i);
            rows.add(new String[]{String.valueOf(i + 1), record.getEpc(), record.getName(),
                    String.valueOf(totalBound),
                    record.getBoundAtMillis() > 0 ? dateTime(record.getBoundAtMillis()) : ""});
        }
        return rows;
    }

    static List<String[]> buildFindRows(List<BindInfo> records, String foundStatus) {
        LinkedHashMap<String, BindInfo> uniqueFoundTags = new LinkedHashMap<>();
        for (BindInfo record : records) {
            if (record != null && foundStatus != null && foundStatus.equals(record.getStatus())) {
                String key = record.getEpc();
                if (key != null && !key.isEmpty()) {
                    BindInfo current = uniqueFoundTags.get(key);
                    if (current == null
                            || record.getLastSeenMillis() >= current.getLastSeenMillis()) {
                        uniqueFoundTags.put(key, record);
                    }
                }
            }
        }

        List<String[]> rows = new ArrayList<>(uniqueFoundTags.size());
        for (BindInfo record : uniqueFoundTags.values()) {
            rows.add(new String[]{record.getEpc(), record.getName(),
                    record.getRssi() == null ? "" : String.valueOf(record.getRssi()),
                    record.getStatus(),
                    record.getLastSeenMillis() > 0 ? dateTime(record.getLastSeenMillis()) : ""});
        }
        return rows;
    }

    static List<String[]> buildTagRows(List<TagInfo> records) {
        List<String[]> rows = new ArrayList<>(records.size());
        for (TagInfo record : records) {
            if (record == null) {
                continue;
            }
            rows.add(new String[]{
                    record.getIndex() == null ? "" : String.valueOf(record.getIndex()),
                    record.getType(),
                    record.getEpc(),
                    record.getTid(),
                    record.getUserData(),
                    record.getReserved(),
                    record.getCount() == null ? "" : String.valueOf(record.getCount()),
                    record.getReadTime() == null ? "" : dateTime(record.getReadTime().getTime())
            });
        }
        return rows;
    }

    private static List<BindInfo> snapshot(List<BindInfo> records) {
        List<BindInfo> snapshot = new ArrayList<>(records.size());
        for (BindInfo record : records) {
            if (record != null) {
                snapshot.add(record.copy());
            }
        }
        return snapshot;
    }

    private static List<TagInfo> snapshotTags(List<TagInfo> records) {
        if (records == null) {
            return new ArrayList<>();
        }
        List<TagInfo> snapshot = new ArrayList<>(records.size());
        for (TagInfo record : records) {
            if (record != null) {
                TagInfo copy = new TagInfo();
                copy.setIndex(record.getIndex());
                copy.setType(record.getType());
                copy.setEpc(record.getEpc());
                copy.setTid(record.getTid());
                copy.setUserData(record.getUserData());
                copy.setReserved(record.getReserved());
                copy.setCount(record.getCount());
                copy.setReadTime(record.getReadTime() == null
                        ? null : new java.util.Date(record.getReadTime().getTime()));
                snapshot.add(copy);
            }
        }
        return snapshot;
    }

    private interface RowsProvider {
        List<String[]> build();
    }

    private static void writeCsvRow(Writer writer, String[] values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                writer.write(',');
            }
            String value = values[i] == null ? "" : values[i];
            boolean quote = value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                    || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
            if (quote) {
                writer.write('"');
            }
            for (int character = 0; character < value.length(); character++) {
                char current = value.charAt(character);
                if (current == '"') {
                    writer.write("\"\"");
                } else {
                    writer.write(current);
                }
            }
            if (quote) {
                writer.write('"');
            }
        }
        writer.write("\r\n");
    }

    private static String fileTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis());
    }

    private static String dateTime(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(timestamp);
    }

    private static void postError(Callback callback, Exception error) {
        postToMainThread(() -> callback.onError(error));
    }

    private static void postToMainThread(Runnable task) {
        new Handler(Looper.getMainLooper()).post(task);
    }

    private static final class CsvDestination {
        private final Uri uri;
        private final OutputStream outputStream;
        private final String location;
        private final File legacyFile;

        private CsvDestination(Uri uri, OutputStream outputStream, String location, File legacyFile) {
            this.uri = uri;
            this.outputStream = outputStream;
            this.location = location;
            this.legacyFile = legacyFile;
        }

        private void publish(Context context) throws IOException {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                if (context.getContentResolver().update(uri, values, null, null) <= 0) {
                    throw new IOException("Could not publish the CSV file in Downloads.");
                }
            }
        }

        private void delete(Context context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.getContentResolver().delete(uri, null, null);
                } else if (legacyFile != null) {
                    legacyFile.delete();
                }
            } catch (RuntimeException error) {
                Log.w(TAG, "Could not remove incomplete CSV export", error);
            }
        }
    }
}
