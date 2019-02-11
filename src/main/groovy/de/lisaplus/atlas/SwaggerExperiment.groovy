package de.lisaplus.atlas

import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.codegen.GeneratorBase
import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Property
import de.lisaplus.atlas.model.Type
import de.lisaplus.atlas.model.UUIDType

class SwaggerExperiment {

    static main(args) {
        // service-junction
        def base = '/home/stefan/Entwicklung/service-junction/models/models-lisa-server/model/'
        def modelPath = args.length == 0 ?
                base + 'junction.json'
                // base + 'shared\\geo_point.json'
                : args[0]
        def forceMainTypes = 'ObjectBase:Tag:Region:ObjectGroup'

        // service-op-message
        /*
        def base = '/home/stefan/Entwicklung/service-op-message/models/models-lisa-server/model/'
        def modelPath = args.length == 0 ?
                base + 'op_message.json'
                : args[0]
        def forceMainTypes = ''
        */

        // service-incident
        /*
        def base = '/home/stefan/Entwicklung/service-op-message/models/models-lisa-server/model/'
        def modelPath = args.length == 0 ?
                base + 'incident.json'
                : args[0]
        def forceMainTypes = ''
        */

        // service-junction-graphics
        /*
        def base = '/home/stefan/Entwicklung/service-junction-graphics/models/models-lisa-server/model/'
        def modelPath = args.length == 0 ?
                base + 'junction_graphics.json'
                : args[0]
        def forceMainTypes = 'ObjectBase:Tag:Region:ObjectGroup'
        */

        // service-user-config
        /*
        def base = '/home/stefan/Entwicklung/service-user-config/models/models-lisa-server/model/'
        def modelPath = args.length == 0 ?
                base + 'user_config.json'
                : args[0]
        def forceMainTypes = ''
        */

        // ines-network
        /*
        def base = '/home/stefan/Entwicklung/models-lisa-server/model/'
        def modelPath = args.length == 0 ?
                base + 'ines_network.json'
                : args[0]
        def forceMainTypes = ''
        */


        def swaggerExp = new SwaggerExperiment(modelPath, forceMainTypes.split(':').toList())
        swaggerExp.execute()
    }

    /** The path to the model definition file */
    String modelPath
    /** The complete model with all types */
    Model model
    /** The bindings of the template / code generation */
    Map data
    /** Indicates whether the current type is a joined type */
    boolean joined
    /** The main type, which is to be processed */
    Type currentType
    /** For enabling/disabling debug output */
    boolean verbose = true
    /** Mimic template environment! */
    Map extraParam = [:]
    /** Name of types, which are to be tagged as main types forcefully, mimic template environment! */
    List<String> forceMainTypes = []
    /** All generated REST paths */
    List<String> restPaths = []
    /** This stack holds the property visited while traversing the object hierarchy.*/
    List<Property> propStack = []
    /** This stack holds the base and intermediate array types needed to build the sub-paths. It is filled while traversing the object hierarchy.*/
    List<Type> typeStack = []
    /** Stack for the elements of the REST paths build using the property names  */
    List<String> pathElementStack = []
    /** Switch to using REST paths created using property names */
    boolean usePropNames = true
    /** Places tags "restSubPath" at all compatible properties to force creation of complete set of REST paths. */
    boolean forceAllSubPaths = true
    /** Mimic template environment */
    def generateGeoJsonPaths
    /** Mimic template environment */
    List<String> providedGeoJsonTypes = []

    /**
     * @param modelPath The path to the (json) file defining the model
     * @param forceMainTypes Name of types, which are to be tagged as main types forcefully
     */
    SwaggerExperiment(String modelPath, List<String> forceMainTypes) {
        this.modelPath = modelPath
        this.forceMainTypes = forceMainTypes
        this.model = readModel(modelPath)
    }

    /**
     * @param modelPath The path to the model definition file.
     * @return The model parsed from the model definition
     */
    private Model readModel(String modelPath) {
        def modelFile = new File(modelPath)
        IModelBuilder builder = new JsonSchemaBuilder()
        Model model = builder.buildModel(modelFile)
        model.types.each { type ->
            boolean isMainType = type.isMainType(modelFile.name)
            if(isMainType) {
                // println "Tagging ${type.name}!"
                type.tags.add('mainType')
            } else if (forceMainTypes.contains(type.name)) {
                // println "Force tagging ${type.name}!"
                type.tags.add('mainType')
            }
        }
        return model
    }

    /**
     * Prepares and performs the code generation for one model
     */
    void execute() {
        GeneratorBase generator = new DummyGenerator()
        data = generator.createTemplateDataMap(model)
        executeForModel()
    }

