# Эксперименты
curl -X POST http://localhost:8080/experiments \
  -H "Content-Type: application/json" \
  -d '{"name":"button_color_test","variants":[{"name":"blue","weight":50},{"name":"red","weight":50}]}'

curl -X POST http://localhost:8080/experiments \
  -H "Content-Type: application/json" \
  -d '{"name":"checkout_flow","variants":[{"name":"old_flow","weight":30},{"name":"new_flow","weight":70}]}'

curl -X POST http://localhost:8080/experiments/1/start
curl -X POST http://localhost:8080/experiments/2/start

# Назначения
for i in $(seq 1 50); do
  curl -s http://localhost:8080/experiments/1/assign > /dev/null
  curl -s http://localhost:8080/experiments/2/assign > /dev/null
done

# Feature flags
curl -X POST http://localhost:8080/feature-flags \
  -H "Content-Type: application/json" -d '{"key":"dark_mode"}'

curl -X POST http://localhost:8080/feature-flags \
  -H "Content-Type: application/json" -d '{"key":"new_checkout"}'

curl -X POST http://localhost:8080/feature-flags/1/enable
curl -X POST http://localhost:8080/feature-flags/2/enable
curl -X POST http://localhost:8080/feature-flags/1/disable
curl -X POST http://localhost:8080/feature-flags/2/disable
curl -X POST http://localhost:8080/feature-flags/1/enable

# Проверки флагов
for i in $(seq 1 10); do
  curl -s http://localhost:8080/feature-flags/1 > /dev/null
  curl -s http://localhost:8080/feature-flags/2 > /dev/null
done

# Ошибки
curl http://localhost:8080/experiments/999/assign
curl http://localhost:8080/experiments/888/assign
curl http://localhost:8080/feature-flags/777
curl http://localhost:8080/feature-flags/666

# Остановка эксперимента
curl -X POST http://localhost:8080/experiments/1/stop