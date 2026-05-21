#include <WiFi.h>
#include <AsyncUDP.h>
#include <ArduinoJson.h>

const char* WIFI_SSID     = "VANESA";
const char* WIFI_PASSWORD = "chilly23";

// ─────────────────────────────────────────────
// Identidad del dispositivo
// ─────────────────────────────────────────────
const char* DEVICE_ID       = "ESP32-001";
const char* DEVICE_NAME     = "Controlador Relés Sala";
const char* FIRMWARE_VER    = "1.0.0";
const uint16_t JRJE_PORT    = 8080;

// ─────────────────────────────────────────────
// Configuración de relés
//   Ajusta los pines según tu hardware.
//   RELAY_ACTIVE_LOW = true  → módulo de relé típico (LOW = encendido)
//   RELAY_ACTIVE_LOW = false → relé activo en HIGH
// ─────────────────────────────────────────────
const bool RELAY_ACTIVE_LOW = true;

struct Relay {
  uint8_t     pin;
  const char* name;
  bool        state;   // true = encendido
};

Relay relays[] = {
  { 26, "Luz principal",   false },
  { 27, "Ventilador",      false },
  { 14, "Tomacorriente 1", false },
  { 12, "Tomacorriente 2", false },
};
const uint8_t NUM_RELAYS = sizeof(relays) / sizeof(relays[0]);

// ─────────────────────────────────────────────
// Variables globales
// ─────────────────────────────────────────────
AsyncUDP udp;

// ─────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────
void setRelay(uint8_t index, bool on) {
  if (index >= NUM_RELAYS) return;
  relays[index].state = on;
  digitalWrite(relays[index].pin, RELAY_ACTIVE_LOW ? !on : on);
  Serial.printf("[RELAY] #%d '%s' → %s\n",
                index, relays[index].name, on ? "ON" : "OFF");
}

// ─────────────────────────────────────────────
// Construye el JSON de respuesta JRJE
// ─────────────────────────────────────────────
String buildIdentityJSON() {
  JsonDocument doc;

  doc["nombreDispositivo"] = "IoT Espressif";
  doc["tipo"]              = "Módulo IoT";
  doc["modelo"]            = "";
  doc["descripcion"]       = "Detectado en red local (modo integrado)";
  doc["fabricanteNombre"]  = "Espressif Systems";
  doc["fabricantePais"]    = "";
  doc["fabricanteSitioWeb"] = "";
  doc["ipAddress"]         = WiFi.localIP().toString();
  doc["macAddress"]        = WiFi.macAddress();
  doc["ubicacion"]         = "";

  String out;
  serializeJson(doc, out);
  return out;
}

// ─────────────────────────────────────────────
// Procesa un comando JSON recibido
//
//  Ejemplo de comandos:
//
//  Encender relé 0:
//    {"cmd":"relay","id":0,"state":true}
//
//  Apagar relé 2:
//    {"cmd":"relay","id":2,"state":false}
//
//  Toggle relé 1:
//    {"cmd":"toggle","id":1}
//
//  Pedir estado (responde JSON):
//    {"cmd":"status"}
//
//  Todos ON / todos OFF:
//    {"cmd":"all","state":true}
//    {"cmd":"all","state":false}
// ─────────────────────────────────────────────
String processCommand(const String& raw) {
  JsonDocument doc;
  DeserializationError err = deserializeJson(doc, raw);
  if (err) {
    return "{\"error\":\"JSON invalido\"}";
  }

  const char* cmd = doc["cmd"] | "";

  // ── relay ──
  if (strcmp(cmd, "relay") == 0) {
    int  id    = doc["id"] | -1;
    bool state = doc["state"] | false;
    if (id < 0 || id >= NUM_RELAYS) return "{\"error\":\"id fuera de rango\"}";
    setRelay(id, state);
    return buildIdentityJSON();
  }

  // ── toggle ──
  if (strcmp(cmd, "toggle") == 0) {
    int id = doc["id"] | -1;
    if (id < 0 || id >= NUM_RELAYS) return "{\"error\":\"id fuera de rango\"}";
    setRelay(id, !relays[id].state);
    return buildIdentityJSON();
  }

  // ── all ──
  if (strcmp(cmd, "all") == 0) {
    bool state = doc["state"] | false;
    for (uint8_t i = 0; i < NUM_RELAYS; i++) setRelay(i, state);
    return buildIdentityJSON();
  }

  // ── status ──
  if (strcmp(cmd, "status") == 0) {
    return buildIdentityJSON();
  }

  return "{\"error\":\"comando desconocido\"}";
}

// ─────────────────────────────────────────────
// Manejador de paquetes UDP
// ─────────────────────────────────────────────
void onUDPPacket(AsyncUDPPacket& packet) {
  size_t len  = packet.length();
  uint8_t* data = packet.data();

  Serial.printf("[UDP] Paquete de %s:%d  (%d bytes)\n",
                packet.remoteIP().toString().c_str(),
                packet.remotePort(), len);

  String response;

  // ── JRJE magic → responder identidad ──
  if (len == 4 &&
      data[0] == 'J' && data[1] == 'R' &&
      data[2] == 'J' && data[3] == 'E') {

    Serial.println("[UDP] Magic JRJE recibido — respondiendo identidad");
    response = buildIdentityJSON();

  } else {
    // ── Intentar parsear como comando JSON ──
    String raw = String((char*)data, len);
    Serial.printf("[UDP] Comando: %s\n", raw.c_str());
    response = processCommand(raw);
  }

  packet.print(response);
  Serial.printf("[UDP] Respuesta enviada (%d bytes)\n", response.length());
}

// ─────────────────────────────────────────────
// Setup
// ─────────────────────────────────────────────
void setup() {
  Serial.begin(115200);
  delay(500);
  Serial.println("\n\n=== ESP32 JRJE — Control de Relés ===");

  // Inicializar pines de relés
  for (uint8_t i = 0; i < NUM_RELAYS; i++) {
    pinMode(relays[i].pin, OUTPUT);
    setRelay(i, false);  // todos apagados al iniciar
  }

  // Conectar WiFi
  Serial.printf("Conectando a %s", WIFI_SSID);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  Serial.printf("✓ Conectado. IP: %s  MAC: %s\n",
                WiFi.localIP().toString().c_str(),
                WiFi.macAddress().c_str());

  // Iniciar servidor UDP
  if (udp.listen(JRJE_PORT)) {
    udp.onPacket(onUDPPacket);
    Serial.printf("✓ Servidor UDP escuchando en puerto %d\n", JRJE_PORT);
  } else {
    Serial.println("✗ Error al abrir el puerto UDP");
  }

  Serial.println("Sistema listo.\n");
}

// ─────────────────────────────────────────────
// Loop
// ─────────────────────────────────────────────
void loop() {
  // El servidor UDP es asíncrono — el loop puede usarse
  // para tareas adicionales (leer sensores, MQTT, etc.)
  delay(10);
}
