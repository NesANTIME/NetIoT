import secrets
import time
import hmac
import hashlib
import base64
import json
from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pydantic import BaseModel

_SECRET = secrets.token_hex(32)
_TOKEN_TTL = 3600

_otps: dict[str, float] = {}
_scan_mode = {"mode": "nmap"}

app = FastAPI(title="IoT Scanner", docs_url=None, redoc_url=None)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://127.0.0.1:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

_bearer = HTTPBearer()


def _make_token() -> str:
    payload = json.dumps({"sub": "scanner", "exp": int(time.time()) + _TOKEN_TTL})
    payload_b64 = base64.urlsafe_b64encode(payload.encode()).decode().rstrip("=")
    sig = hmac.new(_SECRET.encode(), payload_b64.encode(), hashlib.sha256).hexdigest()
    return f"{payload_b64}.{sig}"


def _verify_token(token: str) -> dict:
    parts = token.split(".", 1)
    if len(parts) != 2:
        raise ValueError("Formato inválido")
    payload_b64, sig = parts
    expected = hmac.new(_SECRET.encode(), payload_b64.encode(), hashlib.sha256).hexdigest()
    if not hmac.compare_digest(sig, expected):
        raise ValueError("Firma inválida")
    padding = (4 - len(payload_b64) % 4) % 4
    payload = json.loads(base64.urlsafe_b64decode(payload_b64 + "=" * padding).decode())
    if payload.get("exp", 0) < time.time():
        raise ValueError("Token expirado")
    return payload


def _require_token(creds: HTTPAuthorizationCredentials = Depends(_bearer)):
    try:
        _verify_token(creds.credentials)
    except Exception as e:
        raise HTTPException(status_code=401, detail=str(e))


def generate_startup_otp() -> str:
    otp = secrets.token_urlsafe(20)
    _otps[otp] = time.time() + 90
    return otp


@app.post("/api/auth/request-otp")
def request_otp():
    otp = secrets.token_urlsafe(20)
    _otps[otp] = time.time() + 60
    return {"otp": otp}


class OTPBody(BaseModel):
    otp: str


@app.post("/api/auth/otp-exchange")
def otp_exchange(body: OTPBody):
    exp = _otps.pop(body.otp, None)
    if exp is None or time.time() > exp:
        raise HTTPException(status_code=401, detail="OTP inválido o expirado")
    return {"token": _make_token(), "username": "IoT Scanner"}


@app.get("/api/scan-mode")
def get_mode(_=Depends(_require_token)):
    return {"mode": _scan_mode["mode"]}


class ModeBody(BaseModel):
    mode: str


@app.post("/api/scan-mode")
def set_mode(body: ModeBody, _=Depends(_require_token)):
    if body.mode not in ("nmap", "builtin"):
        raise HTTPException(status_code=400, detail="Modo inválido. Usa 'nmap' o 'builtin'")
    _scan_mode["mode"] = body.mode
    return {"mode": body.mode}


@app.get("/api/scan")
def run_scan(mode: str = "nmap", _=Depends(_require_token)):
    try:
        if mode == "builtin":
            from source.core.module.openscan import scan
        else:
            from source.core.module.scanner_nmap import scan
        return scan()
    except RuntimeError as e:
        raise HTTPException(status_code=503, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error de escaneo: {str(e)}")
