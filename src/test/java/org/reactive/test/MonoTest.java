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

    @Test
    public void monoDoOnOperators() {
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(number -> log.info("Request received, with the back-pressure of {}", number))
                .doOnNext(s -> log.info("Executing donOnNext with the received value from publisher's onNext method {}", s))
                .doOnSuccess(s -> log.info("will only work when value successfully retrieved from publisher, the value is {}", s));

        mono.subscribe(s -> log.info("Value {}", s), Throwable::printStackTrace, () -> log.info("FINISHED"));

    }

    @Test
    public void monoOnError() {
        Mono<Object> error = Mono.error(new IllegalArgumentException("I am illegal error!"))
                .doOnError(throwable -> MonoTest.log.error("Error message {}", throwable.getMessage()))
                .doOnNext(o -> log.info("I am not going to be executed, error happened"))
                .log();

        log.info("-----------------------------");
        StepVerifier.create(error).expectError(IllegalArgumentException.class).verify();

    }

    @Test
    public void monoOnErrorResume() {
        Mono<Object> error = Mono.error(new IllegalArgumentException("I am illegal error!"))
                .doOnError(throwable -> MonoTest.log.error("Error message {}", throwable.getMessage()))
                .onErrorResume(o -> {
                    log.info("Inside of an error resume operator");
                    return Mono.just(name);
                })
                .doOnNext(s -> log.info("Recovered from an error with a new mono, which is {}", s))
                .log();

        log.info("-----------------------------");
        StepVerifier.create(error).expectNext(name).verifyComplete();

    }

    @Test
    public void monoOnErrorReturn() {
        Mono<Object> error = Mono.error(new IllegalArgumentException("I am illegal error!"))
                .onErrorReturn("EMPTY STRING")
                .onErrorResume(o -> {
                    log.info("I am ignored, why? Because onErrorReturn operator has made it first");
                    return Mono.just(name);
                })
                .doOnNext(s -> log.info("Recovered from an error with a new mono, which is {}", s))
                .log();

        log.info("-----------------------------");
        StepVerifier.create(error).expectNext("EMPTY STRING").verifyComplete();

    }
}
