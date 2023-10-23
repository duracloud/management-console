/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.monitor.duplication.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * @author Bill Branan
 * Date: 4/19/13
 */
public class DuplicationInfoTest {

    @Test
    public void testDuplicationInfo() {
        // Check host
        String host = "host";
        DuplicationInfo dupInfo = new DuplicationInfo(host);
        assertEquals(host, dupInfo.getHost());

        // Check issues listing
        assertFalse(dupInfo.hasIssues());
        assertFalse(dupInfo.toString().contains("ISSUES"));

        String issue = "This is an issue";
        dupInfo.addIssue(issue);
        assertTrue(dupInfo.hasIssues());
        List<String> issues = dupInfo.getIssues();
        assertEquals(1, issues.size());
        assertEquals(issue, issues.get(0));

        String issue2 = "This is another issue";
        dupInfo.addIssue(issue2);
        issues = dupInfo.getIssues();
        assertEquals(2, issues.size());

        assertTrue(dupInfo.toString().contains("ISSUES"));

        // Check primary space counts
        String primaryStoreId = "primary";
        Map<String, Long> primaryCounts = dupInfo.getSpaceCounts(primaryStoreId);
        assertEquals(0, primaryCounts.size());

        dupInfo.addSpaceCount(primaryStoreId, "space1", 1);
        dupInfo.addSpaceCount(primaryStoreId, "space2", 2);
        primaryCounts = dupInfo.getSpaceCounts(primaryStoreId);
        assertEquals(2, primaryCounts.size());
        assertEquals(Long.valueOf(1), primaryCounts.get("space1"));
        assertEquals(Long.valueOf(2), primaryCounts.get("space2"));

        // Check secondary space counts
        String secStoreId = "secondary";
        Map<String, Long> secondaryCounts = dupInfo.getSpaceCounts(secStoreId);
        assertEquals(0, secondaryCounts.size());

        dupInfo.addSpaceCount(secStoreId, "space1", 1);
        dupInfo.addSpaceCount(secStoreId, "space2", 2);
        secondaryCounts = dupInfo.getSpaceCounts(secStoreId);
        assertEquals(2, secondaryCounts.size());
        assertEquals(Long.valueOf(1), secondaryCounts.get("space1"));
        assertEquals(Long.valueOf(2), secondaryCounts.get("space2"));

        // Check tertiary space counts
        String terStoreId = "tertiary";
        Map<String, Long> tertiaryCounts = dupInfo.getSpaceCounts(terStoreId);
        assertEquals(0, tertiaryCounts.size());

        dupInfo.addSpaceCount(terStoreId, "space1", 1);
        tertiaryCounts = dupInfo.getSpaceCounts(terStoreId);
        assertEquals(1, tertiaryCounts.size());
        assertEquals(Long.valueOf(1), tertiaryCounts.get("space1"));
    }

}
