package com.pda.uhfdemo.utils;

import org.junit.Test;

import com.pda.uhfdemo.entity.BindInfo;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CsvExportManagerTest {
    @Test
    public void writesExcelCompatibleUtf8CsvWithEscapedValuesAndFooter() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        CsvExportManager.writeCsv(output, new String[]{"EPC", "TIP", "Status"},
                Collections.singletonList(new String[]{"tag-1", "Café, \"ring\"", null}),
                "2026-11-18 12:34:56", 1);

        assertEquals("\uFEFFEPC,TIP,Status\r\n"
                        + "tag-1,\"Café, \"\"ring\"\"\",\r\n"
                        + "Export Time,2026-11-18 12:34:56\r\n"
                        + "Total Records,1\r\n",
                new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void bindRowsIncludeDisplayedFieldsAndCompleteListCounts() {
        BindInfo binding = new BindInfo("epc-1", "Gold ring", "tid-1", 1_700_000_000_000L);
        BindInfo secondBinding = new BindInfo("epc-2", "Silver ring", "tid-2",
                1_700_000_001_000L);

        List<String[]> rows = CsvExportManager.buildBindRows(
                Arrays.asList(binding, secondBinding));
        String[] row = rows.get(0);

        assertEquals(Arrays.asList("1", "epc-1", "Gold ring", "2"),
                Arrays.asList(row).subList(0, 4));
        assertTrue(row[4].matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));

        assertEquals(Arrays.asList("2", "epc-2", "Silver ring", "2"),
                Arrays.asList(rows.get(1)).subList(0, 4));
        assertTrue(rows.get(1)[4].matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    public void findRowsOnlyIncludeUniqueFoundTagsWithLatestScanValues() {
        BindInfo missing = new BindInfo("epc-1", "Missing");
        missing.setStatus("Missing");
        BindInfo olderRead = foundTag("epc-2", "Old goods", -60, 1,
                1_700_000_000_000L);
        BindInfo latestRead = foundTag("epc-2", "Latest goods", -42, 3,
                1_700_000_001_000L);

        List<String[]> rows = CsvExportManager.buildFindRows(
                Arrays.asList(missing, latestRead, olderRead), "Found");

        assertEquals(1, rows.size());
        assertEquals(Arrays.asList("epc-2", "Latest goods", "-42", "Found"),
                Arrays.asList(rows.get(0)).subList(0, 4));
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                .format(new Date(1_700_000_001_000L)), rows.get(0)[4]);
    }

    private BindInfo foundTag(String epc, String goods, long rssi, int scanCount, long lastSeen) {
        BindInfo info = new BindInfo(epc, goods);
        info.setStatus("Found");
        info.setRssi(rssi);
        for (int i = 0; i < scanCount; i++) {
            info.incrementScanCount();
        }
        info.setLastSeenMillis(lastSeen);
        return info;
    }
}
