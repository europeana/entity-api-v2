package eu.europeana.entity.web.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import eu.europeana.api.commons.web.model.ErrorApiResponse;
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

import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.definitions.formats.FormatTypes;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.RankedEntity;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.utils.EntityUtils;
import eu.europeana.entity.web.config.EntityWebConfig;
import eu.europeana.entity.web.exception.InternalServerException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@SwaggerSelect
@Api(tags = "Entity retrieval", description = " ")
public class ResolveController extends BaseRest {
  
    @Resource(name = AppConfigConstants.BEAN_WEB_CONFIG)
    private EntityWebConfig entityWebConfig;

    private static final String ACCEPT = "Accept=";
    private static final String ACCEPT_HEADER_JSONLD = ACCEPT + HttpHeaders.CONTENT_TYPE_JSONLD;
    private static final String ACCEPT_HEADER_JSON = ACCEPT + MediaType.APPLICATION_JSON_VALUE;
    private static final String ACCEPT_HEADER_APPLICATION_RDF_XML = ACCEPT
            + HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML;
    private static final String ACCEPT_HEADER_RDF_XML = ACCEPT + HttpHeaders.CONTENT_TYPE_RDF_XML;
    private static final String ACCEPT_HEADER_APPLICATION_XML = ACCEPT + MediaType.APPLICATION_XML_VALUE;

    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/entity/{type}/{identifier}.jsonld", "/entity/{type}/base/{identifier}.jsonld",
            "/entity/{type}/{identifier}.json",
            "/entity/{type}/base/{identifier}.json" }, method = RequestMethod.GET, produces = {
                    HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> getJsonLdEntity(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
            HttpServletRequest request) throws HttpException {
        return createResponse(type, identifier, FormatTypes.jsonld, null, request);
    }

    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/entity/{type}/{identifier}.schema.jsonld",
            "/entity/{type}/base/{identifier}.schema.jsonld" }, method = RequestMethod.GET, produces = {
                    HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> getSchemaJsonLdEntity(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
            HttpServletRequest request) throws HttpException {
        return createResponse(type, identifier, FormatTypes.schema, null, request);
    }

    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/entity/{type}/{identifier}.xml",
            "/entity/{type}/base/{identifier}.xml" }, method = RequestMethod.GET, produces = {
                    HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML, HttpHeaders.CONTENT_TYPE_RDF_XML,
                    MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<String> getXmlEntity(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
            HttpServletRequest request) throws HttpException {
        return createResponse(type, identifier, FormatTypes.xml, HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML, request);
    }

    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/entity/{type}/{identifier}",
            "/entity/{type}/base/{identifier}" }, method = RequestMethod.GET, headers = { ACCEPT_HEADER_JSONLD,
                    ACCEPT_HEADER_JSON }, produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8,
                            HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    public ResponseEntity<String> getEntity(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
            HttpServletRequest request) throws HttpException {
        return createResponse(type, identifier, FormatTypes.jsonld, null, request);

    }

    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/entity/{type}/{identifier}",
            "/entity/{type}/base/{identifier}" }, method = RequestMethod.GET, headers = {
                    ACCEPT_HEADER_APPLICATION_RDF_XML, ACCEPT_HEADER_RDF_XML,
                    ACCEPT_HEADER_APPLICATION_XML }, produces = { HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML,
                            HttpHeaders.CONTENT_TYPE_RDF_XML, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<String> getXmlHeaderEntity(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
            HttpServletRequest request) throws HttpException {
        return createResponse(type, identifier, FormatTypes.xml, null, request);

    }

    private ResponseEntity<String> createResponse(String type, String identifier, FormatTypes outFormat,
            String contentType, HttpServletRequest request) throws HttpException {
        try {
            verifyReadAccess(request);
            Entity entity = getEntityService().retrieveByUrl(type, identifier);
            
            String jsonLd = serialize(entity, outFormat);

            Date timestamp = ((RankedEntity) entity).getTimestamp();
            Date etagDate = (timestamp != null) ? timestamp : new Date();
            String etag = generateETag(etagDate, outFormat.name(), getApiVersion());

            MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
            headers.add(HttpHeaders.ETAG, "" + etag);
            headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);
            if (!outFormat.equals(FormatTypes.schema)) {
                headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
                headers.add(HttpHeaders.LINK, HttpHeaders.VALUE_LDP_RESOURCE);
            }
            if (contentType != null && !contentType.isEmpty())
                headers.add(HttpHeaders.CONTENT_TYPE, contentType);

            ResponseEntity<String> response = new ResponseEntity<String>(jsonLd, headers, HttpStatus.OK);
            return response;
        } catch (Exception e) {
            throw new InternalServerException(e);
        }
    }

    @ApiOperation(value = "Performs a lookup for the entity in all 4 datasets", nickname = "resolveEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/entity/resolve" }, method = RequestMethod.GET,
            produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    public ResponseEntity<String> resolveEntity(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
            @RequestParam(value = WebEntityConstants.QUERY_PARAM_URI) String uri, HttpServletRequest request)
            throws HttpException {

        try {
            verifyReadAccess(request);
            
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
            headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);
            
            //validate the uri
            String validatedUri = EntityUtils.convertToValidUri(uri);
            if (validatedUri==null) {
                ErrorApiResponse errorResponse = new ErrorApiResponse(wskey, request.getRequestURI(), "Invalid uri parameter.");
                String body = jsonLdSerializer.serializeToJson(errorResponse);
                return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
            }
            
            List<String> entityUris = getEntityService().resolveByUri(validatedUri);
            
            //if empty, return 404 Not Found with appropriate error response
            if (entityUris.isEmpty()) {
                ErrorApiResponse errorResponse = new ErrorApiResponse(wskey, request.getRequestURI(), "No entity found for sameAs/exactMatch URI : " + validatedUri);
                String body = jsonLdSerializer.serializeToJson(errorResponse);
                return new ResponseEntity<>(body, headers, HttpStatus.NOT_FOUND);
            }

            String preferedEntity = EntityUtils.replaceBaseUrlInId(entityUris.get(0), entityWebConfig.getEntityDataEndpoint());
            headers.add(HttpHeaders.LOCATION, preferedEntity);
            
            if(entityUris.size() == 1) {
                return new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
            } else {
                List<String> updatedUris = EntityUtils.updateBaseUrlInIds(entityUris, entityWebConfig.getEntityDataEndpoint());
                String body = jsonLdSerializer.serializeToJson(updatedUris);
                return new ResponseEntity<String>(body, headers, HttpStatus.MULTIPLE_CHOICES);                        
            }

        } catch (Exception e) {
            throw new InternalServerException(e);
        }

    }

}