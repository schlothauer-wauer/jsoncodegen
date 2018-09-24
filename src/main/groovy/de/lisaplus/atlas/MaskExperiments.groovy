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
    // TODO        maintain three stacks propStack, propIsArrayStack and propIsCollectionStack
    // TODO        generate methods List<XXX> getXXX(JunctionContactJoined target) according to propIsCollectionStack
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
                : 'ObjectBaseGis' //' Contact' // 'Junction' // 'JunctionJoined' // 'JunctionNumber'  // 'Contact_type' // 'JunctionLocation' // 'JunctionContact'
        def joined = type.endsWith('Joined')

        def maskExp = new MaskExperiments(type, modelPath)
        maskExp.execute(joined)

        // Check for exception while running code generation for all available types
//        maskExp.generateAll()

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

    /** This stack holds the property (names) visited while traversing the object hierarchy.*/
    List<Property> propStack
    /** Indicates that this property is an array. This sack is build while traversing the object hierarchy. */
    List<Boolean> propIsArrayStack
    /**
     *  Indicates that this property or any of its parents was an array and that we therefore have to process an collection.
     *  This sack is build while traversing the object hierarchy.
     */
    List<Boolean> propIsCollectionStack
    /** The bindings of the template / code generation */
    Map data
    /** Indicates whether the current type is a joined type */
    boolean joined
    /** The name of the Java class of the type. */
    String targetType
    /** For collecting the lines of the case statements. Write sequence is easily messed up in templates! */
    List<String> allCaseLines
    /** Index for generating unique identifiers, e.g. in the case statements. */
    int idx

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
     * @param joined
     */
    void execute(boolean joined) {
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

        /* First loop: method mask */
        println "public class Mask${targetType} {"

        println """
    /**
     * @param source The (unmasked) object, which represents the pristine state of the object.
     * @param target The masked and potentially altered object, where the masked information is to be restored.
     * @param mask The mask defining the attributes, which are to be restored.
     */
    public static void supplementMasked2(${targetType} source, ${targetType} target, PojoMask mask) {
        for (final String key : mask.hiddenKeys()) {
            switch(key) {
"""
        // TODO iterate over model and insert case statements!
        evalAllSupplementCases.call(type)


        println """            default:
                throw new IllegalArgumentException(String.format("Unsupported key '%s' for model class ${targetType}!", key));
            }
        }
    }"""

        println """
    /**
     * This method alters the target object: It masks some of its attributes by setting the attributes to <i>null</i>!
     * @param target The object to mask, not <i>null</i>!
     * @param mask Defines the attributes of the object, which are to be masked, not <i>null</i>!
     */    
    public static void mask(${targetType} target, PojoMask mask) {
        for (final String key : mask.hiddenKeys()) {
            switch(key) {"""
        /* NOTE: The data model is being altered: tune propLookup parameter to loose Suffix Id !!!*/
        evalAllMaskCases.call(type)

        println """           default:
                throw new IllegalArgumentException(String.format("Unsupported key '%s' for model class ${targetType}!", key));
            }
        }
    }"""

        /* Second loop: method checkXXXExists() */
        prepareStacks.call()
        printCheckExistsForType.call(type)

        /* Third loop: method getXXX() */
        prepareStacks.call()
        printGetForType.call(type)

        println '}'
    }

    /**
     * Prepares the stacks for running the next loop over the model
     */
    def prepareStacks = {
        propStack = []; propIsArrayStack = []
        // avoid extra case for handling empty stack!
        propIsCollectionStack = [false]
    }

    /**
     * Creates the case of the supplement method for properties of a complex or reference class
     * Case: parent property is array and has entryId:
     * @param prop The property to process
     */
    def evalSupportParentIsArrayHasEntryId = { Property prop ->
        // Example for parent property is array and has entryId:
        /*
        case "location.streets.classification":
            final Map<String, JunctionLocationStreetsItem> sourceMapping0 = getLocationStreets(source)
                    .stream()
                    .collect(Collectors.toMap(JunctionLocationStreetsItem::getEntryId, Function.identity()));
            if (!sourceMapping0.isEmpty()) {
                final Map<String, JunctionLocationStreetsItem> targetMapping = getLocationStreets(target)
                        .stream()
                        .collect(Collectors.toMap(JunctionLocationStreetsItem::getEntryId, Function.identity()));
                for (final Entry<String, JunctionLocationStreetsItem> entry : sourceMapping0.entrySet()) {
                    final JunctionLocationStreetsItem targetItem = targetMapping.get(entry.getKey());
                    if (targetItem != null) {
                        targetItem.setClassification(entry.getValue().getClassification());
                    }
                }
            }
            case "address.persons.contact.phone":
                for (ContactData data : getAddressPersonsContact(target)) {
                    data.setPhone(null);
                }
                break;
         */
        def parentJavaType = data.typeToJavaForceSingle.call(propStack.last().type)
        def methodName = propStack.subList(0, propStack.size()).collect { data.upperCamelCase.call(it.name) }.join('') // e.g. AddressPersonsContact
        propStack.add(prop)
        def key = propStack.collect{ it.name }.join('.')
        propStack.pop()
        def lines = /            case "${key}":
                final Map<Object, $parentJavaType> sourceMapping${idx} =  get${methodName}(source)
                    .stream()
                    .collect(Collectors.toMap($parentJavaType::getEntryId, Function.identity()));
                if (!sourceMapping${idx}.isEmpty()) {
                    final Map<Object, $parentJavaType> targetMapping =  get${methodName}(target)
                        .stream()
                        .collect(Collectors.toMap($parentJavaType::getEntryId, Function.identity()));
                    if (targetMapping.isEmpty()) {
                        for (final Entry<Object, $parentJavaType> entry : sourceMapping${idx}.entrySet()) {
                            final $parentJavaType targetItem = targetMapping.get(entry.getKey());
                            if (targetItem != null) {
                                targetItem.set${upperPropName}(entry.getValue().get${upperPropName}());
                            }
                        }
                    }
                }
                break;/
        idx+=1
        return lines
    }

    /**
     * Creates the case of the supplement method for properties of a complex or reference class
     * Case: parent property is array but has no entryId:
     * @param prop The property to process
     */
    def evalSupportParentIsArrayNoEntryId = { Property prop ->
        // Example for parent property is array but has no entryId:
        /*
            final List<GeoPoint> sourceList0 = getGisRoutePoints(source);
            final List<GeoPoint> targetList0= getGisRoutePoints(target);
            if (sourceList0.size() == targetList0.size()) {
                // FIXME: Assumes unchanged object sequence!
                final Iterator<GeoPoint> iterSource = sourceList0.iterator();
                final Iterator<GeoPoint> iterTarget = targetList0.iterator();
                while(iterSource.hasNext()) {
                    iterTarget.next().setLon(iterSource.next().getLon());
                }
            } else {
                final String msg =
                        "Target object missmatch for supplementing value associated with 'gis.route.points.lon'";
                throw new IllegalArgumentException(msg);
            }
            break;
         */
        def parentJavaType = data.typeToJavaForceSingle.call(propStack.last().type)
        def methodName = propStack.subList(0, propStack.size()).collect { data.upperCamelCase.call(it.name) }.join('') // e.g. GisRoutePoints
        propStack.add(prop)
        def key = propStack.collect{ it.name }.join('.')
        propStack.pop()

        def lines = /            case "${key}":
                List<$parentJavaType> sourceList${idx} = get${methodName}(source);
                List<$parentJavaType> targetList${idx} = get${methodName}(target);
                if (sourceList${idx}.size() == targetList${idx}.size()) {
                    \/\/ FIXME: Assumes unchanged object sequence!
                    final Iterator<$parentJavaType> iterSource = sourceList${idx}.iterator();
                    final Iterator<$parentJavaType> iterTarget = targetList${idx}.iterator();
                    while(iterSource.hasNext()) {
                        iterTarget.next().setLon(iterSource.next().getLon());
                     }
                } else {
                    final String msg =
                        "Target object missmatch for supplementing value associated with '${key}'";
                    throw new IllegalArgumentException(msg);
                }
                break;/
        idx+=1
        return lines
    }

    /**
     * Creates the case of the supplement method for properties of a complex or reference class
     * Case: array property is before parent, array property has entryId:
     * @param prop The property to process
     */
    def evalSupportArrayBeforeParentHasEntryId = { Property prop, int idxEntryId ->
        // Example for array property before parent, array property has entryId:
        /*
            case "address.persons.contact.email":
            final Map<Object, ContactData> sourceMapping7 =  getAddressPersons(source)
                    .stream()
                    .collect(Collectors.toMap(p -> p.getEntryId(), p -> p.getContact()));
            if (!sourceMapping7.isEmpty()) {
                final Map<Object, ContactData> targetMapping =  getAddressPersons(target)
                        .stream()
                        .collect(Collectors.toMap(p -> p.getEntryId(), p -> p.getContact()));
                if (targetMapping.isEmpty()) {
                    for (final Entry<Object, ContactData> entry : sourceMapping7.entrySet()) {
                        final ContactData targetItem = targetMapping.get(entry.getKey());
                        if (targetItem != null) {
                            targetItem.setEmail(entry.getValue().getEmail());
                        }
                    }
                }
            }
            break;
        */
        List<Property> chainUntilEntryId = propStack.subList(0, idxEntryId+1)
        List<Property> chainAfterEntryId = propStack.subList(idxEntryId+1, propStack.size())
        Property propEntryId = chainUntilEntryId.last()
        /*
        def debugUntil = chainUntilEntryId.collect{ prop2 -> prop2.name }.join('.')
        def debugAfter = chainAfterEntryId.collect{ prop2 -> prop2.name}.join('.')
        println "// chainUntill=${debugUntil} chainAfter=${debugAfter} propId=${propEntryId.name}"
        */
        def parentJavaType = data.typeToJavaForceSingle.call(propStack.last().type)
        def objEntryId = propEntryId.name.take(1)
        def methodNameUntil = chainUntilEntryId.collect {data.upperCamelCase.call(it.name) }.join('') // e.g. AddressPersons
        def methodNameAfter = chainAfterEntryId.collect { "get${data.upperCamelCase.call(it.name)}()" }.join('.') // e.g. getContact().getEmail()

        propStack.add(prop)
        def key = propStack.collect{ it.name }.join('.')
        propStack.pop()

        def lines = /            case "${key}":
                final Map<Object,$parentJavaType> sourceMapping${idx} =  get${methodNameUntil}(source)
                    .stream()
                    .collect(Collectors.toMap(${objEntryId} -> ${objEntryId}.getEntryId(), ${objEntryId} -> ${objEntryId}.${methodNameAfter}));
                if (!sourceMapping${idx}.isEmpty()) {
                    final Map<Object, $parentJavaType> targetMapping =  get${methodNameUntil}(target)
                        .stream()
                        .collect(Collectors.toMap(${objEntryId} -> ${objEntryId}.getEntryId(), ${objEntryId} -> ${objEntryId}.${methodNameAfter}));
                    if (targetMapping.isEmpty()) {
                        for (final Entry<Object, $parentJavaType> entry : sourceMapping${idx}.entrySet()) {
                            final $parentJavaType targetItem = targetMapping.get(entry.getKey());
                            if (targetItem != null) {
                                targetItem.set${upperPropName}(entry.getValue().get${upperPropName}());
                            }
                        }
                    }
                }
                break;/
        idx+=1
        return lines
    }

    /**
     * Actually creates the case of the supplement method for properties of a complex or reference class.
     * @param prop The property to process
     */
    def evalSupplementCaseSimple = { Property prop ->
        def lines
        def upperPropName = data.upperCamelCase.call(prop.name) // e.g. DomainId
        if (propStack.isEmpty()) {
            // Example no parent property:
            /*
                 case "domainId":
                    target.setDomainId(source.getDomainId());
                    break;
             */
            if (prop.hasTag('join')) {
                lines = /            case "${prop.name}":
                target.set${upperPropName}(source.get${upperPropName}());
                target.set${upperPropName}Id(source.get${upperPropName}Id());
                break;/
            } else if ( prop.hasTag('prepLookup')){
                lines = /            case "${prop.name}":
                target.set${upperPropName}Id(source.get${upperPropName}Id());
                break;/
            } else {
                lines = /            case "${prop.name}":
                target.set${upperPropName}(source.get${upperPropName}());
                break;/
            }
        } else if (propIsCollectionStack.last()) {
            Property pProp = propStack.last()
            boolean parentHasEntryId = pProp.isRefTypeOrComplexType() && pProp.type.type.properties.collect { prop2 -> prop2.name }.contains('entryId')
            if (pProp.type.isArray) {
                if (parentHasEntryId) {
                    // Parent property is array and has entryId:
                    lines = evalSupportParentIsArrayHasEntryId.call(prop)
                } else {
                    // Parent property is array but misses entryId:
                    lines = evalSupportParentIsArrayNoEntryId.call(prop)
                }
            } else {
                int idxEntryId = propStack.findLastIndexOf { prop2 -> prop2.type.isArray && prop2.isRefTypeOrComplexType() &&
                                                                      prop2.type.type.properties.collect { prop3 -> prop3.name }.contains('entryId')}
                if (idxEntryId > -1) {
                    //  Array property is before parent property, array property has entryId:
                    lines = evalSupportArrayBeforeParentHasEntryId.call(prop, idxEntryId)
                } else {
                    // Array property is before parent property, array property misses entryId:
                    // GeoPoint?!?
                    propStack.add(prop)
                    def key = propStack.collect{ it.name }.join('.')
                    propStack.pop()

                    lines = /            case "${key}":
                    \/\/ GeoPoint? ${pProp.type.name()}
                    break;/
                }
            }
        } else {
            // Example:
            /*
            case "objectBase.gis.area":
                if (checkObjectBaseGisExists(source)) {
                    if (checkObjectBaseGisExists(target)) {
                        target.getObjectBase().getGis().setArea(source.getObjectBase().getGis().getArea());
                    } else {
                        // TODO Parent may have been removed intentionally!
                        final String msg =
                                "Target object is missing mandatory parent object for supplementing value associated with 'objectBase.gis.area'";
                        throw new IllegalArgumentException(msg);

                    }
                } else {
                    // ensure that target does not contain changes in mask property!
                    if (checkObjectBaseExists(target)) {
                        target.getObjectBase().setGis(null);
                    }
                }
                break;
             */
            def checkMethodPart = propStack.collect{ data.upperCamelCase.call(it.name) }.join('')       // e.g. AddressPersonsContact
            def getChain = propStack.collect{ "get${data.upperCamelCase.call(it.name)}" } .join('().')  // e.g. getObjectBase().getGis().getArea
            propStack.add(prop)
            def key = propStack.collect{ it.name }.join('.')
            propStack.pop()
            lines = /            case "${key}":
                if (check${checkMethodPart}Exists(source)) {
                    if (check${checkMethodPart}Exists(target)) {
                        target.${getChain}().set${upperPropName}(source.${getChain}().get${upperPropName}());
                    } else {
                        \/\/ TODO Parent may have been removed intentionally!
                        final String msg =
                                "Target object is missing mandatory parent object for supplementing value associated with '${key}'";
                        throw new IllegalArgumentException(msg);
                    }
                } else {
                    \/\/ ensure that target does not contain changes in mask property!
                    if (check${checkMethodPart}Exists(target)) {
                        target.${getChain}().set${upperPropName}(null);
                    }
                }
                break;/
        }
        println lines
        // allCaseLines.add(lines)
    }


    /**
     * Prints the case statements of the supplement method for a certain type, calls itself recursively for reference
     * and complex types!
     * @param type The type to process
     */
    def evalSupplementCaseForType = { Type type ->
//        type.properties.findAll { prop -> return !prop.isRefTypeOrComplexType() }.each { prop ->
        data.filterProps.call(type, [refComplex:false]).each { Property prop ->
            // println "// evalSupplementCaseForType/RefTypeOrComplexType=false: type=${type.name} prop=${prop.name}"
            evalSupplementCaseSimple.call(prop)
        }

//        type.properties.findAll { prop -> return prop.isRefTypeOrComplexType() }.each { prop ->
        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
            evalSupplementCaseSimple.call(prop)
            // recursive call!
            putStacks.call(prop)
            evalSupplementCaseComplex.call(prop)
            popStacks.call()
        }
    }

    /**
     * Code for continuing recursive case creation of supplement method for a complex property.
     * @param property The complex property to process.
     */
    def evalSupplementCaseComplex = { Property property ->
        Type type = property.type.type
        evalSupplementCaseForType.call(type)
    }

    /**
     * Performs some preparations and then triggers printing of all switch cases of the supplement method.
     * @param type The top level type to process
     */
    def evalAllSupplementCases = { Type currentType ->
        // The data model is being altered: tune propLookup properties to loose suffix Id!!!
        tuneType.call(currentType)
        prepareStacks.call()
        allCaseLines = []
        idx = 0
        evalSupplementCaseForType.call(currentType)
    }


    /**
     * Performs some preparations and then triggers printing of all switch cases of the mask method.
     * @param type The top level type to process
     */
    def evalAllMaskCases = { Type currentType ->
        // The data model is being altered: tune propLookup properties to loose suffix Id!!!
        // tuneType.call(currentType)

        /* First loop: method mask */
        prepareStacks.call()

        allCaseLines = []
        evalMaskCaseForType.call(currentType)
    }

    /**
     * <b>THIS METHOD ALTERS THE MODEL!!!</b>
     * This method tunes the properties of a (complex or reference) type and calls itself recursively if the type
     * itself holds properties of other (complex or reference) types.
     * Checks for properties with the tag <i>prepLookup</i> and
     * <ul>
     * <li>joined==true: removes the property completely</li>
     * <li>joined==false: removes the suffix Id</li>
     * @param type The type to process.
     */
    def tuneType = { Type type ->
        Closure<Void> action
        if (joined) {
            action = {  Property prop ->
                println "// ATTENTION: Removing lookup property ${prop.name}"
                type.properties.remove(prop)
            }
        } else {
            action = { Property prop ->
                def orig = prop.name
                def shorten = prop.name.take(prop.name.length() - 2)
                println "// ATTENTION: Renaming lookup property from $orig to $shorten"
                prop.setName(shorten)
            }
        }
        Collection<Property> lookupProps = type.properties.findAll { Property prop -> prop.hasTag('prepLookup') && prop.name.endsWith('Id') }
        type.properties.findAll { Property prop -> prop.implicitRefIsRefType()   } each { Property prop -> tuneType.call(prop.implicitRef.type) }
        type.properties.findAll { Property prop -> prop.isRefTypeOrComplexType() } each { Property prop -> tuneType.call(prop.type.type) }
        lookupProps.each(action)
    }

    /**
     * Actually creates the case of the mask method for properties of a complex or reference class.
     * @param prop The property to process
     */
    def evalMakCaseSimple = { Property prop ->
        def lines
        if (propStack.isEmpty()) {
            // Example:
            /*
                 case "domainId":
                    target.setDomainId(null);
                    break;
              */
            if (prop.hasTag('join')) {
                lines = /            case "${prop.name}":
                target.set${data.upperCamelCase.call(prop.name)}(null);
                target.set${data.upperCamelCase.call(prop.name)}Id(null);
                break;/
            } else if ( prop.hasTag('prepLookup')){
                lines = /            case "${prop.name}":
                target.set${data.upperCamelCase.call(prop.name)}Id(null);
                break;/
            } else {
                lines = /            case "${prop.name}":
                target.set${data.upperCamelCase.call(prop.name)}(null);
                break;/
            }
        } else if (propIsCollectionStack.last()) {
            // Example:
            /*
                case "address.persons.contact.phone":
                    for (ContactData data : getAddressPersonsContact(target)) {
                        data.setPhone(null);
                    }
                    break;
             */

            Property pProp = propStack.last()
//             println "// $prop.name"
            def parent = pProp.name.take(1)
            def parentJavaType = data.typeToJavaForceSingle.call(pProp.type)
            def methodName = propStack.subList(0, propStack.size()).collect { data.upperCamelCase.call(it.name) }.join('') // e.g. AddressPersonsContact
            propStack.add(prop)
            def key = propStack.collect{ it.name }.join('.')
            propStack.pop()
            lines = /            case "${key}":
                for(${parentJavaType} ${parent} : get${methodName}(target)){
                    ${parent}.set${data.upperCamelCase.call(prop.name)}(null);
                }
                break;/

        } else {
            // Example:
            /*
                case "objectBase.number":
                    if (checkObjectBaseExists(target)) {
                        target.getObjectBase().setNumber(null);
                    }
                    break;
             */
            def checkMethodPart = propStack.collect{ data.upperCamelCase.call(it.name) }.join('')       // e.g. AddressPersonsContact
            def getChain = propStack.collect{ "get${data.upperCamelCase.call(it.name)}" } .join('().')  // e.g. getObjectBase().getGis().getArea
            propStack.add(prop)
            def key = propStack.collect{ it.name }.join('.')
            propStack.pop()
            lines = /            case "${key}":
                if (check${checkMethodPart}Exists(target)) {
                    target.${getChain}().set${data.upperCamelCase.call(prop.name)}(null);
                }
                break;/
        }
        println lines
        // allCaseLines.add(lines)
    }

    /**
     * Code for continuing recursive case creation of mask method for a complex property.
     * @param property The complex property to process.
     */
    def evalMaskCaseComplex = { Property property ->
        Type type = property.type.type
        evalMaskCaseForType.call(type)
    }

    /**
     * Code for continuing recursive case creation of mask method for a joined property.
     * @param property The complex property to process.
     * @deprecated Currently model holds only explicitly joined types!
     */
    def evalMaskCaseJoined = { Property property ->
        Type type = property.implicitRef.type
        evalMaskCaseForType.call(type)
    }

    /**
     * Prints the case statements of the mask method for a certain type, calls itself recursively for reference and
     * complex types!
     * @param type The type to process
     */
    def evalMaskCaseForType = { Type type ->
//        type.properties.findAll { prop -> return !prop.isRefTypeOrComplexType() }.each { prop ->
        data.filterProps.call(type, [refComplex:false]).each { Property prop ->
            println "// evalMaskCaseForType/RefTypeOrComplexType=false: type=${type.name} prop=${prop.name}"
            evalMakCaseSimple.call(prop)
        }

//        type.properties.findAll { prop -> return prop.isRefTypeOrComplexType() }.each { prop ->
        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
            evalMakCaseSimple.call(prop)
            // recursive call!
            putStacks.call(prop)
            evalMaskCaseComplex.call(prop)
            popStacks.call()
        }

        /*
        if (joined) {
            data.filterProps.call(type, [prepLookup:true, implRefIsRef:true]).each { Property prop ->
                println "// evalMaskCaseForType/prepLookup:  type=${type.name} prop=${prop.name}"
                // recursive call!
                putStacks.call(prop)
                evalMaskCaseJoined.call(prop)
                popStacks.call()
            }
        }
        */
    }

    /**
     * Adds new elements to the stacks
     * @param property The property, which is to be visited
     */
    def putStacks = { Property property ->
        propStack.add(property)
        propIsArrayStack.add(property.type.isArray)
        // If either already collection of if this property is an collection.
        propIsCollectionStack.add(propIsCollectionStack.last() || propIsArrayStack.last())
    }

    /**
     * Pops the latest elements from the stacks
     */
    def popStacks = {
        propStack.pop()
        propIsArrayStack.pop()
        propIsCollectionStack.pop()
    }

    /**
     * Prints the methods checkXXX(target) for a certain type, calls itself recursively for reference and complex types!
     * @param type The type to process
     */
    def printCheckExistsForType = { Type type ->
        int size = propIsCollectionStack.size()
        if ( size > 2 && propIsCollectionStack.get(size-2)) {
            // can not check for null in children of array property -> stop creating more methods checkXXXExists()!
            return
        }
        if (!propStack.isEmpty()) {
            /*
                private static boolean checkObjectBaseGisArea(JunctionNumberJoined target) {
                    return target.getObjectBase() != null
                            && target.getObjectBase().getGis() != null
                            && target.getObjectBase().getGis().getArea() != null;
                }
             */
            def checkMethodPart = propStack.collect{ data.upperCamelCase.call(it.name) }.join('')       // e.g. AddressPersonsContact
            // create longest getter call chain and then process it from one to all elements.
            List lines = []
            List getCalls = propStack.collect { "get${data.upperCamelCase.call(it.name)}()"}
            for (int i = 0; i < getCalls.size(); i++) {
                def cond = getCalls.subList(0, i+1).join('.')
                lines.add("target.${cond} != null")
            }
            def conditions = lines.join('\n                && ')
            def output = "\n    private static boolean check${checkMethodPart}Exists(${targetType} target) {\n        return ${conditions};\n    }"
            println output
        }

//        type.properties.findAll { prop -> return prop.isRefTypeOrComplexType() }.each { prop ->
        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
            // recursive call!
            putStacks.call(prop)
            printCheckExistsForType.call(prop.type.type)
            popStacks.call()
        }

        /*
        if (joined) {
            data.filterProps.call(type, [prepLookup:true, implRefIsRef:true]).each { Property prop ->
                // recursive call!
                putStacks.call(prop)
                printCheckExistsForType.call(prop.implicitRef.type)
                popStacks.call()
            }
        }
        */
    }

    /**
     * Prints the methods checkXXX(target) for a certain type, calls itself recursively for reference and complex types!
     * @param type The type to process
     */
    def printGetForType = { Type type ->
        if (propIsCollectionStack.last()) {
            // Example for key address.persons.contact where persons is the only array type
            // In case of multiple array types use .flatMap() for 2. to last array type!
            // We can not check for null references for objects in the object tree after hitting the first array property.
            // ->
            // A: the method checkXXXExists only checks for null references up to the first array property.
            // B: do use Stream.filter() with a predicate for filtering out the null references!
            /*
                private static List<ContactData> getAddressPersonsContact(JunctionContactJoined target) {
                    if (checkAddressPersonsExists(target)) {
                        return target.getAddress().getPersons().stream()
                                                               .filter(p -> p.getContact() != null)
                                                               .map(p -> p.getContact())
                                                               .collect(Collectors.toList());
                    }
                    return Collections.emptyList();
                }
             */
            def methodName = propStack.subList(0, propStack.size()).collect { data.upperCamelCase.call(it.name) }.join('') // e.g. AddressPersonsContact
            int maxCheckProp = propIsCollectionStack.last() ? propIsCollectionStack.indexOf(Boolean.TRUE) : propStack.size() // The index of the first entry of propIsCollectionStack indicating value collection
            def checkName = propStack.subList(0, maxCheckProp).collect { data.upperCamelCase.call(it.name) }.join('') // e.g. AddressPersons

            // iterate through propStack and propIsArrayStack
            // Before first array type is encountered, add getter calls
            // When first array type is encountered, add .stream() and switch mode to .map(...)
            // Whenever another array type is encountered, use .flatMap(...) instead of .map(...)
            List parts = []
            boolean useGetter = true
            for (int i = 0; i < propStack.size(); i++) {
                def currUpper = data.upperCamelCase.call(propStack[i].name)
                if (useGetter) {
                    // getXXX()
                    parts.add("get${currUpper}()")
                } else {
                    // TODO Teach Eiko and Stephan
                    def parentProp = propStack[i-1].name.take(1)
                    if (propIsArrayStack[i]) {
                        // flatMap(), e.g. flatMap(contact -> contact.getEmail().stream())
                        parts.add("filter(${parentProp} -> ${parentProp}.get${currUpper}() != null)")
                        parts.add("flatMap(${parentProp} -> ${parentProp}.get${currUpper}().stream())")
                    } else {
                        // map(), e.g. map(person -> person.getContact())
                        parts.add("filter(${parentProp} -> ${parentProp}.get${currUpper}() != null)")
                        parts.add("map(${parentProp} -> ${parentProp}.get${currUpper}())")
                    }
                }
                if (useGetter && propIsArrayStack[i]) {
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
            def output = """
    private static List<${retType}> get${methodName}(${targetType} target) {
        if (check${checkName}Exists(target)) {
            return target.${stream};
        }
        return Collections.emptyList();
    }"""
            println output
        }

//        type.properties.findAll { prop -> return prop.isRefTypeOrComplexType() }.each { prop ->
        data.filterProps.call(type, [refComplex:true]).each { Property prop ->
            // recursive call!
            putStacks.call(prop)
            printGetForType.call(prop.type.type)
            popStacks.call()
        }

        /*
        if (joined) {
            data.filterProps.call(type, [prepLookup:true, implRefIsRef:true]).each { Property prop ->
                // recursive call!
                putStacks.call(prop)
                printGetForType.call(prop.implicitRef.type)
                popStacks.call()
            }
        }
        */
    }
}
