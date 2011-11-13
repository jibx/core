/*
 * Copyright (c) 2007-2010, Dennis M. Sosnoski. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.ws.wsdl.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jibx.binding.generator.BindGen;
import org.jibx.binding.generator.BindingMappingDetail;
import org.jibx.binding.model.BindingElement;
import org.jibx.binding.model.BindingHolder;
import org.jibx.binding.model.CollectionElement;
import org.jibx.binding.model.ElementBase;
import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.MappingElementBase;
import org.jibx.custom.classes.ClassCustom;
import org.jibx.custom.classes.CustomBase;
import org.jibx.custom.classes.GlobalCustom;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.schema.IComponent;
import org.jibx.schema.ISchemaResolver;
import org.jibx.schema.MemoryResolver;
import org.jibx.schema.SchemaContextTracker;
import org.jibx.schema.SchemaHolder;
import org.jibx.schema.SchemaVisitor;
import org.jibx.schema.TreeWalker;
import org.jibx.schema.UrlResolver;
import org.jibx.schema.elements.AnnotationElement;
import org.jibx.schema.elements.ComplexTypeElement;
import org.jibx.schema.elements.DocumentationElement;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.elements.SchemaLocationBase;
import org.jibx.schema.elements.SequenceElement;
import org.jibx.schema.generator.MappingDetail;
import org.jibx.schema.generator.SchemaGen;
import org.jibx.schema.types.Count;
import org.jibx.schema.validation.ProblemConsoleLister;
import org.jibx.schema.validation.ProblemLogLister;
import org.jibx.schema.validation.ProblemMultiHandler;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.schema.validation.ValidationProblem;
import org.jibx.schema.validation.ValidationUtils;
import org.jibx.util.DummyClassLocator;
import org.jibx.util.IClass;
import org.jibx.util.IClassLocator;
import org.jibx.util.InsertionOrderedSet;
import org.jibx.util.ResourceMatcher;
import org.jibx.util.Types;
import org.jibx.ws.wsdl.model.Definitions;
import org.jibx.ws.wsdl.model.Message;
import org.jibx.ws.wsdl.model.MessagePart;
import org.jibx.ws.wsdl.model.Operation;
import org.jibx.ws.wsdl.tools.custom.FaultCustom;
import org.jibx.ws.wsdl.tools.custom.OperationCustom;
import org.jibx.ws.wsdl.tools.custom.ServiceCustom;
import org.jibx.ws.wsdl.tools.custom.ThrowsCustom;
import org.jibx.ws.wsdl.tools.custom.ValueCustom;
import org.jibx.ws.wsdl.tools.custom.WsdlCustom;
import org.w3c.dom.Node;

/**
 * Start-from-code WSDL generator using JiBX data binding. This starts from one or more service classes, each with one
 * or more methods to be exposed as service operations, and generates complete bindings and WSDL for the services.
 * Although many of the methods in this class use <code>public</code> access, they are intended for use only by the JiBX
 * developers and may change from one release to the next. To make use of this class from your own code, call the {@link
 * #main(String[])} method with an appropriate argument list.
 * 
 * @author Dennis M. Sosnoski
 */
