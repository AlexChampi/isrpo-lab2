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

Такой подход позволяет разделить:

- контракт API
- реализацию сервиса
- документацию

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
│ ├── java
│ │ └── ru.itmo.isrpo
│ │ ├── controller
│ │ │ ├── ExperimentsController.java
│ │ │ └── FeatureFlagsController.java
│ │
│ └── resources
│
openapi.yaml

build/generated

```

Сгенерированные классы:

```

ru.itmo.isrpo.api
├── ExperimentsApi
└── FeatureFlagsApi

ru.itmo.isrpo.model
├── Experiment
├── ExperimentCreate
├── Variant
├── FeatureFlag
└── FeatureFlagCreate

```

---

# API сервиса

Платформа содержит два основных модуля:

## Experiments (A/B эксперименты)

Позволяет управлять экспериментами.

Эндпоинты:

```

GET /experiments
POST /experiments
GET /experiments/{id}
POST /experiments/{id}/start
POST /experiments/{id}/stop
GET /experiments/{id}/assign

````

Пример эксперимента:

```json
{
  "name": "button_color_test",
  "variants": [
    { "name": "blue", "weight": 50 },
    { "name": "red", "weight": 50 }
  ]
}
````

---

## Feature Flags

Позволяет включать и выключать функциональность в приложении.

Эндпоинты:

```

GET /feature-flags
POST /feature-flags
GET /feature-flags/{id}
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

Запуск сервера:

```

./gradlew bootRun

```

Сервис будет доступен по адресу:

```

http://localhost:8080

```

Swagger UI:

```

http://localhost:8080/swagger-ui.html

```

---

# Swagger UI

API автоматически документируется через Swagger.

### Experiments API

![Experiments](screenshots/experiments.png)

### Feature Flags API

![FeatureFlags](screenshots/featureflags.png)

---

# Пример использования

Создание эксперимента:

```

POST /experiments

```

Запуск эксперимента:

```

POST /experiments/{id}/start

```

Получение варианта для пользователя:

```

GET /experiments/{id}/assign

```

Включение feature flag:

```

POST /feature-flags/{id}/enable

```

---

# Примечание

В данной реализации используется **in-memory хранение данных** (в памяти приложения).

Основная цель работы — продемонстрировать:

* подход API-first
* использование OpenAPI
* генерацию кода через OpenAPI Generator
* проектирование REST API

```