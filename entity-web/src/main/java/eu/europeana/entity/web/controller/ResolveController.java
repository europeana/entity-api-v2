package eu.europeana.entity.web.controller;

import eu.europeana.api.commons_sb3.error.EuropeanaApiErrorResponse;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.config.ErrorConfig;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.api.commons_sb3.error.i18n.I18nService;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.config.I18nConstants;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.utils.EntityUtils;
import eu.europeana.entity.web.config.EntityWebConfig;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.util.Arrays;
import java.util.List;

import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.*;

@Controller
public class ResolveController extends BaseRest {

    @Resource(name = AppConfigConstants.BEAN_WEB_CONFIG)
    private EntityWebConfig entityWebConfig;

    @Resource(name = ErrorConfig.BEAN_I18nService)
    private I18nService i18nService;


    /**
     * Resolves an entity based on the provided URI and returns an appropriate HTTP response.
     *
     * @param uri the URI of the entity to be resolved.
     * @param request the HTTP request object used for authentication and other request-related information.
     * @return a ResponseEntity containing the resolution result. Possible outcomes include:
     *         - HTTP 301 (Moved Permanently) if a single entity URI is resolved.
     *         - HTTP 300 (Multiple Choices) if multiple entity URIs are resolved.
     *         - HTTP 404 (Not Found) if no entity URIs are resolved.
     * @throws EuropeanaApiException if an error occurs during processing.
     */
    @RequestMapping(value = {"/entity/resolve"}, method = RequestMethod.GET,
            produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
    public ResponseEntity<StreamingResponseBody> resolveEntity(
            @RequestParam(value = WebEntityConstants.QUERY_PARAM_URI) String uri, HttpServletRequest request)
            throws EuropeanaApiException {

        if (isAuthEnabled(webConfig.getApiKeyServiceUrl())) {
            verifyReadAccess(request);
        }

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(EXPECTED_SIZE);
        headers.add(ALLOW, ALLOW_GET);

        //validate the uri
        String validatedUri = EntityUtils.convertToValidUri(uri);
        if (validatedUri == null) {
            throw new InvalidParamException(Arrays.asList(WebEntityConstants.QUERY_PARAM_URI, "Valid uri", uri));
        }

        List<String> entityUris = getEntityService().resolveByUri(validatedUri);

        //EA-4372 if empty, return 404 Not Found (exception without logging)
        if (entityUris.isEmpty()) {
            return getResponse(buildErrorResponse(validatedUri, request), headers, HttpStatus.NOT_FOUND);
        }

        String preferredEntity = EntityUtils.replaceBaseUrlInId(entityUris.get(0), entityWebConfig.getEntityDataEndpoint());
        headers.add(HttpHeaders.LOCATION, preferredEntity);

        if (entityUris.size() == 1) {
            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        } else {
            List<String> updatedUris = EntityUtils.updateBaseUrlInIds(entityUris, entityWebConfig.getEntityDataEndpoint());
            return getResponse(updatedUris, headers, HttpStatus.MULTIPLE_CHOICES);
        }
    }


    /**
     * Constructs an instance of {@link EuropeanaApiErrorResponse} representing an error response
     * for scenarios where a requested entity cannot be found based on the provided URI.
     *
     * @param uri the URI of the requested entity that could not be found
     * @param request the HTTP request associated with the error
     * @return an {@link EuropeanaApiErrorResponse} detailing the error including the
     *         HTTP status code, an error message, and other context information
     */
    private EuropeanaApiErrorResponse buildErrorResponse(String uri, HttpServletRequest request) {
      return new EuropeanaApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                i18nService.getMessage(I18nConstants.CANT_FIND_BY_SAME_AS_URI, new String[] {WebEntityConstants.ENTITY_API_RESOURCE, uri}),
                null,
                String.valueOf(request.getRequestURL().append("?").append(request.getQueryString())),
                "entity_same_as_not_found");
    }
}