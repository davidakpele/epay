package com.example.auth_user_service.configurations;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.SetConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

@Configuration
public class HazelcastConfig {
    
    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.setClusterName("wallet-service-cluster");
        config.setInstanceName("wallet-service-instance");
        
        // Configure map for user sessions (24 hours TTL)
        MapConfig userSessionsMapConfig = new MapConfig("user-sessions");
        userSessionsMapConfig.setTimeToLiveSeconds((int) TimeUnit.HOURS.toSeconds(24));
        config.addMapConfig(userSessionsMapConfig);
        
        // Configure set for session indexes
        SetConfig sessionSetConfig = new SetConfig("user_sessions:*");
        config.addSetConfig(sessionSetConfig);
        
        return Hazelcast.newHazelcastInstance(config);
    }
}