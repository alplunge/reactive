
# Reactive streams general rules
 ### 1. Asynchronous
 ### 2. Non-Blocking
 ### 3. Back-pressure
 ### Publisher <- (subscribe) Subscriber
 ### Subscription is created
 ### Publisher (onSubscribe with the subscription) -> Subscriber
 ### Subscription <- (request N) Subscriber
 ### Publisher -> (onNext) Subscriber until:
 #### 1. Publisher sends all requested objects --> Publisher has 10 available object, but subscriber requested 5, so 5 will be sent --> NOT MORE
 #### 2. Publisher sends all the objects it has (onComplete) subscriber and the subscription will be canceled
 #### 3. There is an error. (onError) subscriber and the subscription will be canceled
 
 # Added BlockHound
 ## What it is and how it works?
 BlockHound will transparently instrument the JVM classes and intercept blocking calls (e.g. IO) if they are performed from threads marked as "non-blocking operations only" (ie. threads implementing Reactor's `NonBlocking` marker interface, like those started by `Schedulers.parallel()`). If and when this happens (but remember, this should never happen!:stuck_out_tongue_winking_eye:), an error will be thrown.
 ## [More Info HERE](https://github.com/reactor/BlockHound)
 
