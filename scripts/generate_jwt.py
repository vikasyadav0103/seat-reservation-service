"""Generate a temporary HS256 JWT for local or deployment smoke testing."""

import argparse
import base64
import hashlib
import hmac
import json
import os
import time


def encode_part(value: object) -> str:
    raw = json.dumps(value, separators=(",", ":")).encode("utf-8")
    return base64.urlsafe_b64encode(raw).rstrip(b"=").decode("ascii")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--subject", required=True)
    parser.add_argument("--role", choices=("ADMIN", "USER"), required=True)
    parser.add_argument("--expires-in", type=int, default=3600)
    args = parser.parse_args()

    secret = os.environ.get("JWT_SECRET")
    if not secret or len(secret) < 32:
        raise SystemExit("Set JWT_SECRET in the current terminal; it must be at least 32 characters.")

    now = int(time.time())
    header = {"alg": "HS256", "typ": "JWT"}
    payload = {
        "sub": args.subject,
        "roles": [args.role],
        "iat": now,
        "exp": now + args.expires_in,
    }
    unsigned = f"{encode_part(header)}.{encode_part(payload)}"
    signature = hmac.new(
        secret.encode("utf-8"), unsigned.encode("ascii"), hashlib.sha256
    ).digest()
    token = f"{unsigned}.{base64.urlsafe_b64encode(signature).rstrip(b'=').decode('ascii')}"
    print(token)


if __name__ == "__main__":
    main()

# $env:JWT_SECRET="your-configured-secret"
# $adminJwt = python scripts\generate_jwt.py --subject admin-test --role ADMIN
# $userJwt = python scripts\generate_jwt.py --subject user-test --role USER
