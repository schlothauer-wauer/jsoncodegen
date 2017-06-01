package de.lisaplus.atlas.builder

import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Property
import de.lisaplus.atlas.model.Type
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static de.lisaplus.atlas.builder.helper.BuildHelper.listFromMap
import static de.lisaplus.atlas.builder.helper.BuildHelper.strFromMap
import static de.lisaplus.atlas.builder.helper.BuildHelper.string2Name

/**
 * Created by eiko on 01.06.17.
 */
class JsonSchemaBuilder {
    private Model buildModel(File modelFile) {
        def jsonSlurper = new JsonSlurper()
        def objectModel = jsonSlurper.parse(modelFile)
        if (!objectModel['$schema']) {
            def errorMsg='model file seems to be no JSON schema'
            log.error(errorMsg)
            throw new Exception(errorMsg)
        }
        if (objectModel['properties']) {
            // single type schema
            return modelFromSingeTypeSchema(objectModel,modelFile.getName())
        }
        else if (objectModel['definitions']) {
            // multi type schema
            return modelFromMultiTypeSchema(objectModel)
        }
        else {
            def errorMsg='unknown schema structure'
            log.error(errorMsg)
            throw new Exception(errorMsg)
        }
    }

    private Model modelFromSingeTypeSchema(def objectModel, String modelFileName) {
        Model model = initModel(objectModel)
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
        newType.properties = propsFromMap(objectModel)
        newType.requiredProps=listFromMap(ojbectModel,'required')
        model.types.add(newType)
        return model
    }

    private Model modelFromMultiTypeSchema(def objectModel) {
        Model model = initModel(objectModel)
        // TODO - initialize types
        return model
    }

    private List propsFromMap(def map) {
        def propList = map['properties']
        if (!propList) return []
        def newPropsList = []
        propList.each { typeName,propMap ->
            def newProp = new Property()
            newProp.name = typeName
            newProp.description = strFromMap(propMap,'description')
            setTypeFromPropMap(propMap,newProp)
            newPropsList.add(newProp)
        }
        return newPropsList
    }

    private def getArrayType(def propMap) {
        def itemMap = propMap['items']
        if (!itemMap) {
            def errorMsg = "no item elem found for array: ${propMap}"
            log.error(errorMsg)
            throw new Exception(errorMsg)
        }
        def refStr = strFromMap(itemMap,'$ref')
        if (refStr) {
            // reference
            // TODO initialize from reference
            return PropertyType.t_ref
        }
        else {
            def typeStr = strFromMap(propMap,'type')
            if (!typeStr) {
                def errorMsg = "no type elem found for array: ${itemMap}"
                log.error(errorMsg)
                throw new Exception(errorMsg)
            }
            return typeStr2Type(typeStr)
        }
    }

    def typeStr2Type(def typeStr) {
        switch (typeStr) {
            case 'integer':
                return PropertyType.t_int
            case 'number':
                return PropertyType.t_number
            case 'string':
                def formatStr = strFromMap(propMap, 'format')
                if (!formatStr) {
                    return PropertyType.t_string
                } else {
                    def mappedTypeCont = Model.FORMAT_TYPE_MAPPING[formatStr]
                    if (!mappedType) {
                        def errorMsg = "unsupported property format: ${formatStr}"
                        log.error(errorMsg)
                        /*
                        maybe it's not needed to throw an exception but ... fail first
                         */
                        throw new Exception(errorMsg)
                    } else {
                        return mappedTypeCont.type
                    }
                }
            case 'boolean':
                return PropertyType.t_boolean
            case 'object':
                return null; // TODO complex type
            default:
                def errorMsg = "unknown type: ${typeStr}, ${propMap}"
                log.error(errorMsg)
                throw new Exception(errorMsg)
        }
    }


    private void setTypeFromPropMap(def propMap, Property newProp) {
        def typeStr = strFromMap(propMap,'type')
        if (!typeStr) {
            // if there is no type maybe a reference is given
            def refStr = strFromMap(propMap,'$ref')
            if (!refStr) {
                strFromMap(propMap,'type')
                def errorMsg = "unknown property type: ${propMap}, ${newProp}"
                log.error(errorMsg)
                throw new Exception(errorMsg)
            }
            else {
                newProp.type = PropertyType.t_ref
                // TODO initialize from reference
                newProp.reference = null; // TODO
            }
        }
        else if (typeStr=='array') {
            newProp.isArray = true
            newProp.type = getArrayType(propMap, newProp)
        }
        else {
            newProp.type = typeStr2Type(typeStr)
        }
    }

    private Model initModel(def objectModel) {
        Model model = new Model()
        model.title = strFromMap(objectModel,'title')
        model.description = strFromMap(objectModel,'description')
        return model
    }

    private static final Logger log=LoggerFactory.getLogger(JsonSchemaBuilder.class);
}
