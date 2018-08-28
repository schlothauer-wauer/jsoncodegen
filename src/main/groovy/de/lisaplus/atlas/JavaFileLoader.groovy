package de.lisaplus.atlas

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.codegen.TemplateType
import de.lisaplus.atlas.codegen.external.ExtMultiFileGenarator
import de.lisaplus.atlas.codegen.external.ExtSingleFileGenarator
import de.lisaplus.atlas.codegen.java.*
import de.lisaplus.atlas.codegen.meta.*
import de.lisaplus.atlas.interf.IExternalCodeGen
import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.Model
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Method

/**
 * Main class to start the code generation
 * Created by eiko on 30.05.17.
 */
class JavaFileLoader {
    @Parameter(names = [ '-t', '--template' ], description = "comma separated list of classes to load", required = true)
    String template

    @Parameter(names = ['-p', '--path'], description = "path to load the Java files")
    List<String> paths = []

    @Parameter(names = ['-tp', '--template-parameter'], description = "special parameter that are passed to template via maps")
    List<String> template_parameters = []

    @Parameter(names = ['-c', '--class'], description = "class to load")
    List<String> classes = []

    @Parameter(names = ['-h','--help'], help = true)
    boolean help = false

    static void main(String ... args) {
        JavaFileLoader doCodeGen = new JavaFileLoader()
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
        final GroovyClassLoader classLoader = new GroovyClassLoader()
        classLoader.addClasspath('/home/eiko/prog/gitg_swarco/lisa-junction-server/tmp/server/target/junction-server-1.0.0.jar')
/*
        paths.forEach { path ->
            classLoader.addClasspath(path)
        }
*/
/*
        classes.forEach { className ->
            Class c = classLoader.loadClass(className)
            Method[] methods = c.getMethods()
        }
        */
        Class c = classLoader.loadClass('BOOT-INF.classes.io.swagger.Swagger2SpringBoot$ExitException')
        Method[] methods = c.getMethods()
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

    void printHelp() {
        InputStream usageFile = this.class.getClassLoader().getResourceAsStream('docs/usage.md')
        print (usageFile.getText())
    }


    private static final Logger log=LoggerFactory.getLogger(JavaFileLoader.class)
}
