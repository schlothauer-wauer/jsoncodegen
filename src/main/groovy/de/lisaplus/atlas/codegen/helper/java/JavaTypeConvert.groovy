package de.lisaplus.atlas.codegen.helper.java

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
                break
            case NumberType.NAME:
                return 'Double'
                break
            case StringType.NAME:
                return 'String'
                break
            case BooleanType.NAME:
                return 'Boolean'
                break
            case DateType.NAME:
                return 'Date'
                break
            case DateTimeType.NAME:
                return 'Date'
                break
            case RefType.NAME:
                // TODO
                break
            case ComplexType.NAME:
                // TODO
                break
            case UnsupportedType.NAME:
            // TODO
            break
        default:
            return BaseType.UNKNOWN_TYPE+type
            // TODO
        }
        // TODO
    }
}
