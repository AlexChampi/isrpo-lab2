# Примеры PromQL запросов

## Продуктовые метрики

### Скорость назначения вариантов (в секунду)
```promql
rate(experiment_assignments_total[1m])
```

### Суммарное число назначений по экспериментам
```promql
sum by (experiment) (experiment_assignments_total)
```

### Распределение вариантов в конкретном эксперименте
```promql
experiment_assignments_total{experiment="button_color_test"}
```

### Количество активных экспериментов
```promql
experiments_active_count
```

### Скорость переключений feature flags
```promql
rate(feature_flags_toggles_total[5m])
```

### Количество включённых feature flags
```promql
feature_flags_enabled_count
```

---

## HTTP метрики (автоматические через Actuator)

### Запросов в секунду по эндпоинтам
```promql
rate(http_server_requests_seconds_count[1m])
```

### 95-й перцентиль времени ответа
```promql
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[1m]))
```

### Количество ошибок 5xx
```promql
rate(http_server_requests_seconds_count{status=~"5.."}[5m])
```

---

## JVM метрики

### Использование heap-памяти
```promql
jvm_memory_used_bytes{area="heap"}
```

### Количество живых потоков
```promql
jvm_threads_live_threads
```

### Частота сборок мусора
```promql
rate(jvm_gc_pause_seconds_count[5m])
```
