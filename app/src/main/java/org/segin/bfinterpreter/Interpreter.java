package org.segin.bfinterpreter;

/**
 * Created by Kirn on 7/27/14.
 */
public class Interpreter {
    private Tape tape;
    private int pc;
    private UserIO io;
    public int[] stack;

    public Interpreter() {
        tape = new Tape();
        pc = 0;
    }

    public void setIO (UserIO io) {
        this.io = io;
    }

    public void run(String code) {
        String ocode = optimize(code);

        for (pc = 0; pc < code.length(); pc++) {
            switch(ocode.charAt(pc)) {
                case '>':
                    tape.forward();
                    break;
                case '<':
                    tape.reverse();
                    break;
                case '+':
                    tape.inc();
                    break;
                case '-':
                    tape.dec();
                    break;
                case ',':
                    tape.set(io.input());
                    break;
                case '.':
                    io.output(tape.get());
                    break;
                case '[':
                    if (tape.get() == 0) {
                        int i = 1;
                        while (i > 0) {
                            char c = ocode.charAt(++pc);
                            if (c == '[')
                                i++;
                            else if (c == ']')
                                i--;
                        }
                    }
                    break;
                case ']':
                    if (tape.get() == 0) {
                        int i = 1;
                        while (i > 0) {
                            char c = ocode.charAt(--pc);
                            if (c == '[')
                                i--;
                            else if (c == ']')
                                i++;
                        }
                    }
                    break;
            }
        }
    }

    private String optimize(String code) {
        return code;
    }


}
