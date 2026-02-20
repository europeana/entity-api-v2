package eu.europeana.entity.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.api.commons_sb3.definitions.search.result.ResultsPage;
import eu.europeana.api.commons_sb3.definitions.search.result.ResultsPageSerializer;
import eu.europeana.api.commons_sb3.definitions.statistics.entity.EntityMetric;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.api.commons_sb3.oauth2.BaseRestController;
import eu.europeana.api.commons_sb3.oauth2.service.authorization.AuthorizationService;
import eu.europeana.entity.config.I18nConstants;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.search.SearchProfiles;
import eu.europeana.entity.definitions.model.vocabulary.SuggestAlgorithmTypes;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.utils.EntityUtils;
import eu.europeana.entity.web.config.EntityWebConfig;
import eu.europeana.entity.web.jsonld.EntityResultsPageSerializer;
import eu.europeana.entity.web.jsonld.JsonLdSerializer;
import eu.europeana.entity.web.service.EntityAuthorizationService;
import eu.europeana.entity.web.service.EntityService;
import eu.europeana.entity.web.service.UsageStatsService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.regex.Pattern;

public abstract class BaseRest extends BaseRestController {

    private static final Logger LOGGER                  = LogManager.getLogger(BaseRest.class);

    private static final Set<String> ISO_LANGUAGES      = Set.of(Locale.getISOLanguages());

    private static  final String REGEX_PATTERN          = "\\p{Punct}";
    private static final Pattern pattern                = Pattern.compile(REGEX_PATTERN);
    protected static final int EXPECTED_SIZE            = 5;


    @Resource(name = AppConfigConstants.BEAN_AUTHORIZATION_SERVICE)
    EntityAuthorizationService entityAuthorizationService;

    @Resource(name = AppConfigConstants.BEAN_ENTITY_SERVICE)
    private EntityService entityService;

    @Resource(name = AppConfigConstants.BEAN_WEB_CONFIG)
    EntityWebConfig webConfig;

    @Resource(name = AppConfigConstants.BEAN_EM_JSONLD_SERIALIZER)
    JsonLdSerializer jsonLdSerializer;

    @Resource(name = AppConfigConstants.BEAN_USAGE_SERVICE)
    private UsageStatsService usageStatsService;
    
    public BaseRest() {
        super();
    }

    protected EntityService getEntityService() {
        return entityService;
    }

    public UsageStatsService getUsageStatsService() {
        return usageStatsService;
    }

    /**
     * Serializes the metric data
     * @param metricData data obtained from solr
     * @return string
     * @throws EuropeanaApiException
     */
    protected String serializeMetricView(EntityMetric metricData) throws EuropeanaApiException {
        return jsonLdSerializer.serializeToJson(metricData);
    }

    /**
     * This method verifies if the provided scope parameter is a valid one
     *
     * @param scope
     * @return
     * @throws InvalidParamException
     */
    protected String validateScopeParam(String scope) throws InvalidParamException {
        if (StringUtils.isBlank(scope))
            return null;

        if (!WebEntityConstants.PARAM_SCOPE_EUROPEANA.equalsIgnoreCase(scope))
            throw new InvalidParamException(Arrays.asList(WebEntityConstants.QUERY_PARAM_SCOPE,
                    WebEntityConstants.PARAM_SCOPE_EUROPEANA, scope));

        return WebEntityConstants.PARAM_SCOPE_EUROPEANA;
    }

    /**
     * Validates the page parameter
     *
     * @param page page value
     * @throws InvalidParamException
     */
    protected void validatePageParam(int page) throws InvalidParamException {
        if (page < 1) {
            throw new InvalidParamException(Arrays.asList(WebEntityConstants.QUERY_PARAM_PAGE,
                    "positive integer value and >= 1",
                    String.valueOf(page)));
        }
    }

    /**
     * This method verifies that the provided text parameter is a valid one. It
     * should not contain field names e.g. "who:mozart" and special characters e.g.
     * " or (
     *
     * @param text
     * @return validated text
     * @throws InvalidParamException
     */
    protected String preProcessQuery(String text) {
        if (text == null) {
            return null;
        }
        // remove solr field names
        String query = EntityUtils.removeSolrFieldNames(text);
        query = EntityUtils.removeSolrAndOr(query);

        // remove punctuation
        query = EntityUtils.removePunctuations(query, pattern);
        return query;
    }

    /**
     * Validate language parameter
     *
     * @param language language provided
     * @throws InvalidParamException
     */
    protected void validateLanguage(String language) throws InvalidParamException {
        if (StringUtils.isEmpty(language)) {
            return;
        }
        // multiple language not supported
        if (StringUtils.contains(language, WebEntityConstants.COMMA)) {
            throw new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_LANG,
                    "Multiple lang value not allowed!", language));
        }
        // language value can be 'all' Or ISO language only
        if (!StringUtils.equals(language, WebEntityConstants.PARAM_LANGUAGE_ALL) && !ISO_LANGUAGES.contains(language)) {
            throw new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_LANG,
                    "Only 'all' or ISO language only ", language));
        }
    }

    /**
     * This method verifies if the provided algorithm parameter is a valid one
     *
     * @param algorithm
     * @return validated algorithm
     * @throws InvalidParamException
     */
    protected SuggestAlgorithmTypes validateAlgorithmParam(String algorithm) throws EuropeanaI18nApiException {
        try {
            return SuggestAlgorithmTypes.getByName(algorithm);
        } catch (Exception e) {
            throw new EuropeanaI18nApiException(null, null, null, HttpStatus.BAD_REQUEST,
                    I18nConstants.UNSUPPORTED_ALGORITHM_TYPE, Arrays.asList(algorithm), e);
        }
    }

    /**
     * This method returns the json-ld serialization for the given results page,
     * according to the specifications of the provided search profile
     *
     * @param resPage
     * @param profile
     * @return
     * @throws JsonProcessingException
     */
    protected String serializeResultsPage(ResultsPage<? extends Entity> resPage, SearchProfiles profile,
                                          String entityIdBaseUrl) {
        ResultsPageSerializer<? extends Entity> serializer = new EntityResultsPageSerializer<>(
                resPage,
                CommonLdConstants.ENTITY_CONTEXT,
                CommonLdConstants.ResultPage,
                entityIdBaseUrl);
        String profileVal = (profile == null) ? null : profile.name();
        return serializer.serialize(profileVal);
    }

    @Override
    protected AuthorizationService getAuthorizationService() {
        return entityAuthorizationService;
    }
}
