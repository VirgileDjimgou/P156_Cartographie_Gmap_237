package com.xhaka.wasist.CardPack;

import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;
import java.util.Locale;
import com.xhaka.wasist.R;


public class SpeakSettingActivity extends AppCompatActivity implements OnInitListener {

    EditText editText;
    Button buttonSpeak;
    TextToSpeech textToSpeech;
    SeekBar seekBar_pitch, seekBar_speed;
    float speed = 1.0f;
    float pitch = 1.0f;
    List<Locale> Language;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speak_settings);

        try {
            editText = (EditText) findViewById(R.id.enter_text);
            buttonSpeak = (Button) findViewById(R.id.button_speak);
            textToSpeech = new TextToSpeech(this, this);
            this.seekBar_pitch = (SeekBar) findViewById(R.id.seek_bar_pitch);
            this.seekBar_speed = (SeekBar) findViewById(R.id.seek_bar_speed);

            //These step can be done in xml file
            seekBar_pitch.setMax(20);
            seekBar_speed.setMax(20);

            seekBar_speed.setProgress((int) speed * 10);
            seekBar_pitch.setProgress((int) pitch * 10);

            //  Define Liste of Language
            // Language.add(Locale.ENGLISH);


            buttonSpeak.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String message = editText.getText().toString();
                    //if speed is 0, not running
                    if (speed == 0) {
                        speed = 0.1f;
                    }

                    if (pitch == 0) {
                        pitch = 0.1f;
                    }
                    // tts.setLanguage(Locale.getDefault());
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.setSpeechRate(speed);
                    textToSpeech.setPitch(pitch);

                    textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
                }
            });


            this.seekBar_pitch.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    pitch = seekBar_pitch.getProgress() / 10.0f;
                    return false;
                }
            });

            this.seekBar_speed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    speed = seekBar_speed.getProgress() / 10.0f;
                    return false;
                }
            });
            // ATTENTION: This was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onInit(int status) {
        try {
            if (status == TextToSpeech.SUCCESS) {
                buttonSpeak.setEnabled(true);
            } else {
                buttonSpeak.setEnabled(false);
                Toast.makeText(this, "Cannot talk", Toast.LENGTH_SHORT);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            // ATTENTION: This was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            client.connect();
            Action viewAction = Action.newAction(
                    Action.TYPE_VIEW, // TODO: choose an action type.
                    "Main Page", // TODO: Define a title for the content shown.
                    // TODO: If you have web page content that matches this app activity's content,
                    // make sure this auto-generated web page URL is correct.
                    // Otherwise, set the URL to null.
                    Uri.parse("http://host/path"),
                    // TODO: Make sure this auto-generated app URL is correct.
                    Uri.parse("android-app://com.shaka.wasist/http/host/path")
            );
            AppIndex.AppIndexApi.start(client, viewAction);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        try {
            // ATTENTION: This was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            Action viewAction = Action.newAction(
                    Action.TYPE_VIEW, // TODO: choose an action type.
                    "Main Page", // TODO: Define a title for the content shown.
                    // TODO: If you have web page content that matches this app activity's content,
                    // make sure this auto-generated web page URL is correct.
                    // Otherwise, set the URL to null.
                    Uri.parse("http://host/path"),
                    // TODO: Make sure this auto-generated app URL is correct.
                    Uri.parse("android-app://com.shaka.wasist/http/host/path")
            );
            AppIndex.AppIndexApi.end(client, viewAction);
            client.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
