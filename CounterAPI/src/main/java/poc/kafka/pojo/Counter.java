package poc.kafka.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Counter {
    private long counter;
    private LocalDateTime crtTimestamp;
}
