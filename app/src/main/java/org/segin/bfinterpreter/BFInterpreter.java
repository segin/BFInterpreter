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
import android.support.v7.app.ActionBarActivity;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class BFInterpreter extends ActionBarActivity {

    private Interpreter interpreter;
    private int inputCounter;
    private EditText inputText;
    private EditText codeText;
    private TextView outputText;
    private String output;
    private AsyncTask interpThread;

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
            interpreter = new Interpreter();
            interpreter.setIO(new UserIO() {
                @Override
                public char input() {
                    try {
                        char ret = inputText.getText().toString().charAt(inputCounter);
                        inputCounter++;
                        return ret;
                    } catch (Exception e) {
                        // Panu Kalliokoski behavior
                        return 0;
                    }
                }

                @Override
                public void output(char out) {
                    outputText.setVisibility(View.VISIBLE);
                    output += String.valueOf(out);
                    outputText.setText(output);
                }
            });
            try {
                output = "";
                inputCounter = 0;
                interpreter.run(codeText.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
                output += getString(R.string.crash) + e.toString();
                outputText.setVisibility(View.VISIBLE);
                outputText.setText(output);
            }
            return true;
        }

        if (id == R.id.action_copy) {
            ClipboardManager clipboard = (ClipboardManager)
                getSystemService(this.CLIPBOARD_SERVICE);
            clipboard.setText(output);
            Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    private class InterpreterThread extends AsyncTask<String, Integer, String> {

        private int a;

        @Override
        void onPreExecute() {
            super.onPreExecute();
            output = "";
        }

        @Override
        protected String doInBackground(String... params) {
            interpreter = new Interpreter();
            interpreter.setIO(new UserIO() {
                @Override
                public char input() {
                    try {
                        char ret = inputText.getText().toString().charAt(inputCounter);
                        inputCounter++;
                        return ret;
                    } catch (Exception e) {
                        // Panu Kalliokoski behavior
                        return 0;
                    }
                }

                @Override
                public void output(char out) {
                    outputText.setVisibility(View.VISIBLE);
                    output += String.valueOf(out);
                    outputText.setText(output);
                }
            });
            try {
                output = "";
                inputCounter = 0;
                interpreter.run(codeText.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
                output += getString(R.string.crash) + e.toString();
                outputText.setVisibility(View.VISIBLE);
                outputText.setText(output);
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }
    */
}
