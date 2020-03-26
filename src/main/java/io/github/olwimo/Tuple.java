package io.github.olwimo;

public class Tuple<A, B> {
    A a;
    B b;
    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }
    public A fst() {
        return this.a;
    }
    public B snd() {
        return this.b;
    }
}
