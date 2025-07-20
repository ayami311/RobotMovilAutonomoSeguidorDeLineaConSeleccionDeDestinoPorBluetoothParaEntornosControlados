package com.example.roboticaii;


import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class Destino3Activity extends AppCompatActivity {

    private TextToSpeech tts;
    private Button btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destino1);

        btnVolver = findViewById(R.id.btnVolver);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("es", "ES"));
                hablar("Has llegado al destino uno. Presiona el botón para volver al menú.");
            }
        });

        btnVolver.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    hablar("Volver al menú principal");
                    break;
                case MotionEvent.ACTION_UP:
                    startActivity(new Intent(Destino3Activity.this, MainActivity.class));
                    finish(); // opcional, para que no regrese aquí con “atrás”
                    break;
            }
            return true;
        });
    }

    private void hablar(String texto) {
        tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
