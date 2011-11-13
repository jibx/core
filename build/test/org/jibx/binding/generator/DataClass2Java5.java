package org.jibx.binding.generator;

import java.util.List;

public class DataClass2Java5
{
    private transient int m_transient;
    private static int m_static;
    private List<DataClass1> m_dataClass1s;
    
    public static int getStatic() {
        return m_static;
    }
    public static void setStatic(int stat) {
        m_static = stat;
    }
    public List<DataClass1> getDataClass1s() {
        return m_dataClass1s;
    }
    public void setDataClass1s(List<DataClass1> dataClass1s) {
        m_dataClass1s = dataClass1s;
    }
    public int getTransient() {
        return m_transient;
    }
    public void setTransient(int trans) {
        m_transient = trans;
    }
}