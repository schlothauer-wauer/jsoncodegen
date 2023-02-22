package de.lisaplus.atlas.codegen.test

import de.lisaplus.atlas.codegen.test.base.FileHelper
import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 * Tests the generator for java interfaced beans and template
 * Created by eiko on 19.06.17.
 */
class JavaInterfacedBeans {
    @Test
    void createFromUserModel() {
        def destDir = 'tmp/java_interfaced_beans'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/user.json')

        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_interfaces')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('packageName=de.test.jsoncodegen.interf')
        doCodeGen.run()

        de.lisaplus.atlas.DoCodeGen doCodeGen2 = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen2.models = [modelFile]
        doCodeGen2.generators.add('java_interfaced_beans')
        doCodeGen2.outputBaseDir = destDir
        doCodeGen2.generator_parameters.add('removeEmptyLines=true')
        doCodeGen2.generator_parameters.add('interfacePackageName=de.test.jsoncodegen.interf')
        doCodeGen2.generator_parameters.add('packageName=de.test.jsoncodegen.impl')
        doCodeGen2.run()
    }

    @Test
    void testMapObjMulti() {
        def modelFile = new File('src/test/resources/test_schemas/ds/base_types/map_object_multi_type.json')
        def destDir = 'tmp/java_interfaced_beans'
        FileHelper.removeDirectoryIfExists(destDir)

        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_interfaces')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('packageName=de.test2.jsoncodegen.interf')
        doCodeGen.run()

        de.lisaplus.atlas.DoCodeGen doCodeGen2 = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen2.models = [modelFile]
        doCodeGen2.generators.add('java_interfaced_beans')
        doCodeGen2.outputBaseDir = destDir
        doCodeGen2.generator_parameters.add('removeEmptyLines=true')
        doCodeGen2.generator_parameters.add('interfacePackageName=de.test.jsoncodegen.interf')
        doCodeGen2.generator_parameters.add('packageName=de.test2.jsoncodegen.impl')
        doCodeGen2.run()
    }

    @Test
    void testSimpleMapObj() {
        def modelFile = new File('src/test/resources/test_schemas/ds/base_types/simple_map_object.json')
        def destDir = 'tmp/java_interfaced_beans'
        FileHelper.removeDirectoryIfExists(destDir)

        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_interfaces')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('packageName=de.test3.jsoncodegen.interf')
        doCodeGen.run()

        de.lisaplus.atlas.DoCodeGen doCodeGen2 = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen2.models = [modelFile]
        doCodeGen2.generators.add('java_interfaced_beans')
        doCodeGen2.outputBaseDir = destDir
        doCodeGen2.generator_parameters.add('removeEmptyLines=true')
        doCodeGen2.generator_parameters.add('interfacePackageName=de.test.jsoncodegen.interf')
        doCodeGen2.generator_parameters.add('packageName=de.test3.jsoncodegen.impl')
        doCodeGen2.run()

    }

}
