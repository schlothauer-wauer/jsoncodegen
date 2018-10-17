package de.lisaplus.atlas.model

import org.apache.commons.lang3.builder.ToStringBuilder

/**
 * Type used in meta model
 * Created by eiko on 01.06.17.
 */
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
    List<String> requiredProps=[]

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

    Type() {}

    Type(Type type) {
        this.name = type.name
        this.color = type.color
        this.tags = type.tags == null ? null : new ArrayList<>(type.tags)
        this.properties = type.properties == null ? null : type.properties.collect { p -> new Property(p) }
        // Assumes immutable!
        this.description = type.description
        this.requiredProps = type.requiredProps == null ? null : new ArrayList<>(type.requiredProps)
        this.baseTypes = type.baseTypes == null ? null : new ArrayList<>(type.baseTypes)
        this.sinceVersion = type.sinceVersion
        // In case of cycles we have to switch to deep cloning using serialization!
        this.refOwner = type.refOwner == null ? null : type.refOwner.collect { owner -> Type.copyOf(owner)}
        this.onlyBaseType = type.onlyBaseType
        this.tags = type.tags== null ? null : new ArrayList<>(type.tags)
    }

    String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    void initFromType (Type t) {
        // TODO Check whether incomplete initialization is necessary / intended!
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

    boolean hasPropertyWithName(String name) {
        if (!properties) return false
        return properties.find { prop ->
            return name == prop.name
        } != null
    }

    static <T extends Type> T copyOf(T type) {
        Objects.requireNonNull(type)
        switch (type) {
            case DummyType:
                return new DummyType(type)
            case ExternalType:
                return new ExternalType(type)
            case InnerType:
                return new InnerType(type)
            case Type:
                return new Type(type)
            default:
                throw new RuntimeException("Add handling for type ${type.class}")
        }
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
    // List<BaseType> referencesToChange=[]

    DummyType() {
        super()
    }

    DummyType(DummyType type) {
        super(type)
        referencesToChange = type.referencesToChange == null ? null : type.referencesToChange.collect { ref -> ref instanceof BaseType ? BaseType.copyOf(ref) : ref }
        // referencesToChange = type.referencesToChange == null ? null : type.referencesToChange.collect { ref -> BaseType.copyOf(ref) }
    }
}

/**
 * this type describes an external type
 */
class ExternalType extends Type {
    String refStr

    ExternalType() {
        super()
    }

    ExternalType(ExternalType type) {
        super(type)
        refStr = type.refStr
    }
}
