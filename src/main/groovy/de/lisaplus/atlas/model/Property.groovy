package de.lisaplus.atlas.model

import org.apache.commons.lang3.builder.ToStringBuilder

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
