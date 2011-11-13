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

package org.jibx.binding.model;

/**
 * Built-in &lt;format> definitions.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class BuiltinFormats
{
    /** Default format definitions. */
    public static final FormatElement[] s_builtinFormats = {
        buildFormat("byte.default", "byte", true, "org.jibx.runtime.Utility.serializeByte",
            "org.jibx.runtime.Utility.parseByte", "0"),
        buildFormat("char.default", "char", true, "org.jibx.runtime.Utility.serializeChar",
            "org.jibx.runtime.Utility.parseChar", "0"),
        buildFormat("double.default", "double", true, "org.jibx.runtime.Utility.serializeDouble",
            "org.jibx.runtime.Utility.parseDouble", "0.0"),
        buildFormat("float.default", "float", true, "org.jibx.runtime.Utility.serializeFloat",
            "org.jibx.runtime.Utility.parseFloat", "0.0"),
        buildFormat("int.default", "int", true, "org.jibx.runtime.Utility.serializeInt",
            "org.jibx.runtime.Utility.parseInt", "0"),
        buildFormat("long.default", "long", true, "org.jibx.runtime.Utility.serializeLong",
            "org.jibx.runtime.Utility.parseLong", "0"),
        buildFormat("short.default", "short", true, "org.jibx.runtime.Utility.serializeShort",
            "org.jibx.runtime.Utility.parseShort", "0"),
        buildFormat("boolean.default", "boolean", true, "org.jibx.runtime.Utility.serializeBoolean",
            "org.jibx.runtime.Utility.parseBoolean", "false"),
        buildFormat("Date.default", "java.util.Date", true, "org.jibx.runtime.Utility.serializeDateTime",
            "org.jibx.runtime.Utility.deserializeDateTime", null),
//#!j2me{
        buildFormat("SqlDate.default", "java.sql.Date", true, "org.jibx.runtime.Utility.serializeSqlDate",
            "org.jibx.runtime.Utility.deserializeSqlDate", null),
        buildFormat("SqlTime.default", "java.sql.Time", true, "org.jibx.runtime.Utility.serializeSqlTime",
            "org.jibx.runtime.Utility.deserializeSqlTime", null),
        buildFormat("SqlTimestamp.default", "java.sql.Timestamp", true, "org.jibx.runtime.Utility.serializeTimestamp",
            "org.jibx.runtime.Utility.deserializeTimestamp", null),
        buildFormat("LocalDate.default", "org.joda.time.LocalDate", true,
            "org.jibx.runtime.JodaConvert.serializeLocalDate",
            "org.jibx.runtime.JodaConvert.deserializeLocalDate", null),
        buildFormat("DateMidnight.zoned", "org.joda.time.DateMidnight", false,
            "org.jibx.runtime.JodaConvert.serializeZonedDateMidnight",
            "org.jibx.runtime.JodaConvert.deserializeZonedDateMidnight", null),
        buildFormat("DateMidnight.local", "org.joda.time.DateMidnight", true,
            "org.jibx.runtime.JodaConvert.serializeUnzonedDateMidnight",
            "org.jibx.runtime.JodaConvert.deserializeLocalDateMidnight", null),
        buildFormat("DateMidnight.UTC", "org.joda.time.DateMidnight", false,
            "org.jibx.runtime.JodaConvert.serializeUTCDateMidnight",
            "org.jibx.runtime.JodaConvert.deserializeUTCDateMidnight", null),
        buildFormat("LocalTime.local", "org.joda.time.LocalTime", true,
            "org.jibx.runtime.JodaConvert.serializeUnzonedLocalTime",
            "org.jibx.runtime.JodaConvert.deserializeLocalTime", null),
        buildFormat("LocalTime.UTC", "org.joda.time.LocalTime", false,
            "org.jibx.runtime.JodaConvert.serializeUTCLocalTime",
            "org.jibx.runtime.JodaConvert.deserializeLocalTime", null),
        buildFormat("DateTime.zoned", "org.joda.time.DateTime", false,
            "org.jibx.runtime.JodaConvert.serializeZonedDateTime",
            "org.jibx.runtime.JodaConvert.deserializeZonedDateTime", null),
        buildFormat("DateTime.UTC", "org.joda.time.DateTime", false,
            "org.jibx.runtime.JodaConvert.serializeUTCDateTime",
            "org.jibx.runtime.JodaConvert.deserializeUTCDateTime", null),
        buildFormat("DateTime.local", "org.joda.time.DateTime", true,
            "org.jibx.runtime.JodaConvert.serializeZonedDateTime",
            "org.jibx.runtime.JodaConvert.deserializeLocalDateTime", null),
        buildFormat("DateTime.strict-local", "org.joda.time.DateTime", false,
            "org.jibx.runtime.JodaConvert.serializeZonedDateTime",
            "org.jibx.runtime.JodaConvert.deserializeStrictLocalDateTime", null),
        buildFormat("DateTime.strict-UTC", "org.joda.time.DateTime", false,
            "org.jibx.runtime.JodaConvert.serializeUTCDateTime",
            "org.jibx.runtime.JodaConvert.deserializeStrictUTCDateTime", null),
//#j2me}
        buildFormat("byte-array.default", "byte[]", true, "org.jibx.runtime.Utility.serializeBase64",
            "org.jibx.runtime.Utility.deserializeBase64", null),
        buildFormat("QName.default", "org.jibx.runtime.QName", true, "org.jibx.runtime.QName.serialize", 
            "org.jibx.runtime.QName.deserialize", null),
        buildFormat("String.default", "java.lang.String", true, null, null, null),
        buildFormat("Object.default", "java.lang.Object", true, null, null, null)
    };
    
    /**
     * Default format builder.
     *
     * @param name
     * @param type
     * @param use
     * @param sname
     * @param dname
     * @param dflt
     * @return constructed format
     */
    private static FormatElement buildFormat(String name, String type, boolean use, String sname, String dname,
        String dflt) {
        FormatElement format = new FormatElement();
        format.setLabel(name);
        format.setTypeName(type);
        format.setDefaultFormat(use);
        format.setSerializerName(sname);
        format.setDeserializerName(dname);
        format.setDefaultText(dflt);
        return format;
    }
}