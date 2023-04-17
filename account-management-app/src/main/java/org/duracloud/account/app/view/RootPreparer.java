/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.view;

import java.util.LinkedList;

import org.apache.tiles.AttributeContext;
import org.apache.tiles.preparer.ViewPreparer;
import org.apache.tiles.request.Request;
import org.duracloud.account.app.controller.DuracloudMillController;
import org.duracloud.account.app.controller.GlobalPropertiesController;
import org.duracloud.account.app.controller.NotificationsController;
import org.springframework.stereotype.Component;

@Component("rootPreparer")
public class RootPreparer implements ViewPreparer {
    @Override
    public void execute(final Request request, final AttributeContext attributeContext) {
        final var requestScope = request.getContext("request");
        requestScope.put("primaryTabs", new RootTabs());
        String currentUri = (String) requestScope.get("javax.servlet.forward.request_uri");
        requestScope.put("currentUri", currentUri);
    }

    public static class RootTabs extends LinkedList<Tab> {
        private static final long serialVersionUID = 1L;

        public RootTabs() {
            super();
            add(new Tab("/root/accounts", "accounts"));
            add(new Tab("/root/users", "users"));
            add(new Tab(DuracloudMillController.BASE_MAPPING, "duracloudmill"));
            add(new Tab(GlobalPropertiesController.BASE_MAPPING, "globalproperties"));
            add(new Tab("/root/rootusers", "rootusers"));
            add(new Tab(NotificationsController.BASE_MAPPING, "notifications"));
        }
    }

    public static class Tab {
        public Tab(String id, String name) {
            super();
            this.id = id;
            this.name = name;
        }

        private String id; // view id
        private String name; // message key

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
