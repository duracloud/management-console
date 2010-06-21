package org.duracloud.duradav.servlet.methods;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.duradav.core.Collection;
import org.duracloud.duradav.core.Content;
import org.duracloud.duradav.core.Resource;
import org.duracloud.duradav.error.WebdavException;
import org.duracloud.duradav.store.WebdavStore;

/**
 * Handles DELETE requests.
 */
class DeleteHandler implements MethodHandler {

    public void handleRequest(WebdavStore store,
                              Resource resource,
                              HttpServletRequest req,
                              HttpServletResponse resp) throws WebdavException {
        if (resource.isCollection()) {
            Collection collection = (Collection) resource;
            store.deleteCollection(collection.getCollectionPath());
        } else {
            Content content = (Content) resource;
            store.deleteContent(content.getContentPath());
        }
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

}