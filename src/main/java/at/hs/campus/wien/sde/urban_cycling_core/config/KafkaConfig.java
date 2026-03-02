package at.hs.campus.wien.sde.urban_cycling_core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

  @Value("${kafka.topic:cycling-telemetry}")
  private String topicName;

//  @Bean
//  public NewTopic telemetryTopic() {
//    return TopicBuilder.name(topicName)
//        .partitions(3)
//        .replicas(1)
//        .build();
//  }
}