package pesco.example.withdraw_service.configurations;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {
    
    private static final String IDEMPOTENCY_MAP = "idempotency-store";
    private static final long COMPLETED_TTL_MINUTES = 24 * 60; // 24 hours
    
    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.setClusterName("withdraw-service-cluster");
        config.setInstanceName("withdraw-service-instance");
        
        // Configure map for idempotency with TTL
        MapConfig mapConfig = new MapConfig(IDEMPOTENCY_MAP);
        mapConfig.setTimeToLiveSeconds((int) TimeUnit.MINUTES.toSeconds(COMPLETED_TTL_MINUTES));
        config.addMapConfig(mapConfig);
        
        return Hazelcast.newHazelcastInstance(config);
    }
}
