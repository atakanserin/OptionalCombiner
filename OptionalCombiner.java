package org.example;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;

/**
 * @author Atakan Serin
 */
public class OptionalCombiner<T, R> {

    private static final OptionalCombiner<?, ?> BOTH_EMPTY = new OptionalCombiner<>(Optional.empty(), Optional.empty());

    private final Optional<T> oLeft;

    private final Optional<R> oRight;

    private OptionalCombiner(Optional<T> oLeft, Optional<R> oRight) {
        Objects.requireNonNull(oLeft);
        Objects.requireNonNull(oRight);
        this.oLeft = oLeft;
        this.oRight = oRight;
    }

    public static <T, R> OptionalCombiner<T, R> of(Optional<T> oLeft,
                                                   Optional<R> oRight) {
        return new OptionalCombiner<>(oLeft, oRight);
    }

    private boolean isLeftPresent() {
        return oLeft.isPresent();
    }

    private boolean isRightPresent() {
        return oRight.isPresent();
    }

    private boolean isOnlyLeftPresent() {
        return oLeft.isPresent() && oRight.isEmpty();
    }

    private boolean isOnlyRightPresent() {
        return oLeft.isEmpty() && oRight.isPresent();
    }

    private boolean isBothPresent() {
        return oLeft.isPresent() && oRight.isPresent();
    }

    private boolean isAnyPresent() {
        return oLeft.isPresent() || oRight.isPresent();
    }

    private boolean isLeftEmpty() {
        return oLeft.isEmpty();
    }

    private boolean isRightEmpty() {
        return oRight.isEmpty();
    }

    private boolean isOnlyLeftEmpty() {
        return isOnlyRightPresent();
    }

    private boolean isOnlyRightEmpty() {
        return isOnlyLeftPresent();
    }

    private boolean isBothEmpty() {
        return oLeft.isEmpty() && oRight.isEmpty();
    }

    private boolean isAnyEmpty() {
        return oLeft.isEmpty() || oRight.isEmpty();
    }

    public Optional<T> getLeft() {
        return oLeft;
    }

    public Optional<R> getRight() {
        return oRight;
    }

    public List<Optional<?>> get() {
        return List.of(oLeft, oRight);
    }

    public OptionalCombiner<T, R> filterLeft(Predicate<? super T> predicate) {
        return of(oLeft.filter(predicate), oRight);
    }

    public OptionalCombiner<T, R> filterRight(Predicate<? super R> predicate) {
        return of(oLeft, oRight.filter(predicate));
    }

    public OptionalCombiner<T, R> filter(BiPredicate<? super T, ? super R> biPredicate) {
        Objects.requireNonNull(biPredicate);
        if (isBothPresent()) {
            T valueLeft = oLeft.get();
            R valueRight = oRight.get();
            return biPredicate.test(valueLeft, valueRight)
                    ? this
                    : (OptionalCombiner<T, R>) BOTH_EMPTY;
        } else {
            return this;
        }
    }

    public <U> Optional<U> reduce(BiFunction<? super T, ? super R, ? extends U> reducer) {
        Objects.requireNonNull(reducer);
        if (isBothPresent()) {
            T valueLeft = oLeft.get();
            R valueRight = oRight.get();
            return Optional.of(reducer.apply(valueLeft, valueRight));
        } else
            return Optional.empty();
    }

    public <U> Optional<U> reduceIfBothEmpty(Optional<? super U> defaultOptional) {
        if (isBothEmpty()) {
            return (Optional<U>) defaultOptional;
        } else {
            return Optional.empty();
        }
    }

    public <U> Optional<U> reduceIfLeftPresent(Function<? super T, ? extends U> leftReducer) {
        Objects.requireNonNull(leftReducer);
        return isLeftPresent()
                ? Optional.ofNullable(leftReducer.apply(oLeft.get()))
                : Optional.empty();
    }

    public <U> Optional<U> reduceIfOnlyLeftPresent(Function<? super T, ? extends U> leftReducer) {
        Objects.requireNonNull(leftReducer);
        return isOnlyLeftPresent()
                ? Optional.ofNullable(leftReducer.apply(oLeft.get()))
                : Optional.empty();
    }

    public <U> Optional<U> reduceIfRightPresent(Function<? super R, ? extends U> rightReducer) {
        Objects.requireNonNull(rightReducer);
        return isRightPresent()
                ? Optional.ofNullable(rightReducer.apply(oRight.get()))
                : Optional.empty();
    }

    public <U> Optional<U> reduceIfOnlyRightPresent(Function<? super R, ? extends U> rightReducer) {
        Objects.requireNonNull(rightReducer);
        return isOnlyRightPresent()
                ? Optional.ofNullable(rightReducer.apply(oRight.get()))
                : Optional.empty();
    }

    public <U> Optional<U> flatReduce(BiFunction<? super T, ? super R, ? extends Optional<? extends U>> flatReducer) {
        Objects.requireNonNull(flatReducer);
        if (isBothPresent()) {
            T valueLeft = oLeft.get();
            R valueRight = oRight.get();
            return (Optional<U>) flatReducer.apply(valueLeft, valueRight);
        } else {
            return Optional.empty();
        }
    }

    public <U> Optional<U> flatReduceIfBothEmpty(U defaultValue) {
        if (isBothEmpty()) {
            return Optional.ofNullable(defaultValue);
        } else {
            return Optional.empty();
        }
    }

    public <U> Optional<U> flatReduceLeft(Function<? super T, ? extends Optional<? extends U>> flatReducer) {
        Objects.requireNonNull(flatReducer);
        if (isLeftPresent()) {
            T valueLeft = oLeft.get();
            return (Optional<U>) flatReducer.apply(valueLeft);
        } else {
            return Optional.empty();
        }
    }

    public <U> Optional<U> flatReduceIfOnlyLeftPresent(Function<? super T, ? extends Optional<? extends U>> flatReducer) {
        Objects.requireNonNull(flatReducer);
        if (isOnlyLeftPresent()) {
            T valueLeft = oLeft.get();
            return (Optional<U>) flatReducer.apply(valueLeft);
        } else {
            return Optional.empty();
        }
    }

    public <U> Optional<U> flatReduceRight(Function<? super R, ? extends Optional<? extends U>> flatReducer) {
        Objects.requireNonNull(flatReducer);
        if (isRightPresent()) {
            R valueRight = oRight.get();
            return (Optional<U>) flatReducer.apply(valueRight);
        } else {
            return Optional.empty();
        }
    }

    public <U> Optional<U> flatReduceIfOnlyRightPresent(Function<? super R, ? extends Optional<? extends U>> flatReducer) {
        Objects.requireNonNull(flatReducer);
        if (isOnlyRightPresent()) {
            R valueRight = oRight.get();
            return (Optional<U>) flatReducer.apply(valueRight);
        } else {
            return Optional.empty();
        }
    }

    public <U, D> OptionalCombiner<? extends U, ? extends D> map(BiFunction<? super T, ? super R, ? extends OptionalCombiner<? extends U, ? extends D>> mapper) {
        Objects.requireNonNull(mapper);
        if (isBothPresent()) {
            T valueLeft = oLeft.get();
            R valueRight = oRight.get();
            return Objects.requireNonNull(mapper.apply(valueLeft, valueRight));
        } else {
            return (OptionalCombiner<U, D>) BOTH_EMPTY;
        }
    }

    public OptionalCombiner<T, R> mapIfBothEmpty(OptionalCombiner<? super T, ? super R> defaultOptionalCombiner) {
        if (isBothEmpty()) {
            return (OptionalCombiner<T, R>) defaultOptionalCombiner;
        } else {
            return this;
        }
    }

    public <U> OptionalCombiner<U, R> mapLeft(Function<? super T, ? extends U> leftMapper) {
        return new OptionalCombiner<>(oLeft.map(leftMapper), oRight);
    }

    public <U> OptionalCombiner<U, R> mapIfOnlyLeftPresent(Function<? super T, ? extends U> leftMapper) {
        return isOnlyLeftPresent()
                ? mapLeft(leftMapper)
                : OptionalCombiner.of(Optional.empty(), oRight);
    }

    public <U> OptionalCombiner<T, U> mapRight(Function<? super R, ? extends U> rightMapper) {
        return new OptionalCombiner<>(oLeft, oRight.map(rightMapper));
    }

    public <U> OptionalCombiner<T, U> mapIfOnlyRightPresent(Function<? super R, ? extends U> rightMapper) {
        return isOnlyRightPresent()
                ? mapRight(rightMapper)
                : OptionalCombiner.of(oLeft, Optional.empty());
    }

    public OptionalCombiner<T, R> run(Runnable action) {
        Objects.requireNonNull(action);
        action.run();
        return this;
    }

    private OptionalCombiner<T, R> runIf(boolean condition, Runnable action) {
        Objects.requireNonNull(action);
        if (condition) action.run();
        return this;
    }

