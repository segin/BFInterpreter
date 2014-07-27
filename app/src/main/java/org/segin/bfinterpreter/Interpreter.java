package org.segin.bfinterpreter;

/**
 * Created by Kirn on 7/27/14.
 */
public class Interpreter {
    private Tape tape;
    private int pc;

    public Interpreter() {
        tape = new Tape();
        pc = 0;
    }
}
