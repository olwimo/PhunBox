package io.github.olwimo;

import javafx.util.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser<S, E, A> {
    protected Function<S, Stream<Either<E, Pair<A, S>>>> pf;

    public Pair<Stream<E>, Stream<Pair<A, S>>> runParser(S s) {
        Stream<Either<E, Pair<A, S>>> sa = pf.apply(s);
        return new Pair<Stream<E>, Stream<Pair<A, S>>>(sa.flatMap(ea -> {
            List<E> le = new LinkedList<E>();
            Optional<E> e = ea.err();
            if (e.isPresent()) le.add(e.get());
            return le.stream();
        }), sa.flatMap(ea -> {
            List<Pair<A, S>> la = new LinkedList<Pair<A, S>>();
            Optional<Pair<A, S>> a = ea.val();
            if (a.isPresent()) la.add(a.get());
            return la.stream();
        }));
    }

    public static <S, E, A> Parser<S, E, A> make(A a) {
        Parser<S, E, A> pa = new Parser<S, E, A>();
        pa.pf = s -> {
            List<Either<E, Pair<A, S>>> lep = new LinkedList<Either<E, Pair<A, S>>>();
            lep.add(Either.make(new Pair<A, S>(a, s)));
            return lep.stream();
        };
//        Arrays.stream(new int[] {1,2,3,4}).reduce();
        return pa;
    }

    public static <S, E, A> Parser<S, E, A> empty() {
        Parser<S, E, A> pa = new Parser<S, E, A>();
        pa.pf = s -> new LinkedList<Either<E, Pair<A, S>>>().stream();
        return pa;
    }

    public <B> Parser<S, E, B> bind(Function<A, Parser<S, E, B>> f) {
        Parser<S, E, B> pb = new Parser<S, E, B>();
        Function<S, Stream<Either<E, Pair<A, S>>>> pf = this.pf;

        pb.pf = s -> pf.apply(s).flatMap(ep -> {
            Optional<Pair<A, S>> op = ep.val();
            if (op.isPresent()) {
                Pair<A, S> p = op.get();
                return f.apply(p.getKey()).pf.apply(p.getValue());
            }
            List<Either<E, Pair<B, S>>> lb = new LinkedList<Either<E, Pair<B, S>>>();
            lb.add(Either.fail(ep.err().orElse(null)));
            return lb.stream();
        });

        return pb;
    }

    public <B> Parser<S, E, B> map(Function<A, B> f) {
        return this.bind(a -> Parser.make(f.apply(a)));
    }

    public <B> Parser<S, E, B> then(Parser<S, E, B> sb) {
        return this.bind(a -> sb);
    }

    public <B> Parser<S, E, A> lthen(Function<A, Parser<S, E, B>> f) {
        return this.bind(a -> f.apply(a).then(make(a)));
    }

    public Parser<S, E, A> concatHead(Parser<S, E, A> other) {
        Parser<S, E, A> pa = new Parser<S, E, A>();
        Function<S, Stream<Either<E, Pair<A, S>>>> pf = this.pf;
        pa.pf = s -> {
            List<Either<E, Pair<A, S>>> la = new LinkedList<Either<E, Pair<A, S>>>();
            Optional<E> oe = null;
            for (Either<E, Pair<A, S>> ea : pf.apply(s).collect(Collectors.toList())) {
                if (oe == null) oe = ea.err();
                Optional<Pair<A, S>> oa = ea.val();
                if (!oa.isPresent()) continue;
                la.add(Either.make(oa.get()));
                return la.stream();
            }
            for (Either<E, Pair<A, S>> ea : other.pf.apply(s).collect(Collectors.toList())) {
                if (oe == null) oe = ea.err();
                Optional<Pair<A, S>> oa = ea.val();
                if (!oa.isPresent()) continue;
                la.add(Either.make(oa.get()));
                return la.stream();
            }
            if (oe != null && oe.isPresent()) la.add(Either.fail(oe.get()));
            return la.stream();
        };
        return pa;
    }

    public <B> Parser<S, E, A> followedBy(Parser<S, E, B> pb) {
        Parser<S, E, A> pa = new Parser<S, E, A>();
        Function<S, Stream<Either<E, Pair<A, S>>>> pf = this.pf;
        pa.pf = s -> {
            return pf.apply(s).filter(ea -> {
                Optional<Pair<A, S>> oa = ea.val();
                return oa.isPresent() && pb.pf.apply(oa.get().getValue()).anyMatch(eb -> eb.val().isPresent());
            });
        };
        return pa;
    }

    public static <S, E, A> Parser<S, E, List<A>> many(Parser<S, E, A> pa) {
        return many1(pa).concatHead(empty());
    }

    public static <S, E, A> Parser<S, E, List<A>> many1(Parser<S, E, A> pa) {
        return pa.bind(a -> many(pa).bind(as -> {
            List<A> la = new LinkedList<A>();
            la.add(a);
            la.addAll(as);
            return Parser.make(la);
        }));
    }

    public <B> Parser<S, E, List<A>> sepby(Parser<S, E, B> pb) {
        return sepby1(pb).concatHead(Parser.make(new LinkedList<A>()));
    }

    public <B> Parser<S, E, List<A>> sepby1(Parser<S, E, B> pb) {
        Parser<S, E, A> that = this;
        return bind(a -> many(pb.then(that)).bind(as -> {
            List<A> la = new LinkedList<>();
            la.add(a);
            for (A a2 : as) {
                la.add(a2);
            }
            return Parser.make(la);
        }));
    }

    public Parser<S, E, A> chainl(Parser<S, E, BinaryOperator<A>> op, A a) {
        return chainl1(op).concatHead(Parser.make(a));
    }

    public Parser<S, E, A> chainl1(Parser<S, E, BinaryOperator<A>> op) {
        Parser<S, E, A> that = this;
        Recursive<Function<A, Parser<S, E, A>>> rest = new Recursive<>();
        rest.rec = a -> op.bind(f -> that.bind(a2 -> rest.rec.apply(f.apply(a, a2))));
        return bind(rest.rec);
    }

    public Parser<S, E, A> chainr(Parser<S, E, BinaryOperator<A>> op, A a) {
        return chainr1(op).concatHead(Parser.make(a));
    }

    public Parser<S, E, A> chainr1(Parser<S, E, BinaryOperator<A>> op) {
        Parser<S, E, A> that = this;
        Recursive<Function<A, Parser<S, E, A>>> rest = new Recursive<>();
        rest.rec = a -> op.bind(f -> that.bind(a2 -> rest.rec.apply(a2).concatHead(Parser.make(f.apply(a, a2)))));
        return bind(rest.rec);
    }

    public static <S, E, A> Parser<S, E, A> lift(State<S, A> sa) {
        Parser<S, E, A> pa = new Parser<>();
        pa.pf = s -> {
            List<Either<E, Pair<A, S>>> la = new LinkedList<>();
            la.add(Either.make(sa.sf.apply(s)));
            return la.stream();
        };
        return pa;
    }

}
