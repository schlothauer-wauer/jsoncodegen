package de.lisaplus.atlas

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.codegen.TemplateType
import de.lisaplus.atlas.codegen.external.ExtMultiFileGenarator
import de.lisaplus.atlas.codegen.external.ExtSingleFileGenarator
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
    @Parameter(names = [ '-m', '--model' ], description = "Path to JSON schema to parse", required = true)
    private String model

    @Parameter(names = [ '-o', '--outputBase' ], description = "Base directory for the output", required = true)
    private String outputBaseDir

    @Parameter(names = ['-g', '--generator'], description = "generator that are used with the model. This parameter can be used multiple times")
    private List<String> generators

    @Parameter(names = ['-gp', '--generator-parameter'], description = "special parameter that are passed to template via maps")
    private List<String> generator_parameters

    @Parameter(names = ['-h','--help'], help = true)
    private boolean help = false

    static void main(String ... args) {
        DoCodeGen doCodeGen = new DoCodeGen()
        try {
            JCommander jCommander = JCommander.newBuilder()
                    .addObject(doCodeGen)
                    .build()
            jCommander.setProgramName(doCodeGen.getClass().typeName)
            jCommander.parse(args)
            if (doCodeGen.help) {
                printHelp()
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
        log.info("model=${model}")
        log.info("outPutBase=${outputBaseDir}")

        def modelFile = new File (model)
        if (!modelFile.isFile()) {
            log.error("path to model file doesn't point to a file: ${model}")
            System.exit(1)
        }
        else {
            log.info("use model file: ${model}")
        }

        prepareOutputBaseDir(outputBaseDir)

        IModelBuilder builder = new JsonSchemaBuilder()
        Model dataModel = builder.buildModel(modelFile)
        // convert extra generator parameter to a map
        Map<String,String> extraParameters = getMapFromGeneratorParams(generator_parameters)
        if (generators==null || generators.isEmpty()) {
            log.warn('no generators configured - skip')
        }
        else {
            // TODO start CodeGen
            generators.each { generatorName ->
                def trennerIndex = generatorName.indexOf('=')
                def templateName = trennerIndex!=-1 && trennerIndex < generatorName.length() - 1? generatorName.substring(trennerIndex+1) : null
                // the generator name can be a class that's known to compile time or a later linked one
                def pureGeneratorName = trennerIndex!=-1? generatorName.substring(0,trennerIndex) : generatorName
                // later linked generators needs to contain package seperator
                if (pureGeneratorName.indexOf('.')!=-1) {
                    // a later linked generator
                    useCustomGenerator(pureGeneratorName,templateName,dataModel,extraParameters,outputBaseDir)
                }
                else {
                    // a build in generator
                    useBuiltInGenerator(pureGeneratorName,templateName,dataModel,extraParameters,outputBaseDir)
                }
            }
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

    static void useCustomGenerator(String generatorName, String templateName, Model dataModel, Map<String,String> extraParameters,String outputBaseDir) {
        // TODO
    }

    static void useBuiltInGenerator(String generatorName, String templateName, Model dataModel, Map<String,String> extraParameters,String outputBaseDir) {
        switch (generatorName) {
            case 'java_beans':
                ignoreTemplateMsg(generatorName, templateName)
                // TODO
                break
            case 'swagger_file':
                ignoreTemplateMsg(generatorName, templateName)
                // TODO
                break
            case 'multifiles':
                generateMultiFiles(generatorName,templateName,dataModel,extraParameters,outputBaseDir)
                break
            case 'singlefile':
                generateSingleFile(generatorName,templateName,dataModel,extraParameters,outputBaseDir)
                break
            default:
                def errorMsg = "unknown built in generator: ${generatorName}"
                log.error(errorMsg)
                throw new Exception (errorMsg)
        }
    }

    static void generateMultiFiles(String generatorName,String templateName,Model dataModel, Map<String,String> extraParameters,String outputBaseDir) {
        checkForTemplate(generatorName,templateName)
        IExternalCodeGen generator = new ExtMultiFileGenarator()
        if (isTemplateFile(templateName)) {
            generator.initTemplateFromFile(templateName, TemplateType.GString)
        }
        else {
            generator.initTemplateFromResource(templateName,TemplateType.GString)
        }
        generator.doCodeGen(dataModel,outputBaseDir,extraParameters)
    }

    static void generateSingleFile(String generatorName,String templateName, Model dataModel, Map<String,String> extraParameters,String outputBaseDir) {
        checkForTemplate(generatorName,templateName)
        IExternalCodeGen generator = new ExtSingleFileGenarator()
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


    static void printHelp() {
        print '''
This program provides some kind of extendable code generation. As input it use
JSON schema files that describes a model

Examples:
# 1. reads a model from file ./test/mein_modell.json
# 2. use the internal java_bean generator
# 3. gives the extra parameter packageBase with value 'de.sw.atlas' to the generator
# 4. writes the output to ./test/mein_output
de.lisaplus.atlas.DoCodeGen -o ./test/mein_output -m ./test/mein_modell.json -g java_beans -gp packageBase=de.sw.atlas 

# 1. reads a model from file ./test/mein_modell.json
# 2. use two internal java_bean generators: java_beans, swagger_file
# 3. gives the extra parameter packageBase with value 'de.sw.atlas' to the generator
# 4. writes the output to ./test/mein_output
de.lisaplus.atlas.DoCodeGen -o ./test/mein_output -m ./test/mein_modell.json -g java_beans -g swagger_file \\
    -gp packageBase=de.sw.atlas

# 1. reads a model from file ./test/mein_modell.json
# 2. use two internal 'multifiles'-generator with the external template ./test/myTemplate.txt
# 3. gives the extra parameter packageBase with value 'de.sw.atlas' to the generator
# 4. writes the output to ./test/mein_output
de.lisaplus.atlas.DoCodeGen -o ./test/mein_output -m ./test/mein_modell.json -g multifiles=./test/myTemplate.txt  \\
    -gp packageBase=de.sw.atlas

Already defines internal generators with their extra parameters:

1. Java related
===============
java_beans      - creates a set of Java beans from model
    Extra parameter
    ---------------
    packageBase         - base java package used for the generation

2. Service related 
==================
swagger_file    - creates a swagger file from model

3. Base generators
==================
multifiles      - creates multible files from model and extra given template
singlefile      - creates single file from model and extra given template

'''
    }


    private static final Logger log=LoggerFactory.getLogger(DoCodeGen.class)
}
