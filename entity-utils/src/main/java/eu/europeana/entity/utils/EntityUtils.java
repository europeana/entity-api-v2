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
    
    public static String validateUriAndRemoveQuotes(String uri) {
        boolean startsWithDoubleQuotes = uri.startsWith("\"");
        boolean endsWithDoubleQuotes = uri.endsWith("\"");
        
        if(startsWithDoubleQuotes && endsWithDoubleQuotes) {
          if(UriValidator.isUri(uri.substring(1, uri.length()-1))) {
            return uri.substring(1, uri.length()-1);
          }
        }
        if(!startsWithDoubleQuotes && !endsWithDoubleQuotes) {
          if(UriValidator.isUri(uri)) {
            return uri;
          }
        }
        return null;
    }
}
