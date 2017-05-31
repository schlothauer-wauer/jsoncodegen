package de.lisaplus.atlas.model

import org.apache.commons.lang3.builder.ToStringBuilder

/**
 * Created by eiko on 31.05.17.
 */
class Model {
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

    def debugPrint() {
        println ToStringBuilder.reflectionToString(this);
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
}

enum PropertyType {
    t_int,
    t_string,
    t_key,
    t_boolean,
    t_date,
    t_date_time,
    t_array
}

class Property {
    def description
    def name
    PropertyType type
    def format
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
