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

public class Tape {
    private char[] tape;
    private int position;

    /* User-defined tape size */
    private int usize;

    /* You may change this to implement larger tapes. */
    final static int size = 0x10000;

    public Tape() {
        usize = size;
        initTape();
    }

    public Tape(int usize) {
        if((this.usize = usize) == 0)
            this.usize = size;
        initTape();
    }

    private void initTape() {
        tape = new char[usize];
        position = 0;
    }

    public char get() {
        return tape[position];
    }

    public void set(char value) {
        tape[position] = value;
    }

    public void inc() {
        if (tape[position] >= 255) {
            tape[position] = 0;
        } else {
            tape[position]++;
        }
    }

    public void dec() {
        if (tape[position] <= 0) {
            tape[position] = 255;
        } else {
            tape[position]--;
        }
    }

    public void forward() {
        if (position >= (usize - 1)) {
            position = 0;
        } else {
            position++;
        }
    }

    public void reverse() {
        if (position <= 0) {
            position = (usize - 1);
        } else {
            position--;
        }
    }
}
