package org.duracloud.client;

import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.error.StorageException;
import org.duracloud.error.ContentStoreException;
import org.apache.commons.httpclient.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Provides facilities for connecting to a set of content stores
 *
 * @author Bill Branan
 */
public class ContentStoreManagerImpl implements ContentStoreManager {

    private static final String DEFAULT_CONTEXT = "durastore";

    private String baseURL = null;

    private static RestHttpHelper restHelper = new RestHttpHelper();

    /**
     * <p>Constructor for ContentStoreManagerImpl.</p>
     *
     * @param host a {@link java.lang.String} object.
     * @param port a {@link java.lang.String} object.
     */
    public ContentStoreManagerImpl(String host, String port) {
        this(host, port, DEFAULT_CONTEXT);
    }

    /**
     * <p>Constructor for ContentStoreManagerImpl.</p>
     *
     * @param host the host name on which DuraStore can be accessed
     * @param port the port on which DuraStore can be accessed
     * @param context the application context by which DuraStore can be accessed
     */
    public ContentStoreManagerImpl(String host, String port, String context) {
        if (host == null || host.equals("")) {
            throw new IllegalArgumentException("Host must be a valid server host name");
        }

        if (context == null) {
            context = DEFAULT_CONTEXT;
        }

        if (port == null || port.equals("")) {
            baseURL = "http://" + host + "/" + context;
        } else {
            baseURL = "http://" + host + ":" + port + "/" + context;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, ContentStore> getContentStores() throws ContentStoreException {
        StorageAccountManager acctManager = getStorageAccounts();
        Map<String, StorageAccount> accounts = acctManager.getStorageAccounts();
        Map<String, ContentStore> contentStores =
            new HashMap<String, ContentStore>();
        Iterator<String> acctIDs = accounts.keySet().iterator();
        while (acctIDs.hasNext()) {
            String acctID = acctIDs.next();
            StorageAccount acct = accounts.get(acctID);
            ContentStore contentStore =
                new ContentStoreImpl(baseURL, acct.getType(), acct.getId());
            contentStores.put(acctID, contentStore);
        }
        return contentStores;
    }

    /**
     * {@inheritDoc}
     */
    public ContentStore getContentStore(String storeID) throws ContentStoreException {
        StorageAccountManager acctManager = getStorageAccounts();
        StorageAccount acct = acctManager.getStorageAccount(storeID);
        ContentStore contentStore =
            new ContentStoreImpl(baseURL, acct.getType(), acct.getId());
        return contentStore;
    }

    /**
     * {@inheritDoc}
     */
    public ContentStore getPrimaryContentStore() throws ContentStoreException {
        StorageAccountManager acctManager = getStorageAccounts();
        StorageAccount acct = acctManager.getPrimaryStorageAccount();
        ContentStore contentStore =
            new ContentStoreImpl(baseURL, acct.getType(), acct.getId());
        return contentStore;
    }

    private StorageAccountManager getStorageAccounts() throws ContentStoreException {
        String url = baseURL + "/stores";
        HttpResponse response;
        String error = "Error retrieving content stores. ";
        try {
            response = restHelper.get(url);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                String storesXML = response.getResponseBody();
                if (storesXML != null) {
                    InputStream is = new ByteArrayInputStream(storesXML.getBytes());
                    return new StorageAccountManager(is, true);
                } else {
                    throw new StorageException(error + "Response content was null");
                }
            } else {
                error += "Response code was " + response.getStatusCode() +
                    ", expected value was " + HttpStatus.SC_OK +
                    ". Response Body: " + response.getResponseBody();
                throw new StorageException(error);
            }
        } catch (Exception e) {
            throw new ContentStoreException(error + e.getMessage(), e);
        }
    }
}