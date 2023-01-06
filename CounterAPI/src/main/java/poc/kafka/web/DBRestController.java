package poc.kafka.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.web.bind.annotation.*;
import poc.kafka.pojo.Counter;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class DBRestController {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public DBRestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("counter")
                .usingColumns("counter");
    }

    @PostMapping("/create")
    public ResponseEntity<String> writeIntToDB(
            @RequestParam long counter,
            @RequestParam(defaultValue = "50") int failrate) {
        if (Math.random()*100>failrate) {
            jdbcInsert.execute(Map.of("counter",counter));
            log.info("counter [{}] saved into DB successfully", counter);
            return ResponseEntity.ok().body("record saved!");
        } else {
            log.error("counter [{}] failed to save into DB, 502 return to client", counter);
            return ResponseEntity.internalServerError().body("failed");
        }
    }

    @GetMapping("/topn")
    public Flux<Counter> viewTopN(
            @RequestParam(defaultValue = "100") int topn,
            @RequestParam(defaultValue = "crt_timestamp") String sort) {
        List<Counter> list = jdbcTemplate.query(
                "select * from counter order by crt_timestamp desc limit ?",
                new Object[] {topn},
                (rs,rowNum) -> {
                    Counter record = new Counter();
                    record.setCounter(rs.getLong("counter"));
                    record.setCrtTimestamp(rs.getTimestamp("crt_timestamp").toLocalDateTime());
                    return record;
                });
        return Flux.fromIterable(list);
    }
}
