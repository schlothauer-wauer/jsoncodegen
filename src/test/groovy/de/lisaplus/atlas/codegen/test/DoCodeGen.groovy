package de.lisaplus.atlas.codegen.test

import org.junit.Test

import static junit.framework.Assert.assertFalse
import static junit.framework.Assert.assertNull
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

/**
 * Created by eiko on 07.06.17.
 */
class DoCodeGen {
    @Test
    void test_getMapFromGeneratorParams_1() {
        def input = ['test=test','a=test with spaces','b=täst','c=täst with spaces']
        Map<String,String> output = de.lisaplus.atlas.DoCodeGen.getMapFromGeneratorParams(input)
        assertEquals(4,output.size())
        assertEquals('test',output['test'])
        assertEquals('test with spaces',output['a'])
        assertEquals('täst',output['b'])
        assertEquals('täst with spaces',output['c'])
    }

    @Test
    void test_getMapFromGeneratorParams_2() {
        def input = ['test=test', 'a=test with spaces', 'btäst', 'c=täst with spaces']
        Map<String, String> output = de.lisaplus.atlas.DoCodeGen.getMapFromGeneratorParams(input)
        assertEquals(3,output.size())
        assertEquals('test',output['test'])
        assertEquals('test with spaces',output['a'])
        assertNull(output['b'])
        assertEquals('täst with spaces',output['c'])
    }

    @Test
    void test_prepareOutputBaseDir() {
        def testDir='build/tmp/test_prepareOutputBaseDir/test/test2'
        def f1 = new File ('build/tmp/test_prepareOutputBaseDir')
        if (f1.isDirectory()) f1.deleteDir()
        assertFalse(f1.exists())
        def f2 = new File (testDir)
        try {
            assertFalse(f2.exists())
            de.lisaplus.atlas.DoCodeGen.prepareOutputBaseDir(testDir)
            assertTrue(f2.exists())
        }
        finally {
            f1.deleteDir()
        }
    }
}
