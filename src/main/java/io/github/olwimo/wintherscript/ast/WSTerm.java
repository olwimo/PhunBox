package io.github.olwimo.wintherscript.ast;

import javafx.util.Pair;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class WSTerm {
    public enum Type {
        Float,
        Integer,
        String,
        Any,
        Bool,
        Array,
        Object,
        Function
    }
    public abstract Type getType();
    public static final class Float extends WSTerm {
        float value;
        public Float(float value) {
            this.value = value;
        }
        @Override
        public Type getType() {
            return Type.Float;
        }
    }
    public static final class Integer extends WSTerm {
        int value;
        public Integer(int value) {
            this.value = value;
        }
        @Override
        public Type getType() {
            return Type.Integer;
        }
    }
    public static final class String extends WSTerm {
        java.lang.String value;
        public String(java.lang.String value) {
            this.value = value;
        }
        @Override
        public Type getType() {
            return Type.String;
        }
    }
    public static final class Any extends WSTerm {
        Object value;
        public Any(Object value) {
            this.value = value;
        }
        @Override
        public Type getType() {
            return Type.Any;
        }
    }
    public static final class Bool extends WSTerm {
        boolean value;
        public Bool(boolean value) {
            this.value = value;
        }
        @Override
        public Type getType() {
            return Type.Bool;
        }
    }
    public static final class Array extends WSTerm {
        Stream<WSAction> array;
        public Array(Stream<WSAction> array) {
            this.array = array;
        }
        @Override
        public Type getType() {
            return Type.Array;
        }
    }
    public static final class Object extends WSTerm {
        Map<java.lang.String, WSAction> object;
        public Object(Stream<Pair<java.lang.String, WSAction>> mappings) {
            this.object = mappings.collect(Collectors.toMap(pair -> pair.getKey(), pair -> pair.getValue()));
        }
        @Override
        public Type getType() {
            return Type.Object;
        }
    }
    public static final class Function extends WSTerm {
        Stream<java.lang.String> args;
        WSAction action;
        public Function(Stream<java.lang.String> args, WSAction action) {
            this.args = args;
            this.action = action;
        }
        @Override
        public Type getType() {
            return Type.Function;
        }
    }
}
