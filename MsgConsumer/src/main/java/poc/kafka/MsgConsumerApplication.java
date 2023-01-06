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
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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
    public void recvCounter(ConsumerRecord<String,Long> record, Acknowledgment ack) {
        log.info("receive [{}] from \"{}\" topic...",record.value(),record.topic());

        //

        AtomicBoolean callHasError = new AtomicBoolean(false);

        /*
        webclient.post()
                .uri(builder -> {
                    return builder//.host(apiHost).port(apiPort)
                            .path("/create")
                            .queryParam("counter", record.value())
                            .queryParam("failrate", 100)//failrate)
                            .build();
                })
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> {
                    log.error("call API error",e);
                    //log.error("call API error [{}:{}]",e.getStatusCode(),e.getStatusText());
                    throw new RuntimeException("fall back!");
                })
                //.log()
                .subscribe(s -> {
                    log.info("call API success [{}]",s);
                    ack.acknowledge();
                });
         */
        throw new RuntimeException("fall back2!");
    }
}

@Slf4j
@Configuration
class FactoryConfig {

    @Bean
    public KafkaListenerErrorHandler myListenerErrorHandler() {
        return (msg,ex) -> {
            log.error("Here");
            throw ex;
        };
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String,Long> myListenerContainerFactory(
            ConsumerFactory consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String,Long> factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setClientId("haha-hehe-1");
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 5L)));
        return factory;
    }
}