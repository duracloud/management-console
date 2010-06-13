/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradav.error;

import org.duracloud.duradav.core.Path;

/**
 * Signals an error defined by the WebDAV protocol.
 */
public abstract class WebdavException
        extends Exception {

    private static final long serialVersionUID = 1L;

    private final Path path;

    private final int statusCode;

    /**
     * Creates an instance.
     *
     * @param path the path of the resource in question
     * @param statusCode the status code
     */
    public WebdavException(Path path, int statusCode) {
        super(path.toString());
        this.path = path;
        this.statusCode = statusCode;
    }

    /**
     * Creates an instance with details.
     *
     * @param path the path of the resource in question
     * @param statusCode the status code
     */
    public WebdavException(Path path, int statusCode, String details) {
        super(path.toString() + " (" + details + ")");
        this.path = path;
        this.statusCode = statusCode;
    }

    /**
     * Gets the path of the resource in question.
     *
     * @return the path.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Gets the http status code for this exception.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }

}
