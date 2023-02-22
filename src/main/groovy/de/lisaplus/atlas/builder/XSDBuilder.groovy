package de.lisaplus.atlas.builder

import de.lisaplus.atlas.codegen.helper.java.TypeToColor
import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.AggregationType
import de.lisaplus.atlas.model.BaseType
import de.lisaplus.atlas.model.BooleanType
import de.lisaplus.atlas.model.ByteType
import de.lisaplus.atlas.model.ComplexType
import de.lisaplus.atlas.model.DateTimeType
import de.lisaplus.atlas.model.DateType
import de.lisaplus.atlas.model.DummyType
import de.lisaplus.atlas.model.InnerType
import de.lisaplus.atlas.model.IntType
import de.lisaplus.atlas.model.LongType
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.NumberType
import de.lisaplus.atlas.model.Property
import de.lisaplus.atlas.model.RefType
import de.lisaplus.atlas.model.StringType
import de.lisaplus.atlas.model.Type
import de.lisaplus.atlas.model.UnsupportedType
import org.apache.xmlbeans.SchemaAnnotation
import org.apache.xmlbeans.SchemaGlobalElement
import org.apache.xmlbeans.SchemaLocalElement
import org.apache.xmlbeans.SchemaParticle
import org.apache.xmlbeans.SchemaProperty
import org.apache.xmlbeans.SchemaType
import org.apache.xmlbeans.SchemaTypeSystem
import org.apache.xmlbeans.XmlBeans
import org.apache.xmlbeans.XmlObject


// TODO - implement detection of lists!!!!!
// https://stackoverflow.com/questions/2293873/how-do-i-define-an-array-of-custom-types-in-wsdl
class XSDBuilder implements IModelBuilder {
    /**
     * Container for all created types helps - makes reference handling easier
     */
    def createdTypes=[:] // typeName: TypeObj
    def restrictionTypes=[:] // typeName: PropertyType
    def globalTypes // to avoid late additional parameter

    @Override
    Model buildModel(File modelFile) {
        XmlObject object = XmlObject.Factory.parse(modelFile)
        List<XmlObject> objectList = []
        objectList.add(object)
        SchemaTypeSystem sts = XmlBeans.compileXsd((XmlObject[])objectList.toArray(), XmlBeans.getBuiltinTypeSystem(), null);
        globalTypes = sts.globalTypes()

        Model model = new Model()
        // two interations needed because to replace pure restriction types they must be known
        collectPureRestrictionTypes(model,globalTypes)
        collectNormalTypes(model,globalTypes)
        if (!model.types) {
            collectTypesFromGlobalElements(model,sts.globalElements())
        }
        model.postProcess()

        return model
    }

    void collectTypesFromGlobalElements(Model model, def globalElements) {
        globalElements.each { SchemaGlobalElement globalElement ->
            def type = globalElement.getType()
            handleNormalType(type,model,globalElement.name.localPart)
        }
    }

    String navigateParticleToGetDocumentation(SchemaParticle p, Property prop) {
        if (p==null) return
        if (p.getParticleType()==SchemaParticle.ELEMENT) {
            if (((SchemaLocalElement)p).name.localPart==prop.name) {
                SchemaAnnotation annotation = ((SchemaLocalElement) p).getAnnotation();
                if (annotation != null) {
                    def userInfos = annotation.userInformation
                    def docuTxt = null
                    for (XmlObject userInfo : userInfos) {
                        if (userInfo.getDomNode().localName == 'documentation') {
                            if (docuTxt != null) {
                                docuTxt += ' '
                            } else {
                                docuTxt = ''
                            }
                            docuTxt += userInfo.getDomNode().getFirstChild().getNodeValue().replaceAll('\\W+', ' ').trim()
                        }
                    }
                    prop.description = docuTxt
                }
            }
        }
        else {
            SchemaParticle[] children = p.getParticleChildren();
            if (children!=null) {
                for (int i = 0; i < children.length; i++)
                    navigateParticleToGetDocumentation(children[i],prop);
            }
        }
        /*
        switch (p.getParticleType())
        {
            case SchemaParticle.ALL:
            case SchemaParticle.CHOICE:
            case SchemaParticle.SEQUENCE:
                // These are "container" particles, so iterate over their children
                SchemaParticle[] children = p.getParticleChildren();
                for (int i = 0; i < children.length; i++)
                    navigateParticleToGetDocumentation(children[i],prop);
                break;
            case SchemaParticle.ELEMENT:
                if (((SchemaLocalElement)p).name.localPart==prop.name) {
                    SchemaAnnotation annotation = ((SchemaLocalElement) p).getAnnotation();
                    if (annotation != null) {
                        def userInfos = annotation.userInformation
                        def docuTxt = null
                        for (XmlObject userInfo : userInfos) {
                            if (userInfo.getDomNode().localName == 'documentation') {
                                if (docuTxt != null) {
                                    docuTxt += ' '
                                } else {
                                    docuTxt = ''
                                }
                                docuTxt += userInfo.getDomNode().getFirstChild().getNodeValue().replaceAll('\\W+', ' ').trim()
                            }
                        }
                        prop.description = docuTxt
                    }
                }

        }
        */
    }

