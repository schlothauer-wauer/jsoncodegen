package de.lisaplus.atlas

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.builder.XSDBuilder
import de.lisaplus.atlas.codegen.TemplateType
import de.lisaplus.atlas.codegen.external.ExtMultiFileGenarator
import de.lisaplus.atlas.codegen.external.ExtSingleFileGenarator
import de.lisaplus.atlas.codegen.java.JavaBeanGenerator
import de.lisaplus.atlas.codegen.java.JavaInterfaceGenerator
import de.lisaplus.atlas.codegen.java.JavaInterfacedBeanGenerator
import de.lisaplus.atlas.codegen.java.JavaInterfacedGenericDerivedBeanGenerator
import de.lisaplus.atlas.codegen.java.JavaGenericDerivedBeanGenerator
import de.lisaplus.atlas.codegen.java.MongoBeanGenerator
import de.lisaplus.atlas.codegen.meta.HistModelGenerator
import de.lisaplus.atlas.codegen.meta.JsonSchemaGenerator
import de.lisaplus.atlas.codegen.meta.PlantUmlGenerator
import de.lisaplus.atlas.codegen.meta.SwaggerGenerator
import de.lisaplus.atlas.codegen.meta.SwaggerGeneratorExt
import de.lisaplus.atlas.codegen.meta.XsdGenerator
import de.lisaplus.atlas.interf.IExternalCodeGen
import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.Model
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Main class to start the code generation
 * Created by eiko on 30.05.17.
 */
class DoCodeGen {
    /**
     * model file
     */
    @Parameter(names = [ '-m', '--model' ], description = "Path to JSON schema to parse", required = true)
    List<String> models=[]

    /**
     * Base directory for the output
     */
    @Parameter(names = [ '-o', '--outputBase' ], description = "Base directory for the output")
    String outputBaseDir

    /**
     * Generator to use
     */
    @Parameter(names = ['-g', '--generator'], description = "generator that are used with the model. This parameter can be used multiple times")
    List<String> generators = []


    /**
     * Generator to use
     */
    @Parameter(names = ['-gs', '--generator-scripts'], description = "additional script that should be passed to the used templates")
        String generatorScript

    /**
     * Generator parameter
     */
    @Parameter(names = ['-gp', '--generator-parameter'], description = "special parameter that are passed to template via maps")
    List<String> generator_parameters = []

    /**
     * Type white list
     */
    @Parameter(names = ['-w', '--white-list'], description = "white listed type, multiple usage possible")
    List<String> whiteListed = []

    /**
     * Type black list
     */
    @Parameter(names = ['-b', '--black-list'], description = "black listed type, multiple usage possible")
    List<String> blackListed = []

    /**
     * Print help
     */
    @Parameter(names = ['-h','--help'], help = true)
    boolean help = false

    /**
     * Simply print main types
     */
    @Parameter(names = ['-pmt','--print-main-types'], description = "don't do any code generation, simply loads the model and print the main-types of it")
    boolean printMainTypes = false

    @Parameter(names = ['-pmts','--print-main-types-separator'], description = "separator to use for printing main types")
    String printMainTypesSeparator

    @Parameter(names = ['-pmti','--print-main-types-info'], description = "print with info header")
    boolean printMainTypesInfo = false

    /**
     * an required type property to include into main types
     */
    @Parameter(names = ['-mta','--main-types-attrib'], description = "specify a needed attribute to be a maintype, used in addition to the schema location")
    String mainTypeAttrib

    /**
     * an required type property to include into main types
     */
    @Parameter(names = ['-tmt','--tag-main-types'], description = "if this flag is set all maintypes will be extended with a 'mainType' tag")
    boolean tagMainTypes

    /**
     * List of type-name tag-text tuple, The tags will be merged after initialization with the object tree
     */
    @Parameter(names = ['-at', '--add-tag'], description = "add a text as tag to a specific type, f.e. -at User=unused")
    List<String> typeAddTagList = []

    /**
     * List of type-name tag-text tuple, after initialization the tags will be removed for the given types in the object tree
     */
    @Parameter(names = ['-rt', '--remove-tag'], description = "remove a tag from a specific type, f.e. -rt User=unused")
    List<String> typeRemoveTagList = []

    /**
     * List of tags-text tuple, after initialization the tags will be removed for the given types in the object tree
     */
    @Parameter(names = ['-rta', '--remove-tag-all'], description = "remove a tag from all model types, f.e. -rta rest")
    List<String> typeRemoveTagAllList = []


    /**
     * List of tags-text tuple, after initialization the tags will be removed for the given types in the object tree
     */
    @Parameter(names = ['-rta2', '--remove-tag-all-if-not-main'], description = "remove a tag from all model types that are no main types, f.e. -rta rest")
    List<String> typeRemoveTagAllList2 = []

