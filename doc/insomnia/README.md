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

Then update `doc/insomnia/insomnia-local.yaml` with your real hostnames/tokens as needed.
