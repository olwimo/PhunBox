package io.github.olwimo;

public class Holder<A> {
    A a;
    public Holder(A a) {
        this.a = a;
    }
    public Holder() {
        this.a = null;
    }
    public A get() { return a; }
    public void set(A a) { this.a = a; }
}
