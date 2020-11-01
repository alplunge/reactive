package org.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

@Slf4j
public class FluxTest {
    String person;
    String person2;
    String person3;
    List<Integer> numbers;

    @BeforeEach
    public void init() {
        person = "John";
        person2 = "Denerys";
        person3 = "Sasuke";
        numbers = List.of(1, 2, 3, 4, 5);
    }

    @Test
    public void fluxSubscriber() {
        Flux<String> personasFlux = Flux.just(person, person2, person3).log();
        log.info("-----------------------------");
        StepVerifier.create(personasFlux).expectNext(person, person2, person3).verifyComplete();

    }

    @Test
    public void fluxSubscriberNumbers() {
        Flux<Integer> range = Flux.range(1, 10).log();

        range.limitRate(5).subscribe(integer -> log.info("And we have a number {} out of 10", integer));
        log.info("-----------------------------");
        StepVerifier.create(range).expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).verifyComplete();

    }

    @Test
    public void fluxSubscriberFromList() {
        Flux<Integer> fluxNumbers = Flux.fromIterable(numbers).log();

        fluxNumbers.subscribe(integer -> log.info("And we have a number {}", integer));
        log.info("-----------------------------");
        StepVerifier.create(fluxNumbers).expectNext(1, 2, 3, 4, 5).verifyComplete();

    }

    @Test
    public void fluxSubscriberNumbersOnError() {
        Flux<Integer> range = Flux.range(1, 10)
                .log()
                .map(n -> {
                    if (n == 4) {
                        throw new IndexOutOfBoundsException("Oh nasty nasty");
                    }
                    return n;
                });

        range.subscribe(integer -> log.info("And we have a number {} out of 10", integer), Throwable::printStackTrace, () -> log.info("DONE!"));
        log.info("-----------------------------");
        StepVerifier.create(range).expectNext(1, 2, 3).expectError(IndexOutOfBoundsException.class).verify();

    }

    @Test
    public void fluxSubscriberNumbersWithUglyBackPressure() {
        Flux<Integer> range = Flux.range(1, 10).log();

        range.subscribe(new Subscriber<>() {
            private int count = 0;
            private Subscription subscription;
            private final int requestCount = 2;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(requestCount);

            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    subscription.request(requestCount);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });
        log.info("-----------------------------");
        StepVerifier.create(range).expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).verifyComplete();

    }

    @Test
    public void fluxSubscriberNumbersWithBackPressure() {
        Flux<Integer> range = Flux.range(1, 10).log();

        range.subscribe(new BaseSubscriber<>() {
            private int count = 0;
            private final int requestCount = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    request(requestCount);
                }
            }

        });
        log.info("-----------------------------");
        StepVerifier.create(range).expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).verifyComplete();

    }

    @Test
    public void fluxSubscriberIntervalOne() throws InterruptedException {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100)).take(10).log();
        interval.subscribe(i -> log.info("Number {}", i));
        Thread.sleep(3000);
        log.info("-----------------------------");
    }

    @Test
    public void fluxSubscriberIntervalTwo() throws InterruptedException {
        StepVerifier.withVirtualTime(this::getInterval)
                .expectSubscription()
                .expectNoEvent(Duration.ofDays(1))
                .thenAwait(Duration.ofDays(1))
                .expectNext(0L)
                .thenAwait(Duration.ofDays(1))
                .expectNext(1L)
                .thenCancel()
                .verify();
    }

    private Flux<Long> getInterval() {
        return Flux.interval(Duration.ofDays(1)).log();
    }

    @Test
    public void connectableFlux() throws InterruptedException {
        ConnectableFlux<Integer> connectableFlux = Flux.range(1, 10)
                .log()
                .delayElements(Duration.ofMillis(100))
                .publish();

//        connectableFlux.connect();
//
//        log.info("Thread sleep 300ms");
//        Thread.sleep(300);
//
//        connectableFlux.subscribe(i -> log.info("Subscriber 1 with number {}", i));
//
//        log.info("Thread sleep 200ms");
//        Thread.sleep(200);
//
//        connectableFlux.subscribe(i -> log.info("Subscriber 2 with number {}", i));
        log.info("-----------------------------");
        StepVerifier
                .create(connectableFlux)
                .then(connectableFlux::connect)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .expectComplete()
                .verify();

    }

    @Test
    public void connectableFluxAutoConnect() {
        Flux<Integer> integerFlux = Flux.range(1, 10)
                .log()
                .delayElements(Duration.ofMillis(100))
                .publish()
                .autoConnect(2);
        log.info("-----------------------------");
        StepVerifier
                .create(integerFlux)
                .then(integerFlux::subscribe)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .expectComplete()
                .verify();

    }
}