    def printOperationId = { operationStr, pathStr ->
        return "${operationStr}${data.firstUpperCase.call(pathStr).replaceAll('[^a-zA-Z0-9]','_').replaceAll('__','_')}"
    }

    def includeAdditionalPaths = {
        if (!extraParam.additionalPaths) return ''
        File f = new File (extraParam.additionalPaths)
        if (!f.exists()) {
            return "!!! additionalPaths file not found: $extraParam.additionalPaths"
        }
        else {
            return f.getText('UTF-8')
        }
    }

    def includeAdditionalTypes = {
        if (!extraParam.additionalTypes) return ''
        File f = new File (extraParam.additionalTypes)
        if (!f.exists()) {
            return "!!! additionalTypes file not found: $extraParam.additionalTypes"
        }
        else {
            return f.getText('UTF-8')
        }
    }

    def getParameterStr = { List typeList,boolean isIdPath,boolean isGetPath ->
        if ((!typeList) && (!isGetPath) || ((typeList.size()==1) && (!isIdPath) && (!isGetPath) )) return ''
        def parameterStr=''
        if (isGetPath && (typeList.size()==1)) {
            parameterStr += "\n        - in: query"
            parameterStr += "\n          name: offset"
            parameterStr += "\n          type: integer"
            parameterStr += "\n          description: The number of objects to skip before starting to collect the result set."
            parameterStr += "\n        - in: query"
            parameterStr += "\n          name: limit"
            parameterStr += "\n          type: integer"
            parameterStr += "\n          description: The numbers of objects to return."
            parameterStr += "\n        - in: query"
            parameterStr += "\n          name: filter"
            parameterStr += "\n          type: string"
            parameterStr += "\n          description: Defines a criteria for selecting specific objects"
            parameterStr += "\n        - in: query"
            parameterStr += "\n          name: sort"
            parameterStr += "\n          type: string"
            parameterStr += "\n          description: Defines a sorting order for the objects"
        }
        typeList.findAll {
            isIdPath || (it != typeList[typeList.size()-1])
        }.each {
            def descriptionStr = it.description ? it.description : '???'
            parameterStr += "\n        - name: \"${data.lowerCamelCase.call(it.name)}_id\""
            parameterStr += '\n          in: "path"'
            parameterStr += "\n          description: \"$descriptionStr\""
            parameterStr += '\n          required: true'
            parameterStr += '\n          type: "string"'
            parameterStr += '\n          format: "uuid"'
        }
        def ret = """      parameters:${parameterStr}"""
        return ret
    }

    def printParametersSectionForPost = { parameterStr ->
        if (!parameterStr) {
            return '      parameters:'
        }
        else {
            return parameterStr
        }
    }

    def printAdditionalParametersForPostAndPut = { item ->
        return """        - name: "bodyParam"
          in: "body"
          description: "object to save"
          required: true
          schema:
            ${data.DOLLAR}ref: "#/definitions/${data.upperCamelCase.call(item.name)}\""""
    }

    /**
     * prints out tags section
     */
    def printTags = { type ->
        return """      tags:
        - ${type.name}"""
    }

    /**
     * prints out response section for ID-Paths
     */
    def printIdResponse = { type ->
        return """      responses:
        200:
          description: "in case of success"
          schema:
            ${data.DOLLAR}ref: "#/definitions/${data.upperCamelCase.call(type.name)}"
        404:
          description: "Requested object was not found"
          schema:
            ${data.DOLLAR}ref: "#/definitions/LisaError\"
        default:
          description: "Unexpected error"
          schema:
            ${data.DOLLAR}ref: "#/definitions/LisaError\""""
    }

    /**
     * prints out response section for List-Paths
     */
    def printListResponse = { typeName, boolean withQueryParameter ->
        // TODO Eiko: avoid duplication!
        if(withQueryParameter) {
            // without status code 400
            return """      responses:
        200:
          description: "in case of success"
          schema:
            type: "array"
            items:
              ${data.DOLLAR}ref: "#/definitions/${data.upperCamelCase.call(typeName)}"
        default:
          description: "Unexpected error"
          schema:
            ${data.DOLLAR}ref: "#/definitions/LisaError\""""
        }
        // with status code 400
        return """      responses:
        200:
          description: "in case of success"
          schema:
            type: "array"
            items:
              ${data.DOLLAR}ref: "#/definitions/${data.upperCamelCase.call(typeName)}"
        400:
          description: "in case of broken filter or sort criteria"
          schema:
            ${data.DOLLAR}ref: "#/definitions/LisaError\"
        default:
          description: "Unexpected error"
          schema:
            ${data.DOLLAR}ref: "#/definitions/LisaError\""""
    }

