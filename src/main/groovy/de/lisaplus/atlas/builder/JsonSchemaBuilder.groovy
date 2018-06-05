package de.lisaplus.atlas.builder

import de.lisaplus.atlas.codegen.helper.java.TypeToColor
import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.AggregationType
import de.lisaplus.atlas.model.BaseType
import de.lisaplus.atlas.model.BooleanType
import de.lisaplus.atlas.model.ComplexType
import de.lisaplus.atlas.model.DateTimeType
import de.lisaplus.atlas.model.DateType
import de.lisaplus.atlas.model.DummyType
import de.lisaplus.atlas.model.ExternalType
import de.lisaplus.atlas.model.InnerType
import de.lisaplus.atlas.model.IntType
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.NumberType
import de.lisaplus.atlas.model.Property
import de.lisaplus.atlas.model.RefType
import de.lisaplus.atlas.model.StringType
import de.lisaplus.atlas.model.Type
import de.lisaplus.atlas.model.UUIDType
import de.lisaplus.atlas.model.UnsupportedType
import de.lisaplus.atlas.model.VoidType
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static de.lisaplus.atlas.builder.helper.BuildHelper.strFromMap
import static de.lisaplus.atlas.builder.helper.BuildHelper.string2Name
import static de.lisaplus.atlas.builder.helper.BuildHelper.makeCamelCase

/**
 * Creates meta model from JSON schema
 * Created by eiko on 01.06.17.
 */
class JsonSchemaBuilder implements IModelBuilder {
    /**
     * Container for all created types helps - makes reference handling easier
     */
    def createdTypes=[:]

    /**
     * Container for type from external schemas
     */
    Map<String,ExternalType> externalTypes = [:]

    /**
     * builds a meta model from a model files
     * @param modelFile
     * @return
     */
    Model buildModel(File modelFile) {
        def jsonSlurper = new JsonSlurper()
        def objectModel = jsonSlurper.parse(modelFile)
        if (!objectModel['$schema']) {
            def errorMsg='model file seems to be no JSON schema'
            log.error(errorMsg)
            throw new Exception(errorMsg)
        }
        String currentSchemaPath = getBasePathFromModelFile(modelFile)
        Model model = initModel(objectModel)
        if (objectModel['definitions']) {
            // multi type schema
            loadSchemaTypes(objectModel,currentSchemaPath,model)
        }
        if (objectModel['allOf']) {
            // single type schema
            return modelFromSingeTypeSchema(objectModel,modelFile.getName(),currentSchemaPath,model)
        }
        else if (objectModel['properties']) {
            // single type schema
            return modelFromSingeTypeSchema(objectModel,modelFile.getName(),currentSchemaPath,model)
        }

        if (!model.types) {
            def errorMsg='unknown schema structure'
            log.error(errorMsg)
            throw new Exception(errorMsg)
        }
        // test all existing models if they are used only as base types
        model.markOnlyBaseTypes()
        return model
    }

    static String getBasePathFromModelFile(modelFile) {
        def path = modelFile.getPath()
        def name = modelFile.getName()
        def index = path.indexOf(name)
        return path.substring(0,index)
    }

    private Model modelFromSingeTypeSchema(def objectModel, String modelFileName,String currentSchemaPath,Model model) {
        if (model==null)
            model = initModel(objectModel)
        def typeName = strFromMap(objectModel,'title')
        if (!typeName) {
            int lastDot = modelFileName.lastIndexOf('.')
            if (lastDot==-1) {
                typeName = modelFileName
            }
            else {
                typeName = modelFileName.substring(0,lastDot)
            }
        }
        typeName = string2Name(typeName)
        Type newType = new Type()
        newType.name = typeName
        newType.description = strFromMap(objectModel,'description')
        if (objectModel.allOf) {
            // TODO - add allof Properties
            objectModel.allOf.each { allOfElem ->
                if (allOfElem.properties) {
                    newType.properties.addAll(getProperties(model,allOfElem,typeName,currentSchemaPath))
                }
                else {
                    if (allOfElem.'$ref') {
                        RefType tmp = initRefType(allOfElem.'$ref',currentSchemaPath)
                        newType.properties.addAll(tmp.type.properties)
                        newType.baseTypes.add(tmp.type.name)
                    }
                }
            }
        }
        else {
            newType.properties = getProperties(model,objectModel,typeName,currentSchemaPath)
        }
        // TODO initialize extra stuff
        addNewType(newType,model)
        addExternalTypesToModel(model)
        model.initRefOwnerForTypes()
        model.checkModelForErrors()
        return model
    }

