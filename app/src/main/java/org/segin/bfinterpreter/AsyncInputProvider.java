package org.segin.bfinterpreter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncInputProvider implements InputProvider {
    private final BlockingQueue<Character> queue = new LinkedBlockingQueue<>();

    public void input(char c) {
        queue.offer(c);
    }

    public void input(String s) {
        for (char c : s.toCharArray()) {
            queue.offer(c);
        }
    }

    @Override
    public char read() throws InterruptedException {
        return queue.take();
    }

    @Override
    public boolean hasInput() {
        return !queue.isEmpty();
    }
}
