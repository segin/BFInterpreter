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
