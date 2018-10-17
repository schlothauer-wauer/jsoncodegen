package de.lisaplus.atlas.model
/**
 * Type used in meta model
 * Created by eiko on 01.06.17.
 */
class ArrayType extends BaseType {
    BaseType baseType
    String name () {
        return NAME
    }
    final static NAME='ARRAY'
}

