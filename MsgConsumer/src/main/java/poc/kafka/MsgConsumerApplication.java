package poc.kafka;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.support.LogIfLevelEnabled;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@SpringBootApplication
public class MsgConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsgConsumerApplication.class, args);
    }

    @Value("${api.host:localhost}")
    private String apiHost;

    @Value("${api.port:8080}")
    private String apiPort;

    @Value("${api.failrate:50}")
    private int failrate;

    private WebClient webclient;

    @PostConstruct
    public void postConstruct() {
        //log.info(String.format("http://%s:%s/api",apiHost,apiPort));
        this.webclient = WebClient.builder()
                .baseUrl(String.format("http://%s:%s/api",apiHost,apiPort))
                .build();
    }

    @KafkaListener(topics = "${counter-topic:counter}", containerFactory = "myListenerContainerFactory", errorHandler = "myListenerErrorHandler")
    public void recvCounter(ConsumerRecord<String,Long> record) {//}, Acknowledgment ack) {
        log.info("receive [{}] from \"{}\" topic...",record.value(),record.topic());

        String result = webclient.post()
                .uri(builder -> builder//.host(apiHost).port(apiPort)
                            .path("/create")
                            .queryParam("counter", record.value())
                            .queryParam("failrate", failrate)
                            .build()
                )
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .bodyToMono(String.class)
                //.log()
                .block();

        log.info("call API success [{}]",result);
        //ack.acknowledge();
    }
}

@Slf4j
@Configuration
class FactoryConfig {

    @Bean
    public KafkaListenerErrorHandler myListenerErrorHandler() {
        return (msg,ex) -> {
            log.error("Exception \"{}\" captured by Listener Error Handler while processing message :{}",ex.getCause(),ex.getMessage());
            throw ex;
        };
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String,Long> myListenerContainerFactory(
            ConsumerFactory consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String,Long> factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory);
        //factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setCommitLogLevel(LogIfLevelEnabled.Level.DEBUG);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(500L, 5L)));
        return factory;
    }
}