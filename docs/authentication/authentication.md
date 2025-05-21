## Architecture Overview

```text
[ Client ] <--HTTPS--> [ NGINX ] <--mTLS--> [ kms ] <┐
                                      \             mTLS
                                        --> [ auth ] <┘
                                      \
                                        --> [ backend ]
                                    
```

Block backend-originated traffic from being routed to kms/auth
```nginx
if ($ssl_client_s_dn !~ "CN=client-backend") {
  return 403
}
```

## Certificate
- [cert.md](./cert.md)

## User Authentication
- [jwt.md](./jwt.md)

## Further Improvements (Not Yet Implemented)

- Hardend NGINX (because nignx is a single point of trust)
  - possible mitigations: WAF, rate limits, minimal access, read-only root FS, cert pinning
