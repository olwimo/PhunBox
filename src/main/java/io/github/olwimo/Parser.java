package io.github.olwimo;

import javafx.util.Pair;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Parser<S, E, A> {
    Function<S, Stream<Either<E, Pair<A, S>>>> pf;

    public Parser(Function<S, Stream<Either<E, Pair<A, S>>>> pf) {
        this.pf = pf;
    }

    public Either<E, Stream<Pair<A, S>>> runParser(S s) {
        return pf.apply(s).reduce(Either.make(Stream.empty()),
                (eas, ea) -> eas.bind(sa -> ea.bind(pa -> Either.make(Stream.concat(sa, Stream.of(pa))))),
                (eas, eas2) -> eas.bind(sa -> eas2.bind(sa2 -> Either.make(Stream.concat(sa, sa2)))));
    }
//                flatMap(ea -> ea.map(pa -> Stream.of(pa)).orElse(Stream.empty()))
//        Stream<Either<E, Pair<A, S>>> sa = pf.apply(s);
//        return new Pair<Stream<E>, Stream<Pair<A, S>>>(sa.flatMap(ea ->
//                ea.err().map(e -> Stream.of(e)).orElse(Stream.empty())
/*        {
            List<E> le = new LinkedList<E>();

            Optional<E> e = ea.err();
            if (e.isPresent()) le.add(e.get());
            return le.stream();
        }*/
//        ), sa.flatMap(ea -> ea.val().map(pa -> Stream.of(pa)).orElse(Stream.empty())
/*        {
            List<Pair<A, S>> la = new LinkedList<Pair<A, S>>();
            Optional<Pair<A, S>> a = ea.val();
            if (a.isPresent()) la.add(a.get());
            return la.stream();
        }*/
//        ));
//    }

    public static <S, E, A> Parser<S, E, A> make(A a) {
        return new Parser<>(s -> Stream.of(Either.make(new Pair<A, S>(a, s))));
    }
/*        Parser<S, E, A> pa =
        pa.pf = s -> Stream.of(Either.make(new Pair<A, S>(a, s)));
        {
            List<Either<E, Pair<A, S>>> lep = new LinkedList<Either<E, Pair<A, S>>>();
            lep.add(Either.make(new Pair<A, S>(a, s)));
            return lep.stream();
        };
//        Arrays.stream(new int[] {1,2,3,4}).reduce();
        return pa;
    }*/

    public static <S, E, A> Parser<S, E, A> empty() {
        return new Parser<>(_s -> Stream.empty());
    }
/*        Parser<S, E, A> pa = new Parser<S, E, A>();
        pa.pf = s -> new LinkedList<Either<E, Pair<A, S>>>().stream();
        return pa;
    }*/

    public <B> Parser<S, E, B> bind(Function<A, Parser<S, E, B>> f) {
        Function<S, Stream<Either<E, Pair<A, S>>>> pf = this.pf;

        return new Parser<>(s -> pf.apply(s)
                .flatMap(ep -> ep.val().map(pa -> f.apply(pa.getKey()).pf.apply(pa.getValue()))
                        .orElse(ep.err().map(e -> Stream.of(Either.<E, Pair<B, S>>fail(e))).orElse(Stream.empty()))));
    }
/*        {
            Optional<Pair<A, S>> op = ep.val();
            if (op.isPresent()) {
                Pair<A, S> p = op.get();
                return f.apply(p.getKey()).pf.apply(p.getValue());
            }
            List<Either<E, Pair<B, S>>> lb = new LinkedList<Either<E, Pair<B, S>>>();
            lb.add(Either.fail(ep.err().orElse(null)));
            return lb.stream();
        }
    }
        Parser<S, E, B> pb = new Parser<S, E, B>();

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
    }*/

    public <B> Parser<S, E, B> map(Function<A, B> f) {
        return this.bind(a -> Parser.make(f.apply(a)));
    }

    public <B> Parser<S, E, B> then(Parser<S, E, B> sb) {
        return this.bind(a -> sb);
    }

    public <B> Parser<S, E, A> lbind(Function<A, Parser<S, E, B>> f) {
        return bind(a -> f.apply(a).then(make(a)));
    }

    public <B> Parser<S, E, A> lthen(Parser<S, E, B> pb) {
        return lbind(a -> pb);
    }

    public Parser<S, E, A> concatHead(Parser<S, E, A> pa) {
        Function<S, Stream<Either<E, Pair<A, S>>>> pf = this.pf;

        return new Parser<>(s -> pf.apply(s).findFirst().map(ea -> Stream.of(ea))
                .orElse(pa.pf.apply(s).findFirst().map(ea -> Stream.of(ea)).orElse(Stream.empty())));
    }

    public Parser<S, E, Stream<A>> ap(Parser<S, E, Stream<A>> ps) {
        return bind(a -> ps.map(as -> Stream.concat(Stream.of(a), as)));
    }

    /*                .reduce(Stream.empty(),
                (sea, ea) -> Stream.concat(sea, ea.val().map(pa -> Stream.of(Either.<E, Pair<A, S>>make(pa)))
                        .orElse(ea.err().map(e -> Stream.of(Either.<E, Pair<A, S>>fail(e))).orElse(Stream.empty())))
                .findFirst().map(ea -> Stream.of(ea)).orElse(Stream.empty()),
                (sa, sa2) -> Stream.concat(sa, sa2).findFirst().map(ea -> Stream.of(ea)).orElse(Stream.empty()));
        {
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
        });
    }
        Parser<S, E, A> pa = new Parser<>();
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
    }*/

    public static <S, E, A> Parser<S, E, A> lift(State<S, A> sa) {
        return new Parser<>(s -> Stream.of(Either.make(sa.sf.apply(s))));
    }
/*        Parser<S, E, A> pa = new Parser<>();
        pa.pf = s -> {
            List<Either<E, Pair<A, S>>> la = new LinkedList<>();
            la.add(Either.make(sa.sf.apply(s)));
            return la.stream();
        };
        return pa;
    }*/

    public <B> Parser<S, E, A> followedBy(Parser<S, E, B> pb) {
        Function<S, Stream<Either<E, Pair<A, S>>>> pf = this.pf;

        return new Parser<>(s -> pf.apply(s).filter(ea -> ea.val()
                .map(pa -> pb.pf.apply(pa.getValue()).anyMatch(eb -> eb.val().isPresent())).orElse(false)));
    }
/*        Parser<S, E, A> pa = new Parser<S, E, A>();
        pa.pf = s -> {
            return pf.apply(s).filter(ea -> {
                Optional<Pair<A, S>> oa = ea.val();
                return oa.isPresent() && pb.pf.apply(oa.get().getValue()).anyMatch(eb -> eb.val().isPresent());
            });
        };
        return pa;
    }*/

    public static <S, E, A> Parser<S, E, Stream<A>> many(Parser<S, E, A> pa) {
        return many1(pa).concatHead(empty());
    }

    public static <S, E, A> Parser<S, E, Stream<A>> many1(Parser<S, E, A> pa) {
        return pa.bind(a -> many(pa).bind(as -> Parser.make(Stream.concat(Stream.of(a), as))));
    }
/*    {
            List<A> la = new LinkedList<A>();
            la.add(a);
            la.addAll(as);
            return Parser.make(la);
        }*/

    public <B> Parser<S, E, Stream<A>> sepby(Parser<S, E, B> pb) {
        return sepby1(pb).concatHead(Parser.make(Stream.empty()));
    }

    public <B> Parser<S, E, Stream<A>> sepby1(Parser<S, E, B> pb) {
        Parser<S, E, A> that = this;
        return bind(a -> many(pb.then(that)).bind(as -> Parser.make(Stream.concat(Stream.of(a), as))));
    }
/*    {
            List<A> la = new LinkedList<>();
            la.add(a);
            for (A a2 : as) {
                la.add(a2);
            }
            return Parser.make(la);
        }*/

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

//    public interface Token<T> extends Optional<T> implements Comparable<Token<T>> {
//        public abstract class Token<T extends Comparable<T>> extends Optional<T> implements Comparable<Token<T>> {
//        public Token() {
//            super();
    //super(Optional.empty());
//        }
/*        public Token<T> empty();
        public String make(String str);*/
//    }

/*    public static interface Tokenizer<T extends Comparable T> {

    }*/

/*    public interface Tokenizer<T extends Comparable<T>> {
        Pair<String, Optional<T>> tokenize(String str);
    }*/

    public interface TokenState<T extends Comparable<T>> {
        Optional<T> peek();

        Optional<T> pop();

        TokenState<T> copy();

/*        public static <T>Function<String, Pair<String, Optional<T>>> getTokenizer() {};
        TokenState(Function<String, Pair<String, Optional<T>>> tokenizer) {
            tokenizer = tokenizer;
        }*/

//        public static Pair<String, Optional<T>> tokenize(String str);
/*        protected static class NoState extends TokenState {
            @Override
            public Optional peek() {
                return Optional.empty();
            }

            @Override
            public Optional pop() {
                return Optional.empty();
            }

            @Override
            public Pair<String, Optional> tokenize(String str) {
                return new Pair<>(str, Optional.empty());
            }

            @Override
            public TokenState copy() {
                return new NoState();
            }

            NoState() {
            }
        }

        static TokenState main = new NoState();

        static <T extends Comparable<T>> TokenState<T> getMain() {
            return main;
        }*/
    }

    public static <S, E> Parser<S, E, S> getState() {
        return lift(State.getState());
    }

    public static <S, E> Parser<S, E, S> putState(S s) {
        return lift(State.putState(s));
    }

    public static <S, E> Parser<S, E, S> withState(Function<S, S> f) {
        return lift(State.withState(f));
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Optional<T>> peek() {
        return Parser.<TokenState<T>, E>getState().bind(s -> Parser.make(s.peek()));
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Optional<T>> pop() {
        return Parser.<TokenState<T>, E>getState().bind(s -> Parser.make(s.pop()));
    }

    public static <T extends Comparable<T>, E, A> Parser<TokenState<T>, E, A>
    lookAhead(Parser<TokenState<T>, E, A> pa) {
        return Parser.<TokenState<T>, E>withState(s -> s.copy()).bind(s0 ->
                pa.bind(a -> Parser.<TokenState<T>, E>putState(s0).then(Parser.make(a))));
    }

    public static <T extends Comparable<T>, E, A> Parser<TokenState<T>, E, A>
    maybe(Parser<TokenState<T>, E, A> pa, A a) {
        return pa.concatHead(Parser.make(a));
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Optional<T>> sat(Predicate<Optional<T>> pt) {
        return Parser.<T, E>peek().bind(ot -> pt.test(ot) ?
                Parser.pop() : Parser.empty());
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Optional<T>> scry(Predicate<Optional<T>> pt) {
        return Parser.<T, E>peek().bind(ot -> pt.test(ot) ?
                Parser.make(ot) : Parser.empty());
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Optional<T>> token(Optional<T> t) {
        return tokens(Stream.of(t));
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Optional<T>> tokens(Stream<Optional<T>> ts) {
        return sat(t2 -> ts.anyMatch(t -> t.isPresent() ?
                t2.isPresent() && t.get().compareTo(t2.get()) == 0 : !t2.isPresent()));
    }

    public static abstract class Tokenizer<T extends Comparable<T>> {
        String str;
        public Tokenizer(String str) {
            this.str = str;
        }
//        public abstract Tokenizer<T> make(String str);
        public abstract Stream<Optional<T>> tokenize(String str);
        public Stream<Optional<T>> get() {
            return tokenize(str);
        }
    }
    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Stream<Optional<T>>>
    word(Tokenizer<T> ts) {
        return ts.get().reduce(Parser.<TokenState<T>, E, Stream<Optional<T>>>make(Stream.empty()),
                (acc, ot) -> Parser.<T, E>token(ot).ap(acc),
                (p1, p2) -> p1.bind(s1 -> p2.map(s2 -> Stream.concat(s1, s2))));
    }
/*    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Stream<Optional<T>>>
    word(Function<String, Pair<String, Optional<T>>> tokenizer, String s) {
        Function<String, Parser<TokenState<T>, E, Stream<Optional<T>>>> word = Recursive.make(rec -> str -> {
            if (str.isEmpty()) return Parser.make(Stream.empty());
            Pair<String, Optional<T>> pt = tokenizer.apply(str);
            if (pt.getKey().compareTo(str) == 0) return Parser.empty();
            Parser<TokenState<T>, E, Stream<Optional<T>>> pts = rec.apply(pt.getKey());
            return Parser.<T, E>token(pt.getValue()).bind(ot -> pts.bind(ots ->
                    Parser.make(Stream.concat(Stream.of(ot), ots))));
        });
        return word.apply(s);
    }*/
/*        return new Parser<TokenState<T>, E, Stream<Optional<T>>>(s -> {
            Pair<String, Optional<T>> pt = s.tokenize(str);
            if (pt.getKey().compareTo(str) == 0) return Stream.empty();
            Function<TokenState<T>, Stream<Either<E, Pair<Stream<Optional<T>>, TokenState<T>>>>> pf =
                    Parser.<T, E>word(pt.getKey()).pf;
            Parser.<T, E>token(pt.getValue()).pf.apply(s).flatMap(et -> Stream.of(et.map(p ->
                    pf.apply(p.getValue()).flatMap(ep -> Stream.of(ep.map(ps ->
                            new Pair(Stream.concat(Stream.of(p.getKey()), ps.getKey()), ps.getValue()))))))).;
        });
//        return Parser.empty();
    }*/

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Optional<T>> eof() {
        return Parser.scry(t -> !t.isPresent());
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Optional<T>> notAlphaNum() {
        return Parser.sat(ot -> !ot.map(t -> !t.toString().matches("[_a-zA-Z0-9]")).orElse(false));
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Optional<T>> regex(String re) {
        return Parser.sat(ot -> !ot.map(t -> !t.toString().matches(re)).orElse(false));
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Optional<T>> space() {
        return regex("\\s");
//        return Parser.sat(ot -> !ot.map(t -> !t.toString().matches("\\s")).orElse(false));
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Stream<Optional<T>>> spaces() {
        return Parser.many(Parser.space());
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, Stream<Optional<T>>> spaces1() {
        return Parser.many1(Parser.space());
    }

    public static <T extends Comparable<T>, E, A> Parser<TokenState<T>, E, A> entity(Parser<TokenState<T>, E, A> pa) {
        return pa.lthen(Parser.<T, E>spaces());
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, String>
    stringStream(Parser<TokenState<T>, E, Stream<Optional<T>>> pt) {
        return pt.map(st -> st.reduce(Either.<String, String>make(""),
                (ess, ot) -> ess.bind(s -> ot.map(t -> Either.<String, String>make(s + t.toString()))
                                .orElse(Either.<String, String>fail(s))),
                (ess, ess2) -> ess.bind(s -> ess2.bind(s2 -> Either.<String, String>make(s + s2))))
                .bind(s -> Either.<String, String>fail(s)).err().orElse(""));
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, String>
    symbol(Tokenizer<T> ts) {
        return Parser.<T, E>stringStream(Parser.<T, E, Stream<Optional<T>>>entity(Parser.<T, E>word(ts)));
    }

    public static <T extends Comparable<T>, E> Parser<TokenState<T>, E, String>
    name(Tokenizer<T> ts) {
        return Parser.<T, E>stringStream(Parser.<T, E, Stream<Optional<T>>>entity(Parser.<T, E>word(ts)
                .lthen(Parser.<T, E, Optional<T>>lookAhead(Parser.<T, E>notAlphaNum()))));
    }
}
