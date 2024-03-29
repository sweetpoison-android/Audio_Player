package com.company.audio_player;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class Player extends AppCompatActivity implements SensorEventListener {
ImageView voice;
    static MediaPlayer mp;//assigning memory loc once or else multiple songs will play at once
    int position;
    SeekBar seekBar;
    ArrayList<File> mySongs;
    Thread updateSeekBar;
    Button pause,next,previous;
    TextView songNameText;

    String sname;

    SensorManager sensorManager;
    float acelval;
    float acellast;
    float shake;

    // proximity sensor
    SensorManager proximitySensorManager;
    Sensor proximitysensor;
    Vibrator vibrator;
    boolean isproximitysensoravailable;
    //////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        voice=findViewById(R.id.voice);
        songNameText=findViewById(R.id.playertextview);
        seekBar =findViewById(R.id.playerProgressbar);
        pause=findViewById(R.id.playerplaybutton);
        next=findViewById(R.id.playernextButton);
        previous=findViewById(R.id.playerbackButton);

        sensorManager =(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(sensorlistitener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);

        acelval=SensorManager.GRAVITY_EARTH;
        acellast=SensorManager.GRAVITY_EARTH;
        shake=0.00f;


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Now Playing");

        updateSeekBar=new Thread(){
            @Override
            public void run(){
                int totalDuration = mp.getDuration();
                int currentPosition = 0;
                while(currentPosition < totalDuration){
                    try{
                        sleep(500);
                        try{
                            currentPosition=mp.getCurrentPosition();
                        }catch(Exception e)
                        {
                           
                        }


                        seekBar.setProgress(currentPosition);
                    }
                    catch (InterruptedException e){

                    }
                }
           }
   };




        if(mp != null){
            mp.stop();
            mp.release();
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();


        mySongs = (ArrayList) bundle.getParcelableArrayList("songlist");

        sname = mySongs.get(position).getName().toString();

        String SongName = intent.getStringExtra("songname");
        songNameText.setText(SongName);
        songNameText.setSelected(true);

        position = bundle.getInt("position",0);
        position = position-1;  // because when proximity sensor imolemented position in increased
        Uri u = Uri.parse(mySongs.get(position).toString());

        mp = MediaPlayer.create(getApplicationContext(),u);
        mp.start();
        seekBar.setMax(mp.getDuration());

        updateSeekBar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
            }
        });

        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getVoiceInput();
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setMax(mp.getDuration());
                if(mp.isPlaying()){
                    pause.setBackgroundResource(R.drawable.play);
                   pause.getLayoutParams().width=150;
                    mp.pause();

                }
                else {
                    pause.setBackgroundResource(R.drawable.pause);
                    mp.start();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause.setBackgroundResource(R.drawable.pause);
                mp.stop();
                mp.release();

                    position = ((position + 1) % mySongs.size());
                  //  Toast.makeText(Player.this, String.valueOf(position), Toast.LENGTH_SHORT).show();


                Uri u = Uri.parse(mySongs.get( position).toString());
                // songNameText.setText(getSongName);
                mp = MediaPlayer.create(getApplicationContext(),u);

                sname = mySongs.get(position).getName().toString();
                songNameText.setText(sname);

                try{
                    mp.start();
                }catch(Exception e){}

            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //songNameText.setText(getSongName);
                pause.setBackgroundResource(R.drawable.pause);
                mp.stop();
                mp.release();

                position=((position-1)<0)?(mySongs.size()-1):(position-1);
              //  Toast.makeText(Player.this, String.valueOf(position), Toast.LENGTH_SHORT).show();

                Uri u = Uri.parse(mySongs.get(position).toString());
                mp = MediaPlayer.create(getApplicationContext(),u);
                sname = mySongs.get(position).getName().toString();
                songNameText.setText(sname);
                mp.start();
            }
        });

        // proximity sensor

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null)
        {
            proximitysensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            isproximitysensoravailable = true;
        }
        else
        {
            Toast.makeText(this, "Proximity sensor not available", Toast.LENGTH_SHORT).show();
            isproximitysensoravailable= false;
        }

        /////////////////////////////////////////


    }
