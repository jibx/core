
public class DefaultPackageClass
{
    private int m_int;
    private char m_char;
    private Character m_character;
    private boolean m_boolean;
    private DefaultPackageClass m_linked;
    
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
    public DefaultPackageClass getLinked() {
        return m_linked;
    }
    public void setLinked(DefaultPackageClass linked) {
        m_linked = linked;
    }
    public char getChar() {
        return m_char;
    }
    public void setChar(char chr) {
        m_char = chr;
    }
    public Character getCharacter() {
        return m_character;
    }
    public void setCharacter(Character chr) {
        m_character = chr;
    }
}
