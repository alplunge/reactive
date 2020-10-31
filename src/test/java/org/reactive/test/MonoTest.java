package org.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
public class MonoTest {

    @Test
    public void monoSubscriber() {
        String name = "Uchiha Itachi";
        Mono<String> mono = Mono.just(name).log();
        mono.subscribe();
        log.info("-----------------------------");
        StepVerifier.create(mono).expectNext(name).verifyComplete();

    }

    @Test
    public void monoSubscriberConsumer() {
        String name = "Uchiha Sasuke";
        Mono<String> mono = Mono.just(name).log();
        mono.subscribe(s -> log.info("Value {}", s));
        log.info("-----------------------------");
        StepVerifier.create(mono).expectNext(name).verifyComplete();

    }

    @Test
    public void monoSubscriberConsumerError() {
        String name = "Uchiha Buraku";
        Mono<String> mono = Mono.just(name).log().map(s -> {
            throw new RuntimeException();
        });
        mono.subscribe(s -> log.info("Value {}", s), s -> log.error("Scary"));
        log.info("-----------------------------");
        StepVerifier.create(mono).expectError(RuntimeException.class).verify();

    }
}
