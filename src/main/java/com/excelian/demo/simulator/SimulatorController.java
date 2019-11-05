package com.excelian.demo.simulator;

import com.excelian.demo.common.data.Order;
import com.excelian.demo.simulator.Util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@EnableScheduling
@Slf4j
public class SimulatorController {

    private WebClient webClient = WebClient.create("http://localhost:8090");

    private AtomicBoolean alreadyStarted = new AtomicBoolean(false);

    @Autowired
    TaskScheduler taskScheduler;

    ScheduledFuture<?> scheduledFuture;

    @GetMapping("/start")
    public Mono<ResponseEntity<String>> start(@RequestParam("rate") int rate) {
        if (!alreadyStarted.getAndSet(true)) {
            scheduledFuture = taskScheduler.scheduleAtFixedRate(sendingOrder(), rate);
            return Mono.just("started")
                    .map(s -> ResponseEntity.status(HttpStatus.OK).body(s));
        } else {
            return Mono.just("already started, please stop first")
                    .map(s -> ResponseEntity.status(HttpStatus.OK).body(s));
        }
    }

    @GetMapping("/stop")
    public Mono<ResponseEntity<String>> stop() {
        if (alreadyStarted.getAndSet(false)) {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }
            return Mono.just("stopped").map(s -> ResponseEntity.status(HttpStatus.OK).body(s));
        } else {
            return Mono.just("already stopped").map(s -> ResponseEntity.status(HttpStatus.OK).body(s));
        }
    }

    private Runnable sendingOrder() {
        return () -> {
            String productId = RandomUtil.generateRandomString(12);
            int volume = RandomUtil.generateRandomInteger(1, 10);
            double price = RandomUtil.generateRandomDouble(50, 1000);
            Order order = new Order(productId, volume, price);
            webClient.method(HttpMethod.POST)
                    .uri("/api/v1/order")
                    .body(BodyInserters.fromPublisher(Mono.just(order), Order.class))
                    .exchange()
                    .block()
                    .bodyToMono(Order.class)
                    .subscribe(o -> log.info("Response order {}", o));
        };
    }

}