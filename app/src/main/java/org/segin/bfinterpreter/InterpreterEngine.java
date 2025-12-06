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

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class InterpreterEngine {

    public enum State {
        READY,
        RUNNING,
        PAUSED,
        WAITING_FOR_INPUT,
        FINISHED,
        ERROR
    }

    private int pc;
    private int pointer;
    private long cycles;
    private String code;
    private Tape tape;
    private final Map<Integer, Integer> jumpTable;
    private final Map<Integer, Integer> depthMap;
    private volatile State state;
    private InputProvider inputProvider;
    private OutputConsumer outputConsumer;
    private String errorMessage;

    private static final int TAPE_SIZE = 65536;

    public InterpreterEngine() {
        tape = new Tape(TAPE_SIZE);
        jumpTable = new HashMap<>();
        depthMap = new HashMap<>();
        state = State.READY;
    }

    public void setIO(InputProvider inputProvider, OutputConsumer outputConsumer) {
        this.inputProvider = inputProvider;
        this.outputConsumer = outputConsumer;
    }

    public void load(String code) {
        this.code = code;
        reset();
    }

    public void reset() {
        this.pc = 0;
        this.pointer = 0;
        this.cycles = 0;
        this.state = State.READY;
        this.errorMessage = null;
        this.tape = new Tape(TAPE_SIZE); // Reset memory
        if (code != null) {
            computeJumpTableAndDepth();
        }
    }

    private void computeJumpTableAndDepth() {
        jumpTable.clear();
        depthMap.clear();
        Stack<Integer> stack = new Stack<>();
        int currentDepth = 0;

        for (int i = 0; i < code.length(); i++) {
            depthMap.put(i, currentDepth);
            char c = code.charAt(i);
            if (c == '[') {
                stack.push(i);
                currentDepth++;
            } else if (c == ']') {
                if (stack.isEmpty()) {
                    // Unmatched ], ignore or error?
                    // Standard BF often ignores or errors. Let's error during pre-scan for robustness.
                    // Actually, let's just record it.
                } else {
                    int start = stack.pop();
                    jumpTable.put(start, i);
                    jumpTable.put(i, start);
                    currentDepth--;
                }
            }
        }
        // Handle end depth
        depthMap.put(code.length(), currentDepth);
    }

    // Returns true if step executed, false if finished/error/blocked
    public boolean step() throws InterruptedException {
        if (state == State.FINISHED || state == State.ERROR) return false;

        if (pc >= code.length()) {
            state = State.FINISHED;
            return false;
        }

        state = State.RUNNING;
        char command = code.charAt(pc);

        try {
            switch (command) {
                case '>':
                    pointer = (pointer + 1) % TAPE_SIZE;
                    break;
                case '<':
                    pointer = (pointer - 1 + TAPE_SIZE) % TAPE_SIZE;
                    break;
                case '+':
                    tape.set(pointer, (char)((tape.get(pointer) + 1) & 0xFF));
                    break;
                case '-':
                    tape.set(pointer, (char)((tape.get(pointer) - 1) & 0xFF));
                    break;
                case '.':
                    if (outputConsumer != null) {
                        outputConsumer.print(tape.get(pointer));
                    }
                    break;
                case ',':
                    if (inputProvider != null) {
                        state = State.WAITING_FOR_INPUT;
                        char input = inputProvider.read(); // Blocks
                        state = State.RUNNING;
                        tape.set(pointer, (char)(input & 0xFF));
                    } else {
                         tape.set(pointer, (char)0); // EOF default
                    }
                    break;
                case '[':
                    if (tape.get(pointer) == 0) {
                        if (jumpTable.containsKey(pc)) {
                            pc = jumpTable.get(pc);
                        }
                    }
                    break;
                case ']':
                    if (tape.get(pointer) != 0) {
                        if (jumpTable.containsKey(pc)) {
                            pc = jumpTable.get(pc);
                        }
                    }
                    break;
            }
            pc++;
            cycles++;
        } catch (InterruptedException e) {
            state = State.PAUSED;
            throw e;
        } catch (Exception e) {
            state = State.ERROR;
            errorMessage = e.getMessage();
            return false;
        }

        return true;
    }

    public void pause() {
        if (state == State.RUNNING || state == State.WAITING_FOR_INPUT) {
            state = State.PAUSED;
        }
    }

    // Getters
    public int getPc() { return pc; }
    public int getPointer() { return pointer; }
    public long getCycles() { return cycles; }
    public Tape getTape() { return tape; }
    public State getState() { return state; }
    public String getErrorMessage() { return errorMessage; }

    public int getNestingDepth() {
        return depthMap.containsKey(pc) ? depthMap.get(pc) : 0;
    }
}
