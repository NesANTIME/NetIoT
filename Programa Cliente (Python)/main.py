import threading
import time
import webbrowser
import uvicorn
from api import app, generate_startup_otp

SCANNER_PORT = 8765
DASHBOARD_URL = "http://localhost:8080/dashboard"

def _open_browser():
    time.sleep(1.5)
    otp = generate_startup_otp()
    url = f"{DASHBOARD_URL}?scanner_otp={otp}&scanner_port={SCANNER_PORT}"
    print(f"[IoT Scanner] Abriendo panel: {url}")
    webbrowser.open(url)

if __name__ == "__main__":
    print(f"[IoT Scanner] Servidor iniciando en http://127.0.0.1:{SCANNER_PORT}")
    print(f"[IoT Scanner] El panel se abrirá automáticamente...")

    threading.Thread(target=_open_browser, daemon=True).start()

    uvicorn.run(
        app,
        host="127.0.0.1",
        port=SCANNER_PORT,
        log_level="warning",
    )
