package io.github.olwimo;

import java.util.function.Function;

public class Recursive<I> {
    I rec;
    Recursive() { }
    public static <I>I make(Function<I, I> f) {
        Function<Recursive<I>, I> g = r -> f.apply(r.rec);
        Recursive<I> rec = new Recursive<>();
        return rec.rec = g.apply(rec);
    }
}
