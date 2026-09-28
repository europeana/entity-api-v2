package eu.europeana.entity.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.europeana.api.commons_sb3.definitions.search.enrich.LanguageText;
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import org.apache.commons.lang3.StringUtils;

import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;

public class EntityUtils {

    public static boolean isEmptyOrAll(List<EntityTypes> entityTypes) {
        return entityTypes == null || entityTypes.isEmpty() || entityTypes.contains(EntityTypes.All);
    }

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
     * This method converts a uri string to an escaped URI used for solr search, by removing all leading and trailing spaces,
     * removing a leading or trailing double quote, escaping the double quote in the middle of the uri,
     * and validating the uri to conform to the URI syntax.
     * @param uri a string representation of an URI that might be enclosed in quotes
     * @return the solr escaped URI or null if the provide value is not a valid URI
     */
    public static String convertToValidUri(String uri) {
      uri = uri.trim();
      uri = StringUtils.removeStart(uri, WebEntityConstants.QUOTE);
      uri = StringUtils.removeEnd(uri, WebEntityConstants.QUOTE);
      uri = escapeQuotes(uri);
      if(UriValidator.isUri(uri)) {
        return uri;
      }
      else {
        return null;
      }      
    }

    /**
     * EA-4646
     * Normalizes the input text by removing leading and trailing spaces, and replacing multiple
     * consecutive whitespace characters with a single space.
     *
     * @param text the input text to be normalized
     * @return the normalized version of the input text
     */
    public static String normaliseText(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    public static String escapeBackslashAndQuotes(String text, String backslash, String quotes) {
      //first replace backslash to avoid conflict with string containing both " and /
      text = escapeBackslash(text);
      text = escapeQuotes(text);
      return  text;
    }
    
    public static String escapeBackslash (String text) {
      if (text.contains(WebEntityConstants.BACKSLASH)) {
        text = StringUtils.replace(text, WebEntityConstants.BACKSLASH, WebEntityConstants.SOLR_ESCAPED_BACKSLASH);
      }
      return text;
    }
    
    public static String escapeQuotes (String text) {
      if (text.contains(WebEntityConstants.QUOTE)) {
        text = StringUtils.replace(text, WebEntityConstants.QUOTE, WebEntityConstants.SOLR_ESCAPED_QUOTE);
      }
      return text;
    }

    public static String removeSolrFieldNames(String text) {
        if (!text.contains(WebEntityConstants.FIELD_DELIMITER)) {
            return text;
        }
        String query = text;
        String solrField;
        while (query.contains(WebEntityConstants.FIELD_DELIMITER)) {
            solrField = extractSolrFieldName(query);
            query = StringUtils.replaceOnce(query, solrField, " ");
        }
        return query;
    }

    public static String extractSolrFieldName(String query) {
        int end = query.indexOf(WebEntityConstants.FIELD_DELIMITER);
        //include delimiter
        String fieldName = query.substring(0, end+1);
        int start_space = StringUtils.lastIndexOf(fieldName, " ");
        int start_bracket = StringUtils.lastIndexOf(fieldName, "(");
        int start = Math.max(start_space, start_bracket);
        // if none start from beginning
        start = Math.max(0, start);
        fieldName = fieldName.substring(start);
        return fieldName;
    }
    
    public static String removeSolrAndOr(String query) {
        //remove AND OR
        if(query.contains(WebEntityConstants.SOLR_AND)) {
            query = query.replaceAll(WebEntityConstants.SOLR_AND, " ");
        }

        if(query.contains(WebEntityConstants.SOLR_OR)) {
            query = query.replaceAll(WebEntityConstants.SOLR_OR, " ");
        }
        return query;
    }
    
    public static String removePunctuations(String query, Pattern pattern) {
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            query = matcher.replaceAll(" ");
        }
        return query;
    }

    /**
     * Cleans up a list of {@code LanguageText} objects by escaping special characters and normalizing the text.
     * Each text is processed to escape backslashes and quotes and is then normalized by removing unnecessary spaces.
     *
     * @param input a list of {@code LanguageText} objects to be cleaned up
     * @return a new list of {@code LanguageText} objects with cleaned and normalized text
     */
    public static List<LanguageText> cleanUpText(List<LanguageText> input) {
        List<LanguageText> cleanedText = new ArrayList<>(input.size());
        for (LanguageText languageText : input) {
            String validatedText = EntityUtils.escapeBackslashAndQuotes(
                    languageText.text(),
                    WebEntityConstants.BACKSLASH,
                    WebEntityConstants.QUOTE);
            String normalisedText = normaliseText(validatedText);
            cleanedText.add(new LanguageText(normalisedText, languageText.lang()));
        }
        return cleanedText;
    }
}
