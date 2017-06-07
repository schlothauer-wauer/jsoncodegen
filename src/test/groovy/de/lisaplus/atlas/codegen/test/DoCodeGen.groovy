package de.lisaplus.atlas.codegen.test

import org.junit.Test

import static junit.framework.Assert.assertNull
import static org.junit.Assert.assertEquals

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
}
