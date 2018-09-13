package de.lisaplus.atlas

import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.codegen.GeneratorBase
import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Property
import de.lisaplus.atlas.model.Type

class MaskTestExperiment {

    static main(args) {

        def base = 'C:\\Entwicklung\\lisa-junction-server\\models\\models-lisa-server\\model\\'
        def modelPath = args.length == 0 ?
                base + 'junction.json'
                // base + 'shared\\geo_point.json'
                : args[0]
        def typeName = args.length > 0 ?
                args[1]
                : 'JunctionJoined' // 'Junction' // 'JunctionNumber'  // 'Contact_type' // 'JunctionLocation' // 'JunctionContact'

        def maskExp = new MaskTestExperiment(modelPath)
        maskExp.execute(typeName, typeName.endsWith('Joined'))

        // Check for exception while running code generation for all available types
//        maskExp.generateAll()
    }

    /** The complete model with all types */
    Model model
    /** The bindings of the template / code generation */
    Map data
    /** Indicates whether the current type is a joined type */
    boolean joined
    /** The name of the Java class of the type. */
    String targetType
    /** This stack holds the property (names) visited while traversing the object hierarchy.*/
    List<Property> propStack

    MaskTestExperiment( String modelPath) {
        this.model = readModel(modelPath)
    }

    private Model readModel(String modelPath) {
        def modelFile = new File(modelPath)
        IModelBuilder builder = new JsonSchemaBuilder()
        return builder.buildModel(modelFile)
    }

    /**
     * Execute code generation for all types
     */
    void generateAll() {
        GeneratorBase generator = new DummyGenerator()
        data = generator.createTemplateDataMap(model)
        for(Type type : model.types) {
            executeForType(type, type.name.endsWith('Joined'))
        }
    }

    /**
     * Execute code generation for one type
     * @param typeName The name of the type, which is to be processed
     * @param joined Indicates whether that type is a joined type
     */
    void execute(String typeName, boolean joined) {
        GeneratorBase generator = new DummyGenerator()
        data = generator.createTemplateDataMap(model)
        Type type = data.model.types.find {type -> type.name == typeName}
        executeForType(type, joined)
    }

