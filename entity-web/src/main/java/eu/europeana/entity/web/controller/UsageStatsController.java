package eu.europeana.entity.web.controller;

import eu.europeana.api.commons_sb3.definitions.statistics.UsageStatsFields;
import eu.europeana.api.commons_sb3.definitions.statistics.entity.EntityMetric;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Date;

import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.CONTENT_TYPE_JSONLD_UTF8;
import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.CONTENT_TYPE_JSON_UTF8;

@Controller
public class UsageStatsController extends BaseRest {

    /**
     * Generates usage statistics for entities and returns the result as a streaming JSON response.
     *
     * @param request the HTTP servlet request containing the details of the client's request
     * @return a {@code ResponseEntity<StreamingResponseBody>} containing the streaming response with usage statistics
     * @throws EuropeanaApiException if there is an error during the generation of usage statistics
     */
    @GetMapping(value = "/entity/stats", produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
    public ResponseEntity<StreamingResponseBody> generateUsageStats(
            HttpServletRequest request) throws EuropeanaApiException {
        if (isAuthEnabled(webConfig.getApiKeyServiceUrl())) {
            verifyReadAccess(request);
        }
        return getEntitiesStats();
    }

    /**
     * Retrieves usage statistics for entities and returns the result as a streaming JSON response.
     *
     * @return a {@code ResponseEntity<StreamingResponseBody>} containing the streaming response
     *         with the entity usage statistics
     * @throws EuropeanaApiException if an error occurs during the retrieval of usage statistics
     */
    private ResponseEntity<StreamingResponseBody> getEntitiesStats() throws EuropeanaApiException {
        EntityMetric metric = new EntityMetric();
        metric.setType(UsageStatsFields.OVERALL_TOTAL_TYPE);
        getUsageStatsService().getStatsForLang(metric);
        metric.setTimestamp(new Date());
        return getResponse(metric, null, HttpStatus.OK);
    }
}

