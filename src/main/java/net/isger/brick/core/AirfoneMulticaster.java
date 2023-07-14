package net.isger.brick.core;

public class AirfoneMulticaster implements Airfone {

    private Airfone prev;

    private Airfone next;

    protected AirfoneMulticaster(Airfone prev, Airfone next) {
        this.prev = prev;
        this.next = next;
    }

    public boolean ack(int action) {
        return prev.ack(action) && next.ack(action);
    }

    public static Airfone add(Airfone prev, Airfone next) {
        if (prev == null)
            return next;
        if (next == null)
            return prev;
        return new AirfoneMulticaster(prev, next);
    }

    public static Airfone remove(Airfone prev, Airfone next) {
        if (prev == next || prev == null) {
            return null;
        } else if (prev instanceof AirfoneMulticaster) {
            return ((AirfoneMulticaster) prev).remove(next);
        } else {
            return prev;
        }
    }

    protected Airfone remove(Airfone airfone) {
        if (airfone == prev)
            return next;
        if (airfone == next)
            return prev;
        Airfone a = remove(prev, airfone);
        Airfone b = remove(next, airfone);
        if (a == prev && b == next) {
            return this;
        }
        return add(a, b);
    }

}
