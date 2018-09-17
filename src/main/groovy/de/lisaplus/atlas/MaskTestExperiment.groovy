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
                : 'JunctionContact' // 'Junction' // 'JunctionJoined' // 'JunctionNumber'  // 'Contact_type' // 'JunctionLocation' // 'JunctionContact'

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
    /** The name of the Java class, which is being masked. */
    String targetType
    /** This stack holds the property (names) visited while traversing the object hierarchy.*/
    List<Property> propStack
    /**
     * Defines overwrites for maskKeys (mapping of property name to associated mask key).
     * This may be necessary for e.g. Joined types, where on property holds the Id of the joined object, and another
     * property holds the joined object itself (property objectBaseId vs. property objectBase).
     */
    Map<String, String> maskKeyOverwrites

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

    def applyMaskKeyOverwritesLoop1 = { List<String> maskKeys, Map<String, String> overwrites ->
        if (joined) {
            // Kick out the original maskKey and add the corrected one if that is missing
            overwrites.each {entry ->
                assert maskKeys.contains(entry.key)
                assert maskKeys.contains(entry.value)
                maskKeys.remove(entry.key)
                if (!maskKeys.contains(entry.value)) {
                    maskKeys.add(entry.value)
                    println "ATTENTION: added corrected maskKey ${entry.value} which ususally is already available!"
                }
            }
        } else {
            // replace e.g. objectBaseId with objectBase
            overwrites.each { entry ->
                int idx = maskKeys.indexOf(entry.key)
                if (idx > -1) {
                    maskKeys.set(idx, entry.getValue())
                } else {
                    throw new RuntimeException("Failed to apply overwrite: maskKeys=$maskKeys  overwrite= $entry")
                }
            }
        }
    }

    def applyMaskKeyOverwritesLoop2 = { Map<String,Set<String>> maskKey2affectedProps, Map<String, String> overwrites ->
        if (joined) {
            overwrites.each {entry ->
                // remove entry with out-dated mask key
                maskKey2affectedProps.remove(entry.key)
                // For the entry with the correct mask key, add the the property name with suffix Id, too
                Set<String> affected = maskKey2affectedProps.get(entry.value)
                if (affected != null && !affected.contains(entry.key)) {
                    affected.add(entry.key)
                }
            }
        } else {
            // find the entry with wrong mask key and update the key
            overwrites.each {entry ->
                Set<String> affected = maskKey2affectedProps.remove(entry.key)
                if (affected != null) {
                    maskKey2affectedProps.put(entry.value, affected)
                } else {
                    throw new RuntimeException(entry)
                }
            }
        }
    }

    def applyMaskKeyOverwritesLoop3a = { Map<String,Map<String,Integer>> maskKey2propName2deleteCount, Map<String, String> overwrites ->
        if (joined) {
            overwrites.each { entry ->
                // When masking using key 'objectBase', then the same amount of properties named 'objectBase'
                // and 'objectBaseId' are deleted, but the later one is missing!
                Map<String, Integer> prop2delCount = maskKey2propName2deleteCount.get(entry.value)
                prop2delCount.put(entry.key, prop2delCount.get(entry.value))
            }
        } else {
            // Just update the maskKey
            overwrites.each {entry ->
                Map<String, Integer> prop2delCount = maskKey2propName2deleteCount.get(entry.key)
                if (prop2delCount != null) {
                    maskKey2propName2deleteCount.put(entry.value, prop2delCount)
                } else {
                    throw new RuntimeException("No counts for maskKey ${entry.key}?!?")
                }
            }
        }
    }

    private void executeForType(Type type, boolean joined) {
        this.joined = joined
        targetType = data.upperCamelCase.call(type.name)
        maskKeyOverwrites = [:]
        boolean printDebug = true
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

        // apply maskKey overwrites
//        applyMaskKeyOverwritesLoop1.call(maskKeys, maskKeyOverwrites)

        List<String> sorted = []
        if (printDebug) {
            // Debug output of 1st loop:
            sorted.addAll(propNames); Collections.sort(sorted)
            println "propNames=${sorted}"
            sorted.clear(); sorted.addAll(maskKeys); Collections.sort(sorted)
            println "maskKey=${sorted}"
        }

        /* 2nd loop: find property names affected by masking a mask key */
        propStack = []
        // A mapping of mask key to the property names affected when masking the property associated with that mask key
        Map<String,Set<String>> maskKey2PropNames = [:]
        findKeyAffectedParamsForType.call(type, maskKey2PropNames)

        // apply maskKey overwrites
//        applyMaskKeyOverwritesLoop2.call(maskKey2PropNames, maskKeyOverwrites)

        // Debug output of 2nd loop:
        if (printDebug) {
            maskKey2PropNames.keySet().stream().sorted().each { maskKey ->
                sorted.clear(); sorted.addAll(maskKey2PropNames.get(maskKey)); Collections.sort(sorted)
                println "maskKey='$maskKey' affected=${sorted}"
            }
        }

        /* 3rd loop: find mapping of mask key to the number of deleted property occurrences triggered by actually masking that key. */
        int entryPerArray = 2
        Map<String,Map<String,Integer>> maskKey2propName2deleteCount = ['.': [:]]
        maskKeys.each { key -> maskKey2propName2deleteCount.put(key, [:]) }
        for (String propName : propNames) {
            findMaskKey2propName2CountForType.call(type, propName, entryPerArray, 0, maskKey2propName2deleteCount)
        }

        // apply maskKey overwrites
        applyMaskKeyOverwritesLoop1.call(maskKeys, maskKeyOverwrites)
        applyMaskKeyOverwritesLoop2.call(maskKey2PropNames, maskKeyOverwrites)
        applyMaskKeyOverwritesLoop3a.call(maskKey2propName2deleteCount, maskKeyOverwrites)

        // Debug output of 3rd loop:
        if (printDebug) {
            for (String maskKey : maskKeys) {
                println "maskKey '$maskKey':"
                Map<String, Integer> prop2Count = maskKey2propName2deleteCount.get(maskKey)
                sorted.clear(); sorted.addAll(prop2Count.keySet()); Collections.sort(sorted)
                // display all counts
                sorted.each { prop -> println "prop=$prop maskKey='$maskKey' count=${prop2Count.get(prop)}" }
            }

            for (String maskKey : maskKeys) {
                println "maskKey '$maskKey':"
                Map<String, Integer> prop2Count = maskKey2propName2deleteCount.get(maskKey)
                sorted.clear(); sorted.addAll(prop2Count.keySet()); Collections.sort(sorted)
                // display count > 0!
                sorted.findAll { prop -> prop2Count.get(prop) > 0 }.each { prop -> println "prop=$prop maskKey='$maskKey' count=${prop2Count.get(prop)}" }
            }
        }

        // 4th loop: Find complex properties / notes with child for checking that no NPE is thrown while masking that
        // node's children, even when the parent node is already masked / null!
        List<String> masksOfPropsWithChildren = []
        findMaskOfPropsWithChildren.call( type, masksOfPropsWithChildren)

        // Debug output of 4th loop:
        if (printDebug) {
            masksOfPropsWithChildren.each {
                println "Check that there are no NPE while masking children of the property associated with the mask key '${it}', even when that value is already masked / null!"
            }
        }

        // start printing JUnit

        String propNameSequence = propNames.collect{ name -> /"$name"/ }.join(', ') // e.g. "area", "center", "city"
        String maskKeySequence = maskKeys.collect { key -> /"$key"/ }.join(', ') // e.g. "domainId", "guid", "location"
        String npeMaskKeySequence = masksOfPropsWithChildren.collect { key -> /"$key"/ }.join(', ') // e.g. "address.persons.contact", "address.persons", "address.contact", "address"

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
import de.lisaplus.lisa.junction.model.*;
import de.lisaplus.util.serialization.MapperFactory;
import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;

/**
 * This class contains the Unit test for masking of class $targetType. 
 */
public class TestMask${targetType}2 {
    /** Count of entries in Lists / array properties */
    static final int COLL_SIZE = ${entryPerArray};
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
    /** A mapping mask key to a mapping of property name to the expected count of removed properties when that masking is being performed */
    private static Map<String,Map<String,Integer>> maskKey2propName2deleteCount;
    /** 
     * This list contain the mask keys of all the complex properties. No NullPointerException may occuree while masking
     * these properties or the children of these properties, even if these property itselve are already masked / null!
     */
    private static List<String> npeCheckMaskKeys;

    @BeforeClass
    public static void before() {
        System.out.println ("*********************** TestMask$targetType - Start ***********************");
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
        npeCheckMaskKeys = Arrays.asList($npeMaskKeySequence);

        maskKey2propNames = new HashMap<>();"""
        println fileHead
        maskKeys.each { key ->
            String affectedProps = maskKey2PropNames.get(key).collect{ prop -> /"$prop"/ }.join(', ') // e.g. "city", "classification", "ountry"
            String line = /        maskKey2propNames.put("${key}", new HashSet<>(Arrays.asList($affectedProps)));/
            println line
        }

        println """
        final Map<String, Integer> propName2Count = new HashMap<>();
        maskKey2propName2deleteCount = new HashMap<>();"""

        maskKeys.each { maskKey ->
            println '        propName2Count.clear();  //' + maskKey
            Map<String, Integer> propName2Count = maskKey2propName2deleteCount.get(maskKey)
            // Process only those maskKey with count > 0!
            sorted.clear(); sorted.addAll( propName2Count.keySet().findAll {key -> propName2Count.get(key) > 0} ); Collections.sort(sorted)
            sorted.each { key ->
                println "        propName2Count.put(\"$key\", ${propName2Count.get(key)});"
            }
            println "        maskKey2propName2deleteCount.put(\"$maskKey\", new HashMap<>(propName2Count));"

        }

        println """
    }

    @AfterClass
    public static void after() {
        allProps = null;
        allMaskKeys = null;
        maskKey2propNames = null;
        maskKey2propName2deleteCount = null;
        System.out.println ("*********************** TestMask$targetType - End ***********************");
    }

    @Test
    public void testMaskOne() throws Exception {
        final Map<String,Integer> emptyMap = Collections.emptyMap();
        final Integer zero = Integer.valueOf(0);
        final Map<String, Integer> expDelta = new HashMap<>(allProps.size());
        for (final String maskKey : allMaskKeys) {
            final $targetType t = random.nextObject(${targetType}.class);
            assertNotNull(t);
            final String jsonBefore = mapper.writeValueAsString(t); // JSON Output
            assertNotNull(jsonBefore);
            final Map<String, Integer> occurrencesBefore = countOccurences(jsonBefore);
            final PojoMask mask = new PojoMaskImpl(Collections.singleton(maskKey));
            Mask${targetType}.mask(t, mask);
            final String jsonAfter= mapper.writeValueAsString(t); // JSON Output
            assertNotNull(jsonAfter);
            final Map<String, Integer> occurrencesAfter = countOccurences(jsonAfter);
            expDelta.clear();
            expDelta.putAll(maskKey2propName2deleteCount.getOrDefault(maskKey, emptyMap));
            checkDelta(occurrencesBefore, occurrencesAfter, expDelta, formatJson(jsonBefore, mapper), formatJson(jsonAfter, mapper), maskKey);
        }
    }

    /**
     * This test processes all complex properties of the ${targetType}. It checks that there are no NPE while masking
     * these properties itself or masking children of these properties, when these property are already masked / have
     * value <i>null</i>!
     * @throws Exception
     */
    @Test
    public void testNoNPE() {
        for (final String parentMaskKey : npeCheckMaskKeys) {
            final List<String> childrenMaskKeys = allMaskKeys.stream()
                                                          .filter(key -> key.startsWith(parentMaskKey))
                                                          .collect(Collectors.toList());
            final String message = String.format("No mask keys of children nodes?!?: parent maskKey=%s ", parentMaskKey);
            assertFalse(message, childrenMaskKeys.isEmpty());
            System.out.format("Perform NPE checks: parent maskKey=%s childrenMaskKeys=%s%n", parentMaskKey, childrenMaskKeys);
            final ${targetType} t = random.nextObject(${targetType}.class);
            assertNotNull(t);
            Mask${targetType}.mask(t, new PojoMaskImpl(Collections.singleton(parentMaskKey)));
            for (final String childMaskKey : childrenMaskKeys) {
                try {
                    Mask${targetType}.mask(t, new PojoMaskImpl(Collections.singleton(childMaskKey)));
                } catch (final Exception exception) {
                    final String msg = String.format("Encountered %s: parent maskKey=%s childMaskKey=%s", exception.toString(), parentMaskKey, childMaskKey);
                    fail(msg);
                }
            }
        }
    }

    /**
     * @param json The JSON representation of the POJO / Bean
     * @return A mapping of property name to the occurrence count of that property name throughout the JSON.
     */
    private Map<String, Integer> countOccurences(String json) {
        // Find all matches of pattern "[a-zA-Z]+:" -> keys
        // For every key evaluate how often they occur!
        final Matcher matcher = matcherFactory.get();
        matcher.reset(json);
        final List<String> keys = new ArrayList<>(json.length() / 10);
        while(matcher.find()) {
            // finds "abc":  -> strip quotes and trailing colon!
            final String tmp = matcher.group();
            keys.add(tmp.substring(1, tmp.length()-2));
        }
        if (keys.isEmpty()) {
            // either
            throw new RuntimeException(String.format("No keys found in JSON '%s'", json));
//            // or
//            return Collections.emptyMap();
        }
        return keys.stream().collect(Collectors.groupingBy(key -> key, Collectors.summingInt(l -> 1)));
    }

    /**
     * @param occurrencesBefore A mapping of the property name to the occurence count of that property name in the JSON
     * @param occurrencesAfter
     * @param expDelta Defines the expected loss of property name occurrences. A mapping of property name to the number
     *            of occurrences, which are supposed to have been eliminated by the operation.
     * @param jsonBefore The JSON representation of the POJO / Bean before the operation, used for debugging.
     * @param jsonAfter The JSON representation of the POJO / Bean after the operation, used for debugging.
     * @param maskKey The mask key associated with the operation, used for debugging.
     */
    private void checkDelta(Map<String, Integer> occurrencesBefore,
                            Map<String, Integer> occurrencesAfter,
                            Map<String, Integer> expDelta,
                            String jsonBefore, String jsonAfter,
                            String maskKey) {
        final Integer zero = Integer.valueOf(0);
        // Check that the occurrence of the keys from before match expectation
        for (final String key : occurrencesBefore.keySet()) {
            final Integer countBefore = occurrencesBefore.get(key);
            final Integer countAfter = occurrencesAfter.getOrDefault(key, zero);
            final Integer exp = expDelta.get(key);
            final String msg = String.format(
                    "maskKey='%s' currKey='%s'%njsonBefore:%n%s%njsonAfter:%n%s%n",
                        maskKey, key, jsonBefore, jsonAfter);
            if (exp == null) {
                assertEquals(msg, countBefore, countAfter);
            } else {
                final int expCount = countBefore.intValue() - exp.intValue();
                assertEquals(msg, expCount, countAfter.intValue());
            }
        }
        // Check that masking did not add new keys!
        final Set<String> keysAfter = new HashSet<>(occurrencesAfter.keySet());
        keysAfter.removeAll(occurrencesBefore.keySet());
        final String msg = String.format("newKeys='%s'%njsonBefore:%n%s%njsonAfter:%n%s", keysAfter, jsonBefore, jsonAfter);
        assertTrue(msg, keysAfter.isEmpty());
    }

    /**
     * @param json The JSON string to pretty print
     * @param mapper The mapper doing the actual work
     * @return the pretty printed JSON
     * @throws Exception Should not happen for valid JSON created from a POJO / Bean.
     */
    private static String formatJson(String json, ObjectMapper mapper) throws Exception {
        final Object jsonObject = mapper.readValue(json, Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
    }
}
"""

    }

    /**
     * Adds new elements to the stacks
     * @param property The property, which is to be visited
     */
    def putStacks = { Property property ->
        propStack.add(property)
    }

    /**
     * Pops the latest elements from the stacks
     */
    def popStacks = {
        propStack.pop()
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

        // search property pairs for handling joined objects
        searchPrepLookupProps.call(type)

        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
            // recursive call!
            putStacks.call(prop)
            findNamesKeysForType.call(prop.type.type, propNames, maskKeys)
            popStacks.call()
        }
    }

    /**
     * Use the assumption, that ll properties with tag "prepLookup" have a suffix "Id" and have a associated mask key
     * without that suffix, to populate maskKeyOverwrites!
     */
    def searchPrepLookupProps = { Type type  ->
        // search property pairs for handling joined objects
        String baseMsg = 'The assumption that all properties with tag "prepLookup" have a suffix "Id" and have a associated mask key without that suffix seams to be broken!'
        if (joined) {
            // Assume (and check) that the names off all properties tagged with prepLookup are ending with Id have a
            // corresponding property tagged join / mask key without the suffix Id at the end of the name.
            List<String> joinProps = type.properties.findAll { prop -> prop.hasTag('join')}.collect { prop -> prop.name}
            List<String> prepLookupProps = type.properties.findAll { prop -> prop.hasTag('prepLookup')}.collect { prop -> prop.name}
            List<String> prepLookupWrongSuffix = prepLookupProps.findAll { name -> !name.endsWith('Id')}
            if (!prepLookupWrongSuffix.isEmpty()) {
                throw new RuntimeException(baseMsg + " Properties with missing suffix: " + prepLookupWrongSuffix)
            }
            prepLookupProps.each { name ->
                String maskKey = name.substring(0, name.length()-2)
                if (!joinProps.contains(maskKey)) {
                    throw new RuntimeException(baseMsg + " Property $name with tag prepLookup is misses corresponding property with tag joned. Candidates: $joinProps")
                }
                println "Attention: Overwriting maskKey of property $name: $maskKey!"
                maskKeyOverwrites.put(name, maskKey)
            }
        }  else {
            // Assume that prepLookup properties ending with Id have a corresponding mask key without the suffix Id
            List<String> prepLookupProps = type.properties.findAll { prop -> prop.hasTag('prepLookup')}.collect { prop -> prop.name}
            List<String> prepLookupWrongSuffix = prepLookupProps.findAll { name -> !name.endsWith('Id')}
            if (!prepLookupWrongSuffix.isEmpty()) {
                throw new RuntimeException(baseMsg + " Properties with missing suffix: " + prepLookupWrongSuffix)
            }
            prepLookupProps.each { name ->
                String maskKey = name.substring(0, name.length()-2)
                println "Attention: Overwriting maskKey of property $name: $maskKey!"
                maskKeyOverwrites.put(name, maskKey)
            }
        }
    }

    /**
     * Traverse the properties of a type and collect mapping of mask key to the names of those those properties, which will
     * be affected by removing the property associated with the mask key.
     * This method calls itself recursively if the type contains properties of complex or reference types.
     * @param type the type to process
     * @param maskKey2ParamNames The mapping to extend while traversing.
     */
    Closure<Set<String>> findKeyAffectedParamsForType = { Type type, Map<String,Set<String>> maskKey2PropNames ->
        Set<String> affectedProps = []
        // process nodes with children
        data.filterProps.call(type, [refComplex:true]).each { prop ->
            // Type type = prop.isRefType() ? prop.implicitRef.type : prop.type.type
            putStacks.call(prop)
            affectedProps.add(prop.name)
            affectedProps.addAll(findKeyAffectedParamsForType.call(prop.type.type, maskKey2PropNames))
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
     * @param maskKey2propName2deleteCount The mapping to extend while traversing.
     */
    Closure<Integer> findMaskKey2propName2CountForType = { Type type,
                                                           String propName,
                                                           int entryPerArray,
                                                           int arrayCount,
                                                           Map<String, Map<String, Integer>> maskKey2propName2deleteCount ->
        def isArray = propStack.isEmpty() ? false : propStack.last().type.isArray
        int childArrayCount = arrayCount + (isArray ? 1 : 0)

        int countSum = 0
        if (!propStack.isEmpty() && propStack.last().name == propName) {
            // We are currently processing a complex property with the wanted name
            def count = entryPerArray.power(arrayCount)
            def maskKey = propStack.collect {prop2 -> prop2.name}.join('.')
            println "Found $count occurrences in complex property of wanted name $propName at ${maskKey}"
            countSum += count
        }

        // process nodes with children
        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
            putStacks.call(prop)
            countSum += findMaskKey2propName2CountForType.call(prop.type.type, propName, entryPerArray, childArrayCount, maskKey2propName2deleteCount)
            popStacks.call(prop)
        }
        // process nodes without children
        data.filterProps.call(type, [refComplex:false]).each { Property prop ->
            putStacks.call(prop)
            def count
            if (prop.name == propName) {
                count = entryPerArray.power(childArrayCount)
                def maskKey = propStack.collect {prop2 -> prop2.name}.join('.')
                println "Found $count occurrences in simple property of wanted name $propName at ${maskKey}"
            } else {
                count = 0
            }
            // Do add entries with mask keys associated with this node / property of simple type
            addCount2.call(propName, count, maskKey2propName2deleteCount)
            countSum += count
            popStacks.call(prop)
        }
        addCount2.call(propName, countSum, maskKey2propName2deleteCount)
        return countSum
    }

    /**
     * Adds a delete count to the mapping of mask key to the expected count of removed properties when that masking
     * is being performed.
     * @param propName The name of the property
     * @param count The expected delete count
     * @param maskKey2propName2deleteCount The mapping to extend
     */
    Closure<Void> addCount2 = { String propName,
                                int count,
                                Map<String, Map<String, Integer>> maskKey2propName2deleteCount ->
        String maskKey = propStack.isEmpty() ? '.' : propStack.collect { prop2 -> prop2.name }.join('.')
        if (maskKey2propName2deleteCount.get(maskKey).put(propName, count) != null) {
            String msg = "Count already present: maskKey=$maskKey propName=$propName!"
            System.err.println msg
            throw new RuntimeException(msg)
        }
    }

    /**
     * This method collects all complex properties / nodes with children.
     * This method calls itself recursively if the type contains properties of complex or reference types.
     * @param type the type to process
     * @param masksOfPropsWithChildren The list contains the mask keys associated with the properties / nodes with children.
     * The masks key of the relevant nodes found in the deepest level of the tree come first.
     */
    Closure<Void> findMaskOfPropsWithChildren = { type, List masksOfPropsWithChildren ->
        List<Property> complex = data.filterProps.call(type, [refComplex: true])
        complex.each { prop ->
            putStacks.call(prop)
            def maskKey = propStack.collect { prop2 -> prop2.name }.join('.')
            masksOfPropsWithChildren.add(0,  maskKey)
            popStacks.call()
        }
        complex.each { prop ->
            putStacks.call(prop)
            findMaskOfPropsWithChildren.call(prop.type.type, masksOfPropsWithChildren)
            popStacks.call()
        }
    }

}
