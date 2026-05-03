# Примеры TraceQL запросов

TraceQL — язык запросов к Grafana Tempo. Используется в Grafana → Explore → Tempo.

---

## Базовый поиск трейсов

### Все трейсы сервиса
```traceql
{ resource.service.name = "isrpo-lab2" }
```

### Трейсы конкретной операции (HTTP endpoint)
```traceql
{ name = "GET /experiments/{id}/assign" }
```

### Трейсы кастомного спана назначения варианта
```traceql
{ name = "assign-variant" }
```

### Трейсы переключения feature flags
```traceql
{ name = "toggle-feature-flag" }
```

---

## Фильтрация по атрибутам (тегам)

### Назначения для конкретного эксперимента
```traceql
{ name = "assign-variant" && .experiment.name = "button_color_test" }
```

### Назначения конкретного варианта
```traceql
{ .variant.name = "blue" }
```

### Только ошибочные спаны
```traceql
{ .error = "true" }
```

### Ошибки "experiment not found"
```traceql
{ .error.reason = "experiment_not_found" }
```

### Feature flag enable actions
```traceql
{ name = "toggle-feature-flag" && .flag.action = "enable" }
```

---

## Фильтрация по длительности

### Медленные трейсы (> 100ms)
```traceql
{ duration > 100ms }
```

### Очень быстрые назначения (< 1ms)
```traceql
{ name = "assign-variant" && duration < 1ms }
```

### Назначения между 5ms и 50ms
```traceql
{ name = "assign-variant" && duration >= 5ms && duration <= 50ms }
```

---

## Структурные запросы (pipeline)

### Трейсы, содержащие медленный вложенный спан выбора варианта
```traceql
{ name = "assign-variant" } >> { name = "select-variant-random" && duration > 5ms }
```

### Трейсы с ошибкой в любом спане
```traceql
{ .error = "true" } | select(name, duration, .error.reason)
```

### Все HTTP 404 запросы
```traceql
{ http.response.status_code = 404 }
```

---

## Агрегирующие запросы (метрики из трейсов)

### Rate span-ов в секунду
```traceql
{ name = "assign-variant" } | rate()
```

### Количество ошибок по типу
```traceql
{ .error = "true" } | count_over_time(5m)
```

### Перцентили длительности назначений
```traceql
{ name = "assign-variant" } | quantile_over_time(duration, 0.95, 5m)
```

---

## Полезные паттерны

### Сравнить длительность для двух вариантов
```traceql
{ name = "assign-variant" && .variant.name = "blue" }
```
```traceql
{ name = "assign-variant" && .variant.name = "red" }
```

### Трейсы, которые прошли через создание эксперимента
```traceql
{ name = "create-experiment" && .experiment.variant_count > "1" }
```

### Последние 20 трейсов сервиса с ошибками
```traceql
{ resource.service.name = "isrpo-lab2" && status = error }
```
