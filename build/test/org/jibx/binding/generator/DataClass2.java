package org.jibx.binding.generator;

import java.util.List;

public class DataClass2
{
    private transient int m_transient;
    private static int m_static;
    private List m_dataClass1s;
    private transient int m_nonstandard;
    
    public static int getStatic() {
        return m_static;
    }
    public static void setStatic(int stat) {
        m_static = stat;
    }
    public List getDataClass1s() {
        return m_dataClass1s;
    }
    public void setDataClass1s(List dataClass1s) {
        m_dataClass1s = dataClass1s;
    }
    public int getTransient() {
        return m_transient;
    }
    public void setTransient(int trans) {
        m_transient = trans;
    }
    public int getValue() {
        return m_nonstandard;
    }
    public void setNonstandardValue(int value) {
        m_nonstandard = value;
    }
}