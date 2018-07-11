package de.lisaplus.atlas.model;
/**
 * Type used in meta model
 * Created by eiko on 01.06.17.
 */
abstract class MinMaxType extends BaseType {
    def max
    def exclusiveMax
    def min
    def exclusiveMin
}

class IntType extends MinMaxType {
    String name () {
        return NAME
    }
    final static NAME='INT'
}

class NumberType extends MinMaxType {
    String name () {
        return NAME
    }
    final static NAME='NUMBER'
}
