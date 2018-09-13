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
     * Execute code generation for the type defined by MaskExperiments#typeName
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

        // Debug output of 1st loop:
        List<String> sorted = new ArrayList<>(propNames); Collections.sort(sorted)
        println "propNames=${sorted}"
        sorted.clear(); sorted.addAll(maskKeys); Collections.sort(sorted)
        println "maskKey=${sorted}"

        /* 2nd loop: find property names affected by masking a mask key */
        propStack = []
        // A mapping  of mask key to the property names affected when masking the property associated with that mask key
        Map<String,Set<String>> maskKey2ParamNames = [:]
        Set<String> affectedRoot = finaKeyAffectedParamsForType.call(type, maskKey2ParamNames)
        // maskKey2ParamNames.put('.', affectedRoot)

        // Debug output of 2nd loop:
        maskKey2ParamNames.keySet().stream().sorted().each { maskKey ->
            sorted.clear(); sorted.addAll(maskKey2ParamNames.get(maskKey)); Collections.sort(sorted)
            println "maskKey='$maskKey' affected=${sorted}"
        }

        /* 3rd loop: find mapping of mask key to the number of deleted property occurrences triggered by actually masking that key. */
        int entryPerArray = 2
        Map<String,Map<String,Integer>> propName2maskKey2count = [:]
        for (String propName : propNames) {
            Map<String, Integer> maskKey2Count = [:]
            findMaskKeyCountMappingForType.call(type, propName, entryPerArray, 0, maskKey2Count)
            propName2maskKey2count.put(propName, maskKey2Count)
        }

        // Debug output of 3rd loop:
        for (String propName : propNames) {
            println "prop '$propName':"
            Map<String, Integer> maskKey2Count = propName2maskKey2count.get(propName)
            sorted.clear(); sorted.addAll(maskKey2Count.keySet()); Collections.sort(sorted)
            sorted.each { key -> println "prop=$propName maskKey=$key count=${maskKey2Count.get(key)}" }
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
            def count = entryPerArray.power(arrayCount)
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
