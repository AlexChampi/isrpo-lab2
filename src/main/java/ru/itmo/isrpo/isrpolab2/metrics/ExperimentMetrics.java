package ru.itmo.isrpo.isrpolab2.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Продуктовые метрики платформы A/B тестирования.
 *
 * Собственные (продуктовые) метрики:
 *   - experiment_assignments_total   — счётчик назначений вариантов (по эксперименту и варианту)
 *   - experiments_created_total      — счётчик созданных экспериментов
 *   - experiments_active_count       — gauge текущих активных (RUNNING) экспериментов
 *   - feature_flags_toggles_total    — счётчик переключений feature flags
 *   - feature_flags_created_total    — счётчик созданных feature flags
 *   - feature_flags_enabled_count    — gauge включённых feature flags
 */
@Component
public class ExperimentMetrics {

    private final MeterRegistry registry;

    // Gauge-значения — атомарные, чтобы обновлять из контроллеров
    private final AtomicInteger activeExperiments = new AtomicInteger(0);
    private final AtomicInteger enabledFlags = new AtomicInteger(0);

    public ExperimentMetrics(MeterRegistry registry) {
        this.registry = registry;

        // Gauge: количество активных экспериментов прямо сейчас
        Gauge.builder("experiments.active.count", activeExperiments, AtomicInteger::get)
                .description("Number of currently running experiments")
                .register(registry);

        // Gauge: количество включённых feature flags
        Gauge.builder("feature_flags.enabled.count", enabledFlags, AtomicInteger::get)
                .description("Number of currently enabled feature flags")
                .register(registry);
    }

    // ---- Experiments ----

    /**
     * Главная продуктовая метрика: назначение варианта пользователю.
     * Считает, сколько раз каждый вариант каждого эксперимента был выдан.
     */
    public void recordAssignment(String experimentName, String variantName) {
        Counter.builder("experiment.assignments.total")
                .description("Total variant assignments")
                .tag("experiment", experimentName)
                .tag("variant", variantName)
                .register(registry)
                .increment();
    }

    public void recordExperimentCreated() {
        Counter.builder("experiments.created.total")
                .description("Total experiments created")
                .register(registry)
                .increment();
    }

    public void recordExperimentStarted() {
        activeExperiments.incrementAndGet();
    }

    public void recordExperimentStopped() {
        activeExperiments.decrementAndGet();
    }

    // ---- Feature Flags ----

    public void recordFlagCreated() {
        Counter.builder("feature_flags.created.total")
                .description("Total feature flags created")
                .register(registry)
                .increment();
    }

    public void recordFlagToggle(String flagKey, boolean enabled) {
        Counter.builder("feature_flags.toggles.total")
                .description("Total feature flag toggles")
                .tag("flag", flagKey)
                .tag("action", enabled ? "enable" : "disable")
                .register(registry)
                .increment();

        if (enabled) {
            enabledFlags.incrementAndGet();
        } else {
            enabledFlags.decrementAndGet();
        }
    }
}
