package org.duracloud.customerwebapp.control;

import java.util.List;

import org.apache.log4j.Logger;

import org.duracloud.customerwebapp.domain.ContentItem;
import org.duracloud.customerwebapp.domain.Space;
import org.duracloud.customerwebapp.util.SpaceUtil;
import org.duracloud.customerwebapp.util.StorageProviderFactory;
import org.duracloud.storage.provider.StorageProvider;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import static org.duracloud.storage.util.StorageProviderUtil.getList;

public class AddContentController extends SimpleFormController {

    protected final Logger log = Logger.getLogger(getClass());

	public AddContentController()	{
        setCommandClass(ContentItem.class);
        setCommandName("content");
	}

    @Override
    protected ModelAndView onSubmit(Object command,
                                    BindException errors)
    throws Exception {
        ContentItem content = (ContentItem) command;

        String spaceId = content.getSpaceId();
        if(spaceId == null || spaceId.equals("")) {
            throw new IllegalArgumentException("Space ID must be provided.");
        }

        StorageProvider storage = StorageProviderFactory.getStorageProvider();

        String error = null;
        MultipartFile file = content.getFile();
        if(file == null || file.isEmpty()) {
            error = "A file must be provided in order to add content.";
        } else {
            String contentId = content.getContentId();
            if(contentId == null || contentId.equals("")){
                contentId = file.getOriginalFilename();
            }

            String contentMime = content.getContentMimetype();
            if(contentMime == null || contentMime.equals("")) {
                contentMime = file.getContentType();
            }

            storage.addContent(spaceId,
                               contentId,
                               contentMime,
                               file.getSize(),
                               file.getInputStream());
        }

        // Create a Space for the view
        Space space = new Space();
        space.setSpaceId(spaceId);

        // Get the metadata of the space
        space.setMetadata(SpaceUtil.getSpaceMetadata(storage, spaceId));

        // Get the list of items in the space
        List<String> contents = getList(storage.getSpaceContents(spaceId));
        space.setContents(contents);

        ModelAndView mav = new ModelAndView(getSuccessView());
        mav.addObject("space", space);

        if(error != null) {
            mav.addObject("error", error);
        }

        return mav;
    }

}