    private void executeForType(Type type, boolean joined) {
        this.joined = joined
        targetType = data.upperCamelCase.call(type.name)
        boolean printDebug = false;
        println '###################################################################'
        println "Start of $targetType:"
        println '###################################################################'

        /* 1st loop: find property names and mask keys */
        propStack = []
        // The names of the properties defined in the current type
        Set<String> propNames = []
        // The mask keys, which are available for the current type
        List<String> maskKeys = []
        findNamesKeysForType.call(type, propNames, maskKeys)

        if (printDebug) {
            // Debug output of 1st loop:
            List<String> sorted = new ArrayList<>(propNames); Collections.sort(sorted)
            println "propNames=${sorted}"
            sorted.clear(); sorted.addAll(maskKeys); Collections.sort(sorted)
            println "maskKey=${sorted}"
        }

        /* 2nd loop: find property names affected by masking a mask key */
        propStack = []
        // A mapping of mask key to the property names affected when masking the property associated with that mask key
        Map<String,Set<String>> maskKey2PropNames = [:]
        finaKeyAffectedParamsForType.call(type, maskKey2PropNames)

        // Debug output of 2nd loop:
        if (printDebug) {
            maskKey2PropNames.keySet().stream().sorted().each { maskKey ->
                sorted.clear(); sorted.addAll(maskKey2PropNames.get(maskKey)); Collections.sort(sorted)
                println "maskKey='$maskKey' affected=${sorted}"
            }
        }

        /* 3rd loop: find mapping of mask key to the number of deleted property occurrences triggered by actually masking that key. */
        int entryPerArray = 2
        Map<String,Map<String,Integer>> propName2maskKey2count = [:]
        for (String propName : propNames) {
//        for (String propName : ['route']) {
            Map<String, Integer> maskKey2Count = [:]
            findMaskKeyCountMappingForType.call(type, propName, entryPerArray, 0, maskKey2Count)
            propName2maskKey2count.put(propName, maskKey2Count)
        }

        // Debug output of 3rd loop:
        if (printDebug) {
            for (String propName : propNames) {
                println "prop '$propName':"
                Map<String, Integer> maskKey2Count = propName2maskKey2count.get(propName)
                sorted.clear(); sorted.addAll(maskKey2Count.keySet()); Collections.sort(sorted)
                // display all counts
                sorted.each { key -> println "prop=$propName maskKey='$key' count=${maskKey2Count.get(key)}" }
            }

            for (String propName : propNames) {
                println "prop '$propName':"
                Map<String, Integer> maskKey2Count = propName2maskKey2count.get(propName)
                sorted.clear(); sorted.addAll(maskKey2Count.keySet()); Collections.sort(sorted)
                // display count > 0!
                sorted.findAll { key -> maskKey2Count.get(key) > 0 }.each { key -> println "prop=$propName maskKey='$key' count=${maskKey2Count.get(key)}" }

                // Sanity checks for counts:
                Deque<String> keysNotNull = new LinkedList<>(sorted.findAll { key -> maskKey2Count.get(key) > 0 }.collect())
                // If the next key starts with the previous, then the next count must be <= the previous
                // If the next key does not start with the previous, then the count must be smaller than the first!
                int countFirst = maskKey2Count.get(keysNotNull.peekFirst())
                while (keysNotNull.size() > 1) {
                    String prev = keysNotNull.removeFirst()
                    String curr = keysNotNull.peekFirst()
                    if (curr.startsWith(prev)) {
                        if (maskKey2Count.get(curr) > maskKey2Count.get(prev)) {
                            println "Count error!\nprev: key=$prev count=${maskKey2Count.get(prev)}\ncur: key=$curr count=${maskKey2Count.get(curr)}"
                        }
                    } else {
                        if (maskKey2Count.get(curr) > countFirst) {
                            println "Count error!\nkeys: prev=$prev curr=$curr\n count: first=${countFirst} curr=${maskKey2Count.get(curr)}"
                        }
                    }
                }
            }
        }

        // start printing JUnit content

        String propNameSequence = propNames.collect{ name -> /"$name"/ }.join(', ') // e.g. "area", "center", "city"
        String maskKeySequence = maskKeys.collect { key -> /"$key"/ }.join(', ') // e.g. "domainId", "guid", "location"
        String fileHead = """
package de.lisaplus.lisa.junction.mask;

/**
 * This file is generated by jsonCodeGen. Changes will be overwritten with next code generation run.
 * Template: test_mask.txt
 */

import static java.nio.charset.Charset.forName;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.lisaplus.lisa.junction.mask.api.PojoMask;
import de.lisaplus.lisa.junction.mask.internal.PojoMaskImpl;
import de.lisaplus.lisa.junction.model.JunctionJoined;
import de.lisaplus.util.serialization.MapperFactory;
import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;

/**
 * This class contains the Unit test for masking of class $targetType. 
 */
public class TestMask$targetType {
    /** Count of entries in Lists / array properties */
    static final int COLL_SIZE = 2;
    /** Pattern for looking up the keys. */
    static final String KEY_PATTERN = "\\"[a-zA-Z]+\\":";
    
    /** For generating random POJOs/Beans. */
    static EnhancedRandom random;
    /** Converts the POJOs/Beans into the JSON representation. POJO attributes with value <i>null</i> are being dropped! */
    static ObjectMapper mapper;
    /** Provides the Matchers needed to count the occurrence of the keys in the JSON */
    static ThreadLocal<Matcher> matcherFactory;
    /** The names of the properties defined in the current type */
    private static List<String> allProps;
    /** The mask keys, which are available for the current type */
    private static List<String> allMaskKeys;
    /** A mapping  of mask key to the property names affected when masking the property associated with that mask key */
    private static Map<String,Set<String>> maskKey2propNames;
    /** A mapping property name to a mapping of mask key to the expected count of removed properties when that masking is being performed */
    private static Map<String,Map<String,Integer>> propName2maskKey2deleteCount;

    @BeforeClass
    public static void before() {
        System.out.println ("*********************** de.lisaplus.mask.MaskTestProto - Start ***********************");
        final LocalDate minDate = LocalDate.parse("2000-01-01");
        final LocalDate endDate = LocalDate.parse("2017-12-31");
        random = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
                .charset(forName("UTF-8"))
                .dateRange(minDate, endDate)
                .collectionSizeRange(COLL_SIZE, COLL_SIZE)
                .overrideDefaultInitialization(true)
                .build();
        mapper = MapperFactory.createObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        matcherFactory = ThreadLocal.withInitial(() -> Pattern.compile(KEY_PATTERN).matcher("dummy"));
        allProps = Arrays.asList($propNameSequence);
        allMaskKeys = Arrays.asList($maskKeySequence);
        maskKey2propNames = new HashMap<>();
"""
        println fileHead
        maskKeys.each { key ->
            String affectedProps = maskKey2PropNames.get(key).collect{ prop -> /"$prop"/ }.join(', ') // e.g. "city", "classification", "ountry"
            String line = /        maskKey2propNames.put("${key}", new HashSet<>(Arrays.asList($affectedProps)));/
            println line
        }
    }

    /**
     * Adds new elements to the stacks
     * @param property The property, which is to be visited
     */
    def putStacks = { Property property ->
        propStack.add(property)
//        propIsArrayStack.add(property.type.isArray)
//        // If either already collection of if this property is an collection.
//        propIsCollectionStack.add(propIsCollectionStack.last() || propIsArrayStack.last())
    }

    /**
     * Pops the latest elements from the stacks
     */
    def popStacks = {
        propStack.pop()
//        propIsArrayStack.pop()
//        propIsCollectionStack.pop()
    }