    /**
     * an required type property to include into main types
     */
    @Parameter(names = ['-rta2a','--remove-tag-all-if-not-main-attrib'], description = "don't do any code generation, simply loads the model and print the main-types of it")
    String mainTypeAttrib2

    /**
     * if set enum types insteed of strings will be used
     */
    @Parameter(names = ['-cet','--create-enum-types'], description = "if set the model is built with enum types")
    boolean createEnumTypes = false


    /**
     * The datamodel parsed by the builder. It is public accessible for tests
     */
    Model dataModel

    static void main(String ... args) {
        DoCodeGen doCodeGen = new DoCodeGen()
        try {
            JCommander jCommander = JCommander.newBuilder()
                    .addObject(doCodeGen)
                    .build()
            jCommander.setProgramName(doCodeGen.getClass().typeName)
            jCommander.parse(args)
            if (doCodeGen.help) {
                doCodeGen.printHelp()
                jCommander.usage()
                return
            }
            doCodeGen.run()
        }
        catch(ParameterException e) {
            e.usage()
        }
    }

    void run() {
        log.info("model=${models}")
        log.info("outPutBase=${outputBaseDir}")

        models.each { model ->
            def modelFile = model instanceof File ? model : new File (model)
            def modelName = modelFile.getName()
            if (!modelFile.isFile()) {
                log.error("path to model file doesn't point to a file: ${model}")
                System.exit(1)
            }
            else {
                log.info("use model file: ${model}")
            }

            IModelBuilder builder = modelName.toLowerCase().endsWith('.json') ? new JsonSchemaBuilder() :
                    modelName.toLowerCase().endsWith('.xsd') ? new XSDBuilder() : null
            if (builder==null) {
                log.error("unknown file type, currently only jscon schema and xsd are supported: ${model}")
                System.exit(1)
            }
            if (builder instanceof JsonSchemaBuilder) {
                ((JsonSchemaBuilder)builder).createEnumTypes = createEnumTypes
            }
            Model tmpModel = builder.buildModel(modelFile)
            printMainTypesIfNeeded(tmpModel,modelFile.getName())
            adjustTagsForModel(tmpModel,modelFile.getName())
            if (!dataModel) {
                dataModel = tmpModel
            }
            else {
                mergeIntoDataModel(dataModel,tmpModel)
            }
        }
        sortTypesAndProperties(dataModel)
        prepareOutputBaseDir(outputBaseDir)

        // convert extra generator parameter to a map
        Map<String,String> extraParameters = getMapFromGeneratorParams(generator_parameters)
        extraParameters['blackListed']=blackListed
        extraParameters['whiteListed']=whiteListed
        if (generators==null || generators.isEmpty()) {
            log.warn('no generators configured - skip')
        }
        else {
            generators.each { generatorName ->
                def trennerIndex = generatorName.indexOf('=')
                def templateName = trennerIndex!=-1 && trennerIndex < generatorName.length() - 1? generatorName.substring(trennerIndex+1) : null
                // the generator name can be a class that's known to compile time or a later linked one
                def pureGeneratorName = trennerIndex!=-1? generatorName.substring(0,trennerIndex) : generatorName
                // later linked generators needs to contain package seperator
                if (pureGeneratorName.indexOf('.')!=-1) {
                    // a later linked generator - not implemented yet
                    useCustomGenerator(pureGeneratorName,templateName,dataModel,extraParameters,outputBaseDir,generatorScript)
                }
                else {
                    // a build in generator
                    useBuiltInGenerator(pureGeneratorName,templateName,dataModel,extraParameters,outputBaseDir,generatorScript)
                }
            }
        }
    }

    static void mergeIntoDataModel(Model mainModel,Model newModel) {
        newModel.types.each { type ->
            if (!mainModel.types.find { existingType -> existingType.name==type.name &&
                    existingType.schemaFileName==type.schemaFileName }) {
                mainModel.types.add(type)
            }
        }
    }

    static void sortTypesAndProperties(Model model) {
        model.types.sort{ a,b -> a.name<=>b.name}
        model.types.each { type ->
            type.properties.sort{ a,b -> a.name<=>b.name }
        }
    }

    void printMainTypesIfNeeded(Model dataModel, String fileName) {
        if (printMainTypes) {
            def mfn = fileName
            String separator = printMainTypesSeparator ? printMainTypesSeparator : ' '
            def attrib = mainTypeAttrib ? " - needed attrib: $mainTypeAttrib" : ''
            def info = "mainTypes [$mfn$attrib]:"
            def outStr = printMainTypesInfo ? info : ''
            dataModel.types.each { type ->
                if (type.isMainType(mfn)) {
                    if ((!mainTypeAttrib) || (type.hasPropertyWithName(mainTypeAttrib))) {
                        if (outStr=='') {
                            outStr = "${type.name}"
                        }
                        else {
                            outStr = "${outStr}${separator}${type.name}"
                        }
                    }
                }
            }
            println (outStr)
            System.exit(0)
        }
    }

    /**
     * splits extra generator parameter to Map values
     * expected entries looks like this: packageBase=de.sw.atlas
     * @param generator_parameters
     * @return
     */
    static Map<String,String> getMapFromGeneratorParams(List<String> generator_parameters) {
        Map<String,String> map = [:]
        if (generator_parameters==null) {
            return map
        }
        generator_parameters.each { param ->
            String[] splittedParam = param.split('=')
            if (splittedParam.length!=2) {
                log.warn("extra generator parameter has wrong format and will be ignored: ${param}")
            }
            else {
                map.put(splittedParam[0].trim(),splittedParam[1].trim())
            }
        }
        return map
    }

    /**
     * Creates the output directory for a later use in later programm steps
     * @param outputBaseDir path to the desired directory
     */
    static void prepareOutputBaseDir(String outputBaseDir) {
        File f = new File(outputBaseDir)
        if (f.isDirectory()) return
        if (f.isFile()) {
            def errorMsg = "the given output directory already exists as a file: $outputBaseDir"
            log.error(errorMsg)
            throw new Exception (errorMsg)
        }
        f.mkdirs()
        if (!f.isDirectory()) {
            def errorMsg = "can't create output base dir: $outputBaseDir"
            log.error(errorMsg)
            throw new Exception (errorMsg)
        }
    }

    static void useCustomGenerator(String generatorName, String templateName, Model dataModel, Map<String,String> extraParameters,String outputBaseDir,String generatorScript) {
        // TODO
    }

    static void useBuiltInGenerator(String generatorName, String templateName, Model dataModel, Map<String,String> extraParameters,String outputBaseDir,String generatorScript) {
        switch (generatorName) {
            case 'java_interfaces':
                JavaInterfaceGenerator generator = new JavaInterfaceGenerator()
                generator.initTemplate()
                generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
                break
            case 'java_beans':
                JavaBeanGenerator generator = new JavaBeanGenerator()
                generator.initTemplate()
                generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
                break
            case 'mongo_beans':
                MongoBeanGenerator generator = new MongoBeanGenerator()
                generator.initTemplate()
                generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
                break
            case 'java_generic_derived_beans':
                JavaGenericDerivedBeanGenerator generator = new JavaGenericDerivedBeanGenerator()
                generator.initTemplate()
                generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
                break
            case 'java_interfaced_beans':
                JavaInterfacedBeanGenerator generator = new JavaInterfacedBeanGenerator()
                generator.initTemplate()
                generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
                break
            case 'java_interfaced_generic_derived_beans':
                JavaInterfacedGenericDerivedBeanGenerator generator = new JavaInterfacedGenericDerivedBeanGenerator()
                generator.initTemplate()
                generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
                break
            case 'swagger':
                SwaggerGenerator generator = new SwaggerGenerator()
                generator.initTemplate()
                generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
                break
            case 'swagger_ext':
            case 'swagger-ext':
                SwaggerGenerator generator = new SwaggerGeneratorExt()
                generator.initTemplate()
                generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
                break
            case 'plantuml':
                PlantUmlGenerator generator = new PlantUmlGenerator()
                generator.initTemplate()
                generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
                break
            case 'plantuml_java':
                PlantUmlGenerator generator = new PlantUmlGenerator()
                generator.initTemplate()
                generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
                break
            case 'hist_model':
                HistModelGenerator generator = new HistModelGenerator()
                generator.initTemplate()
                generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
                break
            case 'json_schema':
                JsonSchemaGenerator generator = new JsonSchemaGenerator()
                generator.initTemplate()
                generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
                break
            case 'xsd':
                XsdGenerator generator = new XsdGenerator()
                generator.initTemplate()
                generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
                break
            case 'multifiles':
                generateMultiFiles(generatorName,templateName,dataModel,extraParameters,outputBaseDir,generatorScript)
                break
            case 'singlefile':
                generateSingleFile(generatorName,templateName,dataModel,extraParameters,outputBaseDir,generatorScript)
                break
            default:
                def errorMsg = "unknown built in generator: ${generatorName}"
                log.error(errorMsg)
                throw new Exception (errorMsg)
        }
    }

    static void generateMultiFiles(String generatorName,String templateName,Model dataModel, Map<String,String> extraParameters,String outputBaseDir,String generatorScript) {
        checkForTemplate(generatorName,templateName)
        IExternalCodeGen generator = new ExtMultiFileGenarator()
        generator.setGeneratorScript(generatorScript)
        if (isTemplateFile(templateName)) {
            generator.initTemplateFromFile(templateName, TemplateType.GString)
        }
        else {
            generator.initTemplateFromResource(templateName,TemplateType.GString)
        }
        generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
    }

    static void generateSingleFile(String generatorName,String templateName, Model dataModel, Map<String,String> extraParameters,String outputBaseDir,String generatorScript) {
        checkForTemplate(generatorName,templateName)
        IExternalCodeGen generator = new ExtSingleFileGenarator()
        generator.setGeneratorScript(generatorScript)
        if (isTemplateFile(templateName)) {
            generator.initTemplateFromFile(templateName, TemplateType.GString)
        }
        else {
            generator.initTemplateFromResource(templateName,TemplateType.GString)
        }
        generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
    }

    static boolean isTemplateFile(String templateName) {
        File f = new File(templateName)
        return f.isFile()
    }

    static void ignoreTemplateMsg(String generatorName, String templateName) {
        if (templateName) {
            log.warn("the given external template for generator '${generatorName}' will be ignored")
        }
    }

    static void checkForTemplate(String generatorName, String templateName) {
        if (!templateName) {
            def errorMsg = "no template given for generator '${generatorName}'"
            log.error(errorMsg)
            throw new Exception(errorMsg)
        }
    }


    void printHelp() {
        InputStream usageFile = this.class.getClassLoader().getResourceAsStream('docs/usage.md')
        print (usageFile.getText())
    }

    private void adjustTagsForModel(Model dataModel, String modelFileName) {
        Map<String, List<String>> typeAddTagMap = mapFromConfig(typeAddTagList)
        Map<String, List<String>> typeRemoveTagMap = mapFromConfig(typeRemoveTagList)
        // remove all tags
        dataModel.types.each { type ->
            boolean isMainType = type.isMainType(modelFileName) && ((!mainTypeAttrib) || type.hasPropertyWithName(mainTypeAttrib))
            if(tagMainTypes && isMainType) {
                type.tags.add('mainType')
            }
            // remove all tags
            typeRemoveTagAllList.each { tag ->
                def tagList = tag.indexOf(',')!=-1 ? tag.split(',') : tag.split(':')
                tagList.each { t ->
                    if (type.tags.contains(t)) {
                        type.tags.remove(t)
                    }
                }
            }
            // remove tags from not main types
            typeRemoveTagAllList2.each { tag ->
                def tagList = tag.indexOf(',')!=-1 ? tag.split(',') : tag.split(':')
                if ((!isMainType) && ((!mainTypeAttrib2) || (type.hasPropertyWithName(mainTypeAttrib2)))) {
                    tagList.each { t ->
                        if (type.tags.contains(t)) {
                            type.tags.remove(t)
                        }
                    }
                }
            }
            // remove undesired tags
            List<String> tagsToRemove = typeRemoveTagMap[type.name]
            if (tagsToRemove) {
                // remove tags
                tagsToRemove.each { tag ->
                    type.tags.remove(tag)
                }
            }
            // add new tags
            List<String> tagsToAdd = typeAddTagMap[type.name]
            if (tagsToAdd) {
                // add tags
                tagsToAdd.each { tag ->
                    if (!type.tags.contains(tag)) {
                        type.tags.add(tag)
                    }
                }
            }
        }
    }


    private Map<String, List<String>> mapFromConfig(List<String> config) {
        Map<String, List<String>> ret = [:]
        if (!config) return ret
        config.each { typeTagStr ->
            def typeTagArray = typeTagStr.split('=')
            if (typeTagArray.length!=2) {
                println "[mapFromConfig] - wrong type/tag-tuple: $typeTagStr"
                return
            }
            def typeName = typeTagArray[0].trim()
            def typeNameList = typeName.indexOf(',')!=-1 ? typeName.split(',') : typeName.split(':')
            def tag = typeTagArray[1].trim()
            def tagList = tag.indexOf(',')!=-1 ? tag.split(',') :  tag.split(':')
            typeNameList.each { name ->
                List<String> alreadyExistingValues = ret[name]
                if (alreadyExistingValues) {
                    tagList.each { t ->
                        if (!alreadyExistingValues.contains(t)) {
                            alreadyExistingValues.add(t)
                        }
                    }
                }
                else {
                    List<String> values = []
                    tagList.each { t ->
                        if (!values.contains(t)) {
                            values.add(t)
                        }
                    }
                    ret[name] = values
                }
            }
        }
        return ret
    }
    private static final Logger log=LoggerFactory.getLogger(DoCodeGen.class)
}
