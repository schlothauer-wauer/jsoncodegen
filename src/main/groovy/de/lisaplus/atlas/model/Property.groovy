package de.lisaplus.atlas.model

import org.apache.commons.lang3.builder.ToStringBuilder

class Property {
    def description
    def name
    def format

    /**
     * For entries that points to a global uuid, this flag enable the explicit relation to connected type
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

    /** true if prop contains object of same type as property host */
    boolean selfContainment=false

    /** true if prop is implicit reference of same type as property host */
    boolean selfReference=false

    /**
     * array of free definable strings to add keywords to types and attributes.
     * This keywords can be used to select or deselect types or attributes while code generation
     */
    List<String> tags=[]

    /**
     * Initializes the fields of this Property to equal to that of the source
     * @param source The object to copy from
     * @param typeCopies The types, which were already copied (mapping of type name to the copy of the corresponding Type)
     */
    void initCopy(Property source, Map<String, Type> typeCopies) {
        // Assume Strings / immutable
        description = source.description
        name = source.name
        format = source.format
        if (source.implicitRef == null) {
            implicitRef = null
        } else {
            // reuse existing copies or create new ones if they are missing
            implicitRef = BaseType.copyOf(source.type, typeCopies)
        }
        aggregationType = source.aggregationType
        type = source.type == null ? null : BaseType.copyOf(source.type, typeCopies)

        sinceVersion = source.sinceVersion
        selfContainment = source.selfContainment
        selfReference = source.selfReference
        tags = source.tags==null ? null : new ArrayList<>(source.tags)
    }

    String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    boolean isRefTypeOrComplexType() {
        return type && ( type instanceof RefType || type instanceof ComplexType )
    }

    boolean isRefType() {
        return type && ( type instanceof RefType)
    }

    boolean isArrayType() {
        return type && ( type instanceof ArrayType)
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

    /**
     * The default value defined in the tags, or <i>null</i>, if none was defined.
     * The default value tag <i>defaultTrue</i> is returned as <i>True</i>!
     */
    String getDefaultValue() {
        def value = null
        if (tags) {
            value = tags.find {it =~ 'default.*'}?.drop(7)
        }
        return value
    }
}
