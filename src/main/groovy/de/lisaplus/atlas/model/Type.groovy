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
     * path where the schema for that type was located
     */
    String schemaPath

    /**
     * path where the schema for that type was located
     */
    private String schemaFileName

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
     * defines whether this type is an Enum
     */
    boolean isEnum=false

    /**
     * allowed values in case of Enum
     */
    String[] allowedValues=[]

    /**
     * array of free definable strings to add keywords to types and attributes.
     * This keywords can be used to select or deselect types or attributes while code generation
     */
    List<String> tags=[]

    /**
     * What is the version of a complex or reference type.
     * A "__version" entry in the type is used to populate this
     */
    int version=0

    Type() {}

    /**
     * Initializes the fields of this Type to equal to that of the source
     * @param source The object to copy from
     * @param typeCopies The types, which were already copied (mapping of type name to corresponding Type object)
     */
    void initCopy(Type source, Map<String, Type> typeCopies) {
        this.name = source.name
        this.color = source.color
        this.tags = source.tags == null ? null : new ArrayList<>(source.tags)
        def propCopy = { Property pSource ->
            if (pSource == null)
                return null
            Property copy = new Property()
            copy.initCopy(pSource, typeCopies)
            return copy
        }
        this.properties = source.properties == null ? null : source.properties.collect { p -> /*println "type=${source.name} selfRef=${p.selfReference} prop=${p.name}"; return*/ propCopy.call(p) }
        // Assumes immutable!
        this.description = source.description
        this.requiredProps = source.requiredProps == null ? null : new ArrayList<>(source.requiredProps)
        this.baseTypes = source.baseTypes == null ? null : new ArrayList<>(source.baseTypes)
        this.sinceVersion = source.sinceVersion
        this.refOwner = source.refOwner == null ? null : source.refOwner.collect { owner -> Type.copyOf(owner, typeCopies)}
        this.onlyBaseType = source.onlyBaseType
        this.tags = source.tags== null ? null : new ArrayList<>(source.tags)
        this.isEnum = source.isEnum
        this.allowedValues = source.allowedValues
    }

    String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    void initFromType (Type t) {
        // TODO Check whether incomplete initialization is necessary / intended!
        this.name = t.name
        this.version = t.version
        this.tags = t.tags
        this.schemaPath = t.schemaPath
        this.schemaFileName = t.schemaFileName
        this.properties = t.properties
        this.description = t.description
        this.requiredProps = t.requiredProps
        this.sinceVersion = t.sinceVersion
        this.isEnum = t.isEnum
        this.allowedValues = t.allowedValues
    }

    /**
     *
     * @param model string of the model file name: f.e. junction, junction.json
     */
    boolean isMainType(String model) {
        if (model==null || this.schemaFileName==null || (this instanceof InnerType)) return false
        String mStr = model.indexOf('.')!=-1 ? model.substring(0,model.lastIndexOf('.')) : model
        return (!this.innerType) && this.schemaFileName==mStr
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
     * Reuse existing copies or create new ones if they are missing
     * @param type The type to copy
     * @param typeCopies The types, which were already copied (mapping of type name to the copy of the corresponding Type)
     * @return The copy of the type
     */
    static <T extends Type> T copyOf(T type, Map<String,Type> typeCopies) {
        Objects.requireNonNull(type)
        T copy = (T) typeCopies.get(type.name)
        if (copy == null) {
            // println "Copying ${type.name}"
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

    String getSchemaFileName() {
        return schemaFileName
    }

    void setSchemaFileName(String schemaFileName) {
        this.schemaFileName = schemaFileName
        if (this.schemaFileName!=null) {
            this.schemaFileName = this.schemaFileName.replaceAll('\\.json','')
            this.schemaFileName = this.schemaFileName.replaceAll('\\.xsd','')
            this.schemaFileName = this.schemaFileName.replaceAll('\\.','')
            this.schemaFileName = this.schemaFileName.replaceAll('/','')
        }
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

    @Override
    void initCopy(Type source, Map<String, Type> typeCopies) {
        super.initCopy(source, typeCopies)
        this.refStr = source.refStr
    }
}

