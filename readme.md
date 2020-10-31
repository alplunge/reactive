
# Reactive streams general rules
 ### 1. Asynchronous
 ###2. Non-Blocking
 ###3. Back-pressure
 ###Publisher <- (subscribe) Subscriber
 ###Subscription is created
 ###Publisher (onSubscribe with the subscription) -> Subscriber
 ###Subscription <- (request N) Subscriber
 ###Publisher -> (onNext) Subscriber
 ###until:
 ####1. Publisher sends all requested objects --> Publisher has 10 available object, but subscriber requested 5, so 5 will be sent --> NOT MORE
 ####2. Publisher sends all the objects it has (onComplete) subscriber and the subscription will be canceled
 ####3. There is an error. (onError) subscriber and the subscription will be canceled
