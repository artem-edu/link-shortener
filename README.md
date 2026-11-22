### Сборка и запуск проекта

```bash
cp example.env .env
```
Сборка проекта:
```bash
mvn clean package
```
Запуск:
```bash
mvn exec:java -Dexec.mainClass="mephi.Main"
```

Сборка и запуск через podman (docker):

```bash
podman build -t link-short .  
```

```bash
podman run -it --rm \
  -v $(pwd)/data:/data:Z \
  -v $(pwd)/.env:/data/.env:ro,Z \
  link-short:latest
```
