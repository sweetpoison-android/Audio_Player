package com.company.audio_player;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
   ListView lv;
   String[] item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       lv=findViewById(R.id.mainlistview);

       RuntimePermission();

    }

    public void RuntimePermission()
    {
        Dexter.withActivity(this)
        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                      display();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                     token.continuePermissionRequest();
                    }
                }).check();
    }
    public ArrayList<File> findsong(File file)
    {
        ArrayList<File> arraylist=new ArrayList<>();
        File[] files=file.listFiles();
        for(File singlefile: files)
        {
            if(singlefile.isDirectory() && !singlefile.isHidden())
            {
                arraylist.addAll(findsong(singlefile));
            }
            else
            {
                //if(singlefile.getName().endsWith(".mp3") || singlefile.getName().endsWith(".wav"))
                    if(singlefile.getName().endsWith(".mp3"))
                {
                    arraylist.add(singlefile);
                }
            }
        }
       return arraylist;
    }
    public void display()
    {
        final ArrayList<File> mysongs=findsong(Environment.getExternalStorageDirectory());
        item=new String[mysongs.size()];
        for(int i=0; i<item.length; i++)
        {
            //item[i]=mysongs.get(i).getName().toString().replace("mp3","").replace("wav","");
            item[i]=mysongs.get(i).getName().toString();
        }
        ArrayAdapter<String> ardp=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,item);
        lv.setAdapter(ardp);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songname=lv.getItemAtPosition(i).toString();
                Intent in=new Intent(getApplicationContext(),Player.class);
                in.putExtra("position",i);
                in.putExtra("songname",songname);
                in.putExtra("songlist",mysongs);
                startActivity(in);

            }
        });
    }

}
