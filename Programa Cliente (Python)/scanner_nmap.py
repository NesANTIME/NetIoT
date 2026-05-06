import subprocess
import socket
import xml.etree.ElementTree as ET
import re

def _local_network() -> str:
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        parts = ip.split(".")
        return f"{parts[0]}.{parts[1]}.{parts[2]}.0/24"
    except Exception:
        return "192.168.1.0/24"

def _parse_xml(xml_output: str) -> list[dict]:
    devices = []
    root = ET.fromstring(xml_output)
    for host in root.findall("host"):
        status = host.find("status")
        if status is None or status.get("state") != "up":
            continue

        ip = mac = vendor = hostname = ""
        for addr in host.findall("address"):
            if addr.get("addrtype") == "ipv4":
                ip = addr.get("addr", "")
            elif addr.get("addrtype") == "mac":
                mac = addr.get("addr", "")
                vendor = addr.get("vendor", "")

        hostnames_el = host.find("hostnames")
        if hostnames_el is not None:
            for hn in hostnames_el.findall("hostname"):
                hostname = hn.get("name", "")
                break

        if not ip:
            continue

        nombre = hostname or (f"IoT {vendor.split()[0]}" if vendor else f"Dispositivo {ip}")
        devices.append({
            "nombreDispositivo": nombre,
            "tipo": "Dispositivo de red",
            "modelo": "",
            "descripcion": "Detectado mediante NMAP",
            "fabricanteNombre": vendor or "Desconocido",
            "fabricantePais": "",
            "fabricanteSitioWeb": "",
            "ipAddress": ip,
            "macAddress": mac,
            "ubicacion": "",
        })
    return devices

def _parse_text(output: str) -> list[dict]:
    devices = []
    lines = output.splitlines()
    for i, line in enumerate(lines):
        if "Nmap scan report for" not in line:
            continue
        ip_match = re.search(r"(\d+\.\d+\.\d+\.\d+)", line)
        if not ip_match:
            continue
        ip = ip_match.group(1)
        hn_match = re.match(r"Nmap scan report for ([^\s(]+)", line)
        hostname = hn_match.group(1) if hn_match and "." not in hn_match.group(1) else ""

        mac = vendor = ""
        if i + 1 < len(lines) and "MAC Address:" in lines[i + 1]:
            mac_match = re.search(r"MAC Address: ([0-9A-F:]+)\s+\(([^)]+)\)", lines[i + 1])
            if mac_match:
                mac = mac_match.group(1)
                vendor = mac_match.group(2)

        nombre = hostname or (f"IoT {vendor.split()[0]}" if vendor else f"Dispositivo {ip}")
        devices.append({
            "nombreDispositivo": nombre,
            "tipo": "Dispositivo de red",
            "modelo": "",
            "descripcion": "Detectado mediante NMAP",
            "fabricanteNombre": vendor or "Desconocido",
            "fabricantePais": "",
            "fabricanteSitioWeb": "",
            "ipAddress": ip,
            "macAddress": mac,
            "ubicacion": "",
        })
    return devices

def scan() -> list[dict]:
    network = _local_network()
    try:
        result = subprocess.run(
            ["nmap", "-sn", "--oX", "-", network],
            capture_output=True, text=True, timeout=120
        )
        if result.returncode == 0 and result.stdout.strip():
            return _parse_xml(result.stdout)
        result2 = subprocess.run(
            ["nmap", "-sP", network],
            capture_output=True, text=True, timeout=120
        )
        return _parse_text(result2.stdout)
    except subprocess.TimeoutExpired:
        raise RuntimeError("NMAP tardó demasiado. Intenta con el modo integrado.")
    except FileNotFoundError:
        raise RuntimeError("NMAP no está instalado. Usa el modo integrado o instala NMAP.")