    /**
     * prints out response section for Delete-Paths
     */
    def printDeleteResponse = { typeName ->
        return """      responses:
        200:
          description: "in case of success"
          schema:
            ${data.DOLLAR}ref: "#/definitions/IdObj"
        404:
          description: "if the object to delete was not found"
          schema:
            ${data.DOLLAR}ref: "#/definitions/LisaError\"
        default:
          description: "Unexpected error"
          schema:
            ${data.DOLLAR}ref: "#/definitions/LisaError\""""
    }

    /**
     * prints out response section for List-Paths
     */
    def printPutPatchPostItemResponse = { type ->
        return """      responses:
        200:
          description: "in case of success"
          schema:gi
            ${data.DOLLAR}ref: "#/definitions/${data.upperCamelCase.call(type.name)}"
        404:
          description: "if the object to process was not found"
          schema:
            ${data.DOLLAR}ref: "#/definitions/LisaError\"
        409:
          description: "if altering object would cause inconsistent data model"
          schema:
            ${data.DOLLAR}ref: "#/definitions/LisaError\"
        default:
          description: "Unexpected error"
          schema:
            ${data.DOLLAR}ref: "#/definitions/LisaError\""""
    }

    /**
     * prints out options block
     */
    def printOptionsBlock = { pathStr,lastItem,parameterStr ->
        return """    options:
${printTags(lastItem)}
      summary: Provides meta data of the related type
      description: return a meta data object
      operationId: \"${printOperationId('options',pathStr)}\"
      produces:
        - \"application/xml\"
        - \"application/json\"
${parameterStr}
      responses:
        200:
          description: \"in case of success\"
          schema:
            ${data.DOLLAR}ref: \"#/definitions/OptionsResponse\"
        501:
          description: \"in case of missing implementation\"
          schema:
            ${data.DOLLAR}ref: \"#/definitions/LisaError\"
        default:
          description: \"Unexpected error\"
          schema:
            ${data.DOLLAR}ref: \"#/definitions/LisaError\""""
    }

    /**
     * prints out options block
     */
    def printListOptionsBlock = { pathStr,lastItem,parameterStr ->
        if (parameterStr) {
            return printOptionsBlock.call (pathStr,lastItem,parameterStr)
        }
        parameterStr = "      parameters:"
        return """    options:
${printTags(lastItem)}
      summary: Provides meta data of the related type
      description: return a meta data object
      operationId: \"${printOperationId('options',pathStr)}\"
      produces:
        - \"application/xml\"
        - \"application/json\"
${parameterStr}
        - in: query
          name: filter
          type: string
          description: Defines a criteria for selecting specific objects
      responses:
        200:
          description: \"in case of success\"
          schema:
            ${data.DOLLAR}ref: \"#/definitions/OptionsListResponse\"
        501:
          description: \"in case of missing implementation\"
          schema:
            ${data.DOLLAR}ref: \"#/definitions/LisaError\"
        default:
          description: \"Unexpected error\"
          schema:
            ${data.DOLLAR}ref: \"#/definitions/LisaError\""""
    }

    // If you declare a closure you can use it inside the template

    // https://stackoverflow.com/questions/12580262/groovy-closure-with-optional-arguments
    /**
     * Creates REST path from types
     */
    def buildPathFromTypes = { List typeList, boolean addLastId=true ->
        def pathStr=''
        def lastElem = typeList[typeList.size() - 1]    // Assumption: list is never empty!
        typeList.each {
            pathStr += '/'
            pathStr += data.lowerCamelCase.call(it.name)
            if (it != lastElem || addLastId ) {
                pathStr += "/{${data.lowerCamelCase.call(it.name)}_id}"
            }
        }
        return pathStr
    }

    /**
     * Creates REST path using property names.
     * An example of a path, which ends with the ID of an object, is /objectBase/{objectBase_id}
     * @param addLastId Indicates, whether the last entry of the stack representing the ID of an object, is to be added to the REST path
     */
    def buildPathFromProps = { boolean addLastId=true ->
        String path
        if (addLastId || !pathElementStack.last().endsWith('_id}')) {
            path = pathElementStack.join('')
        } else {
            def lastElement = pathElementStack.pop()
            path = pathElementStack.join('')
            pathElementStack.add(lastElement)
        }
        return path
    }

