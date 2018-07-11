package de.lisaplus.atlas.codegen.helper.java

import de.lisaplus.atlas.codegen.GeneratorBase
import de.lisaplus.atlas.model.*

/**
 * Converts meta model types to Java types
 * Created by eiko on 11.06.17.
 */
class JavaTypeConvert {

    enum DateTypePreset {
        /** No JSR-310 Date and Time classes, just plain old java.util.Date  */
        LEGACY('java.util.Date', 'java.util.Date'),
        /** Format natural to java-mongo-driver 3.7.1 or later */
        JSR_310_LOCAL('java.time.LocalDate', 'java.time.LocalDateTime'),
        /** Format that preserves the time offset, natural to Jackson */
        JSR_310_OFFSET('java.time.LocalDate', 'java.time.OffsetDateTime'),
        /** Format that preserved the time zone and preferres ZoneId */
        JSR_310_ZONED('java.time.LocalDate', 'java.time.ZonedDateTime');
        
        final String dateClass
        final String dateTimeClass;
        
        private DateTypePreset(dateClass, dateTimeClass) {
            this.dateClass = dateClass
            this.dateTimeClass = dateTimeClass
        }
    }

    static DateTypePreset preset = DateTypePreset.LEGACY;

    static {
        switch (System.getProperty('date.type.preset', 'legacy').toLowerCase()) {
            case 'legacy': preset = DateTypePreset.LEGACY; break
            case '310.local': preset = DateTypePreset.JSR_310_LOCAL; break
            case '310.offset': preset = DateTypePreset.JSR_310_OFFSET; break
            case '310.zoned': preset = DateTypePreset.JSR_310_ZONED; break
        }
    }

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
                return type.isArray? "java.util.List<${preset.dateClass}>" : "${preset.dateClass}"
            case DateTimeType.NAME:
                return type.isArray? "java.util.List<${preset.dateTimeClass}>" : "${preset.dateTimeClass}"
            case RefType.NAME:
                return type.isArray? "java.util.List<${prefix}${firstUpperCamelCase(type.type.name)}>" : "${prefix}${firstUpperCamelCase(type.type.name)}"
            case ComplexType.NAME:
                return type.isArray? "java.util.List<${prefix}${firstUpperCamelCase(type.type.name)}>" : "${prefix}${firstUpperCamelCase(type.type.name)}"
            case UnsupportedType.NAME:
                return BaseType.UNSUPPORTED_TYPE+type
            case VoidType.NAME:
                return 'void'
        default:
            return "${BaseType.UNKNOWN_TYPE}\n$type\n${type.name()}"
        }
    }

    private static firstUpperCase (String str) {
        if (!str) return ''
        def first = str.substring(0,1)
        first = first.toUpperCase()
        if (str.length()>1) {
            def rest = str.substring(1)
            return first + rest
        }
        else {
            return first
        }
    }

    private static String firstUpperCamelCase(String str) {
        if (!str) return ''
        def firstUpper = firstUpperCase(str)
        return convertAllUnderLinesToCamelCase(firstUpper)
    }

    private static String convertAllUnderLinesToCamelCase(String str) {
        if (!str) return ''
        def i_ = str.indexOf('_')
        while (i_!=-1) {
            def stopLen = str.length()-1
            if (i_<stopLen) {
                def nextChar = new String(str.charAt(i_+1))
                if (nextChar=='_') {
                    str = str.replace('__','_')
                }
                else {
                    def nextCharUpper = nextChar.toUpperCase()
                    str = str.replace('_'+nextChar,new String(nextCharUpper))
                }
            }
            else
                break
            i_ = str.indexOf('_',i_)
        }
        return str
    }

}
