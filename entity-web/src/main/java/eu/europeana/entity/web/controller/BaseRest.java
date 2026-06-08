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
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entity.definitions.model.vocabulary.SuggestAlgorithmTypes;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.utils.EntityUtils;
import eu.europeana.entity.web.config.EntityWebConfig;
import eu.europeana.entity.web.jsonld.EntityResultsPageSerializer;
import eu.europeana.entity.web.jsonld.JsonLdSerializer;
import eu.europeana.entity.definitions.model.search.enrich.EnrichQuery;
import eu.europeana.entity.web.service.EntityAuthorizationService;
import eu.europeana.entity.web.service.EntityService;
import eu.europeana.entity.web.service.UsageStatsService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Pattern;

import static eu.europeana.entity.utils.EntityUtils.isEmptyOrAll;

public abstract class BaseRest extends BaseRestController {

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
     * Creates a {@link ResponseEntity} containing a {@link StreamingResponseBody} which streams the given object
     * serialized as JSON-LD to the output stream.
     *
     * @param object the object to be serialized and streamed in the response body
     * @param headers the HTTP headers to be set in the response
     * @param status the HTTP status code for the response
     * @return a {@link ResponseEntity} containing the streamed content and the specified headers and status
     */
    public ResponseEntity<StreamingResponseBody> getResponse(Object object,
                                                             MultiValueMap<String, String> headers,
                                                             HttpStatus status) {

        if (headers == null) {
            headers = new LinkedMultiValueMap<>(1);
        }

        if (headers != null && !headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        }

        StreamingResponseBody responseBody = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                jsonLdSerializer.write(object, out);
                out.flush();
            }
        };
        return new ResponseEntity<>(responseBody, headers, status);
    }

    /**
     * Serializes the metric data
     * @param metricData data obtained from solr
     * @return string
     * @throws EuropeanaApiException
     */
    protected void serializeMetricView(EntityMetric metricData, OutputStream out) throws IOException {
        jsonLdSerializer.write(metricData, out);
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

    /**
     * Validates the provided entity type string and returns the corresponding EntityType.
     * If the input string cannot be mapped to a valid entity type, the default EntityTypes.All is returned.
     *
     * @param type the string representation of the entity type to be validated
     * @return the validated EntityType corresponding to the input string, or EntityTypes.All if no match is found
     * @throws EuropeanaI18nApiException if an error occurs during validation or mapping
     */
    public EntityTypes validateEntityType(String type) throws EuropeanaI18nApiException {
        List<EntityTypes> entityTypes = getEntityService().getEntityTypesFromString(type);
        if (isEmptyOrAll(entityTypes)) {
            return null;
        } else {
            return entityTypes.get(0);
        }
    }

    public void validateEnrichQuery(List<EnrichQuery> query) throws InvalidParamException {
        // chek if there is atleast one text present
        if (query == null || query.isEmpty()) {
            throw new InvalidParamException(Arrays.asList("query",
                    "query can not be empty. expected to have one text", "query empty"));
        }

        // chek the mandatory text field and lang param if present
        for (EnrichQuery enrichQuery : query) {
            if (StringUtils.isBlank(enrichQuery.getText())) {
                throw new InvalidParamException(Arrays.asList(WebEntityConstants.QUERY_PARAM_TEXT,
                        "Text is mandatory field for searching", "empty"));
            }
            if (StringUtils.isNotBlank(enrichQuery.getLang())) {
                validateLanguage(enrichQuery.getLang());
            }
        }
    }

    public void validateRows(int rows) throws InvalidParamException {
        if (rows <= 0 || rows > 50) {
            throw new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_ROWS,
                    "Positive integer value and <= 50", String.valueOf(rows)));
        }
    }

    @Override
    protected AuthorizationService getAuthorizationService() {
        return entityAuthorizationService;
    }
}