    /**
     * Print path for List URLs
     */
    def printIDPath = { List typeList ->
        def pathStr = buildPathFromTypes.call(typeList)
        if (usePropNames)
            pathStr = buildPathFromProps.call(true)
        restPaths.add(pathStr)
        def lastItem = typeList[typeList.size()-1]
        def summary = lastItem.description ? lastItem.description : '???'
        def parameterStr = getParameterStr(typeList,true,false)
        // def parameterStrGetList = getParameterStr(typeList,true,true)
        return """  ${pathStr}:
${printOptionsBlock(pathStr,lastItem,parameterStr)}
    get:
${printTags(lastItem)}
      summary: ${summary}
      description: "returns object by id"
      operationId: "${printOperationId('get',pathStr)}"
      produces:
        - "application/xml"
        - "application/json"
${parameterStr}
${printIdResponse(lastItem)}
    put:
${printTags(lastItem)}
      summary: "update ${lastItem.name}"
      description: "update existing ${lastItem.name}"
      operationId: "${printOperationId('upd',pathStr)}"
      produces:
        - "application/xml"
        - "application/json"
      consumes:
        - "application/xml"
        - "application/json"
${parameterStr}
${printAdditionalParametersForPostAndPut(lastItem)}
${printPutPatchPostItemResponse(lastItem)}
    patch:
${printTags(lastItem)}
      summary: "partial update ${lastItem.name}"
      description: "partial update existing ${lastItem.name}"
      operationId: "${printOperationId('patch',pathStr)}"
      produces:
        - "application/xml"
        - "application/json"
      consumes:
        - "application/xml"
        - "application/json"
${parameterStr}
${printAdditionalParametersForPostAndPut(lastItem)}
${printPutPatchPostItemResponse(lastItem)}
    delete:
${printTags(lastItem)}
      summary: "delete ${lastItem.name}"
      description: "delete existing ${lastItem.name}"
      operationId: "${printOperationId('del',pathStr)}"
${parameterStr}
${printDeleteResponse()}"""
    }

    /**
     * Print path for ID URLs and joined types
     */
    def printIDPathJoined = { List typeList ->
        def pathStr = buildPathFromTypes.call(typeList)
        if (usePropNames)
            pathStr = buildPathFromProps.call(true)
        restPaths.add(pathStr)
        def lastItem = typeList[typeList.size()-1]
        def summary = lastItem.description ? lastItem.description : '???'
        def parameterStr = getParameterStr(typeList,true,false)
        // def parameterStrGetList = getParameterStr(typeList,true,true)
        return """  ${pathStr}:
${printOptionsBlock(pathStr,lastItem,parameterStr)}
    get:
${printTags(lastItem)}
      summary: ${summary}
      description: "returns object by id"
      operationId: "${printOperationId('get',pathStr)}"
      produces:
        - "application/xml"
        - "application/json"
${parameterStr}
${printIdResponse(lastItem)}"""
    }

    /**
     * Print path for List URLs that are no arrays
     */
    def printListPath_noArray = { List typeList ->
        def pathStr = buildPathFromTypes.call(typeList, false)
        if (usePropNames)
            pathStr = buildPathFromProps.call(false)
        restPaths.add(pathStr)
        def lastItem = typeList[typeList.size()-1]
        def summary = lastItem.description ? lastItem.description : '???'
        def parameterStr = getParameterStr(typeList,false,false)
        def parameterStrGetList = getParameterStr(typeList,false,true)
        def descriptionExtension = ''
        if (typeList.size==1) {
            descriptionExtension += ", contains optional query parameter for defining offset, limit, object filter and object order"
        }
        // Response of verb/method get:
        // ${printListResponse(lastItem.name,typeList.size!=1)}    // Why ListResponse in _noArray for path ending with blah/{id}/blub?!?
        def ret = """  ${pathStr}:
${printOptionsBlock(pathStr,lastItem,parameterStr)}
    get:
${printTags(lastItem)}
      summary: "${summary}"
      description: "returns a single object${descriptionExtension}"
      operationId: "${printOperationId('get',pathStr)}"
      produces:
        - "application/xml"
        - "application/json"
${parameterStrGetList}
${printIdResponse(lastItem)}
    put:
${printTags(lastItem)}
      summary: "add a new ${lastItem.name}"
      description: ""
      operationId: "${printOperationId('upd',pathStr)}"
      produces:
        - "application/xml"
        - "application/json"
      consumes:
        - "application/xml"
        - "application/json"
${printParametersSectionForPost(parameterStr)}
${printAdditionalParametersForPostAndPut(lastItem)}
${printPutPatchPostItemResponse(lastItem)}
    patch:
${printTags(lastItem)}
      summary: "partial update ${lastItem.name}"
      description: ""
      operationId: "${printOperationId('patch',pathStr)}"
      produces:
        - "application/xml"
        - "application/json"
      consumes:
        - "application/xml"
        - "application/json"
${printParametersSectionForPost(parameterStr)}
${printAdditionalParametersForPostAndPut(lastItem)}
${printPutPatchPostItemResponse(lastItem)}"""
        return ret
    }

