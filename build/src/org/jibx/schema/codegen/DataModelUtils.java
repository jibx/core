/*
 * Copyright (c) 2008-2010, Dennis M. Sosnoski. All rights reserved.
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Utility methods for working with generated data models.
 */
public class DataModelUtils
{
    /**
     * Get the complete data model.
     *
     * @param directory 
     * @return ordered list of class name-values array pairs
     */
    public static StringObjectPair[] getImage(PackageOrganizer directory) {
        ArrayList packs = directory.getPackages();
        ArrayList classdetails = new ArrayList();
        for (int i = 0; i < packs.size(); i++) {
            StringObjectPair[] classfields = ((PackageHolder)packs.get(i)).getClassFields();
            for (int j = 0; j < classfields.length; j++) {
                classdetails.add(classfields[j]);
            }
        }
        return (StringObjectPair[])classdetails.toArray(new StringObjectPair[classdetails.size()]);
    }
    
    /**
     * Write a complete generated data model.
     *
     * @param directory
     * @param writer
     * @throws IOException
     */
    public static void writeImage(PackageOrganizer directory, BufferedWriter writer) throws IOException {
        ArrayList packs = directory.getPackages();
        for (int i = 0; i < packs.size(); i++) {
            StringObjectPair[] claspairs = ((PackageHolder)packs.get(i)).getClassFields();
            for (int j = 0; j < claspairs.length; j++) {
                StringObjectPair claspair = claspairs[j];
                writer.write(claspair.getKey());
                writer.newLine();
                StringPair[] fields = (StringPair[])claspair.getValue();
                for (int k = 0; k < fields.length; k++) {
                    StringPair field = fields[k];
                    writer.write(' ');
                    writer.write(field.getKey());
                    writer.write(" (");
                    writer.write(field.getValue());
                    writer.write(')');
                    writer.newLine();
                }
            }
        }
    }
    
    /**
     * Read a complete generated data model.
     *
     * @param reader
     * @return ordered list of class name-values array pairs
     * @throws IOException
     */
    public static StringObjectPair[] readImage(BufferedReader reader) throws IOException {
        String line;
        ArrayList clasdetails = new ArrayList();
        ArrayList values = new ArrayList();
        String clasname = null;
        while ((line = reader.readLine()) != null) {
            if (line.charAt(0) == ' ') {
                int split = line.indexOf(' ', 1);
                String name = line.substring(1, split);
                split = line.indexOf('(', split);
                String type = line.substring(split+1, line.length()-1);
                values.add(new StringPair(name, type));
            } else {
                if (clasname != null) {
                    StringPair[] valuepairs = (StringPair[])values.toArray(new StringPair[values.size()]);
                    clasdetails.add(new StringObjectPair(clasname, valuepairs));
                }
                values.clear();
                clasname = line;
            }
        }
        return (StringObjectPair[])clasdetails.toArray(new StringObjectPair[clasdetails.size()]);
    }
    
    /**
     * List the values in a class.
     *
     * @param values
     * @param buff
     */
    private static void listClass(StringPair[] values, StringBuffer buff) {
        for (int i = 0; i < values.length; i++) {
            buff.append(' ');
            buff.append(values[i].getKey());
            buff.append(" of type ");
            buff.append(values[i].getValue());
            buff.append('\n');
        }
    }
    
    /**
     * Find the difference between two class value lists.
     *
     * @param name
     * @param pairs1
     * @param pairs2
     * @param buff
     */
    private static void classDiff(String name, StringPair[] pairs1, StringPair[] pairs2, StringBuffer buff) {
        int index1 = 0;
        int index2 = 0;
        boolean header = true;
        while (true) {
            StringPair pair1 = null;
            StringPair pair2 = null;
            boolean delta = false;
            int diff = -1;
            if (index1 < pairs1.length) {
                pair1 = pairs1[index1];
            }
            if (index2 < pairs2.length) {
                pair2 = pairs2[index2];
                if (pair1 == null) {
                    diff = 1;
                } else {
                    diff = pair1.getKey().compareTo(pair2.getKey());
                    if (diff == 0) {
                        delta = !pair1.getValue().equals(pair2.getValue());
                    }
                }
            } else if (pair1 == null) {
                break;
            }
            if (delta || diff != 0) {
                if (header) {
                    buff.append("Difference(s) for class ");
                    buff.append(name);
                    buff.append(":\n");
                    header = false;
                }
                if (diff < 0) {
                    buff.append(" Missing value '");
                    buff.append(pair1.getKey());
                    buff.append("' of type ");
                    buff.append(pair1.getValue());
                    index1++;
                } else if (diff > 0) {
                    buff.append(" Added value '");
                    buff.append(pair2.getKey());
                    buff.append("' of type ");
                    buff.append(pair2.getValue());
                    index2++;
                } else {
                    buff.append(" Value '");
                    buff.append(pair1.getKey());
                    buff.append("' type was ");
                    buff.append(pair1.getValue());
                    buff.append(", changed to ");
                    buff.append(pair2.getValue());
                }
                buff.append('\n');
            }
            if (diff == 0) {
                index1++;
                index2++;
            }
        }
    }
    
    /**
     * Find the difference between two data model images.
     *
     * @param pairs1 reference data model, as class name-value array pairs
     * @param pairs2 comparison data model, as class name-value array pairs
     * @return comparison text output
     */
    public static String imageDiff(StringObjectPair[] pairs1, StringObjectPair[] pairs2) {
        StringBuffer buff = new StringBuffer();
        int index1 = 0;
        int index2 = 0;
        while (true) {
            StringObjectPair pair1 = null;
            StringObjectPair pair2 = null;
            int diff = -1;
            if (index1 < pairs1.length) {
                pair1 = pairs1[index1];
            }
            if (index2 < pairs2.length) {
                pair2 = pairs2[index2];
                if (pair1 == null) {
                    diff = 1;
                } else {
                    diff = pair1.getKey().compareTo(pair2.getKey());
                }
            } else if (pair1 == null) {
                break;
            }
            if (diff == 0) {
                classDiff(pair1.getKey(), (StringPair[])pair1.getValue(), (StringPair[])pair2.getValue(), buff);
                index1++;
                index2++;
            } else if (diff < 0) {
                buff.append("Second image is missing class " + pair1.getKey() + ":\n");
                listClass((StringPair[])pair1.getValue(), buff);
                index1++;
            } else {
                buff.append("Second image has added class " + pair2.getKey() + ":\n");
                listClass((StringPair[])pair2.getValue(), buff);
                index2++;
            }
        }
        if (buff.length() == 0) {
            return null;
        } else {
            return buff.toString();
        }
    }
}