package eu.europeana.entity.solr.config;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.impl.LBHttp2SolrClient;
import org.apache.solr.client.solrj.impl.LBSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import eu.europeana.entity.config.AppConfigConstants;

@Configuration
@PropertySources({ 
  @PropertySource(value = "classpath:entity.properties", ignoreResourceNotFound = true),
  @PropertySource(value = "classpath:entity-solr.properties", ignoreResourceNotFound = true),
  @PropertySource(value = "entity.user.properties", ignoreResourceNotFound = true)
  })
public class EntitySolrConfig {

    @Value("${entity.suggester.snippets:10}")
    private int suggesterSnippets;
    
    @Value("${entity.solr.timeout:60000}")
    private int solrTimeout;
    
    @Value("${entity.solr.facetLimit:750}")
    private int solrFacetLimit;
    
    
    @Value("${entity.solr.url:#{null}}")
    private String solrUrl;
    
    @Value("${entity.solr.zookeeper.url:#{null}}")
    private String solrZookeeperUrl;
    
    @Value("${entity.solr.collection:#{null}}")
    private String solrCollection;
    
    public int getSuggesterSnippets() {
        return suggesterSnippets;
    }

    private SolrClient solrClient;
    
    @Bean(AppConfigConstants.ENTITY_SOLR_CLIENT)
    public SolrClient getSolrClient(){
        if(solrClient != null) {
            return solrClient;
        }else if(StringUtils.isNotBlank(solrZookeeperUrl)) {
            solrClient = initSolrCloudClient();
        }else if(StringUtils.isNotBlank(solrUrl)) {
            solrClient = initSolrClient();
        }
        
        return solrClient;
    }

    private SolrClient initSolrClient() {
      
      String[] solrUrls = solrUrl.split(",");
      LBSolrClient.Endpoint[] endpoints = new LBSolrClient.Endpoint[solrUrls.length];
      for (int i = 0; i < solrUrls.length; i++) {
        endpoints[i] = new LBSolrClient.Endpoint(solrUrls[i]); 
      }
      
      return new LBHttp2SolrClient.Builder(new Http2SolrClient.Builder().build(), endpoints).build(); 
    }

    private SolrClient initSolrCloudClient() {
        
        String[] solrZookeeperUrls = solrZookeeperUrl.trim().split(",");
        return new CloudHttp2SolrClient.Builder(Arrays.asList(solrZookeeperUrls))
            .withZkClientTimeout(solrTimeout, TimeUnit.MILLISECONDS)
            .withDefaultCollection(solrCollection)
            .build();
        
              
    }
}