    /**
     * Print path for List URLs for arrays
     */
    def printListPath_array = { List typeList ->
        def pathStr = buildPathFromTypes.call(typeList, false)
        if (usePropNames)
            pathStr = buildPathFromProps.call(false)
        restPaths.add(pathStr)
        def lastItem = typeList[typeList.size()-1]
        def summary = lastItem.description ? lastItem.description : '???'
        def parameterStr = getParameterStr(typeList,false,false)
        def parameterStrGetList = getParameterStr(typeList,false,true)
        def descriptionExtension = ''
        if (typeList.size==1) {
            descriptionExtension += ", contains optional query parameter for defining offset, limit, object filter and object order"
        }
        def ret = """  ${pathStr}:
${printListOptionsBlock(pathStr,lastItem,parameterStr)}
    get:
${printTags(lastItem)}
      summary: "${summary}"
      description: "returns object list${descriptionExtension}"
      operationId: "${printOperationId('get',pathStr)}"
      produces:
        - "application/xml"
        - "application/json"
${parameterStrGetList}
${printListResponse(lastItem.name,typeList.size!=1)}
    post:
${printTags(lastItem)}
      summary: "add a new ${lastItem.name}"
      description: ""
      operationId: "${printOperationId('add',pathStr)}"
      produces:
        - "application/xml"
        - "application/json"
      consumes:
        - "application/xml"
        - "application/json"
${printParametersSectionForPost(parameterStr)}
${printAdditionalParametersForPostAndPut(lastItem)}
${printPutPatchPostItemResponse(lastItem)}"""
        return ret
    }

    /**
     * Print path for List URLs for arrays
     */
    def printListPathJoined = { List typeList ->
        def pathStr = buildPathFromTypes.call(typeList, false)
        if (usePropNames)
            pathStr = buildPathFromProps.call(false)
        restPaths.add(pathStr)
        def lastItem = typeList[typeList.size()-1]
        def summary = lastItem.description ? lastItem.description : '???'
        def parameterStr = getParameterStr(typeList,false,false)
        def parameterStrGetList = getParameterStr(typeList,false,true)
        def descriptionExtension = ''
        if (typeList.size==1) {
            descriptionExtension += ", contains optional query parameter for defining offset, limit, object filter and object order"
        }
        def ret = """  ${pathStr}:
${printListOptionsBlock(pathStr,lastItem,parameterStr)}
    get:
${printTags(lastItem)}
      summary: "${summary}"
      description: "returns object list${descriptionExtension}"
      operationId: "${printOperationId('get',pathStr)}"
      produces:
        - "application/xml"
        - "application/json"
${parameterStrGetList}
${printListResponse(lastItem.name,typeList.size!=1)}"""
        return ret
    }

    /**
     * Print geoJson path for a point object of that type. It's required that the type is a joined type with objectBase
     */
    def printGeoPath = { def type, String geoObjType ->
        def geoObjTypeLowerCase = geoObjType.toLowerCase()
        def strippedTypeName=type.name.replace('Joined','')
        if (!providedGeoJsonTypes.contains("${strippedTypeName}${geoObjType}".toString())) {
            // for that object is no geoJsonType defined in settings.sh, so path creation is skipped
            return "Test: $providedGeoJsonTypes vs ${strippedTypeName}${geoObjType}";
        }
        def pathName="${strippedTypeName.toLowerCase()}_${geoObjTypeLowerCase}"
        def ret="""
  /${pathName}:
    options:
      tags:
        - ${type.name}
      summary: Provides meta data of the geo ${geoObjTypeLowerCase} request for the related type
      description: return a meta data object
      operationId: "options_${pathName}"
      produces:
        - "application/xml"
        - "application/json"
      parameters:
        - in: query
          name: offset
          type: integer
          description: The number of objects to skip before starting to collect the result set.
        - in: query
          name: limit
          type: integer
          description: The numbers of objects to return.
        - in: query
          name: filter
          type: string
          description: Defines a criteria for selecting specific objects
        - in: query
          name: sort
          type: string
          description: Defines a sorting order for the objects
        - in: query
          name: bbox
          type: string
          description: bbox of the desired area
      responses:
        200:
          description: "in case of success"
          schema:
            ${DOLLAR}ref: "#/definitions/OptionsListResponse"
        default:
          description: "Unexpected error"
          schema:
            ${DOLLAR}ref: "#/definitions/LisaError"
    get:
      tags:
        - ${type.name}
      summary: "GeoJSON list for ${type.name} ${geoObjTypeLowerCase}"
      description: "returns GeoJSON list of ${geoObjTypeLowerCase}"
      operationId: "geo_${pathName}"
      produces:
        - "application/json"
      parameters:
        - in: query
          name: offset
          type: integer
          description: The number of objects to skip before starting to collect the result set.
        - in: query
          name: limit
          type: integer
          description: The numbers of objects to return.
        - in: query
          name: filter
          type: string
          description: Defines a criteria for selecting specific objects
        - in: query
          name: sort
          type: string
          description: Defines a sorting order for the objects
        - in: query
          name: bbox
          type: string
          description: bbox of the desired area
      responses:
        200:
          description: "in case of success"
          schema:
            ${DOLLAR}ref: "#/definitions/${type.name.replace('Joined','')}${geoObjType}"
        400:
          description: "in case of broken filter or sort criteria"
          schema:
            ${DOLLAR}ref: "#/definitions/LisaError"
        default:
          description: "Unexpected error"
          schema:
            ${DOLLAR}ref: "#/definitions/LisaError"
        """
        return ret
    }

