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
        date : new PropertyTypeCont(PropertyType.t_date)
        date_time : new PropertyTypeCont(PropertyType.t_date_time)
    }

    def title
    def description
    /**
     * List of type definitions
     */
    def types=[]

    /**
     * List of required types
     */
    def requiredTypes=[]

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
    def properties=[]
    def description

    /**
     *  List of required properties, String list with property names
     */
    def requiredProps=[];

    String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

/**
 * simple wrapper around the enum ...
 * is needed because it seems that enums are not castable to objects
 */
class PropertyTypeCont {
    PropertyType type;

    public PropertyTypeCont(PropertyType t) {
        this.type = t;
    }
}

/**
 * Defined model property types
 */
enum PropertyType {
    t_int,
    t_number,
    t_string,
    t_key,
    t_boolean,
    t_date,
    t_date_time,
    t_complex,
    t_array
}

class Property {
    def description
    def name
    def format
    PropertyType type
    Type reference

    String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

class NumProperty extends Property {
    def max
    def exclusiveMax
    def min
    def exclusiveMin
}

class StringProperty extends Property {
    def maxLength
    def minLength
    def pattern
}
