package de.lisaplus.atlas.codegen.helper.java

import de.lisaplus.atlas.codegen.GeneratorBase
import de.lisaplus.atlas.model.BaseType
import de.lisaplus.atlas.model.BooleanType
import de.lisaplus.atlas.model.ComplexType
import de.lisaplus.atlas.model.DateTimeType
import de.lisaplus.atlas.model.DateType
import de.lisaplus.atlas.model.IntType
import de.lisaplus.atlas.model.NumberType
import de.lisaplus.atlas.model.RefType
import de.lisaplus.atlas.model.StringType
import de.lisaplus.atlas.model.UUIDType
import de.lisaplus.atlas.model.VoidType
import de.lisaplus.atlas.model.UnsupportedType

/**
 * Converts meta model types to Java types
 * Created by eiko on 11.06.17.
 */
class JavaTypeConvert {
    static def convert = { type,prefix = '' ->
        if (! type instanceof BaseType) {
            return BaseType.WRONG_TYPE+type
        }
        switch(type.name()) {
            case IntType.NAME:
                return type.isArray? 'java.util.List<Integer>' : 'Integer'
            case NumberType.NAME:
                return type.isArray? 'java.util.List<Double>' : 'Double'
            case StringType.NAME:
                return type.isArray? 'java.util.List<String>' : 'String'
            case UUIDType.NAME:
                return type.isArray? 'java.util.List<String>' : 'String'
            case BooleanType.NAME:
                return type.isArray? 'java.util.List<Boolean>' : 'Boolean'
            case DateType.NAME:
                return type.isArray? 'java.util.List<Date>' : 'java.util.Date'
            case DateTimeType.NAME:
                return type.isArray? 'java.util.List<Date>' : 'java.util.Date'
            case RefType.NAME:
                return type.isArray? "java.util.List<${prefix}${type.type.name}>" : "${prefix}${type.type.name}"
            case ComplexType.NAME:
                return type.isArray? "java.util.List<${prefix}${type.type.name}>" : "${prefix}${type.type.name}"
            case UnsupportedType.NAME:
                return BaseType.UNSUPPORTED_TYPE+type
            case VoidType.NAME:
                return 'void'
        default:
            return "${BaseType.UNKNOWN_TYPE}\n$type\n${type.name()}"
        }
    }
}
