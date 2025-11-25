package org.segin.bfinterpreter;

public class Tape {
    private final char[] memory;
    private int size;

    public Tape(int size) {
        this.size = size;
        this.memory = new char[size];
    }

    public char get(int index) {
        if (index >= 0 && index < size) {
            return memory[index];
        }
        return 0;
    }

    public void set(int index, char value) {
        if (index >= 0 && index < size) {
            memory[index] = value;
        }
    }

    public int getSize() {
        return size;
    }
}
