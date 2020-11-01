package org.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
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
        Flux<Integer> numbers = Flux.range(1, 10).log();

        numbers.limitRate(5).subscribe(integer -> log.info("And we have a number {} out of 10", integer));
        log.info("-----------------------------");
        StepVerifier.create(numbers).expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).verifyComplete();

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
        Flux<Integer> numbers = Flux.range(1, 10).log().map(n -> {
            if (n == 4) {
                throw new IndexOutOfBoundsException("Oh nasty nasty");
            }
            return n;
        });

        numbers.subscribe(integer -> log.info("And we have a number {} out of 10", integer), Throwable::printStackTrace, () -> log.info("DONE!"));
        log.info("-----------------------------");
        StepVerifier.create(numbers).expectNext(1, 2, 3).expectError(IndexOutOfBoundsException.class).verify();

    }

    @Test
    public void fluxSubscriberNumbersWithUglyBackPressure() {
        Flux<Integer> numbers = Flux.range(1, 10).log();

        numbers.subscribe(new Subscriber<>() {
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
        StepVerifier.create(numbers).expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).verifyComplete();

    }

    @Test
    public void fluxSubscriberNumbersWithBackPressure() {
        Flux<Integer> numbers = Flux.range(1, 10).log();

        numbers.subscribe(new BaseSubscriber<>() {
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
        StepVerifier.create(numbers).expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).verifyComplete();

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
}
