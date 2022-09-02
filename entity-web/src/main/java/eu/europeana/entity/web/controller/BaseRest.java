package eu.europeana.entity.web.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.europeana.api.commons.definitions.search.result.ResultsPage;
import eu.europeana.api.commons.definitions.statistics.entity.EntityMetric;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;
import eu.europeana.api.commons.definitions.vocabulary.ContextTypes;
import eu.europeana.api.commons.service.authorization.AuthorizationService;
import eu.europeana.api.commons.utils.ResultsPageSerializer;
import eu.europeana.api.commons.web.controller.BaseRestController;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.entity.app.I18nConstants;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.definitions.exceptions.InvalidProfileException;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entity.definitions.exceptions.UnsupportedFormatTypeException;
import eu.europeana.entity.definitions.formats.FormatTypes;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.search.SearchProfiles;
import eu.europeana.entity.definitions.model.vocabulary.LdProfiles;
import eu.europeana.entity.definitions.model.vocabulary.SuggestAlgorithmTypes;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.stats.service.UsageStatsService;
import eu.europeana.entity.utils.EntityUtils;
import eu.europeana.entity.utils.jsonld.EuropeanaEntityLd;
import eu.europeana.entity.web.config.BuildInfo;
import eu.europeana.entity.web.config.EntityWebConfig;
import eu.europeana.entity.web.controller.exception.EntityApiRuntimeException;
import eu.europeana.entity.web.exception.ParamValidationException;
import eu.europeana.entity.web.jsonld.EntityResultsPageSerializer;
import eu.europeana.entity.web.jsonld.EntitySchemaOrgSerializer;
import eu.europeana.entity.web.jsonld.JsonLdSerializer;
import eu.europeana.entity.web.service.EntityAuthorizationService;
import eu.europeana.entity.web.service.EntityService;
import eu.europeana.entity.web.xml.EntityXmlSerializer;

public abstract class BaseRest extends BaseRestController {

    @Resource(name = AppConfigConstants.BEAN_AUTHORIZATION_SERVICE)
    EntityAuthorizationService entityAuthorizationService;

    @Resource(name = AppConfigConstants.BEAN_ENTITY_SERVICE)
    private EntityService entityService;

    @Resource(name = AppConfigConstants.BEAN_WEB_CONFIG)
    EntityWebConfig webConfig;

    @Resource(name = AppConfigConstants.BEAN_BUILD_INFO)
    BuildInfo buildInfo;

    @Resource(name = AppConfigConstants.BEAN_XML_SERIALIZER)
    EntityXmlSerializer entityXmlSerializer;

    @Resource(name = AppConfigConstants.BEAN_EM_JSONLD_SERIALIZER)
    JsonLdSerializer jsonLdSerializer;

    @Resource(name = AppConfigConstants.BEAN_USAGE_SERVICE)
    private UsageStatsService usageStatsService;

    Logger logger = LogManager.getLogger(getClass());

    private static final Set ISO_LANGUAGES = Set.of(Locale.getISOLanguages());

    Pattern pattern = null;

    public BaseRest() {
        super();
        // String regexPattern = "[^A-Za-z0-9]"
        String regexPattern = "\\p{Punct}";
        pattern = Pattern.compile(regexPattern);
    }

    protected EntityService getEntityService() {
        return entityService;
    }

    public void setEntityService(EntityService entityService) {
        this.entityService = entityService;
    }

    public void setUsageStatsService(UsageStatsService usageStatsService) {
        this.usageStatsService = usageStatsService;
    }

    public UsageStatsService getUsageStatsService() {
        return usageStatsService;
    }

    public String getApiVersion() {
        return buildInfo.getAppVersion();
    }

    protected EntityWebConfig getConfig() {
        return webConfig;
    }

    protected String serializeMetricView(EntityMetric metricData) throws EntityApiRuntimeException {
        return jsonLdSerializer.serializeToJson(metricData);
    }

    /**
     * This method verifies if the provided scope parameter is a valid one
     * 
     * @param scope
     * @return
     * @throws ParamValidationException
     */
    protected String validateScopeParam(String scope) throws ParamValidationException {
        if (StringUtils.isBlank(scope))
            return null;

        if (!WebEntityConstants.PARAM_SCOPE_EUROPEANA.equalsIgnoreCase(scope))
            throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, WebEntityConstants.QUERY_PARAM_SCOPE,
                    scope);

