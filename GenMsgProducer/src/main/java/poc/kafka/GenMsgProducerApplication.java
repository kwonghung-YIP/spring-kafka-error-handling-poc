package poc.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@SpringBootApplication
public class GenMsgProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenMsgProducerApplication.class, args);
    }

    @Value("${counter-topic:counter}")
    private String counterTopic;

    @Value("${message-interval:5000}")
    private long msgInterval;

    @Bean
    public NewTopic counterTopic() {
        return TopicBuilder.name(counterTopic)
                .partitions(6)
                .build();
    }

    @Bean
    public ApplicationRunner genMessage(KafkaTemplate<String,Long> kafkaTemplate) {
        return args -> {
            long counter = 1;
            while (true) {
                log.info("Sending [{}] to \"{}\" topic...",counter,counterTopic);
                var key = UUID.randomUUID().toString();
                CompletableFuture<SendResult<String,Long>> future = kafkaTemplate.send("counter",key,counter++);
                future.whenComplete((result,except) -> {
                   if (except==null) {
                       log.info("sent successfully");
                   } else {
                       log.error("sent error!");
                   }
                });
                Thread.sleep(msgInterval);
            }
        };
    }
}
