package poc.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;


@Slf4j
@SpringBootApplication
public class DeadLetterConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeadLetterConsumerApplication.class, args);
    }

    @KafkaListener(topics = "${deadletter-topic:counter-dlt}")
    public void readDeadLetter(ConsumerRecord<String,Long> record) {
        log.info("Read counter [{}] from dead letter topic [{}]",record.value(),record.topic());
    }
}