    /**
     * Print path for List URLs that are no arrays, functional fasade
     */
    def printListPath = { List typeList, boolean array = false ->
        if (array) {
            printListPath_array (typeList)
        }
        else {
            printListPath_noArray (typeList)
        }
    }

    /**
     * Checks whether the property has an identity (its type contains a fied guid or entryId)
     * @param property The element to evaluate.
     */
    def checkHasId = { Property property ->
        return !property.type.type.properties.findAll{ Property prop -> prop.name == 'guid' || prop.name == 'entryId' }.isEmpty()
    }

    /**
     * Prepares the stacks for running the next loop over the model
     * @param the current type
     */
    def prepareStacks = { type ->
        propStack.clear()
        typeStack.clear()
        typeStack.add(type)
        pathElementStack.clear()
        pathElementStack.add("/${data.lowerCamelCase.call(type.name)}") // e.g. /junction
        pathElementStack.add("/{${data.lowerCamelCase.call(type.name)}_id}") // e.g. junction_id
    }

    /**
     * Adds new elements to the stacks
     * @param property The property, which is to be visited
     */
    def putStacks = { Property property ->
        def parent = propStack.isEmpty() ? null : propStack.last()
        propStack.add(property)
        // Assumes Ref or Complex types, only add if property represents an array and therefore has entryId/guid!
        assert property.isRefTypeOrComplexType()
        if (property.type.isArray) {
            typeStack.add(property.type.type)
        }

        def prefix
        if (parent != null && !checkHasId.call(parent)) {
            prefix = '.'
        } else {
            prefix = '/'
        }
        if (checkHasId.call(property)) {
            // e.g. /addressPerson in /junctionContact/{junctionContact_id}/addressPerson(/{addressPerson_id})
            // or   .streets in /junction/{junction_id}/location.streets(/{junctionLocationStreetsItem_id})
            pathElementStack.add("${prefix}${property.name}")
            pathElementStack.add("/{${data.lowerCamelCase.call(property.type.type.name)}_id}")
        } else {
            // e.g. /junctionLocation in /junction/{junction_id}/location
            // or   .area in /objectBase/{objectBase_id}/gis.area
            pathElementStack.add("${prefix}${property.name}")
        }
    }

    /**
     * Pops the latest elements from the stacks
     */
    def popStacks = {
        def dropped = propStack.pop()
        // parentJavaClassNotJoined.pop()
        if (dropped.type.isArray) {
            typeStack.pop()
        }
        pathElementStack.pop()
        if (checkHasId.call(dropped)) {
            // drop extra element /{XXX_id}
            pathElementStack.pop()
        }
    }

    /** Add missing tags recurseToRestSubPath */
    Closure<Void> ensureRecursionTags = {
        propStack.each { prop -> if (!prop.hasTag('recurseToRestSubPath')) prop.tags.add('recurseToRestSubPath') }
    }

    /** Create key from current state of propStack */
    Closure<String> currentKey = {
        return propStack.collect { prop -> prop.name}.join('.')
    }

