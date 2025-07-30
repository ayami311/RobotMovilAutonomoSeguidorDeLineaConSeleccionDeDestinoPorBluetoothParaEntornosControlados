#include <WiFi.h>
#include <WebServer.h>

// Pines de sensores FC-51
const int sensorIzq = 2;
const int sensorCen = 4;
const int sensorDer = 1;

// Pines del driver L298N
const int IN1 = 14;
const int IN2 = 15;
const int IN3 = 13;
const int IN4 = 12;

// Pines del sensor ultrasónico
const int TRIG_PIN = 0;
const int ECHO_PIN = 3;

// WiFi
const char* ssid = "DENILAYDA 2.4G";
const char* password = "41918661";

WebServer server(80);
String destino = "N";

// Variables para controlador PD
int error = 0;
int lastError = 0;
float Kp = 50;  // Proporcional
float Kd = 30;  // Derivativa

// -------------------- SETUP --------------------
void setup() {
  Serial.begin(115200);

  pinMode(sensorIzq, INPUT);
  pinMode(sensorCen, INPUT);
  pinMode(sensorDer, INPUT);

  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);

  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);

  conectarWiFi();
  configurarServidor();

  Serial.println("Listo para seguir línea...");
}

// -------------------- LOOP --------------------
void loop() {
  server.handleClient();

  int izq = digitalRead(sensorIzq);
  int cen = digitalRead(sensorCen);
  int der = digitalRead(sensorDer);
  float distancia = medirDistancia();

  if (distancia < 10) {
    detener();
    return;
  }

  // Si está en cruce, decidir según destino
  if (cen == LOW && izq == LOW && der == LOW) {
    if (destino == "A") {
      girarArriba();
      destino = "N";
    } else if (destino == "B") {
      girarAbajo();
      destino = "N";
    } else if (destino == "C") {
      girarIzquierdaLargo();
      destino = "N";
    } else {
      avanzar();
    }
    return;
  }

  // Controlador PD para seguir la línea
  if (izq == LOW && cen == HIGH && der == HIGH) error = -2;
  else if (izq == LOW && cen == LOW && der == HIGH) error = -1;
  else if (izq == HIGH && cen == LOW && der == HIGH) error = 0;
  else if (izq == HIGH && cen == LOW && der == LOW) error = 1;
  else if (izq == HIGH && cen == HIGH && der == LOW) error = 2;
  else error = 0;

  int output = Kp * error + Kd * (error - lastError);
  lastError = error;

  ajustarMotores(output);
}

// -------------------- MOTORES --------------------
void avanzar() {
  digitalWrite(IN1, HIGH); digitalWrite(IN2, LOW);
  digitalWrite(IN3, HIGH); digitalWrite(IN4, LOW);
}

void detener() {
  digitalWrite(IN1, LOW); digitalWrite(IN2, LOW);
  digitalWrite(IN3, LOW); digitalWrite(IN4, LOW);
}

void girarIzquierda() {
  digitalWrite(IN1, LOW); digitalWrite(IN2, HIGH);
  digitalWrite(IN3, HIGH); digitalWrite(IN4, LOW);
}

void girarDerecha() {
  digitalWrite(IN1, HIGH); digitalWrite(IN2, LOW);
  digitalWrite(IN3, LOW); digitalWrite(IN4, HIGH);
}

void girarArriba() {
  girarDerecha(); delay(600);
  avanzar(); delay(500);
}

void girarAbajo() {
  girarIzquierda(); delay(600);
  avanzar(); delay(500);
}

void girarIzquierdaLargo() {
  girarIzquierda(); delay(1000);
  avanzar(); delay(500);
}

void ajustarMotores(int correccion) {
  int baseSpeed = 150;
  int velocidadIzq = constrain(baseSpeed - correccion, 0, 255);
  int velocidadDer = constrain(baseSpeed + correccion, 0, 255);

  // Motor izquierdo
  analogWrite(IN1, velocidadIzq);
  digitalWrite(IN2, LOW);

  // Motor derecho
  analogWrite(IN3, velocidadDer);
  digitalWrite(IN4, LOW);
}

// -------------------- ULTRASONIDO --------------------
float medirDistancia() {
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);
  long duracion = pulseIn(ECHO_PIN, HIGH, 20000);
  return duracion * 0.034 / 2;
}

// -------------------- WIFI --------------------
void conectarWiFi() {
  Serial.print("Conectando a WiFi: ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);

  int intentos = 0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    if (++intentos > 20) {
      Serial.println("\nError de conexión. Reiniciando...");
      ESP.restart();
    }
  }

  Serial.println("\n¡Conectado!");
  Serial.print("Dirección IP: ");
  Serial.println(WiFi.localIP());
}

// -------------------- SERVIDOR WEB --------------------
void configurarServidor() {
  server.on("/", []() {
    String html = "<html><body><h1>Selecciona un salón</h1>";
    html += "<a href='/A'><button style='font-size:24px;'>Salón A</button></a><br><br>";
    html += "<a href='/B'><button style='font-size:24px;'>Salón B</button></a><br><br>";
    html += "<a href='/C'><button style='font-size:24px;'>Salón C</button></a><br><br>";
    html += "</body></html>";
    server.send(200, "text/html", html);
  });

  server.on("/A", []() {
    destino = "A";
    server.send(200, "text/plain", "Dirigiéndose a Salón A...");
  });

  server.on("/B", []() {
    destino = "B";
    server.send(200, "text/plain", "Dirigiéndose a Salón B...");
  });

  server.on("/C", []() {
    destino = "C";
    server.send(200, "text/plain", "Dirigiéndose a Salón C...");
  });

  server.begin();
  Serial.println("Servidor web iniciado.");
}