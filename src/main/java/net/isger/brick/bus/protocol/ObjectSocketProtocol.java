package net.isger.brick.bus.protocol;

import net.isger.util.Decoder;
import net.isger.util.Encoder;
import net.isger.util.anno.Alias;

@Alias("object.socket")
public class ObjectSocketProtocol implements SocketProtocol {

    private volatile transient Status status;

    private transient ObjectSocketEncoder encoder;

    private transient ObjectSocketDecoder decoder;

    public boolean hasReady() {
        return this.status == Status.INITIALIZED;
    }

    public Status getStatus() {
        return this.status;
    }

    public synchronized void initial() {
        if (!(this.status == Status.UNINITIALIZED || this.status == Status.DESTROYED)) return;
        this.status = Status.INITIALIZING;
        if (this.encoder == null) {
            this.encoder = new ObjectSocketEncoder();
        }
        if (this.decoder == null) {
            this.decoder = new ObjectSocketDecoder();
        }
        this.status = Status.INITIALIZED;
    }

    public Encoder getEncoder() {
        return this.encoder;
    }

    public Decoder getDecoder() {
        return this.decoder;
    }

    public synchronized void destroy() {
        if (this.status == Status.UNINITIALIZED || this.status == Status.DESTROYED) return;
        this.status = Status.DESTROYED;
    }

}
