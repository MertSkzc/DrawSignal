package com.example.mert2.drawsignal;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;

public class MainActivity extends AppCompatActivity {

    Button btnDosyaoku;
    EditText dosyaadi;
    TextView text;
    private  int STORAGE_PERMISSION_CODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDosyaoku = (Button)findViewById(R.id.btnDosyaOku);
        dosyaadi = (EditText)findViewById(R.id.dosyaadi);
        text = (TextView)findViewById(R.id.textt);
    }


    public static double[] Butterworth(double[] indata, double deltaTimeinsec, double CutOff) {
        if (indata == null) return null;
        if (CutOff == 0) return indata;

        double Samplingrate = 1 / deltaTimeinsec;
        int dF2 = indata.length - 1;        // The data range is set with dF2
        double[] Dat2 = new double[dF2 + 4]; // Array with 4 extra points front and back
        double[] data = indata; // Ptr., changes passed data

        // Copy indata to Dat2
        for (int r = 0; r < dF2; r++) {
            Dat2[2 + r] = indata[r];
        }
        Dat2[1] = Dat2[0] = indata[0];
        Dat2[dF2 + 3] = Dat2[dF2 + 2] = indata[dF2];

        final double pi = 3.14159265358979;
        double wc = Math.tan(CutOff * pi / Samplingrate);
        double k1 = 1.414213562 * wc; // Sqrt(2) * wc
        double k2 = wc * wc;
        double a = k2 / (1 + k1 + k2);
        double b = 2 * a;
        double c = a;
        double k3 = b / k2;
        double d = -2 * a + k3;
        double e = 1 - (2 * a) - k3;

        // RECURSIVE TRIGGERS - ENABLE filter is performed (first, last points constant)
        double[] DatYt = new double[dF2 + 4];
        DatYt[1] = DatYt[0] = indata[0];
        for (int s = 2; s < dF2 + 2; s++) {
            DatYt[s] = a * Dat2[s] + b * Dat2[s - 1] + c * Dat2[s - 2]
                    + d * DatYt[s - 1] + e * DatYt[s - 2];
        }
        DatYt[dF2 + 3] = DatYt[dF2 + 2] = DatYt[dF2 + 1];

        // FORWARD filter
        double[] DatZt = new double[dF2 + 2];
        DatZt[dF2] = DatYt[dF2 + 2];
        DatZt[dF2 + 1] = DatYt[dF2 + 3];
        for (int t = -dF2 + 1; t <= 0; t++) {
            DatZt[-t] = a * DatYt[-t + 2] + b * DatYt[-t + 3] + c * DatYt[-t + 4]
                    + d * DatZt[-t + 1] + e * DatZt[-t + 2];
        }

        // Calculated points copied for return
        for (int p = 0; p < dF2; p++) {
            data[p] = DatZt[p];
        }

        return data;
    }

    public void Ciz(String yeniData)
    {
        GraphView graph = (GraphView)findViewById(R.id.graph);
        graph.removeAllSeries();

        //String[] sinyal = datalar.sinyalVerisi.split(",");
        String[] sinyal = yeniData.split(",");

        int[] sayisalVeri = new int[sinyal.length];
        for(int i=0;i<sinyal.length;i++)
        {
            sayisalVeri[i] = Integer.valueOf(sinyal[i]);
        }
        DataPoint[] veri = new DataPoint[sayisalVeri.length];

        for(int i=0;i<sayisalVeri.length;i++)
        {
            veri[i] = new DataPoint(i,sayisalVeri[i]);
        }
        LineGraphSeries<DataPoint> veriler = new LineGraphSeries<DataPoint>(veri);

        graph.addSeries(veriler);
        graph.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.setTitle("TEST SİNYALİ");
        graph.setTitleColor(getResources().getColor(android.R.color.black));

        veriler.setTitle("Filtresiz");
        veriler.setThickness(3);
        veriler.setDrawBackground(true);
        veriler.setColor(Color.RED);
        veriler.setDrawDataPoints(true);
        veriler.setDataPointsRadius(1);
        veriler.setBackgroundColor(android.R.color.holo_red_dark);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    public void Ciz2(String yeniData2)
    {
        GraphView graph2 = (GraphView)findViewById(R.id.graph);
        //graph2.removeAllSeries();

        String[] sinyal = yeniData2.split(",");
        ////////////////////////////////////
        double[] dArray = new double[sinyal.length];
        ///////////FILTRE//////////
        for(int i=0;i<sinyal.length;i++)
        {
            dArray[i] = Double.valueOf(sinyal[i]);
        }

        double[] dArrayFiltreli = Butterworth(dArray,512,0.002);

        int[] fSinyal = new int[dArrayFiltreli.length];
        for(int i=0;i<dArrayFiltreli.length;i++)
        {
            fSinyal[i] = (int)dArrayFiltreli[i];
        }

        ///////////////////////////////////

        DataPoint[] veri = new DataPoint[fSinyal.length];

        for(int i=0;i<fSinyal.length;i++)
        {
            veri[i] = new DataPoint(i,fSinyal[i]);
        }
        LineGraphSeries<DataPoint> veriler = new LineGraphSeries<DataPoint>(veri);

        graph2.addSeries(veriler);
        graph2.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        graph2.getViewport().setScalable(true);
        graph2.getViewport().setScrollable(true);
        graph2.setTitle("TEST SİNYALİ");
        graph2.setTitleColor(getResources().getColor(android.R.color.black));

        veriler.setTitle("Filtreli");
        veriler.setThickness(3);
        veriler.setDrawBackground(true);
        veriler.setColor(Color.GREEN);
        veriler.setDrawDataPoints(true);
        veriler.setDataPointsRadius(1);
        veriler.setBackgroundColor(android.R.color.holo_red_dark);

        graph2.getLegendRenderer().setVisible(true);
        graph2.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    public void Read(View v)
    {

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(MainActivity.this,"iznin var!!",Toast.LENGTH_SHORT).show();

            try
            {
                String gelenData="";
                File textFile = new File(Environment.getExternalStorageDirectory(),dosyaadi.getText().toString());
                FileInputStream fis = new FileInputStream(textFile);

                if(fis!=null){
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader buff = new BufferedReader(isr);

                    String line=null;
                    while ((line=buff.readLine())!=null)
                    {
                        gelenData+=line;
                    }
                    fis.close();
                    //text.setText(sb);
                    Ciz(gelenData);
                    Ciz2(gelenData);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error!!! "+e, Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            RequestStoragePermission();
        }
    }

    private void RequestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            new AlertDialog.Builder(this).setTitle("Permission needed").setMessage("This permission needed").setPositiveButton("ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
                        }
                    }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                }
            }).create().show();
        }
        else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==STORAGE_PERMISSION_CODE)
        {
            if(grantResults.length>0&&grantResults[0]== getPackageManager().PERMISSION_GRANTED) {
                Toast.makeText(this, "PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this,"PERMISSION DENIED",Toast.LENGTH_SHORT).show();
            }

        }

    }



}
