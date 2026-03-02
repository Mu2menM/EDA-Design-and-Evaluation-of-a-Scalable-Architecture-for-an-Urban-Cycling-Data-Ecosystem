package at.hs.campus.wien.sde.urban_cycling_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class UrbanCyclingCoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(UrbanCyclingCoreApplication.class, args);
  }

}
