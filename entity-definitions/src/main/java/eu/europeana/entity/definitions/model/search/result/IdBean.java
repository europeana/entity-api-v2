package eu.europeana.entity.definitions.model.search.result;

import java.util.Date;

/**
 * 
 * @author GordeaS
 * @deprecated it is used only by EntityPreview interface
 */
@Deprecated 
public interface IdBean {

    /**
     * Retrieve the Europeana object unique Id
     * 
     * @return The Europeana object UniqueID
     */
    String getEntityId();

//	/**
//	 * The date the record was created
//	 * @return 
//	 */
//	Date getCreatedTimestamp();

    /**
     * The date the record was updated
     * 
     * @return
     */
    Date getTimestampUpdated();
}
