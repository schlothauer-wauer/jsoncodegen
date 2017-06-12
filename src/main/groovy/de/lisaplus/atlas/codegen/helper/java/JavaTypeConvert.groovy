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
import de.lisaplus.atlas.model.UnsupportedType

/**
 * Converts meta model types to Java types
 * Created by eiko on 11.06.17.
 */
class JavaTypeConvert {
    static def convert = { type ->
        if (! type instanceof BaseType) {
            return BaseType.WRONG_TYPE+type
        }
        switch(type.name()) {
            case IntType.NAME:
                return 'Integer'
            case NumberType.NAME:
                return 'Double'
            case StringType.NAME:
                return 'String'
            case BooleanType.NAME:
                return 'Boolean'
            case DateType.NAME:
                return 'Date'
            case DateTimeType.NAME:
                return 'Date'
            case RefType.NAME:
                def typeName = type.type.name
                return typeName
            case ComplexType.NAME:
                def typeName = type.type.name
                return typeName
            case UnsupportedType.NAME:
                return BaseType.UNSUPPORTED_TYPE+type
            break
        default:
            return BaseType.UNKNOWN_TYPE+type
        }
    }
}