    /** Find all properties with tag restSubPath in one type, distribute tags recurseToRestSubPath to guide generating sub-path */
    Closure<Void> prepType = { Type type, List<String> keys ->
        type.properties.findAll { it.hasTag('restSubPath')}.each { prop ->
            putStacks.call(prop)
            keys.add( currentKey.call() )
            popStacks.call()
            ensureRecursionTags.call()
        }
        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
            putStacks.call(prop)
            prepType.call(prop.type.type, keys)
            popStacks.call()
        }
    }

    /**
     *  Places tags "restSubPath" at all compatible properties pf a type to force creation of complete set of REST paths.
     *  Calls itself recursively to travers through the complete model.
     */
    def forceAllSubPathsForType = { Type type ->
        if (type == null || type.properties == null) {
            println "??? at key=${currentKey.call()}"
        }
        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
            if (prop.type.isArray &&  !checkHasId.call(prop) ) {
                def msg = "Add missing entryId to ${currentKey.call()}"
                if (verbose) println msg
                restPaths.add(msg)
                Type propType = prop.type.type
                Property idProp = new Property()
                idProp.name = 'entryId'
                idProp.type = new UUIDType()
                idProp.description = 'ID property inserted on the fly!'
                idProp.tags.add('notDisplayed')
                idProp.tags.add('notNull')
                propType.properties.add(idProp)
            }
        }
        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
            putStacks.call(prop)
            if (verbose) println "prop=$prop.name key=${currentKey.call()}"
            if (!prop.hasTag('restSubPath')) {
                prop.tags.add('restSubPath')
            }
            if (prop.type.type == null || prop.type.type.properties == null) {
                println "??? at $prop.name key=${currentKey.call()}"
            } else {
                forceAllSubPathsForType.call(prop.type.type)
            }
            popStacks.call()
        }
    }

    /** Find all properties with tag restSubPath in the complete model, distribute tags recurseToRestSubPath to guide generating sub-path */
    Closure<Map<Type, List<String>>> prepareModel = { Model model ->
        def res = [:]
        def types = model.types.findAll { it.hasTag('mainType') && it.hasTag('rest') && !it.hasTag('joinedType') }
        if (forceAllSubPaths) {
            types.each { type ->
                if (verbose) println "Start preparing model for type $type.name"
                prepareStacks.call(type)
                forceAllSubPathsForType.call(type)
            }
        }
        types.each { type ->
            prepareStacks.call(type)
            List<String> keys = []
            prepType.call(type, keys)
            res.put(type, keys)
        }
        return res
    }

    /** Recursively called method for locating and processing the REST sub-paths in one type */
    Closure<String> findSubPaths = { Type type, List<String> keys ->
        String ret = ''
        type.properties.findAll { it.hasTag('restSubPath')}.each { prop ->
            // only ref or complex types are supposed to be tagged with restSubPath
            // Inner types are omitted if corresponding property are not an array and therefore there is no guid/entryId!
            List typeList = typeStack.clone()
            typeList.add(prop.type.type) // always include type of current property!
            putStacks.call(prop)
            String key = currentKey.call()
            ret += printListPath(typeList, prop.type.isArray)
            ret += '\n'
            // ID functions for sub-paths are only needed in case of array elements
            if (prop.type.isArray) {
                ret += printIDPath(typeList)
                ret += '\n'
            }
            boolean res = keys.remove(key)
            assert res : "key=$key prop=$prop.name type=$type.name"
            popStacks.call()
        }
        if (!keys.isEmpty()) {
            type.properties.findAll { it.hasTag('recurseToRestSubPath')}.each { prop ->
                putStacks.call(prop)
                // only ref or complex types are supposed to be tagged with recurseToRestSubPath
                ret += findSubPaths.call(prop.type.type, keys)
                popStacks.call()
            }
        }
        return ret
    }

    /** Kicks of traversing the properties if one main type with the objection to generate the REST sub-paths */
    Closure<String> findAllSubPaths = { Type type, List<String> keys ->
        String ret = ''
        def keys2 = keys.clone()
        prepareStacks.call(type)
        ret += findSubPaths.call(type, keys2)
        assert keys2.isEmpty() : "type=$type.name missed keys=$keys2"
        // remove last \nA ???
        return ret
    }

    private void executeForModel() {
        if (modelPath.endsWith('junction.json')) {
            //service junction
            extraParam = ['basePath'           : '/junction',
                          additionalTypes      : '/home/stefan/Entwicklung/service-junction/rest/swagger/additional/types.yaml',
                          additionalPaths      : '/home/stefan/Entwicklung/service-junction/rest/swagger/additional/paths.yaml',
                          providedGeoJsonTypes : 'JunctionRoutes;JunctionPoints;JunctionAreas',
                          ]
        } else if (modelPath.endsWith('junction_graphics.json')) {
            // service junctionGraphics
            extraParam = [ 'basePath':'/junctionGraphics',
                           additionalTypes:'/home/stefan/Entwicklung/service-junction-graphics/rest/swagger/additional/types.yaml',
                           additionalPaths:'/home/stefan/Entwicklung/service-junction-graphics/rest/swagger/additional/paths.yaml']
        } else if (modelPath.endsWith('incident.json')) {
            // service incident
            extraParam = ['basePath'           : '/incident',
                          additionalTypes      : '/home/stefan/Entwicklung/service-incident/rest/swagger/additional/types.yaml',
                          additionalPaths      : '/home/stefan/Entwicklung/service-incident/rest/swagger/additional/paths.yaml']

        } else if (modelPath.endsWith('op_message.json')) {
            // service incident
            extraParam = ['basePath'           : '/opMessage',
                          additionalTypes      : '/home/stefan/Entwicklung/service-op-message/rest/swagger/additional/types.yaml',
                          additionalPaths      : '/home/stefan/Entwicklung/service-op-message/rest/swagger/additional/paths.yaml']
        } else if (modelPath.endsWith('user_config.json')) {
            // service incident
            extraParam = ['basePath'           : '/userConfig',
                          additionalTypes      : '/home/stefan/Entwicklung/service-user-config/rest/swagger/additional/types.yaml',
                          additionalPaths      : '/home/stefan/Entwicklung/service-user-config/rest/swagger/additional/paths.yaml']
        } else if (modelPath.endsWith('ines_network.json')) {
            // service incident
            extraParam = ['basePath'           : '/inesNetwork']
        } else {
            println 'Missing extraParam!'
            System.exit(1)
        }

        this.generateGeoJsonPaths = extraParam.providedGeoJsonTypes
        this.providedGeoJsonTypes = generateGeoJsonPaths ? extraParam.providedGeoJsonTypes.split(';') : []

        String part1 = $/
swagger: "2.0"
info:
  title: "${model.title}"
  description: "${model.description}"
  version: "${model.version}"
host: "${ -> extraParam.host ? extraParam.host : 'please.change.com' }"
schemes:
  - "http"
  - "https"
basePath: "${ ->
            if (extraParam.basePath) {
                if (extraParam.appendVersionToPath) {
                    "${extraParam.basePath}/v${model.version}"
                } else {
                    "${extraParam.basePath}"
                }
            } else {
                "/v${model.version}"
            }
        }"
