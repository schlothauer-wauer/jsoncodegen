package de.lisaplus.atlas.model

import org.apache.commons.lang3.builder.ToStringBuilder

/**
 * Created by eiko on 31.05.17.
 */
class Model {
    /**
     * defines what format string for schema type string is mapped to what PropertyType
     */
    static def FORMAT_TYPE_MAPPING = {
        date : new DateType()
        date_time : new DateTimeType()
    }
    int version
    def title
    def description
    def fileName

    /**
     * List of type definitions
     */
    List<Type> types=[]

    /**
     * List of required types
     */
    List<String> requiredTypes=[]

    String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * fill for all types the list refOwner with object
     * @param model
     */
    void initRefOwnerForTypes() {
        types.findAll {
            !(it instanceof InnerType) }.each { t ->
                types.findAll {
                    it.name!=t.name && (!(it instanceof InnerType) )}. each { currentType ->
                        currentType.properties.find {
                            it.type instanceof RefType && it.type.type.name==t.name }.each {
                                if (!t.refOwner.contains(currentType)) {
                                    t.refOwner.add(currentType)
                                }
                }
            }
        }
    }

    /**
     * checks whether the model has some errors
     */
    void checkModelForErrors() {
        // TODO
    }

    void markOnlyBaseTypes() {
        def baseTypeNames = []
        // collect baseType names
        types.findAll { type -> return type.baseTypes }.each { type ->
            type.baseTypes.each { baseTypeName ->
                if (!baseTypeNames.contains(baseTypeName)) {
                    baseTypeNames.add(baseTypeName)
                }
            }
        }
        // collect all baseType-names that are also property types
        def foundBaseTypeNames=[]
        types.findAll { type -> return ! baseTypeNames.contains(type.name) }.each { type ->
            // all types their name is no base type name
            type.properties.findAll { prop -> return prop.isRefType() }.each { prop ->
                if (baseTypeNames.contains(prop.type.type.name)) {
                    foundBaseTypeNames.add(prop.type.type.name)
                }
            }
        }

        // mark all types that are not additional property types
        types.findAll { type -> return baseTypeNames.contains(type.name) }.each { type ->
            if (!foundBaseTypeNames.contains(type.name)) {
//                type.onlyBaseType = true
                types.remove(type)
            }
        }
    }
}