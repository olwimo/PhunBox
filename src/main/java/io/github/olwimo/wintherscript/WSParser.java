package io.github.olwimo.wintherscript;

import io.github.olwimo.Parser;
import io.github.olwimo.Recursive;
import io.github.olwimo.wintherscript.ast.WSAction;
import io.github.olwimo.wintherscript.ast.WSExpression;
import io.github.olwimo.wintherscript.ast.WSTerm;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

public class WSParser {
    public static class myTokenizer extends Parser.Tokenizer<String> {
        public myTokenizer(String str) {
            super(str);
        }

        @Override
        public Stream<Optional<String>> tokenize(String str) {
            return Arrays.stream(str.split("")).map(s -> Optional.of(s));
        }
    }

    public static <E>Parser<Parser.TokenState<String>, E, String> symbol(String str) {
        return Parser.symbol(new myTokenizer(str));
    }

    public static <E>Parser<Parser.TokenState<String>, E, String> name(String str) {
        return Parser.name(new myTokenizer(str));
    }

    public static Stream<Optional<String>> makeStream(String str) {
        return Arrays.stream(str.split("")).map(c -> Optional.of(c));
    }
    public static <E>Parser<Parser.TokenState<String>, E, WSTerm> number() {
        return Parser.maybe(Parser.<String, E>token(Optional.of("-")), Optional.empty())
                .bind(prefix -> Parser.<String, E, WSTerm>entity(Parser.<String, E>stringStream(
                        Parser.many(Parser.<String, E>tokens(
                                makeStream("1234567890")
                        ))).bind(n ->
                         Parser.<Parser.TokenState<String>, E, WSTerm>make(
                                new WSTerm.Integer(Integer.valueOf(prefix.map(_m -> "-")
                                        .orElse("") + n))))));
    }
    public static <E>Parser<Parser.TokenState<String>, E, WSTerm> decimal() {
        return Parser.maybe(Parser.<String, E>token(Optional.of("-")), Optional.empty())
                .bind(prefix -> Parser.<String, E, WSTerm>entity(Parser.<String, E>stringStream(
                        Parser.many(Parser.<String, E>tokens(
                                makeStream("1234567890")
                        )).lthen(Parser.token(Optional.of(".")))).bind(n ->
                        Parser.<String, E>stringStream(Parser.many(Parser.<String, E>tokens(
                                makeStream("1234567890")
                        ))).bind(f -> Parser.<Parser.TokenState<String>, E, WSTerm>make(
                                new WSTerm.Float(Float.valueOf(prefix.map(_m -> "-")
                                .orElse("") + n + "." + f)))))));
    }
    public static <E>Parser<Parser.TokenState<String>, E, Optional<String>> escaped(String escapeChars) {
        return Parser.<String, E>token(Optional.of("\\"))
                .then(Parser.tokens(makeStream(escapeChars + "\\")));
    }

    public static <E>Parser<Parser.TokenState<String>, E, String> quoted() {
        Function<String, Parser<Parser.TokenState<String>, E, Stream<Optional<String>>>> parseString =
                str ->
                WSParser.<E>escaped("\"'")
                .concatHead(Parser.<String, E>sat(t -> t.map(s -> s != str).orElse(false)))
                .map(os -> Stream.of(os))
                .chainl(Parser.make((s1, s2) -> Stream.concat(s1, s2)), Stream.empty());
        return Parser.<String, E>token(Optional.of("\"")).then(parseString.apply("\"")).lthen(WSParser.symbol("\""))
                .concatHead(Parser.<String, E>token(Optional.of("'")).then(parseString.apply("'"))
                        .lthen(WSParser.symbol("'")))
                .map(ss -> ss.map(os -> os.orElse("")).reduce((s, s2) -> s + s2).orElse(""));
//                .bind(ss -> ss.map(os -> os.orElse("")).reduce((s, s2) -> s + s2).orElse(""));
/*                        Parser.make(ss.reduce("",
                        (acc, s) -> acc + s.orElse(""),
                        (s1, s2) -> s1 + s2)));*/
    }

    public static <E>Parser<Parser.TokenState<String>, E, String> strRegex(String re) {
        return Parser.<String, E>regex(re).map(ot -> ot.orElse(""));
    }

    public static <E>Parser<Parser.TokenState<String>, E, String> letter() {
        return strRegex("[_a-zA-Z]");
    }

    public static <E>Parser<Parser.TokenState<String>, E, Optional<String>> letterOrNumber() {
        return Parser.regex("[_a-zA-Z0-9]");
    }

    public static <E>Parser<Parser.TokenState<String>, E, String> identifier() {
        return WSParser.<E>letter().bind(s -> Parser.<String, E>stringStream(Parser.many1(letterOrNumber()))
                .map(s2 -> s + s2));
    }

    public static <E>Parser<Parser.TokenState<String>, E, WSTerm> unknown() {
        return WSParser.<E>name("null").then(Parser.make(new WSTerm.Any(null)));
    }

