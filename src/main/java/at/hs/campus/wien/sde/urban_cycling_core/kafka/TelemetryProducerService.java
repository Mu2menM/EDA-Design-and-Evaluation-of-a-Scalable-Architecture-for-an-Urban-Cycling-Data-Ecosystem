package at.hs.campus.wien.sde.urban_cycling_core.kafka;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import at.hs.campus.wien.sde.urban_cycling_core.dto.TelemetryRequest;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryProducerService {

  private final KafkaTemplate<String, TelemetryRequest> kafkaTemplate;
  private final MeterRegistry meterRegistry;

  @Value("${kafka.topic:cycling-telemetry}")
  private String topic;

  private Counter telemetryReceivedCounter;
  private Timer kafkaPublishTimer;

  @PostConstruct
  private void initMetrics() {
    telemetryReceivedCounter = Counter.builder("telemetry.received")
        .description("Number of telemetry requests received by API")
        .register(meterRegistry);

    kafkaPublishTimer = Timer.builder("telemetry.kafka.publish.time")
        .description("Time taken to publish to Kafka")
        .publishPercentiles(0.5, 0.95, 0.99)
        .publishPercentileHistogram()
        .sla(Duration.ofMillis(5), Duration.ofMillis(10), Duration.ofMillis(25),
            Duration.ofMillis(50), Duration.ofMillis(100))
        .register(meterRegistry);
  }

  public void publish(TelemetryRequest request) {
    telemetryReceivedCounter.increment();

    MDC.put("userId", request.getUserId().toString());
    MDC.put("timestamp", request.getTimestamp().toString());

    Timer.Sample sample = Timer.start(meterRegistry);

    try {
      long receptionTime = System.currentTimeMillis();

      Message<TelemetryRequest> message = MessageBuilder
          .withPayload(request)
          .setHeader(KafkaHeaders.TOPIC, topic)
          .setHeader(KafkaHeaders.KEY, UUID.randomUUID().toString())
          .setHeader("reception-time", receptionTime)
          .build();

      CompletableFuture<SendResult<String, TelemetryRequest>> future = kafkaTemplate.send(message);

      future.whenComplete((result, ex) -> {
        sample.stop(kafkaPublishTimer);
        if (ex == null) {
          log.info("Telemetry published successfully. topic={}, partition={}, offset={}", result.getRecordMetadata().topic(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
        } else {
          log.error("Failed to publish telemetry", ex);
        }
        MDC.clear();
      });

    } catch (Exception e) {
      log.error("Failed to build/publish telemetry request", e);
      MDC.clear();
    }
  }

  public List<UUID> publishBatch(List<TelemetryRequest> requests) {
    List<UUID> rideIds = new ArrayList<>();

    for (TelemetryRequest request : requests) {
      telemetryReceivedCounter.increment();
      MDC.put("userId", request.getUserId().toString());

      Timer.Sample sample = Timer.start(meterRegistry);

      try {
        long receptionTime = System.currentTimeMillis();

        Message<TelemetryRequest> message = MessageBuilder
            .withPayload(request)
            .setHeader(KafkaHeaders.TOPIC, topic)
            .setHeader(KafkaHeaders.KEY, UUID.randomUUID().toString())
            .setHeader("reception-time", receptionTime)
            .setHeader("batch-size", String.valueOf(requests.size()))
            .build();

        kafkaTemplate.send(message);
        sample.stop(kafkaPublishTimer);

      } catch (Exception e) {
        log.error("Failed to publish batch telemetry", e);
      } finally {
        MDC.clear();
      }
    }

    return rideIds; // Note: rideIds not known until consumer processes them
  }
}