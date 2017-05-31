package de.lisaplus.atlas

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Property
import de.lisaplus.atlas.model.PropertyType
import de.lisaplus.atlas.model.Type
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by eiko on 30.05.17.
 */
class DoCodeGen {
    @Parameter(names = [ '-m', '--model' ], description = "Path to JSON schema to parse")
    private String model

    @Parameter(names = [ '-o', '--outputBase' ], description = "Base directory for the output")
    private String outputBaseDir

    public static void main(String ... args) {
        DoCodeGen doCodeGen = new DoCodeGen();
        JCommander.newBuilder()
                .addObject(doCodeGen)
                .build()
                .parse(args);
        doCodeGen.run();
    }

    void run() {
        log.info("model=${model}")
        log.info("outPutBase=${outputBaseDir}")

        // remove in finished version - start
        if (!model) {
            model='src/test/resources/schemas/ProcessDataEvent.json'
        }
        // remove in finished version - end

        def modelFile = new File (model);
        if (!modelFile.isFile()) {
            log.error("path to model file doesn't point to a file: ${model}")
            System.exit(1)
        }
        else {
            log.info("use model file: ${model}")
        }
        Model dataModel = buildModel(modelFile)
        // TODO start CodeGen
        println dataModel
    }

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

    static String string2Name(String s) {
        return s.replaceAll('[^a-zA-Z0-9]','_')
    }

    private Model modelFromMultiTypeSchema(def objectModel) {
        Model model = initModel(objectModel)
        return null
    }

    private List listFromMap(def map,String key) {
        def listObj = map[key]
        if (!listObj) {
            return []
        }
        else {
            return listObj
        }

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

    private void setTypeFromPropMap(def propMap, def newType) {
        def typeStr = strFromMap(propMap,'type')
        if (!typeStr) throw new Exception("unknown property type: ${propMap}, ${newType}")
        switch (typeStr) {
            case 'integer':
                // TODO
                break
            case 'number':
                // TODO
                break
            case 'string':
                // TODO
                break
            case 'boolean':
                // TODO
                break
            default:
                throw new Exception ("unknown type: ${typeStr}, ${propMap}")
                // TODO
        }
    }

    private String strFromMap(def map,String key,String defValue=null) {
        def v = map[key]
        if (!v) v = defValue
        return v
    }

    private Model initModel(def objectModel) {
        Model model = new Model()
        model.title = strFromMap(objectModel,'title')
        model.description = strFromMap(objectModel,'description')
        return model
    }

    private static final Logger log=LoggerFactory.getLogger(DoCodeGen.class);
}
