package eu.europeana.entity.web.service;

import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import eu.europeana.api.commons_sb3.error.config.ErrorConfig;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.entity.solr.exception.EntityRetrievalException;
import eu.europeana.entity.solr.exception.InvalidSearchQueryException;
import eu.europeana.entity.solr.model.SolrUtils;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

/**
 * Provides utility methods for handling exceptions and errors related
 * to Solr search operations within the context of the application.
 *
 * @author Srishti Singh
 * @since 2026-05-25
 */
public abstract class SearchServiceUtils {

    public static EuropeanaI18nApiException convertSolrSearchException(String debugInfo, EntityRetrievalException e) {
        if (SolrUtils.isMalformedQueryException(e.getCause())) {
            return new EuropeanaI18nApiException(null, null, null,
                    HttpStatus.BAD_REQUEST,
                    ErrorConfig.SOLR_MALFORMED_QUERY_EXCEPTION,
                    Arrays.asList(debugInfo));
        } else {
            return new EuropeanaI18nApiException(null, null, null,
                    HttpStatus.GATEWAY_TIMEOUT,
                    ErrorConfig.SOLR_EXCEPTION,
                    Arrays.asList(debugInfo),
                    e);
        }
    }


    public static EuropeanaI18nApiException convertSolrSearchException(String debugInfo, InvalidSearchQueryException e) {
        return new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_QUERY,
                "A valid serach query", debugInfo));
    }
}
