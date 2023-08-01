package eu.europeana.entity.web.controller;

import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.definitions.statistics.entity.EntityMetric;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.entity.stats.exception.UsageStatsException;
import eu.europeana.entity.stats.vocabulary.UsageStatsFields;
import eu.europeana.entity.web.controller.exception.EntityApiRuntimeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller
@Api(tags = "Usage Statistics API")
@SwaggerSelect
public class UsageStatsController extends BaseRest {

    /**
     * Method to generate metric for entity api
     *
     * @param wskey
     * @param request
     * @return
     */
    @ApiOperation(value = "Generate Stats", nickname = "generateStats", response = java.lang.Void.class)
    @GetMapping(value = "/entity/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generateUsageStats(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
            HttpServletRequest request) throws EuropeanaApiException, HttpException {
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
    private ResponseEntity<String> getEntitiesStats() throws UsageStatsException, EntityApiRuntimeException {
        EntityMetric metric = new EntityMetric();
        metric.setType(UsageStatsFields.OVERALL_TOTAL_TYPE);
        getUsageStatsService().getStatsForLang(metric);
        metric.setTimestamp(new Date());
        return new ResponseEntity<>(serializeMetricView(metric), HttpStatus.OK);
    }
}

