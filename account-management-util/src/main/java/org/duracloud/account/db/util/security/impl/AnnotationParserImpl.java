/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.security.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.duracloud.account.db.util.security.AnnotationParser;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

/**
 * This class provides a utility for searching through a class and its
 * hierarchy for a given annotation.
 * Once the methods with the given annotation are found, a mapping of the
 * method names and annotation argument values is returned.
 *
 * @author Andrew Woods
 * Date: 4/8/11
 */
public class AnnotationParserImpl implements AnnotationParser {

    private Logger log = LoggerFactory.getLogger(AnnotationParserImpl.class);

    @Override
    public Map<String, Object[]> getMethodAnnotationsForClass(Class annotationClass,
                                                              Class targetClass) {
        log.trace("Collecting annotations {} over {}",
                  annotationClass.getName(),
                  targetClass.getName());

        try {
            return doGetMethodAnnotationsForClass(annotationClass, targetClass);

        } catch (Exception e) {
            log.error("Error getting annotations {} over {}: {}",
                      annotationClass.getName(),
                      targetClass.getName(),
                      e.getMessage());
            throw new DuraCloudRuntimeException(e);
        }
    }

    private Map<String, Object[]> doGetMethodAnnotationsForClass(Class annotationClass,
                                                                 Class targetClass) {
        Map<String, Object[]> methodAnnotations = new HashMap<String, Object[]>();

        // Find class/interface in stack that has the annotation
        Class iface = getAnnotatedInterface(annotationClass, targetClass);

        // Read the annotation metadata from the found class/interface
        MetadataReader metadataReader = getMetadataReader(iface);
        AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();

        Set<MethodMetadata> annotatedMethods = getAnnotatedMethods(
            annotationMetadata,
            annotationClass);

        Iterator itr = getAnnotatedMethodsIterator(annotatedMethods);
        while (itr.hasNext()) {
            MethodMetadata methodMetadata = (MethodMetadata) itr.next();

            Map<String, Object> annotationAtts = methodMetadata.getAnnotationAttributes(
                annotationClass.getName());
            Object[] values = getValues(annotationAtts);
            methodAnnotations.put(methodMetadata.getMethodName(), values);
        }
        return methodAnnotations;
    }

    private Class getAnnotatedInterface(Class annotationClass,
                                        Class targetClass) {
        if (hasAnnotatedMethods(annotationClass, targetClass)) {
            return targetClass;
        }

        for (Class iface : targetClass.getInterfaces()) {
            if (hasAnnotatedMethods(annotationClass, iface)) {
                return iface;
            }
        }

        throw new DuraCloudRuntimeException("No annotationMetadata found of " +
                                            annotationClass.getName() + " over " +
                                            targetClass.getName());
    }

    private boolean hasAnnotatedMethods(Class annotationClass,
                                        Class targetClass) {
        MetadataReader metadataReader = getMetadataReader(targetClass);
        AnnotationMetadata annotationMd = metadataReader.getAnnotationMetadata();
        return annotationMd.hasAnnotatedMethods(annotationClass.getName());
    }

    private MetadataReader getMetadataReader(Class<?> targetClass) {
        MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
        try {
            return metadataReaderFactory.getMetadataReader(targetClass.getName());

        } catch (IOException e) {
            log.warn("Error reading metadata. {}", e.getMessage());
            throw new DuraCloudRuntimeException(e);
        }
    }

    private Set<MethodMetadata> getAnnotatedMethods(AnnotationMetadata metadata,
                                                    Class<?> annotationClass) {
        if (null == metadata) {
            throw new DuraCloudRuntimeException("Arg metadata is null: {}",
                                                annotationClass.getName());
        }
        return metadata.getAnnotatedMethods(annotationClass.getName());
    }

    private Iterator getAnnotatedMethodsIterator(Set<MethodMetadata> annotatedMethods) {
        if (null == annotatedMethods) {
            throw new DuraCloudRuntimeException("Arg annotatedMethods null.");
        }
        return annotatedMethods.iterator();
    }

    /**
     * This method extracts the annotation argument info from the annotation
     * metadata.
     * Since the implementation of access for annotation arguments varies, this
     * method may need to be overwritten by additional AnnotationParsers.
     *
     * @param annotationAtts mapping of annotation-implementation-dependent
     *                       access keys and the annotation arguments
     * @return array of the annotation arguments
     */
    private Object[] getValues(Map<String, Object> annotationAtts) {
        if (null == annotationAtts) {
            throw new DuraCloudRuntimeException("Arg annotationAtts is null.");
        }

        List<Object> values = new ArrayList<Object>();
        for (String key : annotationAtts.keySet()) {

            Object[] objects = (Object[]) annotationAtts.get(key);
            for (Object obj : objects) {
                values.add(obj);
            }
        }

        return values.toArray();
    }
}
