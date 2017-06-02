package de.lisaplus.atlas.model
/**
 * Created by eiko on 01.06.17.
 */
class BaseType {
    boolean isArray=false
}

class MinMaxType extends BaseType {
    def max
    def exclusiveMax
    def min
    def exclusiveMin
}

/**
 * this type is choosen in too complex schemas
 * for instance: use of patternPoperties
 */
class UnsupportedType extends BaseType {
}

class IntType extends MinMaxType {
}

class NumberType extends MinMaxType {
}

class StringType extends BaseType {
    def maxLength
    def minLength
    def pattern
}

class RefType extends BaseType {
    def typeName
    Type type
}

class BooleanType extends BaseType {
}

class DateType extends MinMaxType {
}

class DateTimeType extends MinMaxType {
}

class ComplexType extends BaseType {
    Type type
}
