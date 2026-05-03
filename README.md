# Лабораторная работа №5
## Распределённая трассировка

Расширение сервиса из лабораторной работы №4 распределённой трассировкой.

---

## Стек трассировки

| Компонент       | Технология                        | Назначение                              |
|-----------------|-----------------------------------|-----------------------------------------|
| Инструментация  | Micrometer Tracing + AspectJ      | Генерация спанов в коде                 |
| Экспорт         | OpenTelemetry OTLP Exporter       | Push трейсов из приложения в Tempo      |
| Хранение        | Grafana Tempo                     | Агрегация и хранение трейсов            |
| Визуализация    | Grafana                           | Просмотр трейсов, Service Map           |
| Язык запросов   | TraceQL                           | Запросы к Tempo                         |

---

## Что трассируется

### Автоматические спаны (Spring Boot Actuator)
Каждый HTTP-запрос автоматически оборачивается в спан — без дополнительного кода.

### Кастомные спаны (`@NewSpan`)
- `assign-variant` — вызов сервиса назначения варианта
- `pick-random-variant` — вложенный спан выбора случайного варианта

### Структура трейса для `GET /experiments/{id}/assign`

```
GET /experiments/{id}/assign        ← HTTP спан (автоматический)
  └── assign-variant                ← @NewSpan в AssignService
        └── pick-random-variant     ← @NewSpan вложенный
```

---

## Конфигурация

Трейсы отправляются по OTLP HTTP в Tempo:
```properties
management.otlp.tracing.endpoint=http://tempo:4318/v1/traces
management.tracing.sampling.probability=1.0
```

Семплирование 100% — все запросы трейсируются. В продакшене рекомендуется 0.1–0.5.

---

## Запуск

```bash
docker-compose up --build
```

| Сервис     | URL                        |
|------------|----------------------------|
| Приложение | http://localhost:8080       |
| Tempo      | http://localhost:3200       |
| Grafana    | http://localhost:3000       |

---

## Примеры TraceQL запросов

Полный список — в файле [TRACEQL_EXAMPLES.md](TRACEQL_EXAMPLES.md).

```traceql
# Все трейсы сервиса
{ resource.service.name = "isrpo-lab2" }

# Трейсы назначения вариантов
{ name = "assign-variant" }

# Медленные трейсы
{ duration > 100ms }

# Ошибочные трейсы
{ status = error }
```

---

## Дашборд трассировки в Grafana

Панели:
1. **Service Map** — граф зависимостей сервисов
2. **Trace Search** — поиск трейсов по TraceQL
3. **Slow Traces** — трейсы длительностью > 100ms
4. **Span Rate** — количество спанов в секунду
5. **Span Duration p95** — перцентили длительности
6. **Error Spans** — ошибочные спаны

### Все трейсы
![all_traces.png](screenshots/all_traces.png)

### Трейс с ошибкой
![error_trace.png](screenshots/error_trace.png)

### Трейс с вложенными спанами
![trace_span+1.png](screenshots/trace_span+1.png)

---

# Лабораторная работа №4
## Экспорт логов, сбор, визуализация, язык запросов

Расширение сервиса из лабораторной работы №3 централизованным сбором логов.

---

## Стек логирования

| Компонент       | Технология               | Назначение                            |
|-----------------|--------------------------|---------------------------------------|
| Логирование     | SLF4J + Logback          | Генерация логов в коде                |
| Экспорт         | Loki4j Logback Appender  | Push логов из приложения в Loki       |
| Хранение        | Grafana Loki             | Агрегация и хранение логов            |
| Визуализация    | Grafana                  | Дашборды, просмотр логов              |
| Язык запросов   | LogQL                    | Запросы к Loki                        |

---

## Что логируется

### Experiments
- Создание эксперимента — `INFO` с именем и количеством вариантов
- Запуск / остановка — `INFO` со статусом
- Назначение варианта — `INFO` с MDC-контекстом (experimentId, experimentName, variant)
- Эксперимент не найден — `WARN`
- Нет вариантов при назначении — `ERROR`

### Feature Flags
- Создание флага — `INFO` с ключом
- Включение / отключение — `INFO` с ключом и действием
- Проверка флага — `DEBUG` с текущим состоянием
- Флаг не найден — `WARN`

### Формат логов

Логи отправляются в Loki в JSON-формате:
```json
{
  "timestamp": "2025-01-15T12:00:00.123",
  "level": "INFO",
  "logger": "r.i.i.c.ExperimentController",
  "thread": "http-nio-8080-exec-1",
  "message": "Variant assigned: experiment='button_color_test' variant='blue' duration_us=142",
  "traceId": "4912281cdb0a557d68576c5ebebebdb",
  "spanId": "68576c5ebebebdb"
}
```

Labels в Loki: `application`, `host`, `level`.

---

## Запуск

## Все логи
![all_logs.png](screenshots/all_logs.png)

## С уровнем ERROR и WARN
![err_and_warns.png](screenshots/err_and_warns.png)

## Число 404 относительно всех запросов
![not_found.png](screenshots/not_found.png)

![not_found_2.png](screenshots/not_found_2.png)

---

## Дашборд логов в Grafana

Панели:
1. **All Application Logs** — все логи приложения (logs panel)
2. **Errors & Warnings** — только WARN и ERROR уровни
3. **Log Volume by Level** — гистограмма объёма логов по уровням
4. **Variant Assignment Logs** — логи назначений вариантов
5. **Feature Flag Toggle Logs** — логи переключений флагов
6. **Experiment Lifecycle Logs** — создание, старт, остановка экспериментов
7. **Assignment Rate** — график скорости назначений (логов/мин)

---

