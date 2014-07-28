package org.segin.bfinterpreter;

/*
 * Copyright 2014 Kirn Gill II <segin2005@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Interpreter {
    private Tape tape;
    private int pc;
    private UserIO io;

    public Interpreter() {
        tape = new Tape();
        pc = 0;
    }

    public void setIO (UserIO io) {
        this.io = io;
    }

    public void run(String code) {
        String ocode = optimize(code);

        for (pc = 0; pc < ocode.length(); pc++)  {
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
                            ++pc;
                            char c = ocode.charAt(pc);
                            if (c == '[')
                                i++;
                            else if (c == ']')
                                i--;
                        }
                    }
                    break;
                case ']':
                    if (tape.get() != 0) {
                        int i = 1;
                        while (i > 0) {
                            --pc;
                            char c = ocode.charAt(pc);
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
        String ocode = "";
        for (pc = 0; pc < code.length(); pc++)
            switch (code.charAt(pc)) {
                case '>':
                case '<':
                case ',':
                case '.':
                case '+':
                case '-':
                case '[':
                case ']':
                    ocode += String.valueOf(code.charAt(pc));
                    break;
            }
        return ocode;
    }


}