paths:/$
        println part1

        //// search for all types that should provide entry points
        model.types.findAll { return (it.hasTag('mainType')) && (it.hasTag('rest')) && (!it.hasTag('joinedType')) }.each { type ->
            currentType = type
            prepareStacks.call(type)
            println printListPath.call([type], true)
            println printIDPath.call([type])
        }

        // prepare evaluation of REST sub-paths
        def type2keys = prepareModel.call(model)
        if (verbose) {
            type2keys.findAll { type, keys -> !keys.isEmpty()}.each { type, keys ->
                println "type=$type.name keys=$keys"
            }
        }

        // loop over all types with REST sub-paths
        type2keys.findAll { type, keys -> !keys.isEmpty()}.each { type, keys ->
            currentType = type
            println findAllSubPaths.call(type, keys)
        }

        // Keep around!
        model.types.findAll { it.hasTag('joinedType') && it.hasTag('rest') && it.hasTag('mainType') }.each { type ->
            currentType = type
            prepareStacks.call(type)
            println printListPathJoined.call([type])
            println printIDPathJoined.call([type])
            if (generateGeoJsonPaths && type.hasPropertyWithName('objectBase')) {
                println printGeoPath.call(type,'Points')
                println printGeoPath.call(type,'Areas')
                println printGeoPath.call(type,'Routes')
            }
        }

        // Keep around!
        // mix in an optional file with additional files
        println includeAdditionalPaths.call()
        println 'definitions:'
        model.types.each { type ->  // Always for all types!
            println """  ${data.upperCamelCase.call(type.name)}:
    type: object
    properties:"""
            type.properties.each { prop ->  // Always for all properties!
                println "      ${prop.name}:"
                if (prop.type.isArray) {
                    println "        type: array"
                    println "        items:"
                    if (prop.isRefTypeOrComplexType()) {
                        println "          ${data.DOLLAR}ref: \"#/definitions/${data.upperCamelCase.call(prop.type.type.name)}\""
                    } else {
                        if (prop.description) {
                            println "          description: \"${prop.description}\""
                        }
                        println "          type: \"${data.typeToSwagger.call(prop.type)}\""
                        if (data.typeFormatToSwagger.call(prop.type)) {
                            println "          format: \"${data.typeFormatToSwagger.call(prop.type)}\""
                        }
                    }
                } else {
                    if (prop.isRefTypeOrComplexType()) {
                        println "        ${data.DOLLAR}ref: \"#/definitions/${data.upperCamelCase.call(prop.type.type.name)}\""
                    } else {
                        if (prop.description) {
                            println "        description: \"${prop.description}\""
                        }
                        println "        type: \"${data.typeToSwagger.call(prop.type)}\""
                        if (data.typeFormatToSwagger.call(prop.type)) {
                            println "        format: \"${data.typeFormatToSwagger.call(prop.type)}\""
                        }
                    }
                }
            }
        }
//// mix in an optional file with additional files
        println includeAdditionalTypes.call()

        println '\nREST paths:'
        restPaths.each { println it}
    }
}
