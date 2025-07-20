package com.example.roboticaii;


import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private Button btn1, btn2, btn3;

    private boolean btn1Pressed = false;
    private boolean btn2Pressed = false;
    private boolean btn3Pressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = findViewById(R.id.btnDestino1);
        btn2 = findViewById(R.id.btnDestino2);
        btn3 = findViewById(R.id.btnDestino3);

        // Inicializar TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("es", "ES"));
                hablar("Bienvenido. Toque un botón para escuchar el destino. Mantenga presionado para seleccionar.");
            }
        });

        configurarBoton(btn1, "Destino 1", Destino1Activity.class);
        configurarBoton(btn2, "Destino 2", Destino2Activity.class);
        configurarBoton(btn3, "Destino 3", Destino3Activity.class);
    }

    private void configurarBoton(Button boton, String mensaje, Class<?> destino) {
        boton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    hablar(mensaje);
                    break;
                case MotionEvent.ACTION_UP:
                    startActivity(new Intent(MainActivity.this, destino));
                    break;
            }
            return true; // Evita que el clic normal se dispare
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
