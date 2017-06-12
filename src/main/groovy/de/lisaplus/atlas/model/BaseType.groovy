package de.lisaplus.atlas.model
/**
 * Type used in meta model
 * Created by eiko on 01.06.17.
 */
abstract class BaseType {
    boolean isArray=false
    final static String WRONG_TYPE = 'CODEGEN-ERROR: Used type needs to derived from de.lisaplus.atlas.model.BaseType: '
    final static String UNKNOWN_TYPE = 'CODEGEN-ERROR: Unknown type: '
    final static String UNSUPPORTED_TYPE = 'CODEGEN-ERROR: Unsupported type: '

    abstract String name()
}

abstract class MinMaxType extends BaseType {
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
    String name () {
        return NAME
    }
    final static NAME='UNSUPPORTED'
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

class StringType extends BaseType {
    def maxLength
    def minLength
    def pattern

    String name () {
        return NAME
    }
    final static NAME='STRING'
}

class RefType extends BaseType {
    def typeName
    Type type

    String name () {
        return NAME
    }
    final static NAME='REF'
}

class BooleanType extends BaseType {
    String name () {
        return NAME
    }
    final static NAME='BOOLEAN'
}

class DateType extends MinMaxType {
    String name () {
        return NAME
    }
    final static NAME='DATE'
}

class DateTimeType extends MinMaxType {
    String name () {
        return NAME
    }
    final static NAME='DATETIME'
}

class ComplexType extends BaseType {
    Type type
    String name () {
        return NAME
    }
    final static NAME='COMPLEX'
}
