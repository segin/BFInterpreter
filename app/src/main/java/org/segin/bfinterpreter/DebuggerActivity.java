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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CheckBox;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DebuggerActivity extends AppCompatActivity implements HexDumpAdapter.OnByteClickListener {

    private BFViewModel viewModel;
    private TextView codeText;
    private TextView outputText;
    private TextView regPC;
    private TextView regPtr;
    private TextView regCycles;
    private TextView regDepth;
    private RecyclerView hexRecyclerView;
    private CheckBox checkFollowPointer;
    private EditText inputText;
    private Button btnRun;
    private Button btnStep;
    private Button btnReset;
    private Button btnSendInput;

    private String sourceCode;
    private HexDumpAdapter hexDumpAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debugger);

        // Get Intent Data
        sourceCode = getIntent().getStringExtra("CODE");
        String inputData = getIntent().getStringExtra("INPUT");

        // Init ViewModel
        viewModel = new ViewModelProvider(this).get(BFViewModel.class);
        if (savedInstanceState == null) {
            viewModel.load(sourceCode, true); // Debugger: interactive
            if (inputData != null && !inputData.isEmpty()) {
                viewModel.input(inputData);
            }
        } else {
             // If recreating, ensure engine has code? ViewModel retains state, so engine should be fine.
             // But if process death happened, we might need reload.
             // For now assume ViewModel survives config change.
        }

        // Bind Views
        codeText = findViewById(R.id.debugCodeText);
        outputText = findViewById(R.id.debugOutputText);
        regPC = findViewById(R.id.regPC);
        regPtr = findViewById(R.id.regPtr);
        regCycles = findViewById(R.id.regCycles);
        regDepth = findViewById(R.id.regDepth);
        hexRecyclerView = findViewById(R.id.hexRecyclerView);
        checkFollowPointer = findViewById(R.id.checkFollowPointer);
        inputText = findViewById(R.id.debugInputText);
        btnRun = findViewById(R.id.btnRun);
        btnStep = findViewById(R.id.btnStep);
        btnReset = findViewById(R.id.btnReset);
        btnSendInput = findViewById(R.id.btnSendInput);

        codeText.setText(sourceCode);

        hexRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        Tape initialTape = viewModel.tape.getValue();
        if (initialTape == null) initialTape = new Tape(65536);
        hexDumpAdapter = new HexDumpAdapter(initialTape, this);
        hexRecyclerView.setAdapter(hexDumpAdapter);

        // Observers
        viewModel.tape.observe(this, new Observer<Tape>() {
            @Override
            public void onChanged(Tape tape) {
                // Optimize adapter update
                if (hexDumpAdapter == null || hexDumpAdapter.getTape() != tape) {
                    hexDumpAdapter = new HexDumpAdapter(tape, DebuggerActivity.this);
                    hexRecyclerView.swapAdapter(hexDumpAdapter, false);
                }

                Integer ptr = viewModel.pointer.getValue();
                if (ptr != null) hexDumpAdapter.updatePointer(ptr);
            }
        });

        viewModel.pointer.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer ptr) {
                regPtr.setText(String.format("Ptr: %04X", ptr));
                if (hexDumpAdapter != null) {
                    hexDumpAdapter.updatePointer(ptr);
                    if (checkFollowPointer.isChecked()) {
                        int row = ptr / 16;
                        hexRecyclerView.scrollToPosition(row);
                    }
                }
            }
        });

        viewModel.pc.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer pc) {
                regPC.setText(String.format("PC: %04X", pc));
                highlightCode(pc);
            }
        });

        viewModel.cycles.observe(this, new Observer<Long>() {
             @Override
             public void onChanged(Long cycles) {
                 regCycles.setText("Cycles: " + cycles);
             }
        });

        viewModel.depth.observe(this, new Observer<Integer>() {
             @Override
             public void onChanged(Integer depth) {
                 regDepth.setText("Depth: " + depth);
             }
        });


        viewModel.outputString.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                outputText.setText(s);
            }
        });

        viewModel.state.observe(this, new Observer<InterpreterEngine.State>() {
            @Override
            public void onChanged(InterpreterEngine.State state) {
                if (state == InterpreterEngine.State.RUNNING) {
                    btnRun.setText("Pause");
                    btnStep.setEnabled(false);
                } else {
                    btnRun.setText("Run");
                    btnStep.setEnabled(true);
                }
            }
        });

        // Listeners
        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewModel.state.getValue() == InterpreterEngine.State.RUNNING) {
                    viewModel.pause();
                } else {
                    viewModel.run();
                }
            }
        });

        btnStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.step();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.reset();
            }
        });

        btnSendInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = inputText.getText().toString();
                if (!text.isEmpty()) {
                    viewModel.input(text);
                    inputText.setText("");
                }
            }
        });
    }

    private void highlightCode(int pc) {
        if (sourceCode == null || pc < 0 || pc >= sourceCode.length()) return;

        android.text.SpannableString span = new android.text.SpannableString(sourceCode);
        span.setSpan(new android.text.style.BackgroundColorSpan(android.graphics.Color.YELLOW), pc, pc + 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        codeText.setText(span);
    }

    @Override
    public void onByteClick(final int address, char currentValue) {
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Value (0-255)");
        input.setText(String.valueOf((int)currentValue));

        new AlertDialog.Builder(this)
            .setTitle("Edit Memory [" + String.format("%04X", address) + "]")
            .setView(input)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try {
                        int val = Integer.parseInt(input.getText().toString());
                        if (val < 0) val = 0;
                        if (val > 255) val = 255;

                        // Update Tape. Since Tape is inside Engine which is background, we need a VM method.
                        viewModel.setMemory(address, (char)val);
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