    private addExternalTypesToModel(Model model) {
        externalTypes.each { typeObj ->
            if (!model.types.contains(typeObj.value)) {
                TypeToColor.setColor(typeObj.value)
                model.types.add(typeObj.value)
            }
        }
    }
    private addModelTypesToExternal(Model modelSrc) {
        modelSrc.types.each { type ->
            if (!externalTypes[type.name]) {
                externalTypes[type.name] = type
            }
        }
    }


//    private Model modelFromMultiTypeSchema(def objectModel,String currentSchemaPath) {
      private Model loadSchemaTypes(def objectModel,String currentSchemaPath,Model model) {
//        Model model = initModel(objectModel)
        if (model==null)
            model = initModel(objectModel)
        objectModel.definitions.each { typeObj ->
            def typeName = string2Name(typeObj.key)
            Type newType = new Type()
            newType.name = typeName
            newType.description = strFromMap(typeObj.value,'description')
            newType.properties = []
            if (typeObj.value.allOf) {
                // TODO - add allof Properties
                typeObj.value.allOf.each { allOfElem ->
                    if (allOfElem.properties) {
                        newType.properties.addAll(getProperties(model,allOfElem,typeName,currentSchemaPath))
                    }
                    else {
                        if (allOfElem.'$ref') {
                            RefType tmp = initRefType(allOfElem.'$ref',currentSchemaPath)
                            newType.properties.addAll(tmp.type.properties)
                            newType.baseTypes.add(tmp.type.name)
                        }
                    }
                }
            }
            else if (typeObj.value.'$ref') {
                // this type refers to an external definition
                RefType tmp = initRefType(typeObj.value.'$ref',currentSchemaPath)
                newType.properties.addAll(tmp.type.properties)
                newType.baseTypes.add(tmp.type.name)
            }
            else {
                newType.properties = getProperties(model,typeObj.value,typeName,currentSchemaPath)
            }
            if (typeObj.value.'__tags') {
                newType.tags=typeObj.value.'__tags'
            }
            // TODO  initialize extra stuff
            addNewType(newType,model)
        }
        addExternalTypesToModel(model)
        model.initRefOwnerForTypes()
        model.checkModelForErrors()
        return model
    }

    /**
     * wraps the append of a new type to a model, this function checks for double types
     * @param newType
     * @param model
     */
    private void addNewType(Type newType, def model) {
        def typeName = newType.name
        def alreadyCreated = createdTypes[typeName]
        if (alreadyCreated) {
            if (alreadyCreated instanceof DummyType) {
                // handle forward usage of types in declarations ... references need to be updated
                alreadyCreated.referencesToChange.each { refType ->
                    refType.type = newType
                    refType.typeName = newType.name
                }
            }
            else {
                if (propertiesDontMatch(alreadyCreated.properties, newType.properties)) {
                    def errorMsg = "schema contains dulplicate type: ${typeName}"
                    log.error(errorMsg)
                    throw new Exception(errorMsg)
                }
                else
                    return
            }
        }
        TypeToColor.setColor(newType)
        createdTypes[newType.name] = newType
        model.types.add(newType)
    }

    private List<Property> getProperties(Model model,def propertyParent,def parentName,String currentSchemaPath) {
        List<Property> propList = []
        propertyParent.properties.each { propObj ->
            propList.add(creeateSimpleProperty(model, parentName,currentSchemaPath,propObj))
        }
        return propList
    }