    public static <E>Parser<Parser.TokenState<String>, E, String> justIdentifier() {
        return Parser.entity(WSParser.<E>identifier()).concatHead(WSParser.<E>symbol("#").then(quoted()));
//                .<WSExpression>bind(id -> Parser.make(new WSExpression.Identifier(id)));
    }

    public static <E>Parser<Parser.TokenState<String>, E, BinaryOperator<WSExpression>> makeBinOp(String op) {
        switch (op) {
            case "||":
                return Parser.make((expr1, expr2) -> new WSExpression.BinOp(WSExpression.Type.Or, expr1, expr2));
            case "&&":
                return Parser.make((expr1, expr2) -> new WSExpression.BinOp(WSExpression.Type.And, expr1, expr2));
            case "==":
                return Parser.make((expr1, expr2) -> new WSExpression.BinOp(WSExpression.Type.Eq, expr1, expr2));
            case "!=":
                return Parser.make((expr1, expr2) -> new WSExpression.BinOp(WSExpression.Type.Ne, expr1, expr2));
            case ">=":
                return Parser.make((expr1, expr2) -> new WSExpression.BinOp(WSExpression.Type.Ge, expr1, expr2));
            case "<=":
                return Parser.make((expr1, expr2) -> new WSExpression.BinOp(WSExpression.Type.Le, expr1, expr2));
            case ">":
                return Parser.make((expr1, expr2) -> new WSExpression.BinOp(WSExpression.Type.Gt, expr1, expr2));
            case "<":
                return Parser.make((expr1, expr2) -> new WSExpression.BinOp(WSExpression.Type.Lt, expr1, expr2));
            case "+":
                return Parser.make((expr1, expr2) -> new WSExpression.BinOp(WSExpression.Type.Add, expr1, expr2));
            case "-":
                return Parser.make((expr1, expr2) -> new WSExpression.BinOp(WSExpression.Type.Sub, expr1, expr2));
            case "*":
                return Parser.make((expr1, expr2) -> new WSExpression.BinOp(WSExpression.Type.Mul, expr1, expr2));
            case "/":
                return Parser.make((expr1, expr2) -> new WSExpression.BinOp(WSExpression.Type.Div, expr1, expr2));
            default:
                return Parser.empty();
        }
    }

    public static <E>Parser<Parser.TokenState<String>, E, BinaryOperator<WSExpression>> Or() {
        return WSParser.<E>symbol("||").bind(op -> WSParser.makeBinOp(op));
    }

    public static <E>Parser<Parser.TokenState<String>, E, BinaryOperator<WSExpression>> And() {
        return WSParser.<E>symbol("&&").bind(op -> WSParser.makeBinOp(op));
    }

    public static <E>Parser<Parser.TokenState<String>, E, BinaryOperator<WSExpression>> Cmp() {
        return Stream.of("==", "!=", ">=", "<=", ">", "<").map(str -> WSParser.<E>symbol(str))
                .reduce((p1, p2) -> p1.concatHead(p2)).orElse(Parser.empty()).bind(op -> WSParser.makeBinOp(op));
    }

    public static <E>Parser<Parser.TokenState<String>, E, BinaryOperator<WSExpression>> Add() {
        return Stream.of("+", "-").map(str -> WSParser.<E>symbol(str))
                .reduce((p1, p2) -> p1.concatHead(p2)).orElse(Parser.empty()).bind(op -> WSParser.makeBinOp(op));
    }

    public static <E>Parser<Parser.TokenState<String>, E, BinaryOperator<WSExpression>> Mul() {
        return Stream.of("*", "/").map(str -> WSParser.<E>symbol(str))
                .reduce((p1, p2) -> p1.concatHead(p2)).orElse(Parser.empty()).bind(op -> WSParser.makeBinOp(op));
    }

    public static <E>Parser<Parser.TokenState<String>, E, Pair<String, WSAction>> mapAction() {
        return Parser.entity(WSParser.<E>identifier().concatHead(WSParser.quoted())).lthen(WSParser.symbol(":"))
                .bind(name -> WSParser.<E>action().bind(action -> Parser.make(new Pair<>(name, action))));
    }

    public static <E>Parser<Parser.TokenState<String>, E, BinaryOperator<Pair<WSExpression.Type, WSAction>>>
    ChainPost() {
        return Parser.make((typeActionPair, typeActionPair2) -> new Pair<>(typeActionPair2.getKey(),
                new WSAction.Expression(new WSExpression.Combined(typeActionPair2.getKey(),
                        typeActionPair.getValue(), typeActionPair2.getValue()))));
    }