    private void collectPureRestrictionTypes(Model model, def globalTypes) {
        for (SchemaType type: globalTypes) {
            if (type.contentType==SchemaType.DT_NOT_DERIVED) {
                handlePureRestrictionType(type,model)
            }
        }
    }

    private void collectNormalTypes(Model model, def globalTypes) {
        for (SchemaType type: globalTypes) {
            if (!(type.contentType==SchemaType.DT_NOT_DERIVED)) {
                handleNormalType(type,model,null)
            }
        }
    }

    private void handleNormalType(SchemaType type, Model model, String desiredName = null) {
        Type newType = desiredName==null ? new Type() : new InnerType()
        newType.name = desiredName==null ? xsdToName(type.getName().localPart) : desiredName
        if (model.types.contains(newType)) {
            return
        }
        newType.description = getDescription(type)
        newType.baseTypes.add (xsdToName(type.baseType.getName().localPart))

        def baseTypeName = xsdToName(type.getBaseType().getName().localPart)
        def extendsStr=''
        if (baseTypeName && baseTypeName!='anyType') {
            newType.baseTypes.add(baseTypeName)
            extendsStr = " extends $baseTypeName"
        }
        println "new type: ${newType.name}$extendsStr"
        addProperties(type,model,newType)
        addNewType(newType,model)
        //println "added type: ${newType.name}, contentType: ${type.contentType}"
    }


    private void addProperties(SchemaType type, Model model, Type newType) {
        def properties = type.getProperties()
        for (SchemaProperty prop: properties) {
            // know currently no way to retrieve the documentation of properties
            def propName = xsdToName(prop.getName().localPart)
            def propTypeNameObj = prop.getType().name
            def desiredName = null
            if (prop.getType().contentType==3 && propTypeNameObj==null) {
                // Complex inner type
                println 'FOUND COMPLEX INNER TYPE :D ... have to add it'
                desiredName = newType.name + propName.substring(0,1).toUpperCase() + propName.substring(1)
                println '-- intermediate type creation - start --'
                handleNormalType(prop.getType(), model, desiredName)
                println '-- intermediate type creation - end --'

            }
            def propTypeName = propTypeNameObj != null ? xsdToName(propTypeNameObj.localPart) : desiredName!=null ? desiredName : '???'
            Property newProp = new Property()
            newProp.name = propName
            // TODO this implementation isn't perfect because it parse every time the same contentModel :-/
            // ... and it doesn't cover xsd attributes
            navigateParticleToGetDocumentation (type.getContentModel(),newProp)
            if (propName=='posList') {
                propName = propName
            }
            if (prop.getType().contentType==3) {
                // complex type
                if (propName.endsWith('_id') || propName.endsWith('Id')) { // per convention
                    newProp.aggregationType = AggregationType.composition
                }
                else {
                    newProp.aggregationType = AggregationType.aggregation
                }
                newProp.type = new ComplexType()
                if (propTypeNameObj==null) {
                    // complex inner type
                    newProp.type.type = createdTypes[desiredName] // have to be resolved
                }
                else {
                    // complex type
                    Type t = createdTypes[propTypeName]
                    if (t==null) {
                        // complex type that is currently not created ... handled temporary with dummy type
                        t = new DummyType()
                        ((DummyType)t).referencesToChange.add(newProp.type)
                        createdTypes[propTypeName] = t
                    }
                    else if (t instanceof DummyType ) {
                        ((DummyType)t).referencesToChange.add(newProp.type)
                    }
                    newProp.type.type = t
                }
            }
            else {
                typeStrToType(propTypeName,newProp,prop)
                newProp.type.originalType = propTypeName
            }
            if (prop.maxOccurs==null || prop.maxOccurs>1) {
                newProp.type.isArray = true
            }
            if (prop.isAttribute()) {
                def attributeModel = type.getAttributeModel()
                def attributes = attributeModel.getAttributes()
                for (def attrib: attributes) {
                    if (attrib.name.localPart==newProp.name) {
                        newProp.description = getDescription(attrib)
                        break
                    }
                }
            }
            println "    ${newProp.name}: ${newProp.type.name()} (${prop.getType().contentType}) isArray: ${newProp.type.isArray}"
            newType.properties.add(newProp)
        }
    }

