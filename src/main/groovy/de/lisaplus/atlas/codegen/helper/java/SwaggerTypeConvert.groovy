package de.lisaplus.atlas.codegen.helper.java

import de.lisaplus.atlas.model.*

/**
 * Converts meta model types to Swagger used types
 * Created by eiko on 11.06.17.
 */
class SwaggerTypeConvert {
    static def convert = { type ->
        if (! type instanceof BaseType) {
            return BaseType.WRONG_TYPE+type
        }
        switch(type.name()) {
            case IntType.NAME:
                return 'integer'
            case NumberType.NAME:
                return 'number'
            case StringType.NAME:
                return 'string'
            case UUIDType.NAME:
                return 'string'
            case BooleanType.NAME:
                return 'boolean'
            case DateType.NAME:
                return 'string'
            case DateTimeType.NAME:
                return 'string'
            case RefType.NAME:
                return type.type.name
            case ComplexType.NAME:
                return type.type.name
            case UnsupportedType.NAME:
                return BaseType.UNSUPPORTED_TYPE+type
        default:
            return BaseType.UNKNOWN_TYPE+type
        }
    }

    static def format = { type ->
        switch(type.name()) {
            case IntType.NAME:
                return 'int64'
            case NumberType.NAME:
                return 'double'
            case DateType.NAME:
                return 'date'
            case DateTimeType.NAME:
                return 'date-time'
            case UnsupportedType.NAME:
                return BaseType.UNSUPPORTED_TYPE+type
            default:
                return ''
        }
    }
}
