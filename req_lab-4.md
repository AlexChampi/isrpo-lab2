# Создаём эксперимент

```shell

curl -X POST http://localhost:8080/experiments \
  -H "Content-Type: application/json" \
  -d '{"name":"button_color_test","variants":[{"name":"blue","weight":50},{"name":"red","weight":50}]}'

```

# Запускаем

```shell

curl -X POST http://localhost:8080/experiments/1/start
```

# Назначаем варианты

```shell

for i in $(seq 1 20); do
  curl -s http://localhost:8080/experiments/1/assign > /dev/null
done
```

# Создаём флаг и переключаем

```shell

curl -X POST http://localhost:8080/feature-flags \
  -H "Content-Type: application/json" \
  -d '{"key":"dark_mode"}'
```

```shell

curl -X POST http://localhost:8080/feature-flags/1/enable
```

```shell
curl -X POST http://localhost:8080/feature-flags/1/disable
```

# Генерируем ошибки (404)

```shell

curl http://localhost:8080/experiments/999/assign &
curl http://localhost:8080/feature-flags/999
```