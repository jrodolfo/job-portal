# Insomnia Exports

To avoid leaking private infrastructure details (for example EC2 hostnames), use this workflow:

1. Keep `insomnia-template.yaml` as the only tracked file in git.
2. Create your local working copy with real values:
   - `insomnia-local.yaml` (or any `*-private.yaml`)
3. Do not commit local/private files (they are ignored by `.gitignore`).

Suggested flow:

```bash
cp doc/insomnia/insomnia-template.yaml doc/insomnia/insomnia-local.yaml
```

Then update `doc/insomnia/insomnia-local.yaml` with your real tokens as needed.

URL host configuration is variable-based in the template:

- `local_api_base_url` defaults to `http://localhost:8080`
- `aws_api_base_url` defaults to `http://aws-api.example.com:8080`

For AWS usage, update `aws_api_base_url` once in Insomnia (or your local copy) with your real EC2 hostname.
