It can be the case that code components can return more than one Optionals to be processed together. 
Code can easily get too nested with if else statements or using Optional.flatMap to combine two Optionals.
This library is meant to improve the readability by providing a fluent API on operating with two Optionals at the same time.

There are many attemps to implement alike libraries such as Guava BiOptional library or QuarterBukkit BiOptional. However they
do not have factory methods with Optionals as parameters. They just take in nonOptional values which makes the use case different compared to CombinedOptionals.

The closest implementation to this library is Tomas Linkowski's answer here on: https://stackoverflow.com/questions/51847513/calling-different-methods-based-on-values-of-two-optionals.
But OptionalCombiner transforms it and adds key ideas like combined reduction to Optional class and combined filtering. OptionalCombiner also has some utilities for transofrming the computation to CompletableFutures.

Sample Usage:
```java
Optional<Integer> creditScoreSourceA = sourceAService.fetchCreditScore();
Optional<Integer> creditScoreSourceB = sourceBService.fetchCreditScore();

OptionalCombiner.of(creditScoreSourceA, creditScoreSourceB)
        .filterLeft(Util::isValidCreditScore)
        .filterRight(Util::isValidCreditScore)
        .mapLeft(Util::adjustCreditScore)
        .mapRight(Util::adjustCreditScore)
        .reduce(Util::combineCreditScores)
        .ifPresentOrElse(
                score -> mqttService.fireSuccessEvent(score),
                () -> mqttService.sendFailureEvent(CREDIT_SOURCES_INCOMPLETE));
```
Same code is usually written using nested if elses which clutters and nests the code in my opinion:
```java
Optional<Integer> creditScoreSourceA = sourceAService.fetchCreditScore();
Optional<Integer> creditScoreSourceB = sourceBService.fetchCreditScore();

if (creditScoreSourceA.isPresent() && creditScoreSourceB.isPresent()) {
    Integer scoreA = creditScoreSourceA.get();
    Integer scoreB = creditScoreSourceB.get();
    
    if (Util.isValidCreditScore(scoreA) && Util.isValidCreditScore(scoreB)) {
        Integer adjustedScoreA = Util.adjustCreditScore(scoreA);
        Integer adjustedScoreB = Util.adjustCreditScore(scoreB);
        
        Integer combinedScore = Util.combineCreditScores(adjustedScoreA, adjustedScoreB);
        mqttService.fireSuccessEvent(combinedScore);
    } else {
        mqttService.sendFailureEvent(INCOMPLETE_CREDIT_SOURCES);
    }
} else {
    mqttService.sendFailureEvent(INCOMPLETE_CREDIT_SOURCES);
}
```

Optional flatMap method can be used to combine Optionals also. Better readability compared to if else case, but still not on par with CombinedOptionals in my opinion.

```java
Optional<Integer> creditScoreSourceA = sourceAService.fetchCreditScore();
Optional<Integer> creditScoreSourceB = sourceBService.fetchCreditScore();

creditScoreSourceA
    .filter(Util::isValidCreditScore)
    .map(Util::adjustCreditScore)
    .flatMap(adjustedScoreA -> 
        creditScoreSourceB
            .filter(Util::isValidCreditScore)
            .map(Util::adjustCreditScore)
            .map(adjustedScoreB -> Util.combineCreditScores(adjustedScoreA, adjustedScoreB))
    )
    .ifPresentOrElse(
        combinedScore -> mqttService.fireSuccessEvent(combinedScore),
        () -> mqttService.sendFailureEvent()
    );
```