    public static <E>Parser<Parser.TokenState<String>, E, WSExpression> postFactor(WSExpression expression) {
        return WSParser.<E>symbol("(").then(WSParser.<E>action().sepby(WSParser.symbol(",")))
                .lthen(WSParser.symbol(")"))
                .<Pair<WSExpression.Type, WSAction>>map(actions -> new Pair<>(WSExpression.Type.Call,
                        new WSAction.Expression(new WSExpression.Tuple(actions))))
                .concatHead(WSParser.<E>symbol("[").then(WSParser.action()).lthen(WSParser.symbol("]"))
                .map(action -> new Pair<>(WSExpression.Type.Lookup, action)))
                .chainr(WSParser.ChainPost(), new Pair<>(WSExpression.Type.Lookup,
                        new WSAction.Expression(expression))).<WSExpression>map(p -> {
                            WSAction action = p.getValue();
                            switch (action.getType()) {
                                case Expression:
                                    return ((WSAction.Expression)action).getExpression();
                                default:
                                    return new WSExpression.Tuple(Stream.of(action));
                            }
                });
    }

    public static <E>Parser<Parser.TokenState<String>, E, WSExpression> factor() {
        return WSParser.<E>decimal().concatHead(WSParser.number())
                .concatHead(WSParser.<E>quoted().map(s -> new WSTerm.String(s))).concatHead(WSParser.unknown())
                .concatHead(WSParser.<E>name("true").map(_s -> new WSTerm.Bool(true)))
                .concatHead(WSParser.<E>name("false").map(_s -> new WSTerm.Bool(false)))
                .concatHead(WSParser.<E>symbol("[").then(WSParser.<E>action().sepby(WSParser.symbol(",")))
                        .lthen(WSParser.symbol("]")).map(elements -> new WSTerm.Array(elements)))
                .concatHead(WSParser.<E>symbol("{").then(WSParser.<E>mapAction().sepby(WSParser.symbol(",")))
                        .lthen(WSParser.symbol("}")).map(mappings -> new WSTerm.Object(mappings)))
                .concatHead(WSParser.<E>symbol("(")
                        .then(WSParser.<E>justIdentifier().sepby(WSParser.symbol(",")))
                .lthen(WSParser.symbol(")")).lthen(WSParser.symbol("=>"))
                        .bind(args -> WSParser.<E>action().map(action -> new WSTerm.Function(args, action))))
                .<WSExpression>map(term -> new WSExpression.Term(term))
                .concatHead(WSParser.<E>name("if").then(WSParser.action()).lthen(WSParser.name("then"))
                        .bind(condition -> WSParser.<E>action().lthen(WSParser.name("else"))
                                .bind(ifTrue -> WSParser.<E>action().map(ifFalse ->
                                        new WSExpression.IfThenElse(condition, ifTrue, ifFalse)))))
                .concatHead(WSParser.<E>symbol("(").then(WSParser.<E>action().sepby1(WSParser.symbol(";")))
                .lthen(WSParser.symbol(")")).map(elements -> new WSExpression.Tuple(elements)))
                .concatHead(WSParser.<E>justIdentifier().map(name -> new WSExpression.Identifier(name)))
                .<WSExpression>bind(expr -> WSParser.<E>postFactor(expr));
        //.<WSAction>map(expr -> new WSAction.Expression(expr));
    }

    public static <E>Parser<Parser.TokenState<String>, E, WSExpression> negate() {
        return WSParser.<E>symbol("!").then(WSParser.factor())
                .<WSExpression>map(expr -> new WSExpression.Neg(expr))
                .concatHead(WSParser.factor());
    }

    public static <E>Parser<Parser.TokenState<String>, E, WSExpression> term() {
        return WSParser.<E>negate().chainl1(WSParser.Mul());
    }

    public static <E>Parser<Parser.TokenState<String>, E, WSExpression> expr() {
        return WSParser.<E>term().chainl1(WSParser.Add());
    }

    public static <E>Parser<Parser.TokenState<String>, E, WSExpression> comp() {
        return WSParser.<E>expr().chainl1(WSParser.Cmp());
    }

    public static <E>Parser<Parser.TokenState<String>, E, WSExpression> andComp() {
        return WSParser.<E>comp().chainl1(WSParser.And());
    }

    public static <E>Parser<Parser.TokenState<String>, E, WSExpression> orComp() {
        return WSParser.<E>andComp().chainl1(WSParser.Or());
    }

    public static <E>Parser<Parser.TokenState<String>, E, WSAction> action() {
        return WSParser.<E>justIdentifier()
                .<WSAction>bind(id -> Parser.many(WSParser.<E>symbol("[").then(WSParser.action())
                        .lthen(WSParser.symbol("]"))).lthen(WSParser.symbol("="))
                        .bind(lookups -> WSParser.<E>action()
                                .map(value -> new WSAction.Assignment(id, lookups, value))))
                .concatHead(WSParser.<E>orComp().map(expr -> new WSAction.Expression(expr)));
    }

    public static <E>Parser<Parser.TokenState<String>, E, Stream<WSAction>> program() {
        return WSParser.<E>action().sepby1(WSParser.symbol(";"));
    }

    public static <E>Parser<Parser.TokenState<String>, E, Stream<WSAction>> wsDocument() {
        return Parser.<String, E>spaces().then(WSParser.<E>program()).lthen(Parser.eof());
    }

    public static Optional<Stream<WSAction>> parseWS(String document) {
        return Optional.empty();
    }
}
