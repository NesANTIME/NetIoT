import json
import socket

JRJE_PORT = 8080
UDP_TIMEOUT = 2.5

SIGNAL_COMMANDS: dict[str, dict] = {
    "encendido": {"cmd": "all", "state": True},
    "apagar": {"cmd": "all", "state": False},
}

STATUS_COMMAND = {"cmd": "status"}


def _udp_exchange(ip: str, command: dict) -> dict:
    payload = json.dumps(command).encode("utf-8")
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.settimeout(UDP_TIMEOUT)
    try:
        sock.sendto(payload, (ip, JRJE_PORT))
        data, _ = sock.recvfrom(2048)
        try:
            return json.loads(data.decode("utf-8"))
        except (json.JSONDecodeError, UnicodeDecodeError) as e:
            raise RuntimeError("Respuesta del ESP32 no es JSON válido.") from e
    except socket.timeout:
        raise RuntimeError(
            f"El ESP32 en {ip}:{JRJE_PORT} no respondió. Verifica que esté encendido y en la misma red."
        )
    except OSError as e:
        raise RuntimeError(f"No se pudo enviar el comando UDP: {e}") from e
    finally:
        sock.close()


def _parse_esp32_ok(device_response: dict) -> tuple[bool, str]:
    """Valida respuesta del ESP32: ok explícito o sin campo error (compatibilidad)."""
    if not isinstance(device_response, dict):
        return False, "off"
    if device_response.get("error"):
        return False, "off"
    if device_response.get("ok") is True:
        power = device_response.get("powerState")
        if power in ("on", "off"):
            return True, power
        relays = device_response.get("relays")
        if isinstance(relays, list) and relays:
            all_on = all(bool(r.get("state")) for r in relays if isinstance(r, dict))
            return True, "on" if all_on else "off"
        return True, "off"
    if "ok" in device_response and device_response.get("ok") is not True:
        return False, "off"
    return False, "off"


def query_status(ip_address: str) -> dict:
    ip = (ip_address or "").strip()
    if not ip:
        raise ValueError("Se requiere la dirección IP del dispositivo.")
    device = _udp_exchange(ip, STATUS_COMMAND)
    ok, power_state = _parse_esp32_ok(device)
    if not ok:
        err = device.get("error", "El ESP32 no confirmó el estado.")
        raise RuntimeError(str(err))
    return {
        "ok": True,
        "ipAddress": ip,
        "powerState": power_state,
        "device": device,
    }


def send_signal(ip_address: str, action: str) -> dict:
    action_key = (action or "").strip().lower()
    if action_key not in SIGNAL_COMMANDS:
        raise ValueError(f"Acción inválida. Usa: {', '.join(SIGNAL_COMMANDS)}")

    ip = (ip_address or "").strip()
    if not ip:
        raise ValueError("Se requiere la dirección IP del dispositivo.")

    device = _udp_exchange(ip, SIGNAL_COMMANDS[action_key])
    ok, power_state = _parse_esp32_ok(device)
    if not ok:
        err = device.get("error", "El ESP32 no confirmó la acción (sin OK).")
        raise RuntimeError(str(err))

    expected = "on" if action_key == "encendido" else "off"
    if power_state != expected:
        raise RuntimeError(
            f"El ESP32 respondió OK pero el estado reportado es «{power_state}», se esperaba «{expected}»."
        )

    return {
        "ok": True,
        "action": action_key,
        "ipAddress": ip,
        "powerState": power_state,
        "command": SIGNAL_COMMANDS[action_key],
        "device": device,
    }
