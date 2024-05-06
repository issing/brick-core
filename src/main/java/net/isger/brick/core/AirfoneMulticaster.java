package net.isger.brick.core;

public class AirfoneMulticaster implements Airfone {

    private Airfone prev;

    private Airfone next;

    protected AirfoneMulticaster(Airfone prev, Airfone next) {
        this.prev = prev;
        this.next = next;
    }

    public boolean ack(int action) {
        return this.prev.ack(action) && this.next.ack(action);
    }

    public static Airfone add(Airfone prev, Airfone next) {
        if (prev == null) return next;
        if (next == null) return prev;
        return new AirfoneMulticaster(prev, next);
    }

    public static Airfone remove(Airfone prev, Airfone next) {
        Airfone airfone;
        if (prev == next || prev == null) airfone = null;
        else if (prev instanceof AirfoneMulticaster) airfone = ((AirfoneMulticaster) prev).remove(next);
        else airfone = prev;
        return airfone;
    }

    protected Airfone remove(Airfone airfone) {
        if (airfone == this.prev) return this.next;
        if (airfone == this.next) return this.prev;
        Airfone a = remove(this.prev, airfone);
        Airfone b = remove(this.next, airfone);
        if (a == this.prev && b == this.next) return this;
        return add(a, b);
    }

}