    private Property creeateSimpleProperty (Model model, def parentName,String currentSchemaPath,def propObj) {
        def newProp = new Property()
        newProp.name = string2Name(propObj.key, false)
        newProp.description = propObj.value['description']
        String key = makeCamelCase(propObj.key)
        newProp.type = getPropertyType(model, propObj.value, parentName + string2Name(key), currentSchemaPath)
        if (newProp.type instanceof RefType) {
            if (propObj.key.toLowerCase().endsWith('_id')) {
                newProp.aggregationType = AggregationType.aggregation
            } else {
                /**
                 * additional extension of JSON schema ... property attribute 'aggregationType'
                 */
                if (propObj.value.'__aggregationType') {
                    if (propObj.value.__aggregationType.toLowerCase() == 'aggregation') {
                        newProp.aggregationType = AggregationType.aggregation
                    } else {
                        newProp.aggregationType = AggregationType.composition
                    }
                } else
                    newProp.aggregationType = AggregationType.composition
            }
        }
        // implizit refs for normal types and array types differ
        if (newProp.type.isArray) {
            if (propObj.value.items.'__ref') {
                newProp.implicitRef = initRefType(propObj.value.items.'__ref', currentSchemaPath)
            }
        }
        else {
            if (propObj.value.'__ref') {
                newProp.implicitRef = initRefType(propObj.value.'__ref', currentSchemaPath)
            }
        }
        if (propObj.value.'__ref') {
            newProp.implicitRef = initRefType(propObj.value.'__ref', currentSchemaPath)
        }
        if (propObj.value.'__tags') {
            newProp.tags=propObj.value.'__tags'
        }
        return newProp
    }

    private BaseType getPropertyType(Model model,def propObjMap,def innerTypeBaseName,String currentSchemaPath) {
        if (propObjMap.'$ref') {
            // reference to an external type
            return initRefType(propObjMap.'$ref',currentSchemaPath)
        }
        else if (! propObjMap.type) {
            def errorMsg = "property object w/o any type: ${propObjMap}"
            log.error(errorMsg)
            throw new Exception(errorMsg)
        }
        else {
            return getBaseTypeFromString(model,currentSchemaPath,propObjMap,innerTypeBaseName)
        }
    }

    private RefType initRefType(def refStr,String currentSchemaPath) {
        if (!refStr) {
            def errorMsg = "undefined refStr, so cancel init reference type"
            log.error(errorMsg)
            throw new Exception(errorMsg)
        }
        RefType refType = new RefType()
        // Examples:
        // "$ref": "#/definitions/command"
        // "$ref": "definitions.json#/address"
        // "$ref": "http: //json-schema.org/geo" - HTTP not supported (eiko)
        def localDefStrBase = '#/definitions/'
        if (refStr.startsWith(localDefStrBase)) {
            def schemaTypeName = refStr.substring(localDefStrBase.length())
            Type t = getLocalRefType(schemaTypeName)
            if (t instanceof DummyType) {
                // the needed type isn't already in the model created. later a update to the
                // right references is needed
                ((DummyType)t).referencesToChange.add(refType)
            }
            else {
                refType.type=t
                refType.typeName=t.name
            }
        }
        else {
            // "$ref": "definitions.json#/address"
            // "$ref": "http: //json-schema.org/geo" - HTTP not supported (eiko)
            Type t = getExternalRefType(refStr,currentSchemaPath)
            refType.type=t
            refType.typeName=t.name
        }
        return refType
    }


