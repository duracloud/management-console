/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mainwebapp.domain.repo;

import java.util.List;

import org.duracloud.mainwebapp.domain.model.StorageAcct;

/**
 * <pre>
 * This interface exposes access to the underlying persistence of
 * customer storage accounts.
 * </pre>
 *
 * @author Andrew Woods
 */
public interface StorageAcctRepository {

    /**
     * This method returns storage-account associated with the provided storage
     * acct id.
     *
     * @param Storage
     *        Acct Id
     * @return
     * @throws Exception
     *         If no accounts are found.
     */
    public StorageAcct findStorageAcctById(int storageAcctId) throws Exception;

    /**
     * This method returns all storage-accounts associated with the provided
     * DuraCloud id.
     *
     * @param DuraCloud
     *        Account id
     * @return
     * @throws Exception
     *         If no accounts are found.
     */
    public List<StorageAcct> findStorageAcctsByDuraAcctId(int duraAcctId)
            throws Exception;

    /**
     * This method persists the provided storage-account.
     *
     * @param acct
     * @throws Exception
     */
    public int saveStorageAcct(StorageAcct acct) throws Exception;

    public boolean isStorageNamespaceTaken(String storageAcctNamespace);

}
