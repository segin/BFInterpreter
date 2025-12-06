# Action Plan - Modernize BFInterpreter

## 1. Project Setup & Modernization
- **Goal:** Update the build system and dependencies to support modern Android standards (Play Store compliance).
- **Actions:**
  - Update `build.gradle` (Project & Module) to use AGP 8.9.2 and Gradle 8.14.
  - Set `minSdkVersion` to 19 (KitKat) to support `androidx.appcompat` and `lifecycle` libraries.
  - Set `targetSdkVersion` and `compileSdkVersion` to 34 (Android 14).
  - Add AndroidX dependencies: `appcompat`, `cardview`, `lifecycle-viewmodel`, `lifecycle-livedata`.

## 2. Core Engine Implementation
- **Goal:** Port the reference `bf-interpreter.html` (Polymer/JS) logic to Java, ensuring 1:1 behavior compatibility.
- **Actions:**
  - Create `InterpreterEngine` class:
    - Implement `step()`, `run()`, `reset()`, `pause()`.
    - Implement `Tape` handling (65536 cells, 8-bit wrapping).
    - Pre-calculate jump tables (`[` and `]`) and nesting depth map for performance.
    - Track `cycles` and `pointer`.
  - Create `Tape` class:
    - Encapsulate `char[]` (used as unsigned byte).
    - Provide `get/set` with bounds checking.
  - Create `InputProvider` (Async) and `OutputConsumer` interfaces.

## 3. Architecture & Threading
- **Goal:** Move execution off the main thread to prevent ANRs and allow responsive UI.
- **Actions:**
  - Implement `BFViewModel` (MVVM pattern):
    - Manage `InterpreterEngine` instance.
    - Use `ExecutorService` (Single Thread) for background execution.
    - Handle `Future` objects to support cancellation (Pause/Reset).
    - Expose state via `LiveData` (`pc`, `pointer`, `tape`, `output`, `cycles`, `depth`).
  - Implement `AsyncInputProvider`:
    - Use `BlockingQueue<Character>` to allow the engine to block on input without freezing the thread.

## 4. Visual Debugger UI
- **Goal:** Create a feature-rich debugger matching the Polymer design.
- **Actions:**
  - Create `DebuggerActivity`:
    - **Layout:** Vertical stack (Mobile friendly).
    - **Source Code:** Scrollable text view with active instruction highlighting.
    - **Registers:** Display PC, Pointer, Cycle Count, Nesting Depth.
    - **Memory Viewer:**
      - Implement `HexDumpAdapter` using `RecyclerView` to render memory efficiently.
      - Format: `Offset (Hex) | 16 Bytes (Hex) | ASCII`.
      - Highlight active byte at `pointer`.
      - Support click-to-edit for memory cells.
      - Implement "Follow Pointer" logic.
      - **Optimization:** Use `RecyclerView` view recycling to prevent layout thrashing on updates.
    - **I/O:** Console for Output and Input field.
    - **Controls:** Step, Run/Pause, Reset buttons.

## 5. Main Activity Integration
- **Goal:** Preserve legacy functionality while exposing new features.
- **Actions:**
  - Update `BFInterpreter` (Legacy Activity):
    - Add "Debug" option to the Action Bar menu.
    - Pass current Code and Input to `DebuggerActivity` via Intent.

## 6. Verification & Polish
- **Actions:**
  - Verify Threading safety (Volatile flags, Interrupt handling).
  - Verify Input blocking behavior.
  - Optimize UI updates (throttle to ~60fps).
