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

    def title
    def description
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
}

class Type {
    /**
     * name for that type.
     * Genarally build form JSON schema. For single type schemas first normalized content of title field is used. If
     * ther is not title entry file name is used for type name.
     * In multi type schemas key unter definitions section is used as type name
     */
    def name
    /**
     * List of properties, type of PropertyType
     */
    List<Property> properties=[]
    def description

    /**
     *  List of required properties, String list with property names
     */
    List<String> requiredProps=[];

    String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

class Property {
    def description
    def name
    def format
    /**
     * Type of property field, covers also if the property is an array
     */
    BaseType type

    String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}