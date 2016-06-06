package net.isger.brick.bus.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import net.isger.brick.Constants;
import net.isger.brick.bus.Decoder;

public class TextDecoder implements Decoder {

    private static final int MIN_CACHE = 64;

    public static final int MAX_LIMIT = Integer.MAX_VALUE - MIN_CACHE;

    private String encoding;

    private int limit;

    private transient int capacity;

    private transient int cache;

    private transient byte[] delimiters;

    public TextDecoder() {
        this(Constants.DEFAULT_ENCODING, TextProtocol.DELIMITER);
    }

    public TextDecoder(String encoding, String delimiter) {
        this.encoding = encoding;
        try {
            delimiters = delimiter.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unsupported encoding ["
                    + encoding + "]");
        }
        int maxLimit = MAX_LIMIT - delimiters.length;
        /* 缓存容量 */
        if (limit < 0) {
            limit = 0;
        } else if (limit > maxLimit) {
            limit = maxLimit;
        }
        capacity = limit + delimiters.length;
        cache = capacity + MIN_CACHE;
    }

    public Object decode(InputStream in) {
        in.mark(0);
        byte[] data = new byte[cache];
        try {
            int size = in.read(data);
            if (size >= capacity) {
                size -= delimiters.length;
                next: for (int i = limit; i <= size; i++) {
                    int j = 0;
                    do {
                        if (data[i + j] != delimiters[j]) {
                            continue next;
                        }
                    } while (++j < delimiters.length);
                    in.reset();
                    in.skip(i + j);
                    return new String(data, 0, i, encoding);
                }
            }
            in.reset();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return null;
    }

}
