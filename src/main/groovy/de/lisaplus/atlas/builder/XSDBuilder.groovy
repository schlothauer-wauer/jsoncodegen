package de.lisaplus.atlas.builder

import de.lisaplus.atlas.codegen.helper.java.TypeToColor
import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.AggregationType
import de.lisaplus.atlas.model.BooleanType
import de.lisaplus.atlas.model.ComplexType
import de.lisaplus.atlas.model.DateTimeType
import de.lisaplus.atlas.model.DateType
import de.lisaplus.atlas.model.DummyType
import de.lisaplus.atlas.model.InnerType
import de.lisaplus.atlas.model.IntType
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.NumberType
import de.lisaplus.atlas.model.Property
import de.lisaplus.atlas.model.RefType
import de.lisaplus.atlas.model.StringType
import de.lisaplus.atlas.model.Type
import de.lisaplus.atlas.model.UUIDType
import de.lisaplus.atlas.model.UnsupportedType
import org.apache.xmlbeans.SchemaProperty
import org.apache.xmlbeans.SchemaType
import org.apache.xmlbeans.SchemaTypeSystem
import org.apache.xmlbeans.XmlBeans
import org.apache.xmlbeans.XmlObject

import static de.lisaplus.atlas.builder.helper.BuildHelper.strFromMap
import static de.lisaplus.atlas.builder.helper.BuildHelper.strFromMap


// TODO - implement detection of lists!!!!!
// https://stackoverflow.com/questions/2293873/how-do-i-define-an-array-of-custom-types-in-wsdl
class XSDBuilder implements IModelBuilder {
    /**
     * Container for all created types helps - makes reference handling easier
     */
    def createdTypes=[:] // typeName: TypeObj
    def restrictionTypes=[:] // typeName: PropertyType

    @Override
    Model buildModel(File modelFile) {
        XmlObject object = XmlObject.Factory.parse(modelFile)
        List<XmlObject> objectList = []
        objectList.add(object)
        SchemaTypeSystem sts = XmlBeans.compileXsd((XmlObject[])objectList.toArray(), XmlBeans.getBuiltinTypeSystem(), null);
        def globalTypes = sts.globalTypes()
        Model model = new Model()
        // two interations needed because to replace pure restriction types they must be known
        collectPureRestrictionTypes(model,globalTypes)
        collectNormalTypes(model,globalTypes)
        model.initRefOwnerForTypes()
        model.checkModelForErrors()
        return model
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
                switch(propTypeName) {
                    case 'token':
                        // it is basically a string
                        newProp.type = new StringType()
                        break;
                    case 'string':
                        newProp.type = new StringType()
                        break
                    case 'boolean':
                        newProp.type = new BooleanType()
                        break
                    case 'long':
                        newProp.type = new IntType()
                        break
                    case 'int':
                        newProp.type = new IntType()
                        break
                    case 'integer':
                        newProp.type = new IntType()
                        break
                    case 'decimal':
                        newProp.type = new NumberType()
                        break
                    case 'float':
                        newProp.type = new NumberType()
                        break
                    case 'double':
                        newProp.type = new NumberType()
                        break
                    case 'dateTime':
                        newProp.type = new DateTimeType()
                        break
                    case 'date':
                        newProp.type = new DateType()
                        break
                    case 'time':
                        newProp.type = new UnsupportedType()
                        println "FOUND UNSUPPORTED TIME-TYPE"
                        break
                    default:
                        if (restrictionTypes[propTypeName] != null) {
                            newProp.type = restrictionTypes[propTypeName]
                        }
                        else {
                            newProp.type = new UnsupportedType()
                            println "FOUND UNSUPPORTED TYPE: $propTypeName"
                        }
                }
                newProp.type.originalType = propTypeName
            }
            if (prop.maxOccurs==null || prop.maxOccurs>1) {
                newProp.type.isArray = true
            }
            println "    ${newProp.name}: ${newProp.type.name()} (${prop.getType().contentType}) isArray: ${newProp.type.isArray}"
            newType.properties.add(newProp)
        }
    }

    private void handlePureRestrictionType(SchemaType type, Model model) {
        // per convention -> restriction type with name guid == UUIDType
        def baseName = xsdToName(type.getBaseType().name.localPart)
        def typeName = xsdToName(type.getName().localPart)
        def t = null
        switch(baseName) {
            case 'token':
                // TODO is basically a string
                t = new StringType()
                break;
            case 'string':
                t = typeName.toLowerCase()=='guid' ? new UUIDType() : new StringType()
                break
            case 'boolean':
                t = new BooleanType()
                break
            case 'long':
                t = new IntType()
                break
            case 'int':
                t = new IntType()
                break
            case 'integer':
                t = new IntType()
                break
            case 'decimal':
                t = new NumberType()
                break
            case 'float':
                t = new NumberType()
                break
            case 'double':
                t = new NumberType()
                break
            case 'dateTime':
                t = new DateTimeType()
                break
            case 'date':
                t = new DateType()
                break
            case 'time':
                t = new DateTimeType()
                break
            default:
                t = new UnsupportedType()
                println "unknown restriction type: $baseName"
        }
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
                    docuTxt += userInfo.getDomNode().getFirstChild().getNodeValue().replaceAll('\\W+',' ').trim()
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
