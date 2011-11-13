package org.jibx.binding.generator;

public class DataClass1
{
    private int m_int;
    private String m_string;
    private boolean m_boolean;
    private DataClass1 m_linked;
    
    public boolean isBoolean() {
        return m_boolean;
    }
    public void setBoolean(boolean b) {
        m_boolean = b;
    }
    public int getInt() {
        return m_int;
    }
    public void setInt(int i) {
        m_int = i;
    }
    public DataClass1 getLinked() {
        return m_linked;
    }
    public void setLinked(DataClass1 linked) {
        m_linked = linked;
    }
    public String getString() {
        return m_string;
    }
    public void setString(String string) {
        m_string = string;
    }
}