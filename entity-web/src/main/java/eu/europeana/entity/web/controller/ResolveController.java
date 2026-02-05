package eu.europeana.entity.web.controller;

import eu.europeana.api.commons_sb3.definitions.format.RdfFormat;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.RankedEntity;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.utils.EntityUtils;
import eu.europeana.entity.web.config.EntityWebConfig;
import eu.europeana.entity.web.exception.EntityNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static eu.europeana.api.commons_sb3.definitions.caching.CachingHeaders.ETAG;
import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.*;

@Controller
//@SwaggerSelect
@Api(tags = "Entity retrieval", description = " ")
public class ResolveController extends BaseRest {

    @Resource(name = AppConfigConstants.BEAN_WEB_CONFIG)
    private EntityWebConfig entityWebConfig;

    private static final String ACCEPT = "Accept=";
    private static final String ACCEPT_HEADER_JSONLD = ACCEPT + CONTENT_TYPE_JSONLD;
    private static final String ACCEPT_HEADER_JSON = ACCEPT + MediaType.APPLICATION_JSON_VALUE;
    private static final String ACCEPT_HEADER_APPLICATION_RDF_XML = ACCEPT
            + CONTENT_TYPE_APPLICATION_RDF_XML;
    private static final String ACCEPT_HEADER_RDF_XML = ACCEPT + CONTENT_TYPE_RDF_XML;
    private static final String ACCEPT_HEADER_APPLICATION_XML = ACCEPT + MediaType.APPLICATION_XML_VALUE;