    private Type getExternalRefType(def refStr,String currentSchemaPath) {
        def alreadyLoaded = externalTypes[typeFormRefStr(refStr)]
        if (alreadyLoaded) {
            return alreadyLoaded
        }
        else {
            def indexOfTrenner = refStr.indexOf(EXT_REF_TRENNER)
            // it's needed to avoid StackOverflows in case of self references  (*1)
            ExternalType extT = new ExternalType()
            def tmpTypeName = typeFormRefStr(refStr)
            externalTypes.put(tmpTypeName,extT)
            if (indexOfTrenner != -1) {
                // "$ref": "definitions.json#/address"
                // reference to an external multi type schema
                def fileName = refStr.substring(0,indexOfTrenner)
                Model tmpModel = loadModelFromExternalFile(fileName,refStr,currentSchemaPath)
                if ((!tmpModel) || (!tmpModel.types)) {
                    throw new Exception("loaded model doesn't contain types")
                }
                def desiredName = refStr.substring(indexOfTrenner+EXT_REF_TRENNER.length()).toLowerCase()
                Type extT2 = null
                tmpModel.types.each { type ->
                    if ((type.name!=null) && (type.name.toLowerCase()==desiredName)) {
                        extT2 = type
                    }
                }
                if (extT2==null) {
                    throw new Exception("can't find external type ${desiredName} in model: ${fileName}")
                }
                else {
                    extT.refStr = refStr
                    extT.initFromType(extT2)
                    // the early declaration is needed to avoid StackOverflow-Errors in case of self references
                    externalTypes.remove(tmpTypeName) // this is maybe a critical point
                    addModelTypesToExternal(tmpModel)
                    return extT
                }
            }
            else {
                // "$ref": "definitions.json"
                // reference to an external single type schema
                def fileName = refStr
                Model tmpModel = loadModelFromExternalFile(fileName,refStr,currentSchemaPath)
                if ((!tmpModel) || (!tmpModel.types)) {
                    throw new Exception("loaded model doesn't contain types")
                }
                // because a single type model can contain multiple inner types
                Type tmpT = tmpModel.types.find {
                    ! (it instanceof InnerType)
                }
                extT.refStr = refStr
                extT.initFromType(tmpT)
                // can be removed, because it's identical to the early init call (*1)
                //externalTypes.put(typeFormRefStr(refStr),extT)
                addModelTypesToExternal(tmpModel)
                return extT
            }
        }
    }

    private String typeFormRefStr(String refStr) {
        def lastSlash = refStr.lastIndexOf('/')
        def tmpStr = lastSlash==-1 ? refStr : refStr.substring(lastSlash+1)
        def lastDot = tmpStr.lastIndexOf('.')
        def tmpStr2 = lastDot==-1 ? tmpStr : tmpStr.substring(0,lastDot)
        return string2Name(tmpStr2)
    }

    private Model loadModelFromExternalFile(String fileName, String refStr,String currentSchemaPath) {
        File modelFile = new File(currentSchemaPath+fileName)
        if (!modelFile.exists()) {
            throw new Exception ("can't find external reference File: ${modelFile.path}, refStr=${refStr}")
        }
        return buildModel(modelFile)
    }

    private Type getLocalRefType(def schemaTypeName) {
        // "$ref": "#/definitions/command"

        if (schemaTypeName.indexOf('/')!=-1) {
            // unsupported, something like: #/definitions/command/xxx
            def errorMsg = "unsupported local reference, types need be located under #/definitions: ${schemaTypeName}"
            log.error(errorMsg)
            throw new Exception(errorMsg)
        }
        def typeName=string2Name(schemaTypeName)
        Type alreadyCreatedType = createdTypes[typeName]
        if (alreadyCreatedType) {
            // the type is created in a earlier parsing step - fine :)
            // ... but it's possible that it is a DummyType
            return alreadyCreatedType
        }
        else {
            // the reference Points to a type that is later created - more complicated :-/
            def newDummy = new DummyType()
            createdTypes[typeName] = newDummy
            return newDummy
        }

    }

