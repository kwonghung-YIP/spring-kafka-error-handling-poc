```bash
skaffold build \
  --profile microk8s \
  --skip-tests=true
```

```bash  
skaffold dev \
  --profile microk8s \
  --skip-tests=true \
  --tail=false \
  --port-forward=user
```

```bash
watch -n 1 "curl -s 'http://localhost:8080/api/topn?topn=20' | jq -r '.[]|[.counter,.crtTimestamp]|@tsv'"
```