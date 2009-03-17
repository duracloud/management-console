package org.duraspace.control;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.duraspace.domain.Space;
import org.duraspace.domain.SpaceMetadata;
import org.duraspace.storage.StorageProvider;
import org.duraspace.util.StorageProviderUtil;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ContentsController extends SimpleFormController {

    protected final Logger log = Logger.getLogger(getClass());

	public ContentsController()	{
		setCommandClass(Space.class);
		setCommandName("space");
	}

    @Override
    protected ModelAndView onSubmit(Object command,
                                    BindException errors)
    throws Exception {
        Space space = (Space) command;
        String accountId = space.getAccountId();
        String spaceId = space.getSpaceId();

        StorageProvider storage =
            StorageProviderUtil.getStorageProvider(accountId);

        // Get the name of the space
        Properties spaceProps = storage.getSpaceMetadata(spaceId);
        SpaceMetadata spaceMetadata = new SpaceMetadata();
        String spaceName =
            spaceProps.getProperty(StorageProvider.METADATA_SPACE_NAME);
        if(spaceName == null || spaceName.equals("")) {
            spaceName = spaceId;
        }
        spaceMetadata.setName(spaceName);
        space.setMetadata(spaceMetadata);

        // Get the list of items in the space
        List<String> contents = storage.getSpaceContents(spaceId);
        space.setContents(contents);

        ModelAndView mav = new ModelAndView(getSuccessView());
        mav.addObject("space", space);

        return mav;
    }

}