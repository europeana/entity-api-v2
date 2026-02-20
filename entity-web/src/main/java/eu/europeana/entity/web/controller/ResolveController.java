package eu.europeana.entity.web.controller;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.utils.EntityUtils;
import eu.europeana.entity.web.config.EntityWebConfig;
import eu.europeana.entity.web.exception.EntityNotFoundException;
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

import java.util.Arrays;
import java.util.List;

import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.*;

@Controller
public class ResolveController extends BaseRest {

    @Resource(name = AppConfigConstants.BEAN_WEB_CONFIG)
    private EntityWebConfig entityWebConfig;


    @RequestMapping(value = {"/entity/resolve"}, method = RequestMethod.GET,
            produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
    public ResponseEntity<String> resolveEntity(
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