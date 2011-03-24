/*
 * Copyright (c) 2009-2011 DuraSpace. All rights reserved.
 */
package org.duracloud.account.db.amazonsimple.converter;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import org.duracloud.account.common.domain.ServiceRepository;
import org.duracloud.account.db.util.FormatUtil;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.duracloud.account.db.BaseRepo.COUNTER_ATT;

/**
 * @author: Bill Branan
 * Date: Feb 2, 2011
 */
public class DuracloudServiceRepositoryConverter extends BaseDomainConverter
    implements DomainConverter<ServiceRepository> {

    public DuracloudServiceRepositoryConverter() {
        log = LoggerFactory.getLogger(DuracloudServiceRepositoryConverter.class);
    }

    protected static final String SERVICE_REPOSITORY_TYPE_ATT =
        "SERVICE_REPOSITORY_TYPE";
    protected static final String HOST_NAME_ATT = "HOST_NAME";
    protected static final String SPACE_ID_ATT = "SPACE_ID";
    protected static final String VERSION_ATT = "VERSION";
    protected static final String USERNAME_ATT = "USERNAME";
    protected static final String PASSWORD_ATT = "PASSWORD";

    @Override
    public List<ReplaceableAttribute> toAttributesAndIncrement(
        ServiceRepository serviceRepo) {
        List<ReplaceableAttribute> atts = new ArrayList<ReplaceableAttribute>();

        String counter = FormatUtil.padded(serviceRepo.getCounter() + 1);
        atts.add(new ReplaceableAttribute(
            SERVICE_REPOSITORY_TYPE_ATT,
            asString(serviceRepo.getServiceRepositoryType()),
            true));
        atts.add(new ReplaceableAttribute(
            HOST_NAME_ATT,
            serviceRepo.getHostName(),
            true));
        atts.add(new ReplaceableAttribute(
            SPACE_ID_ATT,
            serviceRepo.getSpaceId(),
            true));
        atts.add(new ReplaceableAttribute(
            VERSION_ATT,
            serviceRepo.getVersion(),
            true));
        atts.add(new ReplaceableAttribute(
            USERNAME_ATT,
            serviceRepo.getUsername(),
            true));
        atts.add(new ReplaceableAttribute(
            PASSWORD_ATT,
            serviceRepo.getPassword(),
            true));
        atts.add(new ReplaceableAttribute(COUNTER_ATT, counter, true));

        return atts;
    }

    protected String asString(
        ServiceRepository.ServiceRepositoryType repoType) {
        return repoType.name();
    }

    @Override
    public ServiceRepository fromAttributes(Collection<Attribute> atts, int id) {
        int counter = -1;

        ServiceRepository.ServiceRepositoryType serviceRepositoryType = null;
        String hostName = null;
        String spaceId = null;
        String version = null;
        String username = null;
        String password = null;

        for (Attribute att : atts) {
            String name = att.getName();
            String value = att.getValue();
            if (COUNTER_ATT.equals(name)) {
                counter = SimpleDBUtils.decodeZeroPaddingInt(value);

            } else if (SERVICE_REPOSITORY_TYPE_ATT.equals(name)) {
                serviceRepositoryType =  typeFromString(value);

            } else if (HOST_NAME_ATT.equals(name)) {
                hostName = value;

            } else if (SPACE_ID_ATT.equals(name)) {
                spaceId = value;

            } else if (VERSION_ATT.equals(name)) {
                version = value;

            } else if (USERNAME_ATT.equals(name)) {
                username = value;

            } else if (PASSWORD_ATT.equals(name)) {
                password = value;

            } else {
                StringBuilder msg = new StringBuilder("Unexpected name: ");
                msg.append(name);
                msg.append(" in domain: ");
                msg.append(getDomain());
                msg.append(" [with id]: ");
                msg.append(id);
                log.info(msg.toString());
            }
        }

        return new ServiceRepository(id,
                                     serviceRepositoryType,
                                     hostName,
                                     spaceId,
                                     version,
                                     username,
                                     password,
                                     counter);
    }

    protected ServiceRepository.ServiceRepositoryType typeFromString(String strType) {
        return ServiceRepository.ServiceRepositoryType.valueOf(strType.trim());
    }

}