import threading
import logging
import signal
import sys
import time
import webbrowser

import uvicorn

# ─────────────────────────────────────────────────────────
from source.core.api_rest import app, generate_startup_otp
# ─────────────────────────────────────────────────────────

SCANNER_HOST        = "127.0.0.1"
DASHBOARD_URL = "http://localhost:8080/dashboard"
SCANNER_PORT        = 8000
BROWSER_DELAY       = 1.2    
UVICORN_LOG_LEVEL   = "warning"

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s — %(message)s",
    datefmt="%H:%M:%S",
)
log = logging.getLogger("kernel")

def _open_browser():
    time.sleep(1.5)
    otp = generate_startup_otp()
    url = f"{DASHBOARD_URL}?scanner_otp={otp}&scanner_port={SCANNER_PORT}"
    print(f"[IoT Scanner] Abriendo panel: {url}")
    webbrowser.open(url)


# ─────────────────────────────────────────────
#  KERNEL
# ─────────────────────────────────────────────

class _UvicornThread(threading.Thread):
    def __init__(self, app, host: str, port: int, log_level: str):
        super().__init__(name="uvicorn-thread", daemon=True)
        self.app       = app
        self.host      = host
        self.port      = port
        self.log_level = log_level
        self.failed    = threading.Event()
        self.exc: Exception | None = None

    def run(self):
        try:
            uvicorn.run(
                self.app,
                host=self.host,
                port=self.port,
                log_level=self.log_level,
            )
        except Exception as exc:
            self.exc = exc
            self.failed.set()
            log.critical("✖  Uvicorn falló: %s", exc, exc_info=True)


class _BrowserThread(threading.Thread):
    def __init__(self, target):
        super().__init__(name="browser-thread", daemon=True)
        self._target = target

    def run(self):
        try:
            self._target()
        except Exception as exc:
            log.warning("⚠  Browser thread falló (no crítico): %s", exc)


class ProcessKernel:
    def __init__(self, app):
        self.app = app
        self._stop = threading.Event()

    def _setup_signals(self):
        def _handler(signum, frame):
            name = signal.Signals(signum).name
            log.info("🔔  Señal recibida: %s — iniciando shutdown…", name)
            self._stop.set()

        signal.signal(signal.SIGINT,  _handler)
        signal.signal(signal.SIGTERM, _handler)

    def run(self):
        log.info("═" * 52)
        log.info("  KERNEL  arrancando en http://%s:%d", SCANNER_HOST, SCANNER_PORT)
        log.info("═" * 52)

        self._setup_signals()

        uv = _UvicornThread(
            app=self.app,
            host=SCANNER_HOST,
            port=SCANNER_PORT,
            log_level=UVICORN_LOG_LEVEL,
        )
        uv.start()

        browser = _BrowserThread(target=_open_browser)
        browser.start()

        log.info("✅  Kernel activo — esperando eventos…")
        try:
            while not self._stop.is_set():
                if uv.failed.is_set():
                    log.critical("💀  Uvicorn terminó inesperadamente. Cerrando kernel.")
                    sys.exit(1)

                if not uv.is_alive():
                    log.warning("⏹  Uvicorn terminó. Cerrando kernel.")
                    sys.exit(0)

                time.sleep(0.25)

        finally:
            log.info("⏹  Kernel detenido.")



ProcessKernel(app).run()