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

    /**
     * Copy constructor
     * @param type The object to copy from
     * @deprecated To avoid issues with circles in the type hierarchy use #initCopy(Property, Map)
     */
    @Deprecated
    Type(Type type) {
        this.name = type.name
        this.color = type.color
        this.tags = type.tags == null ? null : new ArrayList<>(type.tags)
        this.properties = type.properties == null ? null : type.properties.collect { p -> println "type=${type.name} selfRef=${p.selfReference} prop=${p.name}"; return new Property(p) }
        // Assumes immutable!
        this.description = type.description
        this.requiredProps = type.requiredProps == null ? null : new ArrayList<>(type.requiredProps)
        this.baseTypes = type.baseTypes == null ? null : new ArrayList<>(type.baseTypes)
        this.sinceVersion = type.sinceVersion
        // In case of cycles we have to switch to deep cloning using serialization!
        this.refOwner = type.refOwner == null ? null : new ArrayList<>(type.refOwner) // type.refOwner.collect { owner -> Type.copyOf(owner)} // triggers loop!
        this.onlyBaseType = type.onlyBaseType
        this.tags = type.tags== null ? null : new ArrayList<>(type.tags)
    }

    /**
     * Initializes the fields of this Type to equal to that of the source
     * @param source The object to copy from
     * @param typeCopies The types, which were already copied (mapping of type name to corresponding Type object)
     */
    void initCopy(Type source, Map<String, Type> typeCopies) {
        this.name = source.name
        this.color = source.color
        this.tags = source.tags == null ? null : new ArrayList<>(source.tags)
        /* version 1 and 2
        this.properties = type.properties == null ? null : type.properties.collect { p -> println "type=${type.name} selfRef=${p.selfReference} prop=${p.name}"; return new Property(p) }
        */
        // version 3
        def propCopy = { Property pSource ->
            if (pSource == null)
                return pSource
            Property copy = new Property()
            copy.initCopy(pSource, typeCopies)
            return copy
        }
        this.properties = source.properties == null ? null : source.properties.collect { p -> println "type=${source.name} selfRef=${p.selfReference} prop=${p.name}"; return propCopy.call(p) }
        // Assumes immutable!
        this.description = source.description
        this.requiredProps = source.requiredProps == null ? null : new ArrayList<>(source.requiredProps)
        this.baseTypes = source.baseTypes == null ? null : new ArrayList<>(source.baseTypes)
        this.sinceVersion = source.sinceVersion
        /* version 1 and 2
        // In case of cycles we have to switch to deep cloning using serialization!
        this.refOwner = type.refOwner == null ? null : new ArrayList<>(type.refOwner) // type.refOwner.collect { owner -> Type.copyOf(owner)} triggers loop!
        */
        // version 3
        this.refOwner = source.refOwner == null ? null : source.refOwner.collect { owner -> Type.copyOf(owner, typeCopies)}
        this.onlyBaseType = source.onlyBaseType
        this.tags = source.tags== null ? null : new ArrayList<>(source.tags)
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

    /**
     * @deprecated o avoid issues with circles in the type hierarchy use Type#copyOf(T,Map)
     */
    @Deprecated
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
                println "Copying ${type.name}"
                return new Type(type)
            default:
                throw new RuntimeException("Add handling for type ${type.class}")
        }
    }

    /**
     * Reuse existing copies or create new ones if they are missing
     * @param type The type to copy
     * @param typeCopies The types, which were already copied (mapping of type name to the copy of the corresponding Type)
     * @return The copy of the type
     */
    static <T extends Type> T copyOf(T type, Map<String,Type> typeCopies) {
        Objects.requireNonNull(type)
        T copy = (T) typeCopies.get(type.name)
        if (copy == null) {
            switch (type) {
                case DummyType:
                    copy = DummyType()
                    break
                case ExternalType:
                    copy = new ExternalType()
                    break
                case InnerType:
                    copy = new InnerType()
                    break
                case Type:
                    println "Copying ${type.name}"
                    copy = new Type()
                    break
                default:
                    throw new RuntimeException("Add handling for type ${type.class}")
            }
            typeCopies.put(type.name, copy)
            copy.initCopy(type, typeCopies)
        }
        return copy
    }
}

/**
 * This type is used to handle the use of schema types before they are declared in a schema.
 * This could happen with references
 */
class DummyType extends Type {
    /**
     * List of BaseType or RefType objects. After the real Type is created, it's needed to set the right references
     */
    List<BaseType> referencesToChange=[]

    DummyType() {
        super()
    }

    /**
     * Copy constructor
     * @param type The object to copy from
     * @deprecated To avoid issues with circles in the type hierarchy use #initCopy(Property, Map)
     */
    @Deprecated
    DummyType(DummyType type) {
        super(type)
        referencesToChange = type.referencesToChange == null ? null : type.referencesToChange.collect { ref -> BaseType.copyOf(ref) }
    }

    @Override
    void initCopy(Type source, Map<String, Type> typeCopies) {
        super.initCopy(source, typeCopies)
        referencesToChange = source.referencesToChange == null ? null : source.referencesToChange.collect { ref -> BaseType.copyOf(ref, typeCopies) }
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

    /**
     * Copy constructor
     * @param type The object to copy from
     * @deprecated To avoid issues with circles in the type hierarchy use #initCopy(Property, Map)
     */
    @Deprecated
    ExternalType(ExternalType type) {
        super(type)
        refStr = type.refStr
    }

    @Override
    void initCopy(Type source, Map<String, Type> typeCopies) {
        super.initCopy(source, typeCopies)
        this.refStr = source.refStr
    }
}
