package eu.europeana.entity.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.api.commons_sb3.definitions.format.RdfFormat;
import eu.europeana.api.commons_sb3.definitions.http.HttpHeaders;
import eu.europeana.api.commons_sb3.definitions.search.result.ResultsPage;
import eu.europeana.api.commons_sb3.definitions.search.result.ResultsPageSerializer;
import eu.europeana.api.commons_sb3.definitions.statistics.entity.EntityMetric;
import eu.europeana.api.commons_sb3.definitions.utils.HeaderUtils;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.api.commons_sb3.oauth2.BaseRestController;
import eu.europeana.api.commons_sb3.oauth2.service.authorization.AuthorizationService;
import eu.europeana.entity.I18nConstants;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.definitions.exceptions.InvalidProfileException;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.search.SearchProfiles;
import eu.europeana.entity.definitions.model.vocabulary.LdProfiles;
import eu.europeana.entity.definitions.model.vocabulary.SuggestAlgorithmTypes;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.utils.EntityUtils;
import eu.europeana.entity.utils.jsonld.EuropeanaEntityLd;
import eu.europeana.entity.web.config.BuildInfo;
import eu.europeana.entity.web.config.EntityWebConfig;
import eu.europeana.entity.web.jsonld.EntityResultsPageSerializer;
import eu.europeana.entity.web.jsonld.EntitySchemaOrgSerializer;
import eu.europeana.entity.web.jsonld.JsonLdSerializer;
import eu.europeana.entity.web.service.EntityAuthorizationService;
import eu.europeana.entity.web.service.EntityService;
import eu.europeana.entity.web.service.UsageStatsService;
import eu.europeana.entity.web.xml.EntityXmlSerializer;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.regex.Pattern;

public abstract class BaseRest extends BaseRestController {

    private static final Logger LOGGER                  = LogManager.getLogger(BaseRest.class);

    private static Collection<RdfFormat> validFormats   = Arrays.asList(RdfFormat.JSONLD, RdfFormat.JSON,
            RdfFormat.SCHEMA, RdfFormat.XML);

    private static final Set<String> ISO_LANGUAGES      = Set.of(Locale.getISOLanguages());

    private static  final String regexPattern           = "\\p{Punct}";
    private static final Pattern pattern                = Pattern.compile(regexPattern);

    @Resource(name = AppConfigConstants.BEAN_AUTHORIZATION_SERVICE)
    EntityAuthorizationService entityAuthorizationService;

    @Resource(name = AppConfigConstants.BEAN_ENTITY_SERVICE)
    private EntityService entityService;

    @Resource(name = AppConfigConstants.BEAN_WEB_CONFIG)
    EntityWebConfig webConfig;

    @Resource
    BuildInfo buildInfo;

    @Resource(name = AppConfigConstants.BEAN_XML_SERIALIZER)
    EntityXmlSerializer entityXmlSerializer;

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
     * Entity api will only support json OR jsonld Or schema.jsonld, or xml  formats
     * This method verifies if the provided format parameter is a valid one
     *
     * @param extension extension provided in the request
     * @return The valid Rdf Format. Default Jsonld if none provided
     * @throws InvalidParamException
     */
    protected RdfFormat getFormatType(String extension) throws InvalidParamException {
        // default format JsonLd
        if (extension == null) return RdfFormat.JSONLD;

        RdfFormat format = RdfFormat.getFormatByExtension(extension);
        if (format != null && validFormats.contains(format)) {
            return format;
        } else {
            throw new InvalidParamException(Arrays.asList(WebEntityConstants.QUERY_PARAM_FORMAT,
                    validFormats.toString(),
                    extension));
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
     * This method takes profile from a HTTP header if it exists or from the passed
     * request parameter.
     *
     * @param paramProfile The HTTP request parameter
     * @param request      The HTTP request with headers
     * @return profile value
     * @throws InvalidParamException
     */
    public LdProfiles getProfile(String paramProfile, HttpServletRequest request) throws EuropeanaApiException {

        LdProfiles profile = null;
        String preferHeader = request.getHeader(HttpHeaders.PREFER);
        if (preferHeader != null) {
            // identify profile by prefer header
            profile = getProfile(preferHeader);
            LOGGER.debug("Profile identified by prefer header: {}", profile.name());
        } else {
            if (paramProfile == null)
                return LdProfiles.MINIMAL;
            // get profile from param
            try {
                profile = LdProfiles.getByName(paramProfile);
            } catch (InvalidProfileException e) {
                throw new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_PROFILE,
                        Arrays.asList(LdProfiles.values()).toString(), paramProfile), e);
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
                                          String entityIdBaseUrl) {
        ResultsPageSerializer<? extends Entity> serializer = new EntityResultsPageSerializer<>(
                resPage,
                CommonLdConstants.ENTITY_CONTEXT,
                CommonLdConstants.RESULT_PAGE,
                entityIdBaseUrl);
        String profileVal = (profile == null) ? null : profile.name();
        return serializer.serialize(profileVal);
    }

    /**
     * This method retrieves view profile if provided within the "If-Match" HTTP
     * header
     *
     * @return profile value
     * @throws EuropeanaApiException
     */
    // TODO have generic implementation in API-Commons
    LdProfiles getProfile(String preferHeader) throws EuropeanaApiException {
        LdProfiles ldProfile = null;
        String ldPreferHeaderStr = null;
        String INCLUDE = "include";

        if (StringUtils.isNotEmpty(preferHeader)) {
            // log header for debuging
            LOGGER.debug("'Prefer' header value: {} ", preferHeader);
            try {
                Map<String, String> preferHeaderMap = HeaderUtils.parsePreferHeader(preferHeader);
                ldPreferHeaderStr = preferHeaderMap.get(INCLUDE).replace("\"", "");
                ldProfile = LdProfiles.getByHeaderValue(ldPreferHeaderStr.trim());
            } catch (InvalidProfileException e) {
                throw new InvalidParamException(Arrays.asList(HttpHeaders.PREFER,
                        Arrays.asList(LdProfiles.values()).toString(), preferHeader), e);
            } catch (Throwable th) {
                throw new InvalidParamException(Arrays.asList(HttpHeaders.PREFER,
                        "valid header format", preferHeader), th);
            }
        }
        return ldProfile;
    }

    /**
     * This method selects serialization method according to provided format.
     *
     * @param entity The entity to be serialised
     * @param format The format extension
     * @return entity in the requested format
     * @throws EuropeanaI18nApiException
     */
    protected String serialize(Entity entity, RdfFormat format) throws EuropeanaI18nApiException {
        try {
            if (RdfFormat.JSONLD.equals(format)) {
                EuropeanaEntityLd entityLd = new EuropeanaEntityLd(entity, webConfig.getEntityDataEndpoint());
                return entityLd.toString(4);
            } else if (RdfFormat.SCHEMA.equals(format)) {
                return (new EntitySchemaOrgSerializer()).serializeEntity(entity);
            } else if (RdfFormat.XML.equals(format)) {
                return entityXmlSerializer.serializeXml(entity, webConfig.getEntityDataEndpoint());
            }
            return null;
        } catch (UnsupportedEntityTypeException e) {
            throw new EuropeanaI18nApiException(null, null, null,
                    HttpStatus.NOT_FOUND,
                    I18nConstants.UNSUPPORTED_ENTITY_TYPE,
                    Arrays.asList( WebEntityConstants.ENTITY_API_RESOURCE, entity.getType()),
                    e);
        }
    }

    @Override
    protected AuthorizationService getAuthorizationService() {
        return entityAuthorizationService;
    }
}
