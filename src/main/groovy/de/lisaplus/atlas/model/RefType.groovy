package de.lisaplus.atlas.model
/**
 * Type used in meta model
 * Created by eiko on 01.06.17.
 */
class RefType extends BaseType {
    def typeName
    Type type

    String name () {
        return NAME
    }
    final static NAME='REF'
}