        return WebEntityConstants.PARAM_SCOPE_EUROPEANA;
    }

    /**
     * This method verifies if the provided format parameter is a valid one
     * 
     * @param The format string
     * @return The format type
     * @throws ParamValidationException
     */
    protected FormatTypes getFormatType(String extension) throws ParamValidationException {

        // default format, when none provided
        if (extension == null)
            return FormatTypes.jsonld;

        try {
            return FormatTypes.getByExtention(extension);
        } catch (UnsupportedFormatTypeException e) {
            throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, WebEntityConstants.QUERY_PARAM_FORMAT,
                    extension, HttpStatus.NOT_FOUND, null);
        }
    }

    /**
     * This method verifies that the provided text parameter is a valid one. It
     * should not contain field names e.g. "who:mozart" and special characters e.g.
     * " or (
     * 
     * @param text
     * @return validated text
     * @throws ParamValidationException
     */
    protected String preProcessQuery(String text) throws ParamValidationException {
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
     * @param language
     * @throws ParamValidationException
     */
    protected void validateLanguage(String language) throws ParamValidationException {
        if (StringUtils.isEmpty(language)) {
            return;
        }
        // multiple language not supported
        if (StringUtils.contains(language, WebEntityConstants.COMMA)) {
            throw new ParamValidationException(I18nConstants.UNSUPPORTED_MULTIPLE_LANG_VALUE,
                    CommonApiConstants.QUERY_PARAM_LANG, language);
        }
        // language value can be 'all' Or ISO language only
        if (!StringUtils.equals(language, WebEntityConstants.PARAM_LANGUAGE_ALL) && !ISO_LANGUAGES.contains(language)) {
            throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, CommonApiConstants.QUERY_PARAM_LANG,
                    language);
        }
    }

    /**
     * This method verifies if the provided algorithm parameter is a valid one
     * 
     * @param algorithm
     * @return validated algorithm
     * @throws ParamValidationException
     */
    protected SuggestAlgorithmTypes validateAlgorithmParam(String algorithm) throws ParamValidationException {
        try {
            return SuggestAlgorithmTypes.getByName(algorithm);
        } catch (Exception e) {
            throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
                    WebEntityConstants.QUERY_PARAM_ALGORITHM, algorithm);
        }
    }

    /**
     * This method takes profile from a HTTP header if it exists or from the passed
     * request parameter.
     * 
     * @param paramProfile The HTTP request parameter
     * @param request      The HTTP request with headers
     * @return profile value
     * @throws HttpException
     * @throws ConceptSchemeProfileValidationException
     */
    public LdProfiles getProfile(String paramProfile, HttpServletRequest request) throws HttpException {

        LdProfiles profile = null;
        String preferHeader = request.getHeader(HttpHeaders.PREFER);
        if (preferHeader != null) {
            // identify profile by prefer header
            profile = getProfile(preferHeader);
            logger.debug("Profile identified by prefer header: {}", profile.name());
        } else {
            if (paramProfile == null)
                return LdProfiles.MINIMAL;
            // get profile from param
            try {
                profile = LdProfiles.getByName(paramProfile);
            } catch (InvalidProfileException e) {
                throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
                        new String[] { CommonApiConstants.QUERY_PARAM_PROFILE, paramProfile }, HttpStatus.BAD_REQUEST,
                        e);
            }
        }
        return profile;
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
            String entityIdBaseUrl) throws JsonProcessingException {
        ResultsPageSerializer<? extends Entity> serializer = new EntityResultsPageSerializer<>(resPage,
                ContextTypes.ENTITY.getJsonValue(), CommonLdConstants.RESULT_PAGE, entityIdBaseUrl);
        String profileVal = (profile == null) ? null : profile.name();
        return serializer.serialize(profileVal);
    }

    /**
     * This method retrieves view profile if provided within the "If-Match" HTTP
     * header
     * 
     * @param request
     * @return profile value
     * @throws HttpException
     */
    // TODO have generic implementation in API-Commons
    LdProfiles getProfile(String preferHeader) throws HttpException {
        LdProfiles ldProfile = null;
        String ldPreferHeaderStr = null;
        String INCLUDE = "include";

        if (StringUtils.isNotEmpty(preferHeader)) {
            // log header for debuging
            logger.debug("'Prefer' header value: {} ", preferHeader);

            try {
                Map<String, String> preferHeaderMap = parsePreferHeader(preferHeader);
                ldPreferHeaderStr = preferHeaderMap.get(INCLUDE).replace("\"", "");
                ldProfile = LdProfiles.getByHeaderValue(ldPreferHeaderStr.trim());
            } catch (InvalidProfileException e) {
                throw new HttpException(I18nConstants.INVALID_HEADER_VALUE, I18nConstants.INVALID_HEADER_VALUE,
                        new String[] { HttpHeaders.PREFER, preferHeader }, HttpStatus.BAD_REQUEST, null);
            } catch (Throwable th) {
                throw new HttpException(I18nConstants.INVALID_HEADER_FORMAT, I18nConstants.INVALID_HEADER_FORMAT,
                        new String[] { HttpHeaders.PREFER, preferHeader }, HttpStatus.BAD_REQUEST, null);
            }
        }

        return ldProfile;
    }

    /**
     * This method parses prefer header in keys and values
     * 
     * @param preferHeader
     * @return map of prefer header keys and values
     */
    // TODO: move this method to API-Commons
    public Map<String, String> parsePreferHeader(String preferHeader) {
        String[] headerParts = null;
        String[] contentParts = null;
        int KEY_POS = 0;
        int VALUE_POS = 1;

        Map<String, String> resMap = new HashMap<String, String>();

        headerParts = preferHeader.split(";");
        for (String headerPart : headerParts) {
            contentParts = headerPart.split("=");
            resMap.put(contentParts[KEY_POS], contentParts[VALUE_POS]);
        }
        return resMap;
    }

    /**
     * This method selects serialization method according to provided format.
     * 
     * @param entity The entity
     * @param format The format extension
     * @return entity in jsonLd format
     * @throws UnsupportedEntityTypeException
     * @throws HttpException
     */
    protected String serialize(Entity entity, FormatTypes format) throws UnsupportedEntityTypeException, HttpException {

        String responseBody = null;

        if (FormatTypes.jsonld.equals(format)) {
            EuropeanaEntityLd entityLd = new EuropeanaEntityLd(entity, webConfig.getEntityDataEndpoint());
            return entityLd.toString(4);
        } else if (FormatTypes.schema.equals(format)) {
            responseBody = (new EntitySchemaOrgSerializer()).serializeEntity(entity);
        } else if (FormatTypes.xml.equals(format)) {
            responseBody = entityXmlSerializer.serializeXml(entity, webConfig.getEntityDataEndpoint());
        }
        return responseBody;
    }

    @Override
    protected AuthorizationService getAuthorizationService() {
        return entityAuthorizationService;
    }
}
