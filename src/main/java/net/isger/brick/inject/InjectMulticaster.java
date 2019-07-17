package net.isger.brick.inject;

public class InjectMulticaster implements InjectReserver {

    protected final InjectReserver a, b;

    protected InjectMulticaster(InjectReserver a, InjectReserver b) {
        this.a = a;
        this.b = b;
    }

    protected InjectReserver remove(InjectReserver oldl) {
        if (oldl == a)
            return b;
        if (oldl == b)
            return a;
        InjectReserver a2 = removeInternal(a, oldl);
        InjectReserver b2 = removeInternal(b, oldl);
        if (a2 == a && b2 == b) {
            return this;
        }
        return addInternal(a2, b2);
    }

    protected static InjectReserver addInternal(InjectReserver a, InjectReserver b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        return new InjectMulticaster(a, b);
    }

    protected static InjectReserver removeInternal(InjectReserver l, InjectReserver oldl) {
        if (l == oldl || l == null) {
            return null;
        } else if (l instanceof InjectMulticaster) {
            return ((InjectMulticaster) l).remove(oldl);
        } else {
            return l;
        }
    }

    public boolean contains(Key<?> key) {
        return a.contains(key) || b.contains(key);
    }

    public <T> T alternate(Key<T> key, InjectConductor conductor) {
        T instance = a.alternate(key, conductor);
        if (instance == null) {
            instance = b.alternate(key, conductor);
        }
        return instance;
    }

}
