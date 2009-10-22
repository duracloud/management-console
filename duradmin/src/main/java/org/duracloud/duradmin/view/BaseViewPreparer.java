
package org.duracloud.duradmin.view;

import org.apache.tiles.Attribute;
import org.apache.tiles.AttributeContext;
import org.apache.tiles.context.TilesRequestContext;
import org.apache.tiles.preparer.ViewPreparer;
import org.duracloud.duradmin.contentstore.ContentStoreSelector;

public class BaseViewPreparer
        implements ViewPreparer {

    private ContentStoreSelector contentStoreSelector;
    
    
    public ContentStoreSelector getContentStoreSelector() {
        return contentStoreSelector;
    }

    
    public void setContentStoreSelector(ContentStoreSelector contentStoreSelector) {
        this.contentStoreSelector = contentStoreSelector;
    }

    public void execute(TilesRequestContext tilesRequestContext,
                        AttributeContext attributeContext) {
        
        attributeContext.putAttribute("mainMenu", new Attribute(MainMenu
                .instance()), true);

        attributeContext.putAttribute(
                            "contentStoreSelector",
                            new Attribute(contentStoreSelector), true);
        String currentUrl = (String)tilesRequestContext.getRequestScope().get("currentUrl");
        attributeContext.putAttribute("currentUrl", new Attribute(currentUrl), true);
    }
    
    
    
}