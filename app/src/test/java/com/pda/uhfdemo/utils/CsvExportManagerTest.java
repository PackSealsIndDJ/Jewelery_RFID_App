package com.pda.uhfdemo.utils;

import org.junit.Test;

import com.pda.uhfdemo.entity.BindInfo;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public void bindRowsIncludePersistedTidStatusAndDateTime() {
        BindInfo binding = new BindInfo("epc-1", "Gold ring", "tid-1", 1_700_000_000_000L);

        String[] row = CsvExportManager.buildBindRows(Collections.singletonList(binding), false).get(0);

        assertEquals(Arrays.asList("epc-1", "tid-1", "Bound"),
                Arrays.asList(row).subList(0, 3));
        assertTrue(row[3].matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));

        String[] tidOnlyRow =
                CsvExportManager.buildBindRows(Collections.singletonList(binding), true).get(0);
        assertEquals(Arrays.asList("", "epc-1"), Arrays.asList(tidOnlyRow).subList(0, 2));
    }

    @Test
    public void findRowsOnlyIncludeUniqueFoundTagsWithLatestScanValues() {
        BindInfo missing = new BindInfo("epc-1", "Missing");
        missing.setStatus("Missing");
        BindInfo firstRead = foundTag("epc-2", -60, 1, 1_700_000_000_000L);
        BindInfo latestRead = foundTag("epc-2", -42, 3, 1_700_000_001_000L);

        List<String[]> rows = CsvExportManager.buildFindRows(
                Arrays.asList(missing, firstRead, latestRead), "Found", false);

        assertEquals(1, rows.size());
        assertEquals(Arrays.asList("epc-2", null, "-42", "3"),
                Arrays.asList(rows.get(0)).subList(0, 4));
        assertTrue(rows.get(0)[4].matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    private BindInfo foundTag(String epc, long rssi, int scanCount, long lastSeen) {
        BindInfo info = new BindInfo(epc, "Goods");
        info.setStatus("Found");
        info.setRssi(rssi);
        for (int i = 0; i < scanCount; i++) {
            info.incrementScanCount();
        }
        info.setLastSeenMillis(lastSeen);
        return info;
    }
}
