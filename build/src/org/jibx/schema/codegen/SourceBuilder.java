/*
 * Copyright (c) 2007-2010, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.schema.codegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditVisitor;

/**
 * Abstract syntax tree builder. This wraps the AST with convenience methods and added control information.
 */
public class SourceBuilder
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(SourceBuilder.class.getName());
    
    /** Map from primitive type name to type code. */
    private static final Map s_primitiveTypeCodes;
    static {
        s_primitiveTypeCodes = new HashMap();
        s_primitiveTypeCodes.put("boolean", PrimitiveType.BOOLEAN);
        s_primitiveTypeCodes.put("byte", PrimitiveType.BYTE);
        s_primitiveTypeCodes.put("char", PrimitiveType.CHAR);
        s_primitiveTypeCodes.put("double", PrimitiveType.DOUBLE);
        s_primitiveTypeCodes.put("float", PrimitiveType.FLOAT);
        s_primitiveTypeCodes.put("int", PrimitiveType.INT);
        s_primitiveTypeCodes.put("long", PrimitiveType.LONG);
        s_primitiveTypeCodes.put("short", PrimitiveType.SHORT);
        s_primitiveTypeCodes.put("void", PrimitiveType.VOID);
    }
    
    /** Actual AST instance. */
    private final AST m_ast;
    
    /** Package containing this source. */
    private final PackageHolder m_package;
    
    /** Name of this source. */
    private final String m_name;
    
    /** Compilation unit. */
    private final CompilationUnit m_compilationUnit;
    
    /** Tracker for imports. */
    protected final ImportsTracker m_importsTracker;
    
    /** Builders for main classes in file. */
    private ArrayList m_classes;
    
    /**
     * Constructor.
     * 
     * @param ast 
     * @param pack
     * @param name 
     * @param imports
     */
    public SourceBuilder(AST ast, PackageHolder pack, String name, ImportsTracker imports) {
        
        // initialize basic parameters
        m_ast = ast;
        m_package = pack;
        m_name = name;
        m_importsTracker = imports;
        m_classes = new ArrayList();
        
        // create file in appropriate package
        m_compilationUnit = ast.newCompilationUnit();
        String pname = pack.getName();
        if (pname.length() > 0) {
            PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
            packageDeclaration.setName(ast.newName(pname));
            m_compilationUnit.setPackage(packageDeclaration);
        }
    }
    
    /**
     * AST access for related classes.
     *
     * @return AST
     */
    AST getAST() {
        return m_ast;
    }
    
    /**
     * Get the name of the package containing this source file.
     *
     * @return name
     */
    public String getPackageName() {
        return m_package.getName();
    }

    /**
     * Create a type declaration.
     *
     * @param cname class name
     * @param isenum Java 5 enum class flag
     * @return type declaration
     */
    private AbstractTypeDeclaration createClass(String cname, boolean isenum) {
        AbstractTypeDeclaration abstype;
        if (isenum) {
            abstype = m_ast.newEnumDeclaration();
        } else {
            TypeDeclaration type = m_ast.newTypeDeclaration();
            type.setInterface(false);
            abstype = type;
        }
        abstype.modifiers().add(m_ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        abstype.setName(m_ast.newSimpleName(cname));
        return abstype;
    }
    
    /**
     * Add a new main class to the file.
     *
     * @param cname class name
     * @param isenum Java 5 enum class flag
     * @return builder
     */
    public ClassBuilder newMainClass(String cname, boolean isenum) {
        AbstractTypeDeclaration decl = createClass(cname, isenum);
        m_compilationUnit.types().add(decl);
        ClassBuilder builder = new ClassBuilder(decl, this);
        m_classes.add(builder);
        return builder;
    }
    
    /**
     * Add a new inner class to the file.
     *
     * @param cname class name
     * @param outer containing class builder
     * @param isenum Java 5 enum class flag
     * @return builder
     */
    public ClassBuilder newInnerClass(String cname, ClassBuilder outer, boolean isenum) {
        AbstractTypeDeclaration decl = createClass(cname, isenum);
        decl.modifiers().add(m_ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
//        type.superInterfaceTypes().add(createType("java.io.Serializable"));
        return new ClassBuilder(decl, outer);
    }

    /**
     * Create type name.
     *
     * @param type fully-qualified type name
     * @return name
     */
    protected Name createTypeName(String type) {
        String name = m_importsTracker.getName(type);
        if (name.indexOf('.') > 0) {
            return m_ast.newName(name);
        } else {
            return m_ast.newSimpleName(name);
        }
    }
    
    /**
     * Create type definition. This uses the supplied type name, which may include array suffixes and/or type
     * parameters, to construct the actual type definition.
     *
     * @param type fully qualified type name, or primitive type name
     * @return constructed typed definition
     */
    public Type createType(String type) {
        
        // start by stripping off any array suffixes
        int arraydepth = 0;
        while (type.endsWith("[]")) {
            arraydepth++;
            type = type.substring(0, type.length()-2);
        }
        
        // next strip off any type parameters
        String typeparms = null;
        int split = type.indexOf('<');
        if (split > 0) {
            typeparms = type.substring(split+1, type.length()-1);
            type = type.substring(0, split);
        }
        
        // handle the basic type creation
        Type tdef;
        Code code = (Code)s_primitiveTypeCodes.get(type);
        if (code == null) {
            tdef = getAST().newSimpleType(createTypeName(type));
        } else {
            tdef = getAST().newPrimitiveType(code);
        }
        
        // add type parameter information
        if (typeparms != null) {
            ParameterizedType ptype = getAST().newParameterizedType(tdef);
            while ((split = typeparms.indexOf(',')) > 0) {
                ptype.typeArguments().add(createType(typeparms.substring(0, split)));
                typeparms = typeparms.substring(split+1);
            }
            ptype.typeArguments().add(createType(typeparms));
            tdef = ptype;
        }
        
        // add any layers of arrays
        if (arraydepth > 0) {
            tdef = getAST().newArrayType(tdef, arraydepth);
        }
        return tdef;
    }
    
    /**
     * Create a parameterized type.
     *
     * @param type fully qualified type name
     * @param param fully qualified parameter type name
     * @return type
     */
    public Type createParameterizedType(String type, String param) {
        ParameterizedType ptype = getAST().newParameterizedType(createType(type));
        ptype.typeArguments().add(createType(param));
        return ptype;
    }
    
    /**
     * Generate the actual source file.
     * 
     * @param verbose
     */
    public void finish(boolean verbose) {
        
        // finish building all the classes in this source
        long start = System.currentTimeMillis();
        for (int i = 0; i < m_classes.size(); i++) {
            ClassBuilder builder = (ClassBuilder)m_classes.get(i);
            builder.finish();
        }
        
        // add all imports to source
        List imports = m_importsTracker.freeze(m_name);
        for (Iterator iter = imports.iterator(); iter.hasNext();) {
            String type = (String)iter.next();
            ImportDeclaration imp = m_ast.newImportDeclaration();
            imp.setName(m_ast.newName(type));
            m_compilationUnit.imports().add(imp);
        }
        
        // convert generated AST to text document
        Map options = new HashMap();
        options.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "80");
        options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "80");
        options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_SOURCE, "true");
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
        options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AFTER_PACKAGE, "1");
        options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AFTER_IMPORTS, "1");
        options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_PACKAGE, "1");
        options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_IMPORTS, "1");
        options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD, "1");
        options.put(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION,
            DefaultCodeFormatterConstants.NEXT_LINE);
        options.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_SUPERINTERFACES_IN_TYPE_DECLARATION,
            DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT,
            DefaultCodeFormatterConstants.INDENT_BY_ONE));
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
        Document doc = new Document(m_compilationUnit.toString());
        CodeFormatter fmtr = ToolFactory.createCodeFormatter(options);
        String text = doc.get();
        TextEdit edits = fmtr.format(CodeFormatter.K_COMPILATION_UNIT, text, 0, text.length(), 0, null);
        File gendir = m_package.getGenerateDirectory();
        if (gendir != null) {
            try {
                File file = new File(gendir, m_name + ".java");
                FileWriter fwrit = new FileWriter(file);
                WriterVisitor visitor = new WriterVisitor(text, fwrit);
                edits.accept(visitor);
                visitor.finish();
                fwrit.flush();
                fwrit.close();
                s_logger.info("Generated class file " + file.getCanonicalPath() + " (writing AST took " + (System.currentTimeMillis()-start) + " ms.)");
            } catch (IOException e) {
                throw new IllegalStateException("Error in source generation: " + e.getMessage());
            }
        }
    }
    
    /**
     * Visitor to apply edits. This is used to avoid the overhead of standard document processing of the edits generated
     * by formatting.
     */
    private static class WriterVisitor extends TextEditVisitor
    {
        private final String m_base;
        private final Writer m_writer;
        private int m_offset;
        
        /**
         * Constructor.
         * 
         * @param base
         * @param writer
         */
        public WriterVisitor(String base, Writer writer) {
            m_base = base;
            m_writer = writer;
        }
        
        private void skip(int offset) {
            if (offset > m_offset) {
                m_offset = offset;
            } else if (offset < m_offset) {
                throw new IllegalStateException();
            }
        }
        
        private void copy(int offset) {
            if (offset > m_offset) {
                try {
                    m_writer.write(m_base, m_offset, offset-m_offset);
                } catch (IOException e) {
                    throw new RuntimeException("Error writing to file", e);
                }
                m_offset = offset;
            } else if (offset < m_offset) {
                throw new IllegalStateException();
            }
        }
        
        public boolean visit(DeleteEdit edit) {
            copy(edit.getOffset());
            skip(edit.getOffset()+edit.getLength());
            return super.visit(edit);
        }

        public boolean visit(InsertEdit edit) {
            copy(edit.getOffset());
            try {
                m_writer.write(edit.getText());
            } catch (IOException e) {
                throw new RuntimeException("Error writing to file", e);
            }
            return super.visit(edit);
        }

        public boolean visit(ReplaceEdit edit) {
            copy(edit.getOffset());
            skip(edit.getOffset()+edit.getLength());
            try {
                m_writer.write(edit.getText());
            } catch (IOException e) {
                throw new RuntimeException("Error writing to file", e);
            }
            return super.visit(edit);
        }
        
        /**
         * Finish writing output. This needs to be called after visiting the tree, to catch any final bits at the end.
         */
        public void finish() {
            copy(m_base.length());
        }
    }
}