//////////// Shaking Event
   private final SensorEventListener sensorlistitener=new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            float x=sensorEvent.values[0];
            float y=sensorEvent.values[1];
            float z=sensorEvent.values[2];

            acellast=acelval;
            acelval=(float)Math.sqrt((double)(x*x+y*y+z*z));
            float delta=acelval-acellast;
            shake=shake*0.9f+delta;
            if(shake>11)
            {
               // Toast.makeText(getApplicationContext(),"shaking",Toast.LENGTH_SHORT).show();
                pause.setBackgroundResource(R.drawable.pause);
                mp.stop();
                mp.release();
                position=((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get( position).toString());
                // songNameText.setText(getSongName);
                mp = MediaPlayer.create(getApplicationContext(),u);

                sname = mySongs.get(position).getName().toString();
                songNameText.setText(sname);

                try{
                    mp.start();
                }catch(Exception e){}
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    //Audio Control////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == 1 && resultCode== RESULT_OK && null != data)
        {
            ArrayList<String> ar=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if(ar.get(0).toString().equals("stop") || ar.get(0).toString().equals("pause"))
            {
                seekBar.setMax(mp.getDuration());
                if(mp.isPlaying()){
                    pause.setBackgroundResource(R.drawable.play);
                    pause.getLayoutParams().width=120;
                    mp.pause();

                }

            }
            else  if(ar.get(0).toString().equals("play") || ar.get(0).toString().equals("start"))
            {
                pause.setBackgroundResource(R.drawable.pause);
                mp.start();
            }
            else if(ar.get(0).toString().equals("next"))
            {
                pause.setBackgroundResource(R.drawable.pause);
                mp.stop();
                mp.release();
                position=((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get( position).toString());
                // songNameText.setText(getSongName);
                mp = MediaPlayer.create(getApplicationContext(),u);

                sname = mySongs.get(position).getName().toString();
                songNameText.setText(sname);

                try{
                    mp.start();
                }catch(Exception e){}
            }
            else if(ar.get(0).toString().equals("back"))
            {
                pause.setBackgroundResource(R.drawable.pause);
                mp.stop();
                mp.release();

                position=((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri u = Uri.parse(mySongs.get(position).toString());
                mp = MediaPlayer.create(getApplicationContext(),u);
                sname = mySongs.get(position).getName().toString();
                songNameText.setText(sname);
                mp.start();
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    public void getVoiceInput()
    {
        Intent in=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        in.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        in.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        in.putExtra(RecognizerIntent.EXTRA_PROMPT,"what do you want .........");
        try
        {
            startActivityForResult(in,1);
        }
        catch (ActivityNotFoundException e)
        {
            Toast.makeText(this, "please connect with Internet", Toast.LENGTH_SHORT).show();
        }
    }

    // proximity sensor

    @SuppressLint("ResourceAsColor")
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.values[0] == 0.0)
        {
            seekBar.setMax(mp.getDuration());
            if(mp.isPlaying()){
                pause.setBackgroundResource(R.drawable.play);
                pause.getLayoutParams().width=120;
                mp.pause();

            }
        }
        else
        {
            pause.setBackgroundResource(R.drawable.pause);
            mp.stop();
            mp.release();
            position=((position+1)%mySongs.size());
            Uri u = Uri.parse(mySongs.get( position).toString());
            // songNameText.setText(getSongName);
            mp = MediaPlayer.create(getApplicationContext(),u);

            sname = mySongs.get(position).getName().toString();
            songNameText.setText(sname);

            try{
                mp.start();
            }catch(Exception e){}

        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy == 8)
        {
           // constraintLayout.setBackgroundColor(R.color.purple_700);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isproximitysensoravailable)
        {
            sensorManager.registerListener(this, proximitysensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isproximitysensoravailable)
        {
            sensorManager.unregisterListener(this);
        }
    }

    }

