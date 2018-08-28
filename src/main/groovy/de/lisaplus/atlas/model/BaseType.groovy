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

    /**
     * This attribute is only used in case of XSD imports to avoid the loose of additional type information, f.e. double, float
     */
    String originalType=null

    abstract String name()
}

class VoidType extends BaseType {
    String name () {
        return NAME
    }
    final static NAME='VOID'
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

class StringType extends BaseType {
    def maxLength
    def minLength
    def pattern

    String name () {
        return NAME
    }
    final static NAME='STRING'
}

class UUIDType extends BaseType {
    String name () {
        return NAME
    }
    final static NAME='UUID'
}

class BooleanType extends BaseType {
    String name () {
        return NAME
    }
    final static NAME='BOOLEAN'
}
