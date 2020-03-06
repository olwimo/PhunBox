package io.github.olwimo;

import javafx.util.Pair;

import java.util.function.Function;

public class State<S, A> {
    protected Function<S, Pair<A, S>> sf;

    public static <S, A>State<S, A> make(A a) {
        State<S, A> sa = new State<S, A>();
        sa.sf = s -> new Pair<A, S>(a, s);
        return sa;
    }

    public <B>State<S, B> bind(Function<A, State<S, B>> f) {
        State<S, B> sb = new State<S, B>();
        Function<S, Pair<A, S>> sf = this.sf;

        sb.sf = s -> {
            Pair<A, S> p = sf.apply(s);
            return f.apply(p.getKey()).sf.apply(p.getValue());
        };

        return sb;
    }

    public <B>State<S, B> then(State<S, B> sb) {
        return this.bind(a -> sb);
    }

    public <B>State<S, A> lthen(Function<A, State<S, B>> f) {
        Function<S, Pair<A, S>> sf = this.sf;
        return this.bind(a -> f.apply(a).then(make(a)));
    }

    public static <S>State<S, S> getState() {
        State<S, S> ss = new State<>();
        ss.sf = s -> new Pair<>(s, s);
        return ss;
    }

    public static <S>State<S, S> putState(S s) {
        State<S, S> ss = new State<>();
        ss.sf = s1 -> new Pair<>(s1, s);
        return ss;
    }

    public static <S>State<S, S> withState(Function<S, S> f) {
        State<S, S> ss = new State<>();
        ss.sf = s -> new Pair<>(s, f.apply(s));
        return ss;
    }
}
