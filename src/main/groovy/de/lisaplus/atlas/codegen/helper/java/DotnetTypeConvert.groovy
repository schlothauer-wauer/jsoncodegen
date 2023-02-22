package de.lisaplus.atlas.codegen.helper.java

import de.lisaplus.atlas.model.*

/**
 * Converts meta model types to Swagger used types
 * Created by eiko on 11.06.17.
 */
class DotnetTypeConvert {
    static def convert = { type ->
        if (! type instanceof BaseType) {
            return BaseType.WRONG_TYPE+type
        }
        switch(type.name()) {
            case IntType.NAME:
                return type.isArray? 'List<int>' : 'int'
            case LongType.NAME:
                return type.isArray? 'List<long>' : 'long'
            case NumberType.NAME:
                return type.isArray? 'List<double>' : 'double'
            case StringType.NAME:
                return type.isArray? 'List<string>' : 'string'
            case UUIDType.NAME:
                return type.isArray? 'List<Guid>' : 'Guid'
            case BooleanType.NAME:
                return type.isArray? 'List<bool>' : 'bool'
            case ByteType.NAME:
                return type.isArray? 'List<byte>' : 'byte'
            case DateType.NAME:
                return type.isArray? 'List<DateTime>' : 'DateTime'
            case DateTimeType.NAME:
                return type.isArray? 'List<DateTime>' : 'DateTime'
            case RefType.NAME:
                return type.type.name
            case ArrayType.NAME:
                return List
            case ComplexType.NAME: // ?
                return type.type.name
            case UnsupportedType.NAME:
                return "unsupported"
        default:
            return "???"
        }
    }

    static def meta = { type ->
        if (! type instanceof BaseType) {
            return BaseType.WRONG_TYPE+type
        }
        switch(type.name()) {
            case IntType.NAME:
                return 'integer'
            case LongType.NAME:
                return 'long'
            case NumberType.NAME:
                return 'number'
            case StringType.NAME:
                return 'string'
            case UUIDType.NAME:
                return 'string/uuid'
            case BooleanType.NAME:
                return 'boolean'
            case ByteType.NAME:
                return 'byte'
            case DateType.NAME:
                return 'string/date'
            case DateTimeType.NAME:
                return 'string/date-time'
            case RefType.NAME:
                return type.type.name
            case ComplexType.NAME: // ?
                return type.type.name
            case UnsupportedType.NAME:
                return "unsupported"
            default:
                return "???"
        }
    }

    static def format = { type ->
        switch(type.name()) {
            case IntType.NAME:
                return 'int64'
            case LongType.NAME:
                return 'int64'
            case NumberType.NAME:
                return 'double'
            case DateType.NAME:
                return 'date'
            case DateTimeType.NAME:
                return 'date-time'
            case UUIDType.NAME:
                return 'uuid'
            case UnsupportedType.NAME:
                return BaseType.UNSUPPORTED_TYPE+type
            default:
                return ''
        }
    }
}
