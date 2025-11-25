package org.segin.bfinterpreter;

public interface InputProvider {
    char read() throws InterruptedException;
    boolean hasInput();
}
