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

class Type {
    /**
     * name for that type.
     * Genarally build form JSON schema. For single type schemas first normalized content of title field is used. If
     * ther is not title entry file name is used for type name.
     * In multi type schemas key unter definitions section is used as type name
     */
    String name

    /**
     * for Stephan :)
     */
    String color='#000000'

    /**
     * List of properties, type of PropertyType
     */
    List<Property> properties=[]
    def description

    /**
     *  List of required properties, String list with property names
     */
    List<String> requiredProps=[];

    /**
     * List of inheritance base types - currently only used for nice plantuml diagramms
     * Inheritance is not real implemented. attributes of a base Type are simply copied to the
     * derived Type. But sometime the usage of the term inheritance simplify communication
     */
    List<String> baseTypes=[]

    /**
     * since when is the type part of the model
     */
    int sinceVersion

    /**
     * types that reference this type
     */
    List<Type> refOwner=[]

    /**
     * true if the current type is only used as base type
     */
    boolean onlyBaseType=false

    /**
     * array of free definable strings to add keywords to types and attributes.
     * This keywords can be used to select or deselect types or attributes while code generation
     */
    List<String> tags=[]

    String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    void initFromType (Type t) {
        this.name = t.name
        this.tags = t.tags
        this.properties = t.properties
        this.description = t.description
        this.requiredProps = t.requiredProps
        this.sinceVersion = t.sinceVersion
    }

    boolean equals(Object b) {
        if (! b instanceof Type) return false
        return name==b.name
    }

    boolean isInnerType() {
        return this instanceof InnerType
    }

    boolean hasTag(String tag) {
        return tags && tags.contains(tag)
    }

    boolean hasPropertyWithTag(String tag) {
        if (!tags) return false
        if (!properties) return false
        return properties.find { prop ->
            return prop.tags && prop.tags.contains(tag)
        } != null
    }
}

/**
 * This type is used to handle the use of schema types before they are declared in a schema.
 * This could happen with references
 */
class DummyType  extends Type {
    /**
     * List of RefType objects. After the real Type is created, it's needed to set the right references
     */
    def referencesToChange=[]
}

/**
 * this type is for inner declarations of complex types
 */
class InnerType extends Type {
}

/**
 * this type describes an external type
 */
class ExternalType extends Type {
    String refStr
}

enum AggregationType {
    aggregation, composition
}

class Property {
    def description
    def name
    def format

    /**
     * For entries that points to a global uuid, this flag enable the explizit relation to connected type
     */
    BaseType implicitRef
    /**
     * marks the property how it should implemented, as reference or as object
     */
    AggregationType aggregationType=AggregationType.composition
    /**
     * Type of property field, covers also if the property is an array
     */
    BaseType type
    /**
     * since when is the type part of the model
     */
    int sinceVersion

    /**
     * array of free definable strings to add keywords to types and attributes.
     * This keywords can be used to select or deselect types or attributes while code generation
     */
    List<String> tags=[]


    String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    boolean isRefTypeOrComplexType() {
        return type && ( type instanceof RefType || type instanceof ComplexType )
    }

    boolean isRefType() {
        return type && ( type instanceof RefType)
    }

    boolean isComplexType() {
        return type && ( type instanceof ComplexType )
    }

    boolean implicitRefIsRefType() {
        return implicitRef && ( implicitRef instanceof RefType)
    }

    boolean implicitRefIsComplexType() {
        return implicitRef && ( implicitRef instanceof ComplexType )
    }

    boolean isAggregation() {
        return aggregationType==AggregationType.aggregation
    }

    boolean hasTag(String tag) {
      return tags && tags.contains(tag)
    }
}