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


const bool RELAY_ACTIVE_LOW = true;

struct Relay {
  uint8_t     pin;
  const char* name;
  bool        state;
};

Relay relays[] = {
  { 17, "Relé TX2", false },
};

const uint8_t NUM_RELAYS = sizeof(relays) / sizeof(relays[0]);

AsyncUDP udp;

void setRelay(uint8_t index, bool on) {
  if (index >= NUM_RELAYS) return;
  relays[index].state = on;
  digitalWrite(relays[index].pin, RELAY_ACTIVE_LOW ? !on : on);
  Serial.printf("[RELAY] #%d '%s' → %s\n",
                index, relays[index].name, on ? "ON" : "OFF");
}

bool relaysAllOn() {
  for (uint8_t i = 0; i < NUM_RELAYS; i++) {
    if (!relays[i].state) return false;
  }
  return NUM_RELAYS > 0;
}

String buildIdentityJSON() {
  JsonDocument doc;

  doc["nombreDispositivo"] = "IoT Espressif";
  doc["tipo"]              = "Modulo OpenScan";
  doc["modelo"]            = "ESP32-NesANTime";
  doc["descripcion"]       = "Detectado en red local (modo integrado)";
  doc["fabricanteNombre"]  = "OpenAPIoT NesAnTime";
  doc["fabricantePais"]    = "";
  doc["fabricanteSitioWeb"] = "";
  doc["ipAddress"]         = WiFi.localIP().toString();
  doc["macAddress"]        = WiFi.macAddress();
  doc["ubicacion"]         = "";

  String out;
  serializeJson(doc, out);
  return out;
}

String buildErrorJSON(const char* message) {
  JsonDocument doc;
  doc["ok"]    = false;
  doc["error"] = message;
  String out;
  serializeJson(doc, out);
  return out;
}

String buildOkJSON() {
  JsonDocument doc;
  doc["ok"]         = true;
  doc["powerState"] = relaysAllOn() ? "on" : "off";
  JsonArray arr = doc["relays"].to<JsonArray>();
  for (uint8_t i = 0; i < NUM_RELAYS; i++) {
    JsonObject r = arr.add<JsonObject>();
    r["id"]    = i;
    r["name"]  = relays[i].name;
    r["state"] = relays[i].state;
  }
  String out;
  serializeJson(doc, out);
  return out;
}

String processCommand(const String& raw) {
  JsonDocument doc;
  DeserializationError err = deserializeJson(doc, raw);
  if (err) {
    return buildErrorJSON("JSON invalido");
  }

  const char* cmd = doc["cmd"] | "";

  if (strcmp(cmd, "relay") == 0) {
    int  id    = doc["id"] | -1;
    bool state = doc["state"] | false;
    if (id < 0 || id >= NUM_RELAYS) return buildErrorJSON("id fuera de rango");
    setRelay(id, state);
    return buildOkJSON();
  }

  if (strcmp(cmd, "toggle") == 0) {
    int id = doc["id"] | -1;
    if (id < 0 || id >= NUM_RELAYS) return buildErrorJSON("id fuera de rango");
    setRelay(id, !relays[id].state);
    return buildOkJSON();
  }


  if (strcmp(cmd, "all") == 0) {
    bool state = doc["state"] | false;
    for (uint8_t i = 0; i < NUM_RELAYS; i++) setRelay(i, state);
    return buildOkJSON();
  }


  if (strcmp(cmd, "status") == 0) {
    return buildOkJSON();
  }

  return buildErrorJSON("comando desconocido");
}


void onUDPPacket(AsyncUDPPacket& packet) {
  size_t len  = packet.length();
  uint8_t* data = packet.data();

  Serial.printf("[UDP] Paquete de %s:%d  (%d bytes)\n",
                packet.remoteIP().toString().c_str(),
                packet.remotePort(), len);

  String response;

  if (len == 4 &&
      data[0] == 'J' && data[1] == 'R' &&
      data[2] == 'J' && data[3] == 'E') {

    Serial.println("[UDP] Magic JRJE recibido — respondiendo identidad");
    response = buildIdentityJSON();

  } else {
    String raw = String((char*)data, len);
    Serial.printf("[UDP] Comando: %s\n", raw.c_str());
    response = processCommand(raw);
  }

  packet.print(response);
  Serial.printf("[UDP] Respuesta enviada (%d bytes)\n", response.length());
}


void setup() {
  Serial.begin(115200);
  delay(500);
  Serial.println("\n\n=== ESP32 OpenAPIoT ===");

  for (uint8_t i = 0; i < NUM_RELAYS; i++) {
    pinMode(relays[i].pin, OUTPUT);
    setRelay(i, false);
  }

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

  if (udp.listen(JRJE_PORT)) {
    udp.onPacket(onUDPPacket);
    Serial.printf("✓ Servidor UDP escuchando en puerto %d\n", JRJE_PORT);
  } else {
    Serial.println("✗ Error al abrir el puerto UDP");
  }

  Serial.println("Sistema listo.\n");
}


void loop() {
  delay(10);
}
