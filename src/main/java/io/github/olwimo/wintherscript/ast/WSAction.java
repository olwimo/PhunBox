package io.github.olwimo.wintherscript.ast;

import java.util.stream.Stream;

public abstract class WSAction {
    public enum Type {
        Assignment,
//        Program,
//        Call,
//        Lookup,
        Expression
    }
    public abstract Type getType();
    public final static class Assignment extends WSAction {
        String identifier;
        Stream<WSAction> lookups;
        WSAction value;
        @Override
        public Type getType() {
            return Type.Assignment;
        }
        public Assignment(String identifier, Stream<WSAction> lookups, WSAction value) {
            this.identifier = identifier;
            this.lookups = lookups;
            this.value = value;
        }
    }
    /*
    public final static class Tuple extends WSAction {
        Stream<WSAction> elements;
        @Override
        public Type getType() { return Type.Program; }
        public Tuple(Stream<WSAction> elements) {
            this.elements = elements;
        }
    }
     */
    /*
    public final static class Combined extends WSAction {
        Type type;
        WSAction object, argument;
        @Override
        public Type getType() { return Type.Call; }
        public Combined(Type type, WSAction object, WSAction argument) {
            this.type = type;
            this.object = object;
            this.argument = argument;
        }
    }
    */
    public final static class Expression extends WSAction {
        WSExpression expression;
        public WSExpression getExpression() { return expression; }
        @Override
        public Type getType() {
            return Type.Expression;
        }
        public Expression(WSExpression expression) {
            this.expression = expression;
        }
    }
}
