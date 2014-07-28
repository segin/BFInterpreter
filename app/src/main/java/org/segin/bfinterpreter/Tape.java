package org.segin.bfinterpreter;

/**
 * Created by Kirn on 7/27/14.
 */
public class Tape {
    private char[] tape;
    private int position;

    public Tape() {
        tape = new char[0x10000];
        position = 0;
    }

    public char get() {
        return tape[position];
    }

    public void set(char value) {
        tape[position] = value;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void inc() {
        if (tape[position] >= 255) {
            tape[position] = 0;
        } else {
            tape[position]++;
        }
    }

    public void dec() {
        if (tape[position] == 0) {
            tape[position] = 255;
        } else {
            tape[position]++;
        }
    }

    public void forward() {
        if (position >= 65535) {
            position = 0;
        } else {
            position++;
        }
    }

    public void reverse() {
        if (position <= 0) {
            position = 65535;
        } else {
            position++;
        }
    }
}