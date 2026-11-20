package com.pda.uhfdemo.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TagInfoTest {
    @Test
    public void repeatedScansUpdateCountAndTimesWithoutLosingSuccessfulData() {
        TagInfo tag = new TagInfo();
        TagInfo successfulRead = new TagInfo();
        successfulRead.setRssi("-40");
        successfulRead.setEncodedData("ABCD");
        successfulRead.setEncodedDataBank("User");
        successfulRead.setEncodedReadSuccess(true);

        tag.recordScan(successfulRead, 1000L);
        TagInfo failedRead = new TagInfo();
        failedRead.setRssi("-52");
        failedRead.setEncodedReadError("Empty memory");
        tag.recordScan(failedRead, 2000L);

        assertEquals(2, tag.getScanCount());
        assertEquals(1000L, tag.getFirstScanTime());
        assertEquals(2000L, tag.getLastScanTime());
        assertEquals("-52", tag.getRssi());
        assertEquals("ABCD", tag.getEncodedData());
        assertTrue(tag.isEncodedReadSuccess());
        assertEquals(1, tag.getEncodedReadSuccessCount());
        assertEquals(1, tag.getEncodedReadFailureCount());
        assertFalse(tag.isErrorTag());
    }
}
