package eu.europeana.entity.utils;

import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;

public class EntityUtils {

	public static String createWikimediaResourceString(String wikimediaCommonsId) {
		assert wikimediaCommonsId.contains("Special:FilePath/");
		return wikimediaCommonsId.replace("Special:FilePath/", "File:");
	}
	
	public static String toGeoUri(String latLon){
		return WebEntityConstants.PROTOCOL_GEO + latLon;
	}
	
	public static int ordinalIndexOf(String str, String substr, int n) {
	    int pos = -1;
	    do {
	        pos = str.indexOf(substr, pos + 1);
	    } while (n-- > 1 && pos != -1);
	    return pos;
	}
	
	public static String replaceBaseUrlInId (String id, String replaceWith) {
	  if(replaceWith==null) {
	    return id;
	  }
	  else {
	    int indexOfNthSlash = ordinalIndexOf(id, "/", 3);
	    if(indexOfNthSlash==-1) {
	      return id;
	    }
	    else {
	      return id.replace(id.substring(0, indexOfNthSlash + 1), replaceWith);
	    }
	  }
	}
    
}
