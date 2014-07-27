package org.segin.bfinterpreter;

/**
 * Created by Kirn on 7/27/14.
 */
public class Tape {
    private byte[] tape;
    private short position;

    public Tape() {
        tape = new byte[0x10000];
        position = 0;
    }

    public int get() {
        return tape[position];
    }

    public void set(short position) {
        this.position = position;
    }

    public void inc() {
        tape[position]++;
    }

    public void dec() {
        tape[position]--;
    }
}