package net.isger.brick.bus.protocol;

import net.isger.util.Decoder;
import net.isger.util.Encoder;
import net.isger.util.anno.Alias;

@Alias("command.socket")
public class CommandSocketProtocol implements SocketProtocol {

    private volatile transient Status status;

    private CommandSocketEncoder encoder;

    private CommandSocketDecoder decoder;

    public CommandSocketProtocol() {
        this.status = Status.UNINITIALIZED;
    }

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
            this.encoder = new CommandSocketEncoder();
        }
        if (this.decoder == null) {
            this.decoder = new CommandSocketDecoder();
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
