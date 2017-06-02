package de.lisaplus.atlas

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.Model
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


    private static final Logger log=LoggerFactory.getLogger(DoCodeGen.class);
}