    private ComplexType initComplexType(Model model,def propertiesParent,def baseTypeName, String currentSchemaPath) {
        if (!propertiesParent) {
            def errorMsg = "undefined properties map, so cancel init complex type"
            log.error(errorMsg)
            throw new Exception(errorMsg)
        }
        ComplexType complexType = new ComplexType()
        Type newType = new InnerType()
        newType.name = baseTypeName
        newType.properties = getProperties(model,propertiesParent,baseTypeName,currentSchemaPath)
        complexType.type = newType
        addNewType(newType,model)
        return complexType
    }

    private BaseType getBaseTypeFromString(Model model,String currentSchemaPath,def propObjMap, def innerTypeBaseName, def isArrayAllowed=true) {
        switch (propObjMap.type) {
            case 'string':
                if (propObjMap.format && propObjMap.format.toLowerCase()=="uuid") {
                    return new UUIDType()
                }
                else if (propObjMap.format && propObjMap.format.toLowerCase()=="datetime") {
                    // legacy, still present for legacy reasons
                    return new DateTimeType()
                }
                else if (propObjMap.format && propObjMap.format.toLowerCase()=="date-time") {
                    return new DateTimeType()
                }
                else if (propObjMap.format && propObjMap.format.toLowerCase()=="date") {
                    return new DateType()
                }
                else
                    return new StringType()
            case 'integer':
                return new IntType()
            case 'number':
                return new NumberType()
            case 'boolean':
                return new BooleanType()
            case 'object':
                if (propObjMap.patternProperties) {
                    log.warn("unsupported 'patternProperties' entry found")
                    return new UnsupportedType()
                }
                else
                    return initComplexType(model,propObjMap,innerTypeBaseName,currentSchemaPath)
            case 'array':
                if (!isArrayAllowed) {
                    def errorMsg = "detect not allowed sub array type"
                    log.error(errorMsg)
                    throw new Exception(errorMsg)
                }
                if (propObjMap.items.type) {
                    BaseType ret = getBaseTypeFromString(model,currentSchemaPath,propObjMap.items,innerTypeBaseName+'Item',false)
                    ret.isArray = true
                    if (propObjMap.'__tags') {
                        ret.type.tags=propObjMap.'__tags'
                    }
                    return ret
                }
                else if (propObjMap.items['$ref']) {
                    BaseType ret = initRefType(propObjMap.items['$ref'],currentSchemaPath)
                    ret.isArray = true
                    return ret
                }
                else if (propObjMap.items.size()==0) {
                    return new VoidType()
                }
                else {
                    def errorMsg = "unknown array type"
                    log.error(errorMsg)
                    throw new Exception(errorMsg)
                }
            default:
                def errorMsg = "property with unknown type: ${propObjMap.type}"
                log.error(errorMsg)
                throw new Exception(errorMsg)
        }
    }

    private boolean propertiesDontMatch(def propList1, def propList2) {
        if (propList1==null && propList2!=null) {
            return true
        }
        if (propList1!=null && propList2==null) {
            return true
        }
        if (propList1==null && propList2==null) {
            return true
        }
        if (propList1.size()!=propList2.size()) {
            return true
        }
        for (def i=0;i<propList1.size();i++) {
            def prop1 = propList1[i]
            def prop2 = propList2[i]
            if (prop1.type.NAME != prop2.type.NAME) {
                return true
            }
            if (prop1.name != prop2.name) {
                return true
            }
            if (prop1.format != prop2.format) {
                return true
            }
        }
        return false
    }

    private Model initModel(def objectModel) {
        Model model = new Model()
        model.title = strFromMap(objectModel, 'title')
        model.description = strFromMap(objectModel, 'description')

        if (objectModel.properties && objectModel.properties.model_version && objectModel.properties.model_version.enum) {
            objectModel.properties.model_version.enum.each {
                if (!model.version) {
                    model.version = it
                }
            }
        }
        else if (objectModel.version) {
            model.version = objectModel.version
        }
        return model
    }

    private static final String EXT_REF_TRENNER='#/'
    private static final Logger log=LoggerFactory.getLogger(JsonSchemaBuilder.class);
}
