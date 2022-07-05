package eu.europeana.entity.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;

public class EntityUtils {

    public static String createWikimediaResourceString(String wikimediaCommonsId) {
        assert wikimediaCommonsId.contains("Special:FilePath/");
        return wikimediaCommonsId.replace("Special:FilePath/", "File:");
    }

    public static String toGeoUri(String latLon) {
        return WebEntityConstants.PROTOCOL_GEO + latLon;
    }

    public static String replaceBaseUrlInId(String id, String dataEndpoint) {
        if (dataEndpoint == null || id.startsWith(dataEndpoint)) {
            return id;
        }

        int slashCountForEndOfDomain = 3;
        int pathStartPos = StringUtils.ordinalIndexOf(id, WebEntityConstants.SLASH, slashCountForEndOfDomain);
        if(dataEndpoint.endsWith(WebEntityConstants.SLASH)) {
            //avoid double slash
            pathStartPos++;
        }

        if (pathStartPos > 0) {
            return dataEndpoint + id.substring(pathStartPos);
        } 
        //should not happen
        return id;
    }
    
    public static List<String> updateBaseUrlInIds(List<String> entityUris, String dataEndPoint) {
        if(entityUris == null || entityUris.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> updatedUris = new ArrayList<>(entityUris.size());
        for(String id : entityUris) {
            updatedUris.add(replaceBaseUrlInId(id, dataEndPoint));   
        }
        return updatedUris;
    }
    
    /**
     * This method converts a uri string to a valid URI, by removing all leading and trailing spaces,
     * removing a leading or trailing double quote, and validating it to conform to the URI syntax.
     * In case that a uri does not conform to the URI syntax, null is returned. 
     * @param uri
     * @return
     */
    public static String convertToValidUri(String uri) {
      uri = uri.trim();
      uri = StringUtils.removeStart(uri, "\"");
      uri = StringUtils.removeEnd(uri, "\"");
      if(UriValidator.isUri(uri)) {
        return uri;
      }
      else {
        return null;
      }      
    }
}
