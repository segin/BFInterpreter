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

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BFViewModel extends AndroidViewModel {
    private InterpreterEngine engine;
    private AsyncInputProvider asyncInputProvider;
    private StringBuilder outputBuffer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> runningTask;

    // LiveData
    public final MutableLiveData<InterpreterEngine.State> state = new MutableLiveData<>();
    public final MutableLiveData<Integer> pc = new MutableLiveData<>();
    public final MutableLiveData<Integer> pointer = new MutableLiveData<>();
    public final MutableLiveData<Tape> tape = new MutableLiveData<>();
    public final MutableLiveData<String> outputString = new MutableLiveData<>();
    public final MutableLiveData<Long> cycles = new MutableLiveData<>();
    public final MutableLiveData<Integer> depth = new MutableLiveData<>();

    public BFViewModel(@NonNull Application application) {
        super(application);
        engine = new InterpreterEngine();
        outputBuffer = new StringBuilder();
        updateLiveData();
    }

    public void load(final String code, final boolean interactive) {
        pause(); // Ensure we stop previous run

        // Initialize I/O immediately to avoid race condition with input()
        asyncInputProvider = new AsyncInputProvider();
        asyncInputProvider.setInteractive(interactive);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                engine.load(code);
                outputBuffer.setLength(0);
                outputString.postValue("");

                engine.setIO(asyncInputProvider, new OutputConsumer() {
                    @Override
                    public void print(char c) {
                        synchronized (outputBuffer) {
                            outputBuffer.append(c);
                        }
                        outputString.postValue(outputBuffer.toString());
                    }
                });
                updateLiveData();
            }
        });
    }

    public void input(String s) {
        if (asyncInputProvider != null) {
            asyncInputProvider.input(s);
        }
    }

    public void run() {
        if (runningTask != null && !runningTask.isDone()) return;

        runningTask = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted() &&
                           engine.getState() != InterpreterEngine.State.FINISHED &&
                           engine.getState() != InterpreterEngine.State.ERROR &&
                           engine.getState() != InterpreterEngine.State.PAUSED) {

                        // Batch execution for performance
                        long batchStart = System.currentTimeMillis();
                        int steps = 0;
                        while (steps < 1000) {
                            if (!engine.step()) break;
                            steps++;
                        }

                        updateLiveData();

                        // Throttle to ~60fps
                        long elapsed = System.currentTimeMillis() - batchStart;
                        if (elapsed < 16) {
                            Thread.sleep(16 - elapsed);
                        }
                    }
                } catch (InterruptedException e) {
                    // Stopped
                    engine.pause();
                    Thread.currentThread().interrupt();
                } finally {
                    updateLiveData();
                }
            }
        });
    }

    public void step() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    engine.step();
                } catch (InterruptedException e) {
                    // Ignore
                }
                updateLiveData();
            }
        });
    }

    public void pause() {
        engine.pause();
        if (runningTask != null) {
            runningTask.cancel(true); // Interrupt if waiting for input
        }
        updateLiveData();
    }

    public void reset() {
        pause();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                engine.reset();
                outputBuffer.setLength(0);
                outputString.postValue("");
                updateLiveData();
            }
        });
    }

    public void setMemory(final int address, final char value) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                if (engine.getTape() != null) {
                    engine.getTape().set(address, value);
                    updateLiveData();
                }
            }
        });
    }

    private void updateLiveData() {
        state.postValue(engine.getState());
        pc.postValue(engine.getPc());
        pointer.postValue(engine.getPointer());
        tape.postValue(engine.getTape());
        cycles.postValue(engine.getCycles());
        depth.postValue(engine.getNestingDepth());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }

    public InterpreterEngine getEngine() { return engine; }
}