public class Jibx2Wsdl
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(Jibx2Wsdl.class.getName());
    
    /** Parameter information for generation. */
    private final WsdlGeneratorCommandLine m_generationParameters;
    
    /** Binding generator. */
    private final BindGen m_bindingGenerator;
    
    /** Schema generator. */
    private final SchemaGen m_schemaGenerator;
    
    /** Map from schema namespace URIs to schema holders. */
    private final Map m_uriSchemaMap;
    
    /**
     * Constructor.
     * 
     * @param parms generation parameters
     */
    private Jibx2Wsdl(WsdlGeneratorCommandLine parms) {
        m_generationParameters = parms;
        GlobalCustom global = parms.getGlobal();
        m_bindingGenerator = new BindGen(global);
        m_schemaGenerator = new SchemaGen(parms.getLocator(), global, parms.getUriNames());
        m_uriSchemaMap = new HashMap();
    }
    
    /**
     * Get the qualified name used for an abstract mapping. This throws an exception if the qualified name is not found.
     * 
     * @param type
     * @param mapping
     * @return qualified name
     */
    private QName getMappingQName(String type, MappingElement mapping) {
        MappingDetail detail = m_schemaGenerator.getMappingDetail(mapping);
        if (detail == null) {
            throw new IllegalStateException("No mapping found for type " + type);
        } else if (detail.isType()) {
            return detail.getTypeName();
        } else {
            throw new IllegalStateException("Need abstract mapping for type " + type);
        }
    }
    
    /**
     * Add reference to another schema. The reference may either be to a definition in a supplied schema (in which case
     * it'll be in the map) or in a generated schema (in which case the namespace is used to lookup the schema).
     * 
     * @param qname referenced definition name
     * @param namemap map from qualified name to holder for defining schema (only for predefined schemas)
     * @param holder schema making the reference
     */
    private void addSchemaReference(QName qname, Map namemap, SchemaHolder holder) {
        if (qname != null && !IComponent.SCHEMA_NAMESPACE.equals(qname.getUri())) {
            SchemaHolder target = (SchemaHolder)namemap.get(qname);
            if (target == null) {
                target = (SchemaHolder)m_uriSchemaMap.get(qname.getUri());
            }
            holder.addReference(target);
        }
    }

    /**
     * Build an element representing a parameter or return value.
     * 
     * @param parm
     * @param typemap map from parameterized type to abstract mapping qualified name
     * @param namemap map from qualified name to holder for defining schema (only for predefined schemas)
     * @param hold containing schema holder
     * @return constructed element
     */
    private ElementElement buildValueElement(ValueCustom parm, Map typemap, Map namemap, SchemaHolder hold) {
        
        // create the basic element definition
        ElementElement elem = new ElementElement();
        if (!parm.isRequired()) {
            elem.setMinOccurs(Count.COUNT_ZERO);
        }
        String type = parm.getWorkingType();
        boolean repeat = false;
        
        // check type or reference for element
        boolean isref = false;
        String ptype = parm.getBoundType();
        QName tname = (QName)typemap.get(ptype);
        if (tname == null) {
            
            // no mapped handling, so check first for object type parameter with corresponding schema type (byte[])
            tname = Types.schemaType(type);
            if (tname == null && (tname = Types.schemaType(ptype)) == null) {
                
                // must be either an array collection, or a reference
                repeat = parm.isCollection();
                String usetype = ptype.endsWith(">") ? type : ptype;
                BindingMappingDetail detail = m_bindingGenerator.getMappingDetail(usetype);
                if (detail == null) {
                    throw new IllegalStateException("No mapping found for type " + usetype);
                } else if (detail.isExtended()) {
                    elem.setRef(detail.getElementQName());
                    isref = true;
                } else {
                    MappingElement mapping = detail.getAbstractMapping();
                    tname = mapping.getTypeQName();
                    if (tname == null) {
                        tname = getMappingQName(usetype, mapping);
                    }
                }
                
            }
        }
        if (isref) {
            addSchemaReference(elem.getRef(), namemap, hold);
        } else {
            
            // set element type and name
            m_schemaGenerator.setElementType(tname, elem, hold);
            String ename = parm.getXmlName();
            if (ename == null) {
                ename = tname.getName();
            }
            elem.setName(ename);
            addSchemaReference(tname, namemap, hold);
        }
        
        // handle repeated value
        if (repeat) {
            elem.setMaxOccurs(Count.COUNT_UNBOUNDED);
        }
        
        // add documentation if available
        List nodes = parm.getDocumentation();
        if (nodes != null) {
            AnnotationElement anno = new AnnotationElement();
            DocumentationElement doc = new DocumentationElement();
            for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                Node node = (Node)iter.next();
                doc.addContent(node);
            }
            anno.getItemsList().add(doc);
            elem.setAnnotation(anno);
        }
        return elem;
    }
    
    /**
     * Build WSDL for service.
     * 
     * @param service
     * @param ptypemap map from parameterized type to abstract mapping name
     * @param classelems fully-qualified class name to element qualified name map
     * @param elemschemas element qualified name to schema holder map
     * @param classtypes fully-qualified class name to type qualified name map
     * @param typeschemas type qualified name to schema holder map
     * @return constructed WSDL definitions
     */
    private Definitions buildWSDL(ServiceCustom service, Map ptypemap, Map classelems, Map elemschemas, Map classtypes,
        Map typeschemas) {
        
        // initialize root object of definition
        String wns = service.getWsdlNamespace();
        Definitions def = new Definitions(service.getPortTypeName(), service.getBindingName(), service.getServiceName(),
            service.getPortName(), "tns", wns);
        def.setServiceLocation(service.getServiceAddress());
        
        // add service documentation if available
        IClassLocator locator = m_generationParameters.getLocator();
        IClass info = locator.getClassInfo(service.getClassName());
        if (info != null) {
            List nodes = m_generationParameters.getWsdlCustom().getFormatter(service).docToNodes(info.getJavaDoc());
            def.setPortTypeDocumentation(nodes);
        }
        
        // find or create the schema element and namespace
        String sns = service.getNamespace();
        SchemaHolder holder = m_schemaGenerator.findSchema(sns);
        SchemaElement schema = holder.getSchema();
        
        // process messages and operations used by service
        ArrayList ops = service.getOperations();
        Map fltmap = new HashMap();
        Set imports = new InsertionOrderedSet();
        Map typemap = new HashMap(ptypemap);
        for (int i = 0; i < ops.size(); i++) {
            
            // get information for operation
            OperationCustom odef = (OperationCustom)ops.get(i);
            String oname = odef.getOperationName();
            Operation op = new Operation(oname);
            op.setDocumentation(odef.getDocumentation());
            op.setSoapAction(odef.getSoapAction());
            
            // generate input message information
            Message rqmsg = new Message(odef.getRequestMessageName(), wns);
            op.addInputMessage(rqmsg);
            def.addMessage(rqmsg);
            QName rqelem = null;
            if (m_generationParameters.isDocLit()) {
                
                // check if input parameter defined for method
                ArrayList parms = odef.getParameters();
                if (parms.size() > 0) {
                    
                    // check for existing element definition matching the parameter type
                    ValueCustom parm = (ValueCustom)parms.get(0);
                    rqelem = (QName)classelems.get(parm.getWorkingType());
                    if (rqelem == null) {
                        
                        // create new element for parameter
                        ElementElement pelem = buildValueElement(parm, ptypemap, typeschemas, holder);
                        schema.getTopLevelChildren().add(pelem);
                        rqelem = pelem.getQName();
                        
                    } else {
                        
                        // import and use existing element definition
                        imports.add(elemschemas.get(rqelem));
                        addSchemaReference(rqelem, elemschemas, holder);
                        
                    }
                }
                
            } else {
                
                // construct a sequence for wrapped method parameters
                SequenceElement seq = new SequenceElement();
                ArrayList parms = odef.getParameters();
                for (int j = 0; j < parms.size(); j++) {
                    ValueCustom parm = (ValueCustom)parms.get(j);
                    String type = parm.getWorkingType();
                    ElementElement pelem;
                    if (!typemap.containsKey(type)) {
                        
                        // add predefined mapping type to known types and require schema import
                        QName tname = (QName)classtypes.get(type);
                        if (tname != null) {
                            typemap.put(type, tname);
                            imports.add(typeschemas.get(tname));
                        }
                        
                    }
                    pelem = buildValueElement(parm, ptypemap, typeschemas, holder);
                    seq.getParticleList().add(pelem);
                }
                
                // add corresponding schema definition to schema
                ComplexTypeElement tdef = new ComplexTypeElement();
                tdef.setContentDefinition(seq);
                ElementElement elem = new ElementElement();
                String wname = odef.getRequestWrapperName();
                elem.setName(wname);
                elem.setTypeDefinition(tdef);
                schema.getTopLevelChildren().add(elem);
                rqelem = new QName(sns, wname);
                
            }
            
            // add part definition to message (if present)
            if (rqelem != null) {
                MessagePart part = new MessagePart("part", rqelem);
                rqmsg.getParts().add(part);
                def.addNamespace(rqelem.getUri());
            }
            
            // generate output message information
            Message rsmsg = new Message(odef.getResponseMessageName(), wns);
            op.addOutputMessage(rsmsg);
            def.addMessage(rsmsg);
            ValueCustom rtrn = odef.getReturn();
            QName rselem = null;
            if (m_generationParameters.isDocLit()) {
                
                // check if return value defined for method
                if (!"void".equals(rtrn.getWorkingType())) {
                    
                    // check for existing element definition matching the return type
                    rselem = (QName)classelems.get(rtrn.getWorkingType());
                    if (rselem == null) {
                        
                        // create new element for return
                        ElementElement relem = buildValueElement(rtrn, ptypemap, typeschemas, holder);
                        schema.getTopLevelChildren().add(relem);
                        rselem = relem.getQName();
                        
                    } else {
                        
                        // import and use existing element definition
                        imports.add(elemschemas.get(rselem));
                        addSchemaReference(rqelem, elemschemas, holder);
                        
                    }
                }
                
            } else {
                
                // add corresponding schema definition to schema
                SequenceElement seq = new SequenceElement();
                if (!"void".equals(rtrn.getWorkingType())) {
                    ElementElement relem = buildValueElement(rtrn, ptypemap, typeschemas, holder);
                    seq.getParticleList().add(relem);
                }
                ComplexTypeElement tdef = new ComplexTypeElement();
                tdef.setContentDefinition(seq);
                ElementElement elem = new ElementElement();
                String wname = odef.getResponseWrapperName();
                elem.setName(wname);
                elem.setTypeDefinition(tdef);
                schema.getTopLevelChildren().add(elem);
                rselem = new QName(sns, wname);
                
            }
            
            // add part definition to message (if present)
            if (rselem != null) {
                MessagePart part = new MessagePart("part", rselem);
                rsmsg.getParts().add(part);
                def.addNamespace(rselem.getUri());
            }
            
            // process fault message(s) for operation
            ArrayList thrws = odef.getThrows();
            WsdlCustom wsdlcustom = m_generationParameters.getWsdlCustom();
            for (int j = 0; j < thrws.size(); j++) {
                ThrowsCustom thrw = (ThrowsCustom)thrws.get(j);
                String type = thrw.getType();
                Message fmsg = (Message)fltmap.get(type);
                if (fmsg == null) {
                    
                    // first time for this throwable, create the message
                    FaultCustom fault = wsdlcustom.forceFaultCustomization(type);
                    QName fqname = new QName(sns, fault.getElementName());
                    MessagePart part = new MessagePart("fault", fqname);
                    fmsg = new Message(fault.getFaultName(), wns);
                    fmsg.getParts().add(part);
                    def.addMessage(fmsg);
                    def.addNamespace(sns);
                    
                    // make sure the corresponding mapping exists
                    BindingMappingDetail detail = m_bindingGenerator.getMappingDetail(fault.getDataType());
                    if (detail == null) {
                        throw new IllegalStateException("No mapping found for type " + type);
                    }
                    
                    // record that the fault has been defined
                    fltmap.put(type, fmsg);
                }
                
                // add fault to operation definition
                op.addFaultMessage(fmsg);
            }
            
            // add operation to list of definitions
            def.addOperation(op);
            
        }
        
        // include embedded schema for message definitions only if needed
        if (holder.getReferences().size() > 0 || schema.getChildCount() > 0) {
            def.getSchemas().add(schema);
        }
        return def;
    }
    
    /**
     * Accumulate data type(s) from value to be included in binding.
     * 
     * @param value
     * @param clasmap map with classes to be excluded as keys
     * @param dataset set of types for binding
     */
    private void accumulateData(ValueCustom value, Map clasmap, Set dataset) {
        String type = value.getBoundType();
        if (!dataset.contains(type) && !clasmap.containsKey(type) && !Types.isSimpleValue(type)) {
            String itype = value.getItemType();
            if (itype == null) {
                dataset.add(type);
            } else {
                dataset.add(itype);
            }
        }
    }
    
    /**
     * Add the &lt;mapping> definition for a typed collection to a binding. This always creates an abstract mapping with
     * the type name based on both the item type and the collection type, except in the case where an array is being
     * used in unwrapped (non-plain doc/lit) form.
     * 
     * @param doclit plain doc/lit handling flag
     * @param value collection value
     * @param typemap map from parameterized type to abstract mapping name
     * @param bind target binding
     * @return qualified name for collection
     */
    public QName addCollectionBinding(boolean doclit, ValueCustom value, Map typemap, BindingHolder bind) {
        
        // check for existing mapping
        String ptype = value.getBoundType();
        QName qname = (QName)typemap.get(ptype);
        if (qname == null) {
            
            // check for either not an array, or plain doc/lit
            String type = value.getWorkingType();
            if (doclit || !type.endsWith("[]")) {
                
                // create abstract mapping for collection class type
                MappingElementBase mapping = new MappingElement();
                mapping.setClassName(type);
                mapping.setAbstract(true);
                mapping.setCreateType(value.getCreateType());
                mapping.setFactoryName(value.getFactoryMethod());
                
                // generate the mapping type name from item class name and suffix
                String suffix;
                GlobalCustom global = m_generationParameters.getGlobal();
                IClass clas = global.getClassInfo(type);
                if (clas.isImplements("Ljava/util/List;")) {
                    suffix = "List";
                } else if (clas.isImplements("Ljava/util/Set;")) {
                    suffix = "Set";
                } else {
                    suffix = "Collection";
                }
                String itype = value.getItemType();
                ClassCustom cust = global.addClassCustomization(itype);
                
                // register the type name for mapping
                String name = cust.getSimpleName() + suffix;
                String uri = bind.getNamespace();
                qname = new QName(uri, CustomBase.convertName(name, CustomBase.CAMEL_CASE_NAMES));
                mapping.setTypeQName(qname);
                bind.addTypeNameReference(uri, uri);
                typemap.put(ptype, qname);
                
                // add collection definition details
                CollectionElement coll = new CollectionElement();
                m_bindingGenerator.defineCollection(itype, value.getItemName(), coll, bind);
                mapping.addChild(coll);
                
                // add mapping to binding
                bind.addMapping(mapping);
            }
        }
        return qname;
    }
    
    /**
     * Generate based on list of service classes.
     * 
     * @param classes service class list
     * @param extras list of extra classes for binding
     * @param classelems fully-qualified class name to element qualified name map
     * @param elemschemas element qualified name to schema element map
     * @param classtypes fully-qualified class name to type qualified name map
     * @param typeschemas type qualified name to schema element map
     * @param exists existing schemas potentially referenced
     * @return list of WSDLs
     * @throws JiBXException
     * @throws IOException
     */
    private List generate(List classes, List extras, Map classelems, Map elemschemas, Map classtypes, Map typeschemas,
        Collection exists) throws JiBXException, IOException {
        
        // add any service classes not already present in customizations
        WsdlCustom wsdlcustom = m_generationParameters.getWsdlCustom();
        for (int i = 0; i < classes.size(); i++) {
            String sclas = (String)classes.get(i);
            if (wsdlcustom.getServiceCustomization(sclas) == null) {
                wsdlcustom.addServiceCustomization(sclas);
            }
        }
        
        // accumulate unmapped data classes used by all service operations
        // TODO: throws class handling, with multiple services per WSDL
        InsertionOrderedSet abstrs = new InsertionOrderedSet();
        InsertionOrderedSet concrs = new InsertionOrderedSet();
        ArrayList qnames = new ArrayList();
        List services = wsdlcustom.getServices();
        boolean doclit = m_generationParameters.isDocLit();
        for (Iterator iter = services.iterator(); iter.hasNext();) {
            ServiceCustom service = (ServiceCustom)iter.next();
            List ops = service.getOperations();
            for (Iterator iter1 = ops.iterator(); iter1.hasNext();) {
                OperationCustom op = (OperationCustom)iter1.next();
                List parms = op.getParameters();
                if (doclit && parms.size() > 1) {
                    System.err.println("Multiple parmameters not allowed for doc/lit: method " + op.getMethodName());
                }
                for (Iterator iter2 = parms.iterator(); iter2.hasNext();) {
                    ValueCustom parm = (ValueCustom)iter2.next();
                    if (doclit) {
                        accumulateData(parm, classelems, concrs);
                    } else {
                        accumulateData(parm, classtypes, abstrs);
                    }
                }
                if (doclit) {
                    accumulateData(op.getReturn(), classelems, concrs);
                } else {
                    accumulateData(op.getReturn(), classtypes, abstrs);
                }
                ArrayList thrws = op.getThrows();
                for (int i = 0; i < thrws.size(); i++) {
                    
                    // add concrete mapping for data type, if used
                    ThrowsCustom thrw = (ThrowsCustom)thrws.get(i);
                    FaultCustom fault = wsdlcustom.forceFaultCustomization(thrw.getType());
                    if (!concrs.contains(fault.getDataType())) {
                        concrs.add(fault.getDataType());
                        qnames.add(new QName(service.getNamespace(), fault.getElementName()));
                    }
                }
            }
        }
        
        // include extra classes as needing concrete mappings
        GlobalCustom global = m_generationParameters.getGlobal();
        for (int i = 0; i < extras.size(); i++) {
            String type = (String)extras.get(i);
            if (!concrs.contains(type)) {
                concrs.add(type);
                global.addClassCustomization(type);
                qnames.add(null);
            }
        }
        
        // generate bindings for all data classes used
        m_bindingGenerator.generateSpecified(qnames, concrs.asList(), abstrs.asList());
        
        // add binding definitions for collections passed or returned in plain doc/lit, and find empty service bindings
        Map typemap = new HashMap();
        Set unbounduris = new HashSet();
        for (Iterator iter = services.iterator(); iter.hasNext();) {
            ServiceCustom service = (ServiceCustom)iter.next();
            List ops = service.getOperations();
            String uri = service.getNamespace();
            BindingHolder hold = m_bindingGenerator.addBinding(uri, false);
            for (Iterator iter1 = ops.iterator(); iter1.hasNext();) {
                OperationCustom op = (OperationCustom)iter1.next();
                List parms = op.getParameters();
                for (Iterator iter2 = parms.iterator(); iter2.hasNext();) {
                    ValueCustom parm = (ValueCustom)iter2.next();
                    if (parm.getItemType() != null) {
                        addCollectionBinding(doclit, parm, typemap, hold);
                    }
                }
                ValueCustom ret = op.getReturn();
                if (ret.getItemType() != null) {
                    addCollectionBinding(doclit, ret, typemap, hold);
                }
            }
            if (hold.getMappingCount() == 0) {
                unbounduris.add(uri);
            }
        }
        
        // ensure references for URIs with no mapping definitions
        m_bindingGenerator.addRootUris(unbounduris);
        
        // complete bindings and check if anything to be written
        String name = m_generationParameters.getBindingName();
        File path = m_generationParameters.getGeneratePath();
        BindingHolder rhold = m_bindingGenerator.finish(name);
        if (rhold.getBinding().topChildren().size() > 0) {
            
            // write and validate the bindings
            List bindings = m_bindingGenerator.validateFiles(path, m_generationParameters.getLocator(), rhold);
            if (bindings == null) {
                return null;
            }
            
            // build and record the schemas
            List schemas = m_schemaGenerator.buildSchemas(bindings);
            for (Iterator iter = schemas.iterator(); iter.hasNext();) {
                SchemaHolder holder = (SchemaHolder)iter.next();
                m_uriSchemaMap.put(holder.getNamespace(), holder);
            }
        }
        
        // build the WSDL for each service
        ArrayList wsdls = new ArrayList();
        for (Iterator iter = services.iterator(); iter.hasNext();) {
            wsdls.add(buildWSDL((ServiceCustom)iter.next(), typemap, classelems, elemschemas, classtypes, typeschemas));
        }
        m_schemaGenerator.finishSchemas(exists);
        return wsdls;
    }

    /**
     * Accumulate all mapping definitions, including those found in included bindings. For each named abstract mapping
     * found, the class name is associated with the type name in the type map; for each concrete mapping found, the
     * class name is associated with the element name in the element map. Included bindings are handled with recursive
     * calls.
     *
     * @param binding binding definition root
     * @param elemmap map from fully-qualified class name to element qualified name
     * @param typemap map from fully-qualified class name to type qualified name
     */
    private static void accumulateBindingDefinitions(BindingElement binding, Map elemmap, Map typemap) {
        ArrayList childs = binding.topChildren();
        for (int i = 0; i < childs.size(); i++) {
            ElementBase element = (ElementBase)childs.get(i);
            if (element.type() == ElementBase.INCLUDE_ELEMENT) {
                
                // use recursive call to add nested definitions in included binding
                accumulateBindingDefinitions(((org.jibx.binding.model.IncludeElement)element).getBinding(), elemmap,
                    typemap);
                
            } else if (element.type() == ElementBase.MAPPING_ELEMENT) {
                
                // handle mapping as type if abstract with type name, or as element if concrete
                MappingElementBase mapping = (MappingElementBase)element;
                String cname = mapping.getClassName();
                if (mapping.isAbstract()) {
                    QName qname = mapping.getTypeQName();
                    if (qname != null) {
                        typemap.put(cname, qname);
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("Added class " + cname + " with type " + qname);
                        }
                    }
                } else {
                    QName qname = new QName(mapping.getNamespace().getUri(), mapping.getName());
                    elemmap.put(cname, qname);
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Added class " + cname + " with element " + qname);
                    }
                }
                
            }
        }
    }

    /**
     * Load and validate binding and process all mapping definitions, including those in included bindings.
     *
     * @param url binding definition path
     * @param elemmap map from element qualified name to class data
     * @param typemap map from type qualified name to class data
     * @return binding
     * @throws JiBXException
     * @throws IOException
     */
    public static BindingElement processPregeneratedBinding(URL url, Map elemmap, Map typemap)
        throws JiBXException, IOException {
        
        // get binding definition file name from path
        String name = "";
        String path = url.getPath();
        if (path != null) {
            name = path;
            int split = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
            if (split > 0) {
                name = name.substring(split+1);
            }
            split = name.lastIndexOf('.');
            name = name.substring(0, split);
        }
        
        // construct object model for binding
        org.jibx.binding.model.ValidationContext vctx =
            new org.jibx.binding.model.ValidationContext(new DummyClassLocator());
        BindingElement binding = BindingElement.readBinding(url.openStream(), name, null, true,
            vctx);
        binding.setBaseUrl(url);
        vctx.setBindingRoot(binding);
        
        // validate the binding definition
        binding.runValidation(vctx);
        
        // list validation errors
        ArrayList probs = vctx.getProblems();
        if (probs.size() > 0) {
            for (int i = 0; i < probs.size(); i++) {
                org.jibx.binding.model.ValidationProblem prob =
                    (org.jibx.binding.model.ValidationProblem)probs.get(i);
                System.out.print(prob.getSeverity() >=
                    ValidationProblem.ERROR_LEVEL ? "Error: " : "Warning: ");
                System.out.println(prob.getDescription());
            }
            if (vctx.getErrorCount() > 0 || vctx.getFatalCount() > 0) {
                throw new JiBXException("Errors in binding");
            }
        }
        
        // add all the mapping and format definitions in binding to qualified name maps
        accumulateBindingDefinitions(binding, elemmap, typemap);
        return binding;
    }
    
    /**
     * Run the WSDL generation using command line parameters.
     * 
     * @param args
     * @throws JiBXException
     * @throws IOException
     */
    public static void main(String[] args) throws JiBXException, IOException {
        WsdlGeneratorCommandLine parms = new WsdlGeneratorCommandLine();
        if (args.length > 0 && parms.processArgs(args)) {
            
            // build set of schemas provided on command line
            final Set resolves = new HashSet();
            List errors = ResourceMatcher.matchPaths(new File("."), null, parms.getUseSchemas(),
                new ResourceMatcher.ReportMatch() {
                    public void foundMatch(String path, URL url) {
                        resolves.add(new UrlResolver(path, url));
                    }
                });
            if (errors.size() > 0) {
                for (Iterator iter = errors.iterator(); iter.hasNext();) {
                    System.err.println(iter.next());
                }
                System.exit(1);
            }
            
            // load and validate schemas
            ValidationContext vctx = new ValidationContext();
            ValidationUtils.load(resolves, null, vctx);
            ProblemMultiHandler handler = new ProblemMultiHandler();
            handler.addHandler(new ProblemConsoleLister());
            handler.addHandler(new ProblemLogLister(s_logger));
            if (vctx.reportProblems(handler)) {
                System.exit(2);
            }
            
            // build maps from qualified names to schema holders, and from schema to resolver (necessary since a new
            //  resolver will be set during the WSDL generation processing)
            final Map elemschemas = new HashMap();
            final Map typeschemas = new HashMap();
            final Set exists = new HashSet();
            TreeWalker wlkr = new TreeWalker(null, new SchemaContextTracker());
            for (Iterator iter = resolves.iterator(); iter.hasNext();) {
                SchemaElement schema = vctx.getSchemaById(((ISchemaResolver)iter.next()).getId());
                exists.add(schema);
                final SchemaHolder holder = new SchemaHolder(schema);
                SchemaVisitor visitor = new SchemaVisitor() {
                    
                    public boolean visit(SchemaBase node) {
                        return false;
                    }
                    
                    public boolean visit(ComplexTypeElement node) {
                        typeschemas.put(node.getQName(), holder);
                        return false;
                    }
                    
                    public boolean visit(ElementElement node) {
                        elemschemas.put(node.getQName(), holder);
                        return false;
                    }
                    
                };
                wlkr.walkChildren(schema, visitor);
            }
            
            // build set of binding definitions provided on command line
            final Set bindings = new HashSet();
            errors = ResourceMatcher.matchPaths(new File("."), null, parms.getUseBindings(),
                new ResourceMatcher.ReportMatch() {
                    public void foundMatch(String path, URL url) {
                        bindings.add(url);
                    }
                });
            if (errors.size() > 0) {
                for (Iterator iter = errors.iterator(); iter.hasNext();) {
                    System.err.println(iter.next());
                }
                System.exit(3);
            }
            
            // build maps of type and element mappings from bindings
            Map classelems = new HashMap();
            Map classtypes = new HashMap();
            for (Iterator iter = bindings.iterator(); iter.hasNext();) {
                URL url = (URL)iter.next();
                processPregeneratedBinding(url, classelems, classtypes);
            }
            
            // generate services, bindings, and WSDLs
            Jibx2Wsdl inst = new Jibx2Wsdl(parms);
            ArrayList extras = new ArrayList(parms.getExtraTypes());
            ArrayList classes = parms.getGlobal().getUnmarshalledClasses();
            for (int i = 0; i < classes.size(); i++) {
                ClassCustom clas = (ClassCustom)classes.get(i);
                if (clas.isForceMapping()) {
                    extras.add(clas.getName());
                }
            }
            List wsdls = inst.generate(parms.getExtraArgs(), extras, classelems, elemschemas, classtypes, typeschemas,
                exists);
            if (wsdls != null) {
                
                // write the schemas and WSDLS
                SchemaGen.writeSchemas(parms.getGeneratePath(), inst.m_uriSchemaMap.values());
                WsdlWriter writer = new WsdlWriter();
                for (Iterator iter = wsdls.iterator(); iter.hasNext();) {
                    Definitions def = (Definitions)iter.next();
                    File file = new File(parms.getGeneratePath(), def.getServiceName() + ".wsdl");
                    writer.writeWSDL(def, new FileOutputStream(file));
                }
                
                // find existing schemas referenced (directly or indirectly) from WSDL schemas
                final Set needschemas = new HashSet();
                SchemaVisitor visitor = new SchemaVisitor() {
                    
                    private int m_existsDepth;
                    
                    public void exit(SchemaElement node) {
                        if (exists.contains(node)) {
                            m_existsDepth--;
                        }
                    }

                    public boolean visit(SchemaBase node) {
                        return false;
                    }

                    public boolean visit(SchemaElement node) {
                        if (exists.contains(node)) {
                            m_existsDepth++;
                        }
                        return true;
                    }

                    public boolean visit(SchemaLocationBase node) {
                        SchemaElement schema = node.getReferencedSchema();
                        if (needschemas.contains(schema)) {
                            return false;
                        } else {
                            if (m_existsDepth > 0 || exists.contains(schema)) {
                                needschemas.add(schema);
                            }
                            return true;
                        }
                    }
                    
                };
                for (int i = 0; i < wsdls.size(); i++) {
                    Definitions def = (Definitions)wsdls.get(i);
                    ArrayList schemas = def.getSchemas();
                    for (int j = 0; j < schemas.size(); j++) {
                        wlkr.walkChildren((SchemaBase)schemas.get(0), visitor);
                    }
                }
                
                // copy all referenced schemas to target directory
                byte[] buff = new byte[4096];
                for (Iterator iter = needschemas.iterator(); iter.hasNext();) {
                    SchemaElement schema = (SchemaElement)iter.next();
                    UrlResolver resolver = (UrlResolver)schema.getResolver();
                    InputStream is = resolver.getUrl().openStream();
                    File file = new File(parms.getGeneratePath(), resolver.getName());
                    FileOutputStream os = new FileOutputStream(file);
                    int actual;
                    while ((actual = is.read(buff)) > 0) {
                        os.write(buff, 0, actual);
                    }
                    schema.setResolver(new MemoryResolver(resolver.getName()));
                }
            }
            
        } else {
            if (args.length > 0) {
                System.err.println("Terminating due to command line errors");
            } else {
                parms.printUsage();
            }
            System.exit(1);
        }
    }
}