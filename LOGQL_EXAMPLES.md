# Примеры LogQL запросов

## Базовые запросы (Log Stream Selector)

### Все логи приложения
```logql
{application="isrpo-lab2"}
```

### Только ошибки
```logql
{application="isrpo-lab2", level="ERROR"}
```

### Предупреждения и ошибки
```logql
{application="isrpo-lab2", level=~"WARN|ERROR"}
```

---

## Фильтрация по содержимому (Line Filter)

### Логи назначения вариантов
```logql
{application="isrpo-lab2"} |= "Variant assigned"
```

### Логи feature flag переключений
```logql
{application="isrpo-lab2"} |= "Feature flag" |~ "ENABLED|DISABLED"
```

### Логи жизненного цикла экспериментов
```logql
{application="isrpo-lab2"} |~ "STARTED|STOPPED|Created experiment"
```

### Логи ошибок назначения
```logql
{application="isrpo-lab2"} |= "Assignment failed"
```

### Логи по конкретному эксперименту
```logql
{application="isrpo-lab2"} |= "button_color_test"
```

### Исключить health-check логи
```logql
{application="isrpo-lab2"} != "actuator"
```

---

## Парсинг JSON (Log Pipeline)

### Извлечение полей из JSON-логов
```logql
{application="isrpo-lab2"} | json | message =~ ".*Variant assigned.*"
```

### Фильтрация по уровню через JSON-поле
```logql
{application="isrpo-lab2"} | json | level = "ERROR"
```

---

## Метрические запросы (Metric Queries)

### Количество логов в минуту по уровням
```logql
sum by (level) (count_over_time({application="isrpo-lab2"}[1m]))
```

### Скорость назначений вариантов (логов/мин)
```logql
count_over_time({application="isrpo-lab2"} |= "Variant assigned" [1m])
```

### Скорость ошибок (логов/мин)
```logql
count_over_time({application="isrpo-lab2", level="ERROR"} [5m])
```

### Количество переключений feature flags за последний час
```logql
count_over_time({application="isrpo-lab2"} |~ "ENABLED|DISABLED" [1h])
```

### Соотношение ошибок к общему числу логов
```logql
sum(count_over_time({application="isrpo-lab2", level="ERROR"}[5m]))
/
sum(count_over_time({application="isrpo-lab2"}[5m]))
```

---

## Полезные паттерны

### Top ошибок (по сообщению)
```logql
topk(10, sum by (message) (count_over_time({application="isrpo-lab2", level="ERROR"} | json [1h])))
```

### Логи за последние 5 минут без DEBUG
```logql
{application="isrpo-lab2", level!="DEBUG"}
```
