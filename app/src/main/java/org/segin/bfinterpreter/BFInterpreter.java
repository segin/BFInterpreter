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

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class BFInterpreter extends AppCompatActivity {

    private Interpreter interpreter;
    private int inputCounter;
    private EditText inputText;
    private EditText codeText;
    private TextView outputText;
    private String output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bfinterpreter);


        inputText = (EditText) findViewById(R.id.inputText);
        codeText = (EditText) findViewById(R.id.codeText);
        outputText = (TextView) findViewById(R.id.outputText);

        output = "";
        inputCounter = 0;
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
            new InterpreterThread().execute(code, input);
            return true;
        }

        if (id == R.id.action_copy) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("BF Output", output);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private class InterpreterThread extends AsyncTask<String, Void, String> {
        private String error = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            output = "";
            outputText.setText("");
            outputText.setVisibility(View.VISIBLE);
            inputCounter = 0;
            // Consider disabling the run button here to prevent multiple executions
        }

        @Override
        protected String doInBackground(String... params) {
            final String code = params[0];
            final String input = params[1];
            final StringBuilder outputBuilder = new StringBuilder();

            interpreter = new Interpreter();
            interpreter.setIO(new UserIO() {
                @Override
                public char input() {
                    if (inputCounter < input.length()) {
                        return input.charAt(inputCounter++);
                    }
                    return 0; // End of input, Panu Kalliokoski behavior
                }

                @Override
                public void output(char out) {
                    outputBuilder.append(out);
                }
            });

            try {
                interpreter.run(code);
            } catch (Exception e) {
                e.printStackTrace();
                error = e.toString();
            }
            return outputBuilder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            output = result;
            if (error != null) {
                // R.string.crash might not exist, using a hardcoded string for safety.
                output += "\n" + "Error: " + error;
            }
            outputText.setText(output);
            // Consider re-enabling the run button here
        }
    }
}
