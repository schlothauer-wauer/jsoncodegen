package de.lisaplus.atlas

import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.codegen.GeneratorBase
import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
import de.lisaplus.atlas.model.Property

class MaskExperiments {

    String type = 'JunctionContact'

    String[] filters = [
    'address',
    'type',
    'domainId',
    'guid',
    'objectBaseId',
    'objectBase',
    'address.city',
    'address.country',
    'address.department',
    'address.extra',
    'address.name',
    'address.postalCode',
    'address.street',
    'address.type',
    'address.web',
    'address.contact.email',
    'address.contact.fax',
    'address.contact.phone',
    'address.persons.active',
    'address.persons.addressId',
    'address.persons.firstName',
    'address.persons.name',
    'address.persons.title',
    'address.persons.entryId',
    'address.persons.contact.email',
    'address.persons.contact.fax',
    'address.persons.contact.phone'
    ]

    String[] filtersJoined = [
            'address',
            'type',
            'domainId',
            'guid',
            'objectBaseId',
            'objectBase',
            'address.city',
            'address.country',
            'address.department',
            'address.extra',
            'address.name',
            'address.postalCode',
            'address.street',
            'address.type',
            'address.web',
            'address.contact.email',
            'address.contact.fax',
            'address.contact.phone',
            'address.persons.active',
            'address.persons.addressId',
            'address.persons.firstName',
            'address.persons.name',
            'address.persons.title',
            'address.persons.entryId',
            'address.persons.contact.email',
            'address.persons.contact.fax',
            'address.persons.contact.phone',
            'objectBase.domainId',
            'objectBase.guid',
            'objectBase.name',
            'objectBase.number',
            'objectBase.objectGroupId',
            'objectBase.tags',
            'objectBase.gis.area.projection',
            'objectBase.gis.area.points.lon',
            'objectBase.gis.area.points.lat',
            'objectBase.gis.area.points.projection',
            'objectBase.gis.center.lon',
            'objectBase.gis.center.lat',
            'objectBase.gis.center.projection',
            'objectBase.gis.route.projection',
            'objectBase.gis.route.points.lon',
            'objectBase.gis.route.points.lat',
            'objectBase.gis.route.points.projection'
    ]

    // TODO read model
    // TODO extract type
    // Version 1
    // TODO generate Lists filters and filtersJoined from type
    // TODO generate all methods chechXXXExists(JunctionContact target)
    // TODO generate all methods chechXXXExists(JunctionContactJoined target)
    // TODO generate all method List<XXX> getXXX(JunctionContactJoined target) where at least one of the inner entris of the propChain is an array
    // TODO generate three lists propChain
    // Version 2
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
        // Second loop: methods checkXXXExists() and getXXX()
        propChain = []; propIsArrayChain = []; propAnyParentIsArrayChain = []
        printCheckExistsForType(type)

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
     * Prints the case statements for a certain type, calls itself recursively for reference and complex types!
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
            println "\n    private boolean check${checkMethodPart}Exists(${targetType} target) {\n        return ${conditions};\n    }"
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
}
