package de.lisaplus.atlas.codegen.test

import de.lisaplus.atlas.codegen.test.base.FileHelper
import org.junit.Test

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class MultiFileTemplates {
    @Test
    void testHandlingTemplate() {
        def destDir = 'tmp/handling'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/incident.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('multifiles=src/test/resources/templates/handling.txt')
        doCodeGen.generatorScript = 'src/test/resources/templates/handling_helper.groovy'
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('packageName=de.handling')
        doCodeGen.run()
    }

}