# Лабораторная работа №3
## Метрики для платформы A/B тестирования

Расширение сервиса из лабораторной работы №2 метриками, сбором в TSDB и визуализацией.

---

## Стек мониторинга

| Компонент        | Технология         | Назначение                       |
|------------------|--------------------|----------------------------------|
| Инструментация   | Micrometer         | Сбор метрик в коде               |
| Экспорт          | Spring Boot Actuator + Prometheus exporter | Отдача метрик в формате Prometheus |
| TSDB             | Prometheus         | Хранение временных рядов         |
| Визуализация     | Grafana            | Дашборды, графики                |
| Язык запросов    | PromQL             | Запросы к Prometheus             |

---

## Продуктовые метрики

### `experiment.assignments.total` (Counter) — **главная продуктовая метрика**
Считает, сколько раз каждый вариант каждого эксперимента был назначен пользователю.
Теги: `experiment`, `variant`.

### `experiments.created.total` (Counter)
Общее число созданных экспериментов.

### `experiments.active.count` (Gauge)
Количество экспериментов в статусе RUNNING прямо сейчас.

### `feature_flags.toggles.total` (Counter)
Количество переключений feature flags. Теги: `flag`, `action`.

### `feature_flags.created.total` (Counter)
Общее число созданных feature flags.

### `feature_flags.enabled.count` (Gauge)
Количество включённых feature flags прямо сейчас.

---

## Стандартные метрики (через Actuator)

Кроме продуктовых метрик автоматически собираются:
- HTTP-метрики (количество запросов, время ответа, статус-коды)
- JVM-метрики (heap, threads, GC)
- System-метрики (CPU, uptime)

---

## Запуск

```bash
docker-compose up --build
```

| Сервис      | URL                          |
|-------------|------------------------------|
| Приложение  | http://localhost:8080         |
| Swagger UI  | http://localhost:8080/swagger-ui.html |
| Prometheus  | http://localhost:9090         |
| Grafana     | http://localhost:3000 (admin/admin) |

---

## Примеры PromQL запросов

Полный список — в файле [PROMQL_EXAMPLES.md](PROMQL_EXAMPLES.md).

```promql
# Скорость назначений вариантов
rate(experiment_assignments_total[1m])

# Распределение по вариантам
sum by (variant) (experiment_assignments_total{experiment="button_color_test"})

# Количество активных экспериментов
experiments_active_count

# 95-й перцентиль времени ответа
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[1m]))
```

---

## Дашборд Grafana

Дашборд подгружается автоматически при запуске через provisioning.

![grafana_1.png](screenshots/grafana_1.png)
![grafana_2.png](screenshots/grafana_2.png)
![grafana_3.png](screenshots/grafana_3.png)

Панели:
1. **Variant Assignments** — rate назначений вариантов по экспериментам (timeseries)
2. **Active Experiments** — gauge текущих запущенных экспериментов (stat)
3. **Enabled Feature Flags** — gauge включённых флагов (stat)
4. **Feature Flag Toggles** — rate переключений (timeseries)
5. **HTTP Request Rate** — запросы в секунду по эндпоинтам (timeseries)
6. **HTTP Response Time p95/p50** — перцентили времени ответа (timeseries)
7. **JVM Heap Memory** — использование памяти (timeseries)
8. **JVM Threads** — количество потоков (timeseries)
9. **Assignments per Experiment** — кумулятивная гистограмма назначений (barchart)

---

## Эндпоинт метрик

После запуска метрики доступны по адресу:

```
http://localhost:8080/actuator/prometheus
```

![prometheus.png](screenshots/prometheus.png)


---

# Лабораторная работа №2
## Платформа для A/B тестирования

REST API сервис для управления **A/B экспериментами** и **feature flags**.

Проект демонстрирует использование подхода **API-first** с применением **OpenAPI спецификации** и **автоматической генерации кода**.

---

# Подход API-first

Сервис был разработан с использованием подхода **API-first**:

1. Сначала был описан API с помощью **OpenAPI 3.0 спецификации** (`openapi.yaml`)
2. Затем с помощью **OpenAPI Generator** были сгенерированы интерфейсы для Spring
3. После этого была написана реализация этих интерфейсов с использованием **Spring Boot**

---

# Используемые технологии

- Java 21
- Spring Boot
- Gradle
- OpenAPI 3
- OpenAPI Generator
- Swagger UI

---

# Структура проекта

```
src
├── main
│   ├── java
│   │   └── ru.itmo.isrpo
│   │       ├── controller
│   │       │   ├── ExperimentsController.java
│   │       │   └── FeatureFlagsController.java
│   │       └── service
│   │           └── AssignService.java
│   └── resources

openapi.yaml
build/generated
```

---

# API сервиса

## Experiments (A/B эксперименты)

```
GET  /experiments
POST /experiments
GET  /experiments/{id}
POST /experiments/{id}/start
POST /experiments/{id}/stop
GET  /experiments/{id}/assign
```

Пример эксперимента:

```json
{
  "name": "button_color_test",
  "variants": [
    { "name": "blue", "weight": 50 },
    { "name": "red", "weight": 50 }
  ]
}
```

---

## Feature Flags

```
GET  /feature-flags
POST /feature-flags
GET  /feature-flags/{id}
POST /feature-flags/{id}/enable
POST /feature-flags/{id}/disable
```

Пример feature flag:

```json
{
  "key": "new_checkout_flow"
}
```

---

# Запуск приложения

```bash
./gradlew bootRun
```

Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

---

# Swagger UI

### Experiments API
![Experiments](screenshots/experiments.png)

### Feature Flags API
![FeatureFlags](screenshots/featureflags.png)