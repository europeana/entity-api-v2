package eu.europeana.entity.web.controller;

import eu.europeana.api.commons_sb3.definitions.statistics.UsageStatsFields;
import eu.europeana.api.commons_sb3.definitions.statistics.entity.EntityMetric;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller
@Api(tags = "Usage Statistics API")
//@SwaggerSelect
public class UsageStatsController extends BaseRest {

    /**
     * Method to generate metric for entity api
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "Generate Stats", nickname = "generateStats")
    @GetMapping(value = "/entity/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generateUsageStats(
            HttpServletRequest request) throws EuropeanaApiException {
        if (isAuthEnabled(webConfig.getApiKeyServiceUrl())) {
            verifyReadAccess(request);
        }
        return getEntitiesStats();
    }

    /**
     * Get the usage statistics for entity api
     *
     * @return
     */
    private ResponseEntity<String> getEntitiesStats() throws EuropeanaApiException {
        EntityMetric metric = new EntityMetric();
        metric.setType(UsageStatsFields.OVERALL_TOTAL_TYPE);
        getUsageStatsService().getStatsForLang(metric);
        metric.setTimestamp(new Date());
        return new ResponseEntity<>(serializeMetricView(metric), HttpStatus.OK);
    }
}

