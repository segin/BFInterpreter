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

import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class BFInterpreter extends AppCompatActivity {

    private BFViewModel viewModel;
    private EditText inputText;
    private EditText codeText;
    private TextView outputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bfinterpreter);

        inputText = (EditText) findViewById(R.id.inputText);
        codeText = (EditText) findViewById(R.id.codeText);
        outputText = (TextView) findViewById(R.id.outputText);

        viewModel = new ViewModelProvider(this).get(BFViewModel.class);

        // Bind output
        viewModel.outputString.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                outputText.setText(s);
            }
        });

        viewModel.state.observe(this, new Observer<InterpreterEngine.State>() {
             @Override
             public void onChanged(InterpreterEngine.State state) {
                 if (state == InterpreterEngine.State.ERROR) {
                     String err = viewModel.getEngine().getErrorMessage();
                     if (err != null) {
                        Toast.makeText(BFInterpreter.this, "Error: " + err, Toast.LENGTH_LONG).show();
                     }
                 }
             }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bfinterpreter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_run) {
            String code = codeText.getText().toString();
            String input = inputText.getText().toString();
            viewModel.load(code, false); // Legacy mode: non-interactive
            if (!input.isEmpty()) viewModel.input(input);
            viewModel.run();
            return true;
        }

        if (id == R.id.action_debug) {
            String code = codeText.getText().toString();
            String input = inputText.getText().toString();
            Intent intent = new Intent(this, DebuggerActivity.class);
            intent.putExtra("CODE", code);
            intent.putExtra("INPUT", input);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_copy) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("BF Output", outputText.getText().toString());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
