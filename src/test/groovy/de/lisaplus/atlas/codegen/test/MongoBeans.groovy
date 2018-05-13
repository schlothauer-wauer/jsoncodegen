package de.lisaplus.atlas.codegen.test

import de.lisaplus.atlas.codegen.test.base.FileHelper
import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 * Tests the java bean generator and template
 * Created by eiko on 19.06.17.
 */
class MongoBeans {
    @Test
    void createFromUserModel() {
        def destDir = 'tmp/mongo_beans'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/junction2.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model = modelFile
        doCodeGen.generators.add('mongo_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('packageName=de.schlothauer.test.mongo')
        doCodeGen.run()
    }
}
