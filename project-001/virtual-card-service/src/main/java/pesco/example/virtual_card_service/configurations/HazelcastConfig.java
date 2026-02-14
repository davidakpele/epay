package pesco.example.virtual_card_service.configurations;

import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.SetConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

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

        // Network and cluster settings
        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5701).setPortAutoIncrement(true); 

        JoinConfig join = network.getJoin();
        join.getMulticastConfig().setEnabled(false); 
        join.getTcpIpConfig()
            .setEnabled(true)
            .addMember("wallet-service") 
            .setConnectionTimeoutSeconds(5);

        return Hazelcast.newHazelcastInstance(config);
    }
}
