package com.example.karl.processexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etCommand = (EditText) findViewById(R.id.etCommand);
        tvMain = (TextView) findViewById(R.id.tvMain);
        initProcess();
    }
    public void myClickHandler(View view) 			// ClickHandler 					(ButtonEvents)
    {
        switch (view.getId())
        {
            case R.id.btnPerform:
                try {writeToProcess(etCommand.getText().toString() + "\n");}
                catch (IOException e) {e.printStackTrace();}

                String line = "";
                String text = "";
                int cnt = 0;
                while (cnt < 100)
                {
                    try
                    {
                        line = readFromProcess();
                        if (line != null)
                        {
                            text = text + line + "\n";
                        }
                    }
                    catch (IOException e) {e.printStackTrace();}
                    try {Thread.sleep(10);}
                    catch (InterruptedException e) {}
                    cnt++;
                }
                tvMain.setText(text);
                break;
        }
    }
    private void initProcess()
    {
        dataEnginesPath = getApplicationContext().getFilesDir() + "/";
        try
        {
            InputStream istream = getAssets().open(ASSET_PROCESS_1);
            writeProcessToData(dataEnginesPath, DATA_PROCESS_1, istream);
            startProcess(dataEnginesPath, DATA_PROCESS_1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public boolean writeProcessToData(String filePath, String fileName, InputStream is)
    { // if no engine exists in >/data/data/ccc.chess.engine.stockfish/engines< install default engine!
        File file = new File(filePath);
        if (!file.exists())
        {
            if (!file.mkdir())
            {
                Log.i(TAG, "error creating directory");
                return false;
            }
            else
            {
//				Log.i(TAG, dataEnginesPath + " created");
            }
        }
        boolean engineUpdated = false;
        File f = new File(filePath, fileName);
        if (!f.exists())
        {
            try
            {
                InputStream istream = null;
                if (is != null)
                {
                    istream = is;
                }
                else
                {
                    f = new File(filePath, fileName);
                    istream = new FileInputStream(f);
                }
                FileOutputStream fout = new FileOutputStream(filePath + fileName);
                byte[] b = new byte[1024];
                int noOfBytes = 0;
                while ((noOfBytes = istream.read(b)) != -1)
                {
                    fout.write(b, 0, noOfBytes);
                }
                istream.close();
                fout.close();

                try
                {
                    String cmd[] = { "chmod", "744", filePath + fileName };
                    Process process = Runtime.getRuntime().exec(cmd);
                    try
                    {
                        process.waitFor();
                        engineUpdated = true;
                    }
                    catch (InterruptedException e)
                    {
						Log.i(TAG, "InterruptedException ???");
                        return false;
                    }
                }
                catch (IOException e)
                {
					Log.i(TAG, "IOException ???");
                    return false;
                }
                engineUpdated = true;
                Log.i(TAG, filePath + fileName + " created");
            }
            catch (IOException e)
            {
//				Log.i(TAG, "IOException 2 ???");
                return false;
            }
        }
        else
            Log.i(TAG, filePath + fileName + " exists");
        return engineUpdated;
    }
    private final boolean startProcess(String filePath, String fileName)	//>174 start native process
    {
        try
        {
            ProcessBuilder builder = new ProcessBuilder(filePath + fileName);
//            Log.i(TAG, "startProcess(), file: " + filePath + fileName);
            process = builder.start();
//            Log.i(TAG, "builder.start()");
            OutputStream stdout = process.getOutputStream();
            InputStream stdin = process.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stdin));
            writer = new BufferedWriter(new OutputStreamWriter(stdout));
            return true;
        }
        catch (IOException e)
        {
            Log.i(TAG, filePath + fileName + ": startProcess, IOException");
            Log.i(TAG, e.toString());
            return false;
        }
    }
    private final void writeToProcess(String data) throws IOException //>175 write data to the process
    {
        if (writer != null)
        {
            writer.write(data);
            writer.flush();
        }
    }
    private final String readFromProcess() throws IOException //>176 read a line of data from the process
    {
        String line = null;
        if (reader != null && reader.ready())
        {
            line = reader.readLine();
        }
        return line;
    }

    final String TAG = "MainActivity";
    EditText etCommand = null;
    TextView tvMain = null;
    private Process process;
    final String ASSET_PROCESS_1 = "stockfish_7_0";
    String dataEnginesPath = "";
    final String DATA_PROCESS_1 = "process_1";
    private BufferedReader reader = null;
    private BufferedWriter writer = null;
}
