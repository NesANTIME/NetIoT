import subprocess
import socket
import re
import ipaddress
import concurrent.futures

def _local_network():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        parts = ip.split(".")
        return f"{parts[0]}.{parts[1]}.{parts[2]}.0/24", ip
    except Exception:
        return "192.168.1.0/24", ""

def _ping(ip: str) -> bool:
    try:
        r = subprocess.run(
            ["ping", "-n", "1", "-w", "400", ip],
            capture_output=True, timeout=2
        )
        return r.returncode == 0
    except Exception:
        return False

def _arp_table() -> dict[str, str]:
    table = {}
    try:
        r = subprocess.run(["arp", "-a"], capture_output=True, text=True, timeout=5)
        for line in r.stdout.splitlines():
            m = re.search(r"(\d+\.\d+\.\d+\.\d+)\s+([\da-fA-F-]{17})", line)
            if m:
                table[m.group(1)] = m.group(2).upper().replace("-", ":")
    except Exception:
        pass
    return table

_OUI = {
    "B8:27:EB": ("Raspberry Pi Foundation", "Microcontrolador"),
    "DC:A6:32": ("Raspberry Pi Foundation", "Microcontrolador"),
    "E4:5F:01": ("Raspberry Pi Foundation", "Microcontrolador"),
    "18:FE:34": ("Espressif Systems", "Módulo IoT"),
    "24:0A:C4": ("Espressif Systems", "Módulo IoT"),
    "A4:CF:12": ("Espressif Systems", "Módulo IoT"),
    "CC:50:E3": ("Espressif Systems", "Módulo IoT"),
    "EC:FA:BC": ("Espressif Systems", "Módulo IoT"),
    "30:AE:A4": ("Espressif Systems", "Módulo IoT"),
    "AC:67:B2": ("Espressif Systems", "Módulo IoT"),
    "84:F3:EB": ("Espressif Systems", "Módulo IoT"),
    "40:F5:20": ("Espressif Systems", "Módulo IoT"),
    "FC:F5:C4": ("Espressif Systems", "Módulo IoT"),
    "34:86:5D": ("Espressif Systems", "Módulo IoT"),
    "78:21:84": ("TP-Link Technologies", "Router/Switch"),
    "60:A4:4C": ("TP-Link Technologies", "Router/Switch"),
    "B0:95:75": ("TP-Link Technologies", "Router/Switch"),
    "A8:96:75": ("Amazon Technologies", "Asistente inteligente"),
    "FC:A1:83": ("Amazon Technologies", "Asistente inteligente"),
    "44:65:0D": ("Amazon Technologies", "Asistente inteligente"),
    "18:74:2E": ("Amazon Technologies", "Asistente inteligente"),
    "3C:BD:D8": ("Amazon Technologies", "Asistente inteligente"),
    "AC:BC:32": ("Apple", "Dispositivo Apple"),
    "D4:40:F0": ("Xiaomi", "Dispositivo inteligente"),
    "AC:C1:EE": ("Xiaomi", "Dispositivo inteligente"),
    "38:F7:3D": ("Samsung Electronics", "Electrodoméstico inteligente"),
    "1C:1B:B5": ("Samsung Electronics", "Electrodoméstico inteligente"),
    "00:0C:29": ("VMware", "Máquina virtual"),
    "08:00:27": ("Oracle VirtualBox", "Máquina virtual"),
}

def _vendor_info(mac: str) -> tuple[str, str]:
    prefix = mac[:8].upper() if len(mac) >= 8 else ""
    entry = _OUI.get(prefix)
    if entry:
        return entry
    return ("", "Dispositivo de red")

def _hostname(ip: str) -> str:
    try:
        return socket.gethostbyaddr(ip)[0]
    except Exception:
        return ""

def scan() -> list[dict]:
    network, local_ip = _local_network()
    net = ipaddress.ip_network(network, strict=False)
    hosts = [str(h) for h in net.hosts()][:254]

    alive: set[str] = set()
    with concurrent.futures.ThreadPoolExecutor(max_workers=60) as ex:
        for ip, up in zip(hosts, ex.map(_ping, hosts)):
            if up:
                alive.add(ip)

    arp = _arp_table()
    prefix = ".".join(network.split(".")[:3])
    for ip in list(arp):
        if ip.startswith(prefix):
            alive.add(ip)

    if local_ip in alive:
        alive.discard(local_ip)

    arp = _arp_table()
    devices = []
    for ip in sorted(alive):
        mac = arp.get(ip, "")
        fabricante, tipo = _vendor_info(mac)
        hostname = _hostname(ip)
        nombre = hostname or (f"IoT {fabricante.split()[0]}" if fabricante else f"Dispositivo {ip}")
        devices.append({
            "nombreDispositivo": nombre,
            "tipo": tipo,
            "modelo": "",
            "descripcion": "Detectado en red local (modo integrado)",
            "fabricanteNombre": fabricante or "Desconocido",
            "fabricantePais": "",
            "fabricanteSitioWeb": "",
            "ipAddress": ip,
            "macAddress": mac,
            "ubicacion": "",
        })
    return devices
