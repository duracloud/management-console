/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.util;

import java.util.ArrayList;
import java.util.List;

import org.duracloud.storage.domain.StorageProviderType;

public class StorageProviderTypeUtil {
    private static final List<StorageProviderType> SECONDARY_PROVIDER_TYPES;

    private StorageProviderTypeUtil() {
        // Ensures no instances are made of this class, as there are only static members.
    }

    static {
        SECONDARY_PROVIDER_TYPES = new ArrayList<>();
        SECONDARY_PROVIDER_TYPES.add(StorageProviderType.AMAZON_GLACIER);
        SECONDARY_PROVIDER_TYPES.add(StorageProviderType.CHRONOPOLIS);
    }

    public static List<StorageProviderType> getAvailableSecondaryTypes() {
        return new ArrayList<>(SECONDARY_PROVIDER_TYPES);
    }

    public static List<StorageProviderType> getAvailableTypes() {
        List<StorageProviderType> types = new ArrayList<>(SECONDARY_PROVIDER_TYPES);
        types.add(0, StorageProviderType.AMAZON_S3);
        return types;
    }

}
