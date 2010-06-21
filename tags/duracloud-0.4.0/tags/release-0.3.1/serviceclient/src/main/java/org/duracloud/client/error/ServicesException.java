package org.duracloud.client.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * Exception thrown by the Services Manager.
 *
 * @author Bill Branan
 */
public class ServicesException extends DuraCloudCheckedException {

    private static final long serialVersionUID = 1L;

    public ServicesException (String message) {
        super(message);
    }

    public ServicesException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ServicesException(Throwable throwable) {
        super(throwable);
    }

}
