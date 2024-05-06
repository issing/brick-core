package net.isger.brick.inject;

public class InjectMulticaster implements InjectReserver {

    protected final InjectReserver a, b;

    InjectMulticaster(InjectReserver a, InjectReserver b) {
        this.a = a;
        this.b = b;
    }

    protected InjectReserver remove(InjectReserver oldl) {
        if (oldl == this.a) return this.b;
        if (oldl == this.b) return this.a;
        InjectReserver a2 = removeInternal(this.a, oldl);
        InjectReserver b2 = removeInternal(this.b, oldl);
        if (a2 == this.a && b2 == this.b) {
            return this;
        }
        return addInternal(a2, b2);
    }

    protected static InjectReserver addInternal(InjectReserver a, InjectReserver b) {
        if (a == null) return b;
        if (b == null) return a;
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
        return this.a.contains(key) || this.b.contains(key);
    }

    public <T> T alternate(Key<T> key) {
        T instance = this.a.alternate(key);
        if (instance == null) {
            instance = this.b.alternate(key);
        }
        return instance;
    }

}
