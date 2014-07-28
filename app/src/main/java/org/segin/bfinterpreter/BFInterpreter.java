package org.segin.bfinterpreter;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;


public class BFInterpreter extends ActionBarActivity {

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

        interpreter = new Interpreter();
        interpreter.setIO(new UserIO() {
            @Override
            public char input() {
                try {
                    return inputText.getText().toString().charAt(inputCounter++);
                } catch (Exception e) {
                    // Panu Kalliokoski behavior
                    return 0;
                }
            }

            @Override
            public void output(char out) {
                output += new StringBuilder().append(out).toString();
                outputText.setText(output);
            }
        });
        setupButton();
    }

    private void setupButton() {

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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
