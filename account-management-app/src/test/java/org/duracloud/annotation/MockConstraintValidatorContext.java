/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.annotation;

import javax.validation.ConstraintValidatorContext;

/**
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 */
public class MockConstraintValidatorContext implements ConstraintValidatorContext {

    /* (non-Javadoc)
     * @see javax.validation.ConstraintValidatorContext#buildConstraintViolationWithTemplate(java.lang.String)
     */
    @Override
    public ConstraintViolationBuilder buildConstraintViolationWithTemplate(
        String messageTemplate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.validation.ConstraintValidatorContext#disableDefaultConstraintViolation()
     */
    @Override
    public void disableDefaultConstraintViolation() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.validation.ConstraintValidatorContext#getDefaultConstraintMessageTemplate()
     */
    @Override
    public String getDefaultConstraintMessageTemplate() {
        // TODO Auto-generated method stub
        return null;
    }

}
