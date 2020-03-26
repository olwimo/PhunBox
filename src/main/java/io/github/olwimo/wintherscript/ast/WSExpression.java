package io.github.olwimo.wintherscript.ast;

import java.util.stream.Stream;

public abstract class WSExpression {
    public enum Type {
        Or,
        And,
        Eq,
        Ne,
        Ge,
        Le,
        Gt,
        Lt,
        Add,
        Sub,
        Mul,
        Div,
        Neg,
        IfThenElse,
        Term,
        Identifier,
        Tuple,
        Call,
        Lookup
    }
    public abstract Type getType();
    public final static class BinOp extends WSExpression {
        Type type;
        WSExpression left;
        WSExpression right;
        @Override
        public Type getType() { return type; }
        public BinOp(Type type, WSExpression left, WSExpression right) {
            this.type = type;
            this.left = left;
            this.right = right;
        }
    }
    public final static class Neg extends WSExpression {
        WSExpression factor;
        @Override
        public Type getType() { return Type.Neg; }
        public Neg(WSExpression factor) {
            this.factor = factor;
        }
    }
    public final static class IfThenElse extends WSExpression {
        WSAction condition;
        WSAction ifTrue;
        WSAction ifFalse;
        @Override
        public Type getType() { return Type.IfThenElse; }
        public IfThenElse(WSAction condition, WSAction ifTrue, WSAction ifFalse) {
            this.condition = condition;
            this.ifTrue = ifTrue;
            this.ifFalse = ifFalse;
        }
    }
    public final static class Term extends WSExpression {
        WSTerm term;
        @Override
        public Type getType() { return Type.Term; }
        public Term(WSTerm term) {
            this.term = term;
        }
    }
    public final static class Identifier extends WSExpression {
        String name;
        @Override
        public Type getType() { return Type.Identifier; }
        public Identifier(String name) {
            this.name = name;
        }
    }
    public final static class Tuple extends WSExpression {
        Stream<WSAction> elements;
        @Override
        public Type getType() { return Type.Tuple; }
        public Tuple(Stream<WSAction> elements) {
            this.elements = elements;
        }
    }
    public final static class Combined extends WSExpression {
        Type type;
        WSAction object, argument;
        @Override
        public Type getType() { return type; }
        public Combined(Type type, WSAction object, WSAction argument) {
            this.type = type;
            this.object = object;
            this.argument = argument;
        }
    }
/*
    public final static class Call extends WSExpression {
        WSAction function;
        WSAction arg;
        @Override
        public Type getType() { return Type.Call; }
        public Call(WSAction function, WSAction arg) {
            this.function = function;
            this.arg = arg;
        }
    }
    public final static class Lookup extends WSExpression {
        WSExpression root;
        WSAction lookup;
        @Override
        public Type getType() { return Type.Lookup; }
        public Lookup(WSExpression root, WSAction lookup) {
            this.root = root;
            this.lookup = lookup;
        }
    }
 */
}
