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
     * removing a leading or trailing double quote, escaping the double quote in the middle of the uri,
     * and validating the uri to conform to the URI syntax.
     * @param uri
     * @return
     */
    public static String convertToValidUri(String uri, String quotes) {
      uri = uri.trim();
      uri = StringUtils.removeStart(uri, "\"");
      uri = StringUtils.removeEnd(uri, "\"");
      uri = escapeQuotes(uri, quotes);
      if(UriValidator.isUri(uri)) {
        return uri;
      }
      else {
        return null;
      }      
    }
    
    public static String escapeBackslashAndQuotes(String text, String backslash, String quotes) {
      //first replace backslash to avoid conflict with string containing both " and /
      text = escapeBackslash(text, backslash);
      text = escapeQuotes(text, quotes);
      return  text;
    }
    
    public static String escapeBackslash (String text, String backslash) {
      if (text.contains(backslash)) {
        text = StringUtils.replace(text, backslash, "\\\\");
      }
      return text;
    }
    
    public static String escapeQuotes (String text, String quotes) {
      if (text.contains(quotes)) {
        text = StringUtils.replace(text, quotes, "\\\"");
      }
      return text;
    }

}
