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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncInputProvider implements InputProvider {
    private final BlockingQueue<Character> queue = new LinkedBlockingQueue<>();
    private volatile boolean interactive = true;

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

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
        if (!interactive && queue.isEmpty()) {
            return 0; // Legacy behavior: EOF returns 0 immediately if non-interactive
        }
        return queue.take();
    }

    @Override
    public boolean hasInput() {
        return !queue.isEmpty();
    }
}
