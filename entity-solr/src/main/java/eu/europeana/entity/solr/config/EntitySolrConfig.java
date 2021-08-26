package eu.europeana.entity.solr.config;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({ @PropertySource(value = "classpath:entity.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "classpath:entity-solr.properties", ignoreResourceNotFound = true) })
public class EntitySolrConfig {

    public static final String ENTITY_SOLR_CLIENT = "entitySolrClient";
    public static final String ENTITY_SOLR_SERVICE = "entitySolrService";
    
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
    
    @Bean(ENTITY_SOLR_CLIENT)
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
        HttpSolrClient.Builder builder = new HttpSolrClient.Builder();
        return builder.withBaseSolrUrl(solrUrl).withConnectionTimeout(solrTimeout).build();
    }

    private SolrClient initSolrCloudClient() {
        String[] solrZookeeperUrls = solrZookeeperUrl.trim().split(",");
        CloudSolrClient.Builder builder = new CloudSolrClient.Builder(Arrays.asList(solrZookeeperUrls));
        builder.withConnectionTimeout(solrTimeout); 
        
        CloudSolrClient client =  builder.build();
        client.setDefaultCollection(solrCollection);
        return client;      
    }
}