    private BaseType typeForString(String s) {
        switch(s) {
            case 'token':
                // it is basically a string
                return new StringType()
            case 'string':
                return new StringType()
            case 'boolean':
                return  new BooleanType()
            case 'byte':
                return new ByteType()
            case 'long':
                return new LongType()
            case 'int':
                return new IntType()
            case 'unsignedLong':
                return new LongType()
            case 'unsignedInt':
                return new IntType()
            case 'positiveInteger':
                return new IntType()
            case 'unsignedShort':
                return new IntType()
            case 'integer':
                return new IntType()
            case 'decimal':
                return new NumberType()
            case 'float':
                return new NumberType()
            case 'double':
                return new NumberType()
            case 'dateTime':
                return new DateTimeType()
            case 'date':
                return new DateType()
            case 'time':
                return new StringType()
            case 'NCName':
                return new StringType()
            case 'NMTOKEN':
                return new StringType()
            case 'gMonthDay':
                return new StringType()
            default:
                return null
        }
    }

    private void typeStrToType(String typeName, def newProp, def prop) {
        newProp.type = typeForString(typeName)
        if (!newProp.type) {
            if (restrictionTypes[typeName] != null) {
                newProp.type = restrictionTypes[typeName]
            }
            else if (createdTypes[typeName] != null) {
                // type is currently in the model
                newProp.type = new RefType()
                def alreadyCreated = createdTypes[typeName]
                if (alreadyCreated instanceof DummyType) {
                    ((DummyType)alreadyCreated).referencesToChange.add(newProp.type)
                }
                newProp.type.type = alreadyCreated
            }
            else if (globalTypes.find{ it.getName().localPart==typeName }) {
                newProp.type = new RefType()
                Type t = new DummyType()
                ((DummyType)t).referencesToChange.add(newProp.type)
                createdTypes[typeName] = t
                newProp.type.type = t
            }
            else {
                // try to look into the XMLBeans implementations to find some XSD specifics
                if (prop!=null) {
                    def type = prop.getType()
                    if (type.isSimpleType() && type.getBaseType()) {
                        def simpleTypeName = type.getBaseType().name.localPart
                        newProp.type = typeForString(simpleTypeName)
                    }
                }
                if (!newProp.type) {
                    newProp.type = new UnsupportedType()
                    println "FOUND UNSUPPORTED TYPE: $typeName"
                }
            }
        }
    }

    private void handlePureRestrictionType(SchemaType type, Model model) {
        // per convention -> restriction type with name guid == UUIDType
        def baseName = xsdToName(type.getBaseType().name.localPart)
        def typeName = xsdToName(type.getName().localPart)
        def t = typeForString(baseName)
        if (t!=null) {
            t.originalType = baseName
            restrictionTypes[typeName] = t
        }
    }

    private String getDescription(elem) {
        def docuTxt = null
        def annotation = elem.getAnnotation()
        if (annotation!=null) {
            def userInfos = annotation.userInformation
            for (XmlObject userInfo: userInfos ) {
                if (userInfo.getDomNode().localName=='documentation') {
                    if (docuTxt!=null) {
                        docuTxt+=' '
                    }
                    else {
                        docuTxt=''
                    }
                    def firstChild = userInfo.getDomNode().getFirstChild()
                    if (firstChild!=null) {
                        docuTxt += firstChild.getNodeValue().replaceAll('\\W+',' ').trim()
                    }
                }
            }
        }
        return docuTxt
    }

    private void addNewType(Type newType, def model) {
        def typeName = newType.name
        def alreadyCreated = createdTypes[typeName]
        if (alreadyCreated) {
            if (alreadyCreated instanceof DummyType) {
                // handle forward usage of types in declarations ... references need to be updated
                alreadyCreated.referencesToChange.each { refType ->
                    refType.type = newType
                }
                createdTypes[typeName] = newType
            }
        }
        TypeToColor.setColor(newType)
        createdTypes[newType.name] = newType
        model.types.add(newType)
    }

    private String xsdToName(String localPart) {
        return localPart.replaceAll('-','_')
    }

}