    /**
     * @deprecated since = "04-02-2026" ,
     *             Entity Management APi is used for entity retrieval now
     */
    @Deprecated(since = "04-02-2026")
    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
    @RequestMapping(value = {"/entity/{type}/{identifier}.jsonld", "/entity/{type}/base/{identifier}.jsonld",
            "/entity/{type}/{identifier}.json",
            "/entity/{type}/base/{identifier}.json"}, method = RequestMethod.GET, produces = {
            CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getJsonLdEntity(
            @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
            HttpServletRequest request) throws EuropeanaApiException {
        return createResponse(type, identifier, RdfFormat.JSONLD, null, request);
    }

    /**
     * @deprecated since = "04-02-2026" ,
     *             Entity Management APi is used for entity retrieval now
     */
    @Deprecated(since = "04-02-2026")
    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
    @RequestMapping(value = {"/entity/{type}/{identifier}.schema.jsonld",
            "/entity/{type}/base/{identifier}.schema.jsonld"}, method = RequestMethod.GET, produces = {
            CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getSchemaJsonLdEntity(
            @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
            HttpServletRequest request) throws EuropeanaApiException {
        return createResponse(type, identifier, RdfFormat.SCHEMA, null, request);
    }

    /**
     * @deprecated since = "04-02-2026" ,
     *             Entity Management APi is used for entity retrieval now
     */
    @Deprecated(since = "04-02-2026")
    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
    @RequestMapping(value = {"/entity/{type}/{identifier}.xml",
            "/entity/{type}/base/{identifier}.xml"}, method = RequestMethod.GET, produces = {
            CONTENT_TYPE_APPLICATION_RDF_XML, CONTENT_TYPE_RDF_XML,
            MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> getXmlEntity(
            @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
            HttpServletRequest request) throws EuropeanaApiException {
        return createResponse(type, identifier, RdfFormat.XML, CONTENT_TYPE_APPLICATION_RDF_XML, request);
    }

    /**
     * @deprecated since = "04-02-2026" ,
     *             Entity Management APi is used for entity retrieval now
     */
    @Deprecated(since = "04-02-2026")
    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
    @RequestMapping(value = {"/entity/{type}/{identifier}",
            "/entity/{type}/base/{identifier}"}, method = RequestMethod.GET, headers = {ACCEPT_HEADER_JSONLD,
            ACCEPT_HEADER_JSON}, produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
    public ResponseEntity<String> getEntity(
            @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
            HttpServletRequest request) throws EuropeanaApiException {
        return createResponse(type, identifier, RdfFormat.JSONLD, null, request);

    }

    /**
     * @deprecated since = "04-02-2026" ,
     *             Entity Management APi is used for entity retrieval now
     */
    @Deprecated(since = "04-02-2026")
    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
    @RequestMapping(value = {"/entity/{type}/{identifier}",
            "/entity/{type}/base/{identifier}"}, method = RequestMethod.GET, headers = {
            ACCEPT_HEADER_APPLICATION_RDF_XML, ACCEPT_HEADER_RDF_XML,
            ACCEPT_HEADER_APPLICATION_XML}, produces = {CONTENT_TYPE_APPLICATION_RDF_XML,
            CONTENT_TYPE_RDF_XML, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> getXmlHeaderEntity(
            @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
            HttpServletRequest request) throws EuropeanaApiException {
        return createResponse(type, identifier, RdfFormat.XML, null, request);

    }

    /**
     * @deprecated since = "04-02-2026" ,
     *             Entity Management APi is used for entity retrieval now
     */
    @Deprecated(since = "04-02-2026")
    private ResponseEntity<String> createResponse(String type, String identifier, RdfFormat format,
                                                  String contentType, HttpServletRequest request) throws EuropeanaApiException {
        if (isAuthEnabled(webConfig.getApiKeyServiceUrl())) {
            verifyReadAccess(request);
        }

        Entity entity = getEntityService().retrieveByUrl(type, identifier);
        String jsonLd = serialize(entity, format);

        Date timestamp = ((RankedEntity) entity).getTimestamp();
        Date etagDate = (timestamp != null) ? timestamp : new Date();
        String etag = generateETag(etagDate, format.name(), getApiVersion());

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
        headers.add(ETAG, "" + etag);
        headers.add(ALLOW, ALLOW_GET);
        if (!format.equals(RdfFormat.SCHEMA)) {
            headers.add(HttpHeaders.VARY, ACCEPT);
            headers.add(LINK, VALUE_LDP_RESOURCE);
        }
        if (contentType != null && !contentType.isEmpty())
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        ResponseEntity<String> response = new ResponseEntity<>(jsonLd, headers, HttpStatus.OK);
        return response;
    }

    @ApiOperation(value = "Performs a lookup for the entity in all 4 datasets", nickname = "resolveEntity")
    @RequestMapping(value = {"/entity/resolve"}, method = RequestMethod.GET,
            produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
    public ResponseEntity<String> resolveEntity(
            @RequestParam(value = WebEntityConstants.QUERY_PARAM_URI) String uri, HttpServletRequest request)
            throws EuropeanaApiException {

        if (isAuthEnabled(webConfig.getApiKeyServiceUrl())) {
            verifyReadAccess(request);
        }

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
        headers.add(ALLOW, ALLOW_GET);

        //validate the uri
        String validatedUri = EntityUtils.convertToValidUri(uri);
        if (validatedUri == null) {
            throw new InvalidParamException(Arrays.asList(WebEntityConstants.QUERY_PARAM_URI, "Valid uri", uri));
        }

        List<String> entityUris = getEntityService().resolveByUri(validatedUri);

        //if empty, return 404 Not Found with appropriate error response
        if (entityUris.isEmpty()) {
            throw new EntityNotFoundException(Arrays.asList(WebEntityConstants.ENTITY_API_RESOURCE, validatedUri));
        }

        String preferredEntity = EntityUtils.replaceBaseUrlInId(entityUris.get(0), entityWebConfig.getEntityDataEndpoint());
        headers.add(HttpHeaders.LOCATION, preferredEntity);

        if (entityUris.size() == 1) {
            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        } else {
            List<String> updatedUris = EntityUtils.updateBaseUrlInIds(entityUris, entityWebConfig.getEntityDataEndpoint());
            String body = jsonLdSerializer.serializeToJson(updatedUris);
            return new ResponseEntity<>(body, headers, HttpStatus.MULTIPLE_CHOICES);
        }
    }
}