    public OptionalCombiner<T, R> runIfLeftPresent(Runnable action) {
        return runIf(isLeftPresent(), action);
    }

    public OptionalCombiner<T, R> runIfOnlyLeftPresent(Runnable action) {
        return runIf(isOnlyLeftPresent(), action);
    }

    public OptionalCombiner<T, R> runIfRightPresent(Runnable action) {
        return runIf(isRightPresent(), action);
    }

    public OptionalCombiner<T, R> runIfOnlyRightPresent(Runnable action) {
        return runIf(isOnlyRightPresent(), action);
    }

    public OptionalCombiner<T, R> runIfLeftEmpty(Runnable action) {
        return runIf(isLeftEmpty(), action);
    }

    public OptionalCombiner<T, R> runIfOnlyLeftEmpty(Runnable action) {
        return runIf(isOnlyLeftEmpty(), action);
    }

    public OptionalCombiner<T, R> runIfRightEmpty(Runnable action) {
        return runIf(isRightEmpty(), action);
    }

    public OptionalCombiner<T, R> runIfOnlyRightEmpty(Runnable action) {
        return runIf(isOnlyRightEmpty(), action);
    }

    public <U> CompletableFuture<U> thenCombine(BiFunction<? super T, ? super R, ? extends U> biFunction) {
        if (isBothPresent())
            return CompletableFuture.completedFuture(oLeft.get())
                    .thenCombine(CompletableFuture.completedFuture(oRight.get()), biFunction);
        else
            return CompletableFuture.completedFuture(null);
    }

    public <U> CompletableFuture<U> thenCombineAsync(BiFunction<? super T, ? super R, ? extends U> biFunction) {
        if (isBothPresent())
            return CompletableFuture.supplyAsync(() -> oLeft.get())
                    .thenCombine(CompletableFuture.completedFuture(oRight.get()), biFunction);
        else
            return CompletableFuture.completedFuture(null);
    }

    public <U> CompletableFuture<U> thenApplyLeft(Function<? super T, ? extends U> function) {
        if (isLeftPresent())
            return CompletableFuture.completedFuture(oLeft.get())
                    .thenApply(function);
        else
            return CompletableFuture.completedFuture(null);
    }

    public <U> CompletableFuture<U> thenApplyIfOnlyLeftPresent(Function<? super T, ? extends U> function) {
        if (isOnlyLeftPresent())
            return CompletableFuture.completedFuture(oLeft.get())
                    .thenApply(function);
        else
            return CompletableFuture.completedFuture(null);
    }

    public <U> CompletableFuture<U> thenApplyAsyncLeft(Function<? super T, ? extends U> function) {
        if (isLeftPresent())
            return CompletableFuture.supplyAsync(() -> oLeft.get())
                    .thenApply(function);
        else
            return CompletableFuture.completedFuture(null);
    }

    public <U> CompletableFuture<U> thenApplyAsyncIfOnlyLeftPresent(Function<? super T, ? extends U> function) {
        if (isOnlyLeftPresent())
            return CompletableFuture.supplyAsync(() -> oLeft.get())
                    .thenApply(function);
        else
            return CompletableFuture.completedFuture(null);
    }

    public <U> CompletableFuture<U> thenApplyRight(Function<? super R, ? extends U> function) {
        if (isRightPresent())
            return CompletableFuture.completedFuture(oRight.get())
                    .thenApply(function);
        else
            return CompletableFuture.completedFuture(null);
    }

    public <U> CompletableFuture<U> thenApplyIfOnlyRightPresent(Function<? super R, ? extends U> function) {
        if (isOnlyRightPresent())
            return CompletableFuture.completedFuture(oRight.get())
                    .thenApply(function);
        else
            return CompletableFuture.completedFuture(null);
    }

    public <U> CompletableFuture<U> thenApplyAsyncRight(Function<? super R, ? extends U> function) {
        if (isRightPresent())
            return CompletableFuture.supplyAsync(() -> oRight.get())
                    .thenApply(function);
        else
            return CompletableFuture.completedFuture(null);
    }

    public <U> CompletableFuture<U> thenApplyAsyncIfOnlyRightPresent(Function<? super R, ? extends U> function) {
        if (isOnlyRightPresent())
            return CompletableFuture.supplyAsync(() -> oRight.get())
                    .thenApply(function);
        else
            return CompletableFuture.completedFuture(null);
    }

    public OptionalCombiner<R, T> swap() {
        return new OptionalCombiner<>(oRight, oLeft);
    }
}

