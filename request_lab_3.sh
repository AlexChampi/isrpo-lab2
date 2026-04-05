# 1. Создаём эксперименты
curl -X POST http://localhost:8080/experiments \
  -H "Content-Type: application/json" \
  -d '{"name":"button_color_test","variants":[{"name":"blue","weight":50},{"name":"red","weight":50}]}'

curl -X POST http://localhost:8080/experiments \
  -H "Content-Type: application/json" \
  -d '{"name":"checkout_flow","variants":[{"name":"old_flow","weight":30},{"name":"new_flow","weight":70}]}'

# 2. Запускаем эксперименты
curl -X POST http://localhost:8080/experiments/1/start
curl -X POST http://localhost:8080/experiments/2/start

# 3. Назначаем варианты (основная продуктовая метрика) — 50 раз на каждый
for i in $(seq 1 50); do
  curl -s http://localhost:8080/experiments/1/assign > /dev/null
  curl -s http://localhost:8080/experiments/2/assign > /dev/null
done

# 4. Создаём feature flags
curl -X POST http://localhost:8080/feature-flags \
  -H "Content-Type: application/json" \
  -d '{"key":"new_checkout_flow"}'

curl -X POST http://localhost:8080/feature-flags \
  -H "Content-Type: application/json" \
  -d '{"key":"dark_mode"}'

curl -X POST http://localhost:8080/feature-flags \
  -H "Content-Type: application/json" \
  -d '{"key":"recommendations_v2"}'

# 5. Переключаем флаги туда-сюда
curl -X POST http://localhost:8080/feature-flags/1/enable
curl -X POST http://localhost:8080/feature-flags/2/enable
curl -X POST http://localhost:8080/feature-flags/3/enable
curl -X POST http://localhost:8080/feature-flags/2/disable
curl -X POST http://localhost:8080/feature-flags/2/enable

# 6. Останавливаем один эксперимент
curl -X POST http://localhost:8080/experiments/1/stop

# 7. Ещё пачка назначений на второй эксперимент
for i in $(seq 1 30); do
  curl -s http://localhost:8080/experiments/2/assign > /dev/null
done

# 8. GET-запросы для HTTP-метрик
curl http://localhost:8080/experiments
curl http://localhost:8080/feature-flags
curl http://localhost:8080/experiments/1
curl http://localhost:8080/experiments/999  # 404 — тоже метрика

# 9. Проверяем, что метрики отдаются
curl -s http://localhost:8080/actuator/prometheus | grep -E "experiment_assignments|experiments_active|feature_flags"