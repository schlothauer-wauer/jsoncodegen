package de.lisaplus.atlas

import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.codegen.GeneratorBase
import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
import de.lisaplus.atlas.model.Property

class MaskExperiments {

    // TODO read model
    // TODO extract type
    // TODO traversal the model and
    // TODO        generate all methods chechXXXExists(JunctionContact target) OR
    // TODO        generate all methods chechXXXExists(JunctionContactJoined target)
    // TODO        see usage of extraParam.joinedRun in codeGenTemplates\java\mongodb_beans_tests.txt
    // TODO        maintain three stacks propChain, propIsArrayChain and propAnyParentIsArrayChain
    // TODO        generate methods List<XXX> getXXX(JunctionContactJoined target) according to propAnyParentIsArrayChain
    // TODO        Add cases to switch of method mask(JunctionContactJoined target, PojoMask mask)
    // TODO        (Collect list of lines and write out after loop is done or write while looping?)
    // TODO        Run multiple loops:
    // TODO        Loop1: writer method mask(JunctionContactJoined target, PojoMask mask)
    // TODO        Loop2: write methods chechXXXExists(JunctionContact target) and getXXX(JunctionContactJoined target) below method mask

    String typeName
    Model model

    static main(args) {

        def base = 'C:\\Entwicklung\\lisa-junction-server\\models\\models-lisa-server\\model\\'
        def modelPath = args.length == 0 ?
                base + 'junction.json'
                // base + 'shared\\geo_point.json'
                : args[0]
        def type = args.length > 0 ?
                args[1]
                : 'JunctionContact'
        def joined = args.length > 1 ?
                Boolean.valueOf(args[2])
                : true

        def maskExp = new MaskExperiments(type, modelPath)
        maskExp.execute(joined)

    }

    MaskExperiments(typeName, modelPath) {
        this.typeName = typeName
        this.model = readModel(modelPath)
    }

    private Model readModel(String modelPath) {
        def modelFile = new File(modelPath)
        IModelBuilder builder = new JsonSchemaBuilder()
        return builder.buildModel(modelFile)
    }

    List propChain
    List propIsArrayChain
    List propAnyParentIsArrayChain
    Map data
    boolean joined
    String targetType

    void execute(boolean joined) {
        this.joined = joined;
        GeneratorBase generator = new DummyGenerator()
        data = generator.createTemplateDataMap(model)
        Type type = data.model.types.find {type -> type.name == typeName}
        targetType = type.name + (joined ? 'Joined' : '')

        // First loop: method mask
        propChain = []; propIsArrayChain = []; propAnyParentIsArrayChain = []

        println '''    public static void mask(JunctionContactJoined target, PojoMask mask) {
        for (final String key : mask.hiddenKeys()) {
            switch(key) {'''

        printCaseForType(type)

        println '''            }
        }
    }'''
        // Second loop: method checkXXXExists()
        propChain = []; propIsArrayChain = []; propAnyParentIsArrayChain = []
        printCheckExistsForType(type)

        // Third loop: method getXXX()
        printGetForType(type)

    }

    /** Simple setXXX(null) without any array types in-between -> no for(xxx item : getXXX()) item.setYYY(null) */
    void printCaseSimple(Property prop) {
        def lines
        if (propChain.isEmpty()) {
            lines = /            case "${prop.name}":
                target.set${data.upperCamelCase.call(prop.name)}(null);
                break;/
        } else {
            def checkMethodPart = propChain.collect{ data.upperCamelCase.call(it) }.join('')       // e.g. AddressPersonsContact
            def getChain = propChain.collect{ "get${data.upperCamelCase.call(it)}" } .join('().')  // e.g. getObjectBase().getGis().getArea
            propChain.add(prop.name)
            def key = propChain.join('.')
            propChain.pop()
            lines = /            case "${key}":
                if (chech${checkMethodPart}Exists(target)) {
                    target.${getChain}().set${data.upperCamelCase.call(prop.name)}(null);
                }
                break;/
        }
        println lines
    }

    void printCaseComplex(Property property) {
        Type type = property.type.type
        printCaseForType(type)
    }

    void printCaseJoined(Property property) {
        Type type = property.implicitRef.type
        printCaseForType(type)
    }

    /**
     * Prints the case statements for a certain type, calls itself recursively for reference and complex types!
     * @param type The type to process
     */
    void printCaseForType(Type type) {
        data.filterProps.call(type, [refComplex:false]).each { Property prop ->
            printCaseSimple(prop)
        }

//        type.properties.findAll { prop -> return prop.isRefTypeOrComplexType() }.each { prop ->
        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
            // recursive call!
            propChain.add(prop.name)
//            println "debug ${prop.name}: ${prop.type}"
//            if (!prop.type) {
//                println 'why is it missing, need isArray!'
//            }
            propIsArrayChain.add(prop.type.isArray)
            propAnyParentIsArrayChain.add('TODO')
            printCaseComplex(prop)
            propChain.pop()
            propIsArrayChain.pop()
            propAnyParentIsArrayChain.pop()
        }

        if (!joined) {
            return
        }
        data.filterProps.call(type, [prepLookup:true, implRefIsRef:true]).each { Property prop ->
            // recursive call!
            propChain.add(prop.name)
//            println "debug ${prop.name}: ${prop.type}"
//            if (!prop.type) {
//                println 'why is it missing, need isArray!'
//            }
            propIsArrayChain.add(prop.type.isArray)
            propAnyParentIsArrayChain.add('TODO')
            printCaseJoined(prop)
            propChain.pop()
            propIsArrayChain.pop()
            propAnyParentIsArrayChain.pop()
        }
    }

    /**
     * Prints the methods checkXXX(target) for a certain type, calls itself recursively for reference and complex types!
     * @param type The type to process
     */
    void printCheckExistsForType(Type type) {
        if (!propChain.isEmpty()) {
            /*
                private static boolean checkObjectBaseGisArea(JunctionNumberJoined target) {
                    return target.getObjectBase() != null
                            && target.getObjectBase().getGis() != null
                            && target.getObjectBase().getGis().getArea() != null;
                }
             */
            def checkMethodPart = propChain.collect{ data.upperCamelCase.call(it) }.join('')       // e.g. AddressPersonsContact
            // create longest getter call chain and then process it from one to all elements.
            List lines = []
            List getCalls = propChain.collect { "get${data.upperCamelCase.call(it)}()"}
            for (int i = 0; i < getCalls.size(); i++) {
                def cond = getCalls.subList(0, i+1).join('.')
                lines.add("target.${cond} != null")
            }
            def conditions = lines.join('\n                && ')
            println "\n    private static boolean check${checkMethodPart}Exists(${targetType} target) {\n        return ${conditions};\n    }"
        }

        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
//        type.properties.findAll { prop -> return prop.isRefTypeOrComplexType() }.each { prop ->
            // recursive call!
            propChain.add(prop.name)
            propIsArrayChain.add(prop.type.isArray)
            propAnyParentIsArrayChain.add('TODO2')
            printCheckExistsForType(prop.type.type)
            propChain.pop()
            propIsArrayChain.pop()
            propAnyParentIsArrayChain.pop()
        }

        if (joined) {
            data.filterProps.call(type, [prepLookup:true, implRefIsRef:true]).each { Property prop ->
                // recursive call!
                propChain.add(prop.name)
                propIsArrayChain.add(prop.type.isArray)
                propAnyParentIsArrayChain.add('TODO2')
                printCheckExistsForType(prop.implicitRef.type)
                propChain.pop()
                propIsArrayChain.pop()
                propAnyParentIsArrayChain.pop()
            }
        }
    }

    /**
     * Prints the methods checkXXX(target) for a certain type, calls itself recursively for reference and complex types!
     * @param type The type to process
     */
    void printGetForType(Type type) {
        if (!propChain.isEmpty()) {
            // Example for key address.persons.contact where persons is the only array type
            // In case of multiple array types use .flatMap() for 2. to last array type!
            /*
                private static List<ContactData> getContactData(JunctionContactJoined target) {
                    if (checkAddressPersonsContactExists(target)) {
                        return target.getAddress().getPersons().stream()
                                                               .map(p -> p.getContact())
                                                               .collect(Collectors.toList());
                    }
                    return Collections.emptyList();
                }
             */
            def checkMethodPart = propChain.subList(0, propChain.size()).collect { data.upperCamelCase.call(it) }.join('') // e.g. AddressPersonsContact

            // iterate through propChain and propIsArrayChain
            // Before first array type is encountered, add getter calls
            // When first array type is encountered, add .stream() and switch mode to .map(...)
            // Whenever another array type is encountered, use .flatMap(...) instead of .map(...)
            List parts = []
            boolean useGetter = true;
            for (int i = 0; i < propChain.size(); i++) {
                def currUpper = data.upperCamelCase.call(propChain[i])
                if (useGetter) {
                    // getXXX()
                    parts.add("get${currUpper}()")
                } else {
                    def parentProp = propChain[i-1].take(1)
                    if (propIsArrayChain[i]) {
                        // flatMap(), e.g. flatMap(contact -> contact.getEmail().stream())
                        parts.add("flatMap(${parentProp} -> ${parentProp}.get${currUpper}().stream())")
                    } else {
                        // map(), e.g. map(person -> person.getContact())
                        parts.add("map(${parentProp} -> ${parentProp}.get${currUpper}()")
                    }
                }
                if (useGetter && propIsArrayChain[i]) {
                    parts.add("stream()")
                    useGetter = false
                }
            }
            parts.add('collect(Collectors.toList())')

            // If the very last entry is the first array type, then these is no need to appending
            // .stream() and .collect(Collectors.toList()) -> just discard these two entries!
            if (parts[parts.size()-2] == "stream()") {
                parts = parts.subList(0, parts.size() - 2)
            }
            def retType = data.upperCamelCase.call(type.name)
            def stream = parts[0] + parts.subList(1,parts.size()).collect {"\n                    .$it"}.join('')
                    println """
    private static List<${retType}> get${retType}(${targetType} target) {
        if (check${checkMethodPart}Exists(target) {
            return target.${stream};
        }
        return Collections.emptyList();
    }"""
        }

        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
//        type.properties.findAll { prop -> return prop.isRefTypeOrComplexType() }.each { prop ->
            // recursive call!
            propChain.add(prop.name)
            propIsArrayChain.add(prop.type.isArray)
            propAnyParentIsArrayChain.add('TODO3')
            printGetForType(prop.type.type)
            propChain.pop()
            propIsArrayChain.pop()
            propAnyParentIsArrayChain.pop()
        }

        if (joined) {
            data.filterProps.call(type, [prepLookup:true, implRefIsRef:true]).each { Property prop ->
                // recursive call!
                propChain.add(prop.name)
                propIsArrayChain.add(prop.type.isArray)
                propAnyParentIsArrayChain.add('TODO3')
                printGetForType(prop.implicitRef.type)
                propChain.pop()
                propIsArrayChain.pop()
                propAnyParentIsArrayChain.pop()
            }
        }
    }
}