    /**
     * Traverse the properties of a type and collect the mask keys and property names.
     * This method calls itself recursively if the type contains properties of complex or reference types.
     */
    def findNamesKeysForType = { Type type, Set<String> propNames, List<String> maskKeys ->
        type.properties.each { prop -> propNames.add(prop.name) }
        type.properties.each { prop ->
            propStack.add(prop)
            maskKeys.add(propStack.collect { prop2 -> prop2.name }.join('.'))
            propStack.pop()
        }

        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
            // recursive call!
            putStacks.call(prop)
            findNamesKeysForType.call(prop.type.type, propNames, maskKeys)
            popStacks.call()
        }
    }

    /**
     * Traverse the properties of a type and collect mapping of mask key to the names of those those properties, which will
     * be affected by removing the property associated with the mask key.
     * This method calls itself recursively if the type contains properties of complex or reference types.
     * @param type the type to process
     * @param maskKey2ParamNames The mapping to extend while traversing.
     */
    Closure<Set<String>>  finaKeyAffectedParamsForType = { Type type, Map<String,Set<String>> maskKey2PropNames ->
        Set<String> affectedProps = []
        // process nodes with children
        data.filterProps.call(type, [refComplex:true]).each { prop ->
            // Type type = prop.isRefType() ? prop.implicitRef.type : prop.type.type
            putStacks.call(prop)
            affectedProps.add(prop.name)
            affectedProps.addAll(finaKeyAffectedParamsForType.call(prop.type.type, maskKey2PropNames))
            popStacks.call()
        }
        // process nodes without children
        data.filterProps.call(type, [refComplex:false]).each { prop ->
            putStacks.call(prop)
            addAffected.call(Collections.singleton(prop.name), maskKey2PropNames)
            popStacks.call()
            affectedProps.add(prop.name)
        }
        if (!propStack.isEmpty()) {
            affectedProps.add(propStack.last().name)
        }
        addAffected.call(affectedProps, maskKey2PropNames)
        return affectedProps
    }

    /**
     * Adds the names of the affected properties to the mapping maskKey2ParamNames.
     * The mask key is created by examining propStack.
     */
    def addAffected = { Set<String> affected, Map<String,Set<String>> maskKey2PropNames ->
        String maskKey = propStack.isEmpty() ? '.' : propStack.collect { prop2 -> prop2.name }.join('.')
        if (maskKey2PropNames.put(maskKey, affected) != null) {
            String msg = "maskKey already present: $maskKey!"
            System.err.println msg
            throw new RuntimeException(msg)
        }
    }

    /**
     * Traverse the properties of a type and, for a given property name, collect mapping of mask key to the expected
     * count of removed properties when that masking is being performed.
     * This method calls itself recursively if the type contains properties of complex or reference types.
     * @param type the type to process
     * @param propName The name of the property, where the expected count is to evaluate
     * @param entryPerArray The count of entries per array property.
     * @param maskKey2Count The mapping to extend while traversing.
     */
    Closure<Integer> findMaskKeyCountMappingForType = { Type type,
                                               String propName,
                                               int entryPerArray,
                                               int arrayCount,
                                               Map<String, Integer> maskKey2Count ->

        int countSum = 0
        if (!propStack.isEmpty() && propStack.last().name == propName) {
            // We are currently processing a complex property with the wanted name
            def count = entryPerArray.power(Math.max(0, arrayCount-1))
            def maskKey = propStack.collect {prop2 -> prop2.name}.join('.')
            println "Found $count occurrences in complex property of wanted name $propName at ${maskKey}"
            countSum += count
        }

        // process nodes with children
        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
            putStacks.call(prop)
            int childArrayCount = arrayCount + (prop.type.isArray ? 1 : 0)
            countSum += findMaskKeyCountMappingForType.call(prop.type.type, propName, entryPerArray, childArrayCount, maskKey2Count)
            popStacks.call(prop)
        }
        // process nodes without children
        data.filterProps.call(type, [refComplex:false]).each { Property prop ->
            putStacks.call(prop)
            if (prop.name == propName) {
                def count = entryPerArray.power(arrayCount)
                def maskKey = propStack.collect {prop2 -> prop2.name}.join('.')
                println "Found $count occurrences in simple property of wanted name $propName at ${maskKey}"
                addCount.call(count, maskKey2Count)
                countSum += count
            } else {
                addCount.call(0, maskKey2Count)
            }
            popStacks.call(prop)
        }
        addCount.call(countSum, maskKey2Count)
        return countSum
    }


    Closure<Void> addCount = { int count, Map<String, Integer> maskKey2Count ->
        String maskKey = propStack.isEmpty() ? '.' : propStack.collect { prop2 -> prop2.name }.join('.')
        if (maskKey2Count.put(maskKey, count) != null) {
            String msg = "maskKey already present: $maskKey!"
            System.err.println msg
            throw new RuntimeException(msg)
        }
    }
}
