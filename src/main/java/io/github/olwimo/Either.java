package io.github.olwimo;

import java.util.Optional;
import java.util.function.Function;

public abstract class Either<E, A> {
    public static final class Right<E, A> extends Either<E, A> {
        public A a;
        public Right(A a) {
            this.a = a;
        }
        public <B>Either<E, B> bind(Function<A, Either<E, B>>f) {
            return f.apply(a);
        }
        public Optional<A> val() {
            return Optional.of(a);
        }
        public Optional<E> err() {
            return Optional.empty();
        }
    }
    public static final class Left<E, A> extends Either<E, A> {
        public E e;
        public Left(E e) {
            this.e = e;
        }
        public <B>Either<E, B> bind(Function<A, Either<E, B>>f) {
            return new Left<>(e);
        }
        public Optional<A> val() {
            return Optional.empty();
        }
        public Optional<E> err() {
            return Optional.of(e);
        }
    }
    protected static class None extends Either {
        public Either bind(Function f) {
            return this;
        }
        public Optional<?> val() {
            return Optional.empty();
        }
        public Optional<?> err() {
            return Optional.empty();
        }
    }
//    protected static Either.None none = new None();
    public abstract Optional<A> val();
    public abstract Optional<E> err();
    public abstract <B>Either<E, B> bind(Function<A, Either<E, B>>f);
    public static <E, A>Either<E, A> make(A a) {
        return new Right<>(a);
    }
    public static <E, A>Either<E, A> fail(E e) {
        return new Left<>(e);
    }
    public <B> Either<E, B> map(Function<A, B> f) {
        return this.bind(a -> Either.<E, B>make(f.apply(a)));
    }
/*    public A orElse(A a) {
        return val().orElse(a);
    }*/

    public <B>Either<E, B> then(Either<E, B> eb) {
        return this.bind(a -> eb);
    }

    public <B>Either<E, A> lbind(Function<A, Either<E, B>> f) {
        return this.bind(a -> f.apply(a).then(Either.make(a)));
    }

    public <B> Either<E, A> lthen(Either<E, B> eb) {
        return lbind(a -> eb);
    }
}
