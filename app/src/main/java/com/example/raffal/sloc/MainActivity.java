package com.example.raffal.sloc;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {


    public TextView text;
    public int duration;
    public EditText txt2;
    public TextView progress;
    MediaRecorder recorder;
    public String finalName;
    public String finalPath;
    Spinner spn;
    Spinner spn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};


        while (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, permissions, 42);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        File workDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/JavaApps");
        if (!workDir.exists())
            workDir.mkdir();
        txt2 = (EditText)findViewById(R.id.editText2);
        progress = (TextView)findViewById(R.id.progress);
        Button btn1 = (Button) findViewById(R.id.SET);
        Button btn2 = (Button) findViewById(R.id.SEND);
        Button btn3 = (Button) findViewById(R.id.OUT);


        List<String> fileList = getNames();
        spn2 = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, fileList);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn2.setAdapter(dataAdapter2);

        spn = (Spinner) findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>();
        list.add("mp4");
        list.add("3gp");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn.setAdapter(dataAdapter);

        btn1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                recorder = new MediaRecorder();
                duration = Integer.parseInt(txt2.getText().toString());
                String selectedFormat = spn.getItemAtPosition(spn.getSelectedItemPosition()).toString();
                File wavFile;

                if(selectedFormat == "mp4") {
                    int i = 0;
                    while ((wavFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JavaApps/" + "output" + i + ".mp4")).isFile())
                        i++;
                    finalName = "output" + i + ".mp4";
                    finalPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/JavaApps/" + "output" + i + ".mp4";
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                } else if(selectedFormat == "3gp"){
                    int i = 0;
                    while ((wavFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JavaApps/" + "output" + i + ".3gp")).isFile())
                        i++;
                    finalName = "output" + i + ".3gp";
                    finalPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/JavaApps/" + "output" + i + ".3gp";
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                } else {
                    int i = 0;
                    while ((wavFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JavaApps/" + "output" + i + ".aac")).isFile())
                        i++;
                    finalName = "output" + i + ".aac";
                    finalPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/JavaApps/" + "output" + i + ".aac";
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                }


                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                recorder.setOutputFile(wavFile.getAbsolutePath());
                try {
                    recorder.prepare();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                recordAudio();
                progress.setText("Recorded sound to file: " + finalPath);
                List<String> fileList = getNames();
                spn2 = (Spinner) findViewById(R.id.spinner2);
                ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, fileList);
                dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spn2.setAdapter(dataAdapter2);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendFile();
            }
        });
        btn3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getOutput();
            }
        });


    }



    void getOutput(){
        progress = (TextView) findViewById(R.id.progress);
        FTPClient ftpClient = new FTPClient();
        boolean success;

        try {
            ftpClient.connect("SERVER_NAME");
            showServerReply(ftpClient);
            ftpClient.enterLocalPassiveMode();
            success = ftpClient.login("LOGIN", "PASSWORD");

            ftpClient.enterLocalPassiveMode();
            showServerReply(ftpClient);
            if (!success) {
                progress.setText("Failed login");
            }
            success = ftpClient.changeWorkingDirectory("/public_html/uploads/");
            showServerReply(ftpClient);
            if (!success) {
                progress.setText("Failed change directory");
            }

            //BufferedInputStream buffIn = null;
            BufferedReader reader = null;
            String outStr = null;
            try{
                InputStream stream = ftpClient.retrieveFileStream("output.txt");
                reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                outStr = reader.readLine();
            } finally {
                if(reader != null) try {reader.close(); } catch (IOException logOrIgnore) {}
            }



            reader.close();
            ftpClient.logout();
            ftpClient.disconnect();
            progress.setText(outStr);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    void sendFile() {

        progress = (TextView) findViewById(R.id.progress);
        String name = spn2.getItemAtPosition(spn2.getSelectedItemPosition()).toString();
        File fileSource = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JavaApps/"+name);

        if (!fileSource.isFile()) {
            progress.setText("File not found");
            return;
        }

        FTPClient ftpClient = new FTPClient();
        boolean success;

        try {

            ftpClient.connect("SERVER_NAME");
            showServerReply(ftpClient);
            ftpClient.enterLocalPassiveMode();
            success = ftpClient.login("LOGIN", "PASSWORD");

            ftpClient.enterLocalPassiveMode();
            showServerReply(ftpClient);
            if (!success) {
                progress.setText("Failed login");
            }
            success = ftpClient.changeWorkingDirectory("/public_html/uploads/");
            showServerReply(ftpClient);
            if (!success) {
                progress.setText("Failed change directory");
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            //BufferedInputStream buffIn = null;
            FileInputStream buffIn;
            buffIn = new FileInputStream(fileSource);

            ftpClient.enterLocalPassiveMode();
            String mdl = android.os.Build.MODEL;
            success = ftpClient.storeFile(mdl+"_"+name, buffIn);
            showServerReply(ftpClient);

            buffIn.close();
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
/*
    public List<File> getListFiles(File dir){
        List<File> inFiles = new ArrayList<>();
        Queue<File> files = new LinkedList<>();
        files.addAll(Arrays.asList(dir.listFiles()));
        while(!files.isEmpty()){
            File file = files.remove();
            if(file.isDirectory()){
                files.addAll(Arrays.asList(file.listFiles()));
            } else if(file.getName().endsWith(".mp4") || file.getName().endsWith(".3gp")) {
                inFiles.add(file);
            }
        }
        return inFiles;
    }*/

    public List<String> getNames(){
        List<String> nameList = new ArrayList<>();
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JavaApps");
        for(File f : dir.listFiles()){
            if(f.isFile()) {
                String name = f.getName();
                nameList.add(name);
            }
        }
        return nameList;
    }

    void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        progress = (TextView)findViewById(R.id.progress);
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
                progress.setText(/*progress.getText()+*/aReply);
            }
        }
    }



   public void recordAudio()
    {

        recorder.start();

        try {
            Thread.sleep(duration*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

            recorder.stop();


            recorder = null;

    }

}
