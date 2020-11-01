package org.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class OperatorsTest {

    @Test
    public void subscribeOnSimple() {
        Flux<Integer> range = Flux.range(1, 4)
                .map(i -> {
                    log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                }).log();
        log.info("-----------------------------");
        StepVerifier.create(range)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void publishOnSimple() {
        Flux<Integer> range = Flux.range(1, 4)
                .map(i -> {
                    log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                }).log();
        log.info("-----------------------------");
        StepVerifier.create(range)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void multipleSubscribeOnSimple() {
        Flux<Integer> range = Flux.range(1, 4)
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                }).log();
        log.info("-----------------------------");
        StepVerifier.create(range)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void multiplePublishOnSimple() {
        Flux<Integer> range = Flux.range(1, 4)
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                }).log();
        log.info("-----------------------------");
        StepVerifier.create(range)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void publishOnAndSubscribeOnSimple() {
        Flux<Integer> range = Flux.range(1, 4)
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                }).log();
        log.info("-----------------------------");
        StepVerifier.create(range)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void subscribeOnAndPublishOnSimple() {
        Flux<Integer> range = Flux.range(1, 4)
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                }).log();
        log.info("-----------------------------");
        StepVerifier.create(range)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void subscribeOnIO() {
        Mono<List<String>> textLines = Mono.fromCallable(() -> Files.readAllLines(Path.of("text")))
                .log()
                .subscribeOn(Schedulers.boundedElastic());
        log.info("-----------------------------");
        StepVerifier.create(textLines)
                .expectSubscription()
                .thenConsumeWhile(lines -> {
                    Assertions.assertFalse(lines.isEmpty());
                    log.info("Size {}", lines);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void switchIfEmptyOperator() {
        Flux<Object> empty = Flux.empty()
                .switchIfEmpty(Flux.just("Switched to non empty flux"))
                .log();
        log.info("-----------------------------");
        StepVerifier.create(empty)
                .expectSubscription()
                .expectNext("Switched to non empty flux")
                .expectComplete()
                .verify();
    }

    @Test
    public void deferOperator() throws InterruptedException {
        Mono<Long> just = Mono.just(System.currentTimeMillis());
        Mono<Long> defer = Mono.defer(() -> Mono.just(System.currentTimeMillis()));

        defer.subscribe(aLong -> log.info("tine {}", aLong));
        Thread.sleep(100);
        defer.subscribe(aLong -> log.info("tine {}", aLong));
        Thread.sleep(100);
        defer.subscribe(aLong -> log.info("tine {}", aLong));
        Thread.sleep(100);
        defer.subscribe(aLong -> log.info("tine {}", aLong));

        AtomicLong atomicLong = new AtomicLong();
        defer.subscribe(atomicLong::set);
        Thread.sleep(100);
        defer.subscribe(atomicLong::set);
        Assertions.assertTrue(atomicLong.get() > 0);
    }
}
