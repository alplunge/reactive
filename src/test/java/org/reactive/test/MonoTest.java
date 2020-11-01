package org.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
public class MonoTest {
    String name;

    @BeforeEach
    public void init() {
        name = "Uchiha Itachi";
    }

    @Test
    public void monoSubscriber() {
        Mono<String> mono = Mono.just(name).log();
        mono.subscribe();
        log.info("-----------------------------");
        StepVerifier.create(mono).expectNext(name).verifyComplete();

    }

    @Test
    public void monoSubscriberConsumer() {
        Mono<String> mono = Mono.just(name).log();
        mono.subscribe(s -> log.info("Value {}", s));
        log.info("-----------------------------");
        StepVerifier.create(mono).expectNext(name).verifyComplete();

    }

    @Test
    public void monoSubscriberConsumerError() {
        Mono<String> mono = Mono.just(name).log().map(s -> {
            throw new RuntimeException();
        });
        mono.subscribe(s -> log.info("Value {}", s), s -> log.error("Scary"));
        log.info("-----------------------------");
        StepVerifier.create(mono).expectError(RuntimeException.class).verify();

    }

    @Test
    public void monoSubscriberConsumerComplete() {
        Mono<String> mono = Mono.just(name).log().map(String::toUpperCase);
        mono.subscribe(s -> log.info("Value {}", s), Throwable::printStackTrace, () -> log.info("FINISHED"));
        log.info("-----------------------------");
        StepVerifier.create(mono).expectNext(name.toUpperCase()).verifyComplete();

    }

    @Test
    public void monoSubscriberConsumerSubscription() {
        Mono<String> mono = Mono.just(name).log().map(String::toUpperCase);
        mono.subscribe(s -> log.info("Value {}", s), Throwable::printStackTrace, () -> log.info("FINISHED"), Subscription::cancel);
        log.info("-----------------------------");
        StepVerifier.create(mono).expectNext(name.toUpperCase()).verifyComplete();

    }
}
