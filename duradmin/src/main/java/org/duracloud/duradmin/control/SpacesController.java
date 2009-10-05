
package org.duracloud.duradmin.control;

import java.util.List;

import org.apache.log4j.Logger;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStore.AccessType;
import org.duracloud.duradmin.domain.Space;
import org.duracloud.duradmin.util.SpaceUtil;
import org.duracloud.duradmin.view.MainMenu;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class SpacesController
        extends BaseController {

    protected final Logger log = Logger.getLogger(getClass());

    public SpacesController() {
        setCommandClass(Space.class);
        setCommandName("space");
    }

    @Override
    protected ModelAndView onSubmit(Object command, BindException errors)
            throws Exception {
        Space space = (Space) command;

        ContentStore store = null;
        try {
            store = getContentStore();
        } catch (Exception se) {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", se.getMessage());
            return mav;
        }

        String error = null;
        String action = space.getAction();
        if (action != null) {
            String spaceId = space.getSpaceId();
            if (action.equals("update-access")) {
                String newAccess = space.getAccess();
                if (newAccess != null) {
                    AccessType oldAccess = store.getSpaceAccess(spaceId);
                    if (newAccess.equals("CLOSED")
                            && oldAccess.equals(AccessType.OPEN)) {
                        store.setSpaceAccess(spaceId, AccessType.CLOSED);
                    } else if (newAccess.equals("OPEN")
                            && oldAccess.equals(AccessType.CLOSED)) {
                        store.setSpaceAccess(spaceId, AccessType.OPEN);
                    }
                }
            }
        }

        List<Space> spaces = SpaceUtil.getSpacesList(store.getSpaces());
        ModelAndView mav = new ModelAndView(getSuccessView());
        mav.addObject("spaces", spaces);
        if (error != null) {
            mav.addObject("error", error);
        }
        return mav;
    }

}