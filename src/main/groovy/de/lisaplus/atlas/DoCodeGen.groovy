package de.lisaplus.atlas

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.Model
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by eiko on 30.05.17.
 */
class DoCodeGen {
    @Parameter(names = [ '-m', '--model' ], description = "Path to JSON schema to parse", required = true)
    private String model

    @Parameter(names = [ '-o', '--outputBase' ], description = "Base directory for the output", required = true)
    private String outputBaseDir

    @Parameter(names = ['-g', '--generator'], description = "generator that are used with the model. This parameter can be used multiple times")
    private List<String> generators

    @Parameter(names = ['-h','--help'], help = true)
    private boolean help = false;

    public static void main(String ... args) {
        DoCodeGen doCodeGen = new DoCodeGen();
        try {
            JCommander jCommander = JCommander.newBuilder()
                    .addObject(doCodeGen)
                    .build()
            jCommander.setProgramName(doCodeGen.getClass().typeName)
            jCommander.parse(args);
            if (doCodeGen.help) {
                doCodeGen.printHelp()
                jCommander.usage()
                return
            }
            doCodeGen.run();
        }
        catch(ParameterException e) {
            e.usage()
        }
    }

    void run() {
        log.info("model=${model}")
        log.info("outPutBase=${outputBaseDir}")

        def modelFile = new File (model);
        if (!modelFile.isFile()) {
            log.error("path to model file doesn't point to a file: ${model}")
            System.exit(1)
        }
        else {
            log.info("use model file: ${model}")
        }
        IModelBuilder builder = new JsonSchemaBuilder()
        Model dataModel = builder.buildModel(modelFile)
        // TODO start CodeGen
        println dataModel
    }

    void printHelp() {
        print '''
This program provides some kind of extendable code generation. As input it use
JSON schema files that describes a model

Examples:
 
'''
    }


    private static final Logger log=LoggerFactory.getLogger(DoCodeGen.class);
}
