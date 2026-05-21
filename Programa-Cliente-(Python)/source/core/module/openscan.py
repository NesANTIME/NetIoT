import socket
import ipaddress
import concurrent.futures
import json

JRJE_MAGIC  = b"JRJE"
JRJE_PORT   = 8080
UDP_TIMEOUT = 0.4
MAX_WORKERS = 80

def _local_network() -> tuple[str, str]:
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        parts = ip.split(".")
        return f"{parts[0]}.{parts[1]}.{parts[2]}.0/24", ip
    except Exception:
        return "192.168.1.0/24", ""

def _scan_api(ip: str) -> tuple[bool, dict | None]:
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.settimeout(UDP_TIMEOUT)
    try:
        sock.sendto(JRJE_MAGIC, (ip, JRJE_PORT))
        data, _ = sock.recvfrom(2048)
        payload = json.loads(data.decode("utf-8"))
        return True, payload
    except (socket.timeout, OSError, json.JSONDecodeError, UnicodeDecodeError):
        return False, None
    finally:
        sock.close()

def _map_device(raw: dict, ip: str) -> dict:
    return {
        "nombreDispositivo": raw.get("nombreDispositivo", raw.get("name", f"Dispositivo {ip}")),
        "tipo":              raw.get("tipo", "Módulo IoT"),
        "modelo":            raw.get("modelo", ""),
        "descripcion":       raw.get("descripcion", "Detectado mediante protocolo JRJE"),
        "fabricanteNombre":  raw.get("fabricanteNombre", raw.get("fabricante", "Desconocido")),
        "fabricantePais":    raw.get("fabricantePais", ""),
        "fabricanteSitioWeb": raw.get("fabricanteSitioWeb", ""),
        "ipAddress":         raw.get("ipAddress", ip),
        "macAddress":        raw.get("macAddress", raw.get("mac", "")),
        "ubicacion":         raw.get("ubicacion", ""),
    }

def scan() -> list[dict]:
    network, _ = _local_network()
    net   = ipaddress.ip_network(network, strict=False)
    hosts = [str(h) for h in net.hosts()][:254]
    results: list[dict] = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=MAX_WORKERS) as ex:
        futures = {ex.submit(_scan_api, ip): ip for ip in hosts}
        for future in concurrent.futures.as_completed(futures):
            ip = futures[future]
            try:
                found, data = future.result()
                if found and data is not None:
                    results.append(_map_device(data, ip))
            except Exception:
                pass
    return results