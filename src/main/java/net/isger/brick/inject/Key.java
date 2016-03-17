package net.isger.brick.inject;

import net.isger.util.Asserts;

/**
 * 实例键
 * 
 * @author issing
 * @param <T>
 */
public class Key<T> {

    final Class<T> type;

    final String name;

    final int hashCode;

    private Key(Class<T> type, String name) {
        Asserts.isNotNull(type, "The key type not be null");
        Asserts.isNotEmpty(name, "The key name not be null or empty");
        this.type = type;
        this.name = name;
        this.hashCode = type.hashCode() << 8 + name.hashCode() & 0xFF;
    }

    public static <T> Key<T> newInstance(Class<T> type, String name) {
        return new Key<T>(type, name);
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Key)) {
            return false;
        } else if (o == this) {
            return true;
        }
        Key<?> key = (Key<?>) o;
        return name.equals(key.name) && type.equals(key.type);
    }

}
