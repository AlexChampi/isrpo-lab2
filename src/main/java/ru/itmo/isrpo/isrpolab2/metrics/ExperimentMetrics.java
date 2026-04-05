package ru.itmo.isrpo.isrpolab2.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ExperimentMetrics {

    private final MeterRegistry registry;

    private final AtomicInteger activeExperiments = new AtomicInteger(0);
    private final AtomicInteger enabledFlags = new AtomicInteger(0);
    private final AtomicInteger totalExperiments = new AtomicInteger(0);
    private final AtomicInteger totalFlags = new AtomicInteger(0);

    private final Timer assignmentTimer;
    private final DistributionSummary variantsPerExperiment;

    public ExperimentMetrics(MeterRegistry registry) {
        this.registry = registry;

        Gauge.builder("experiments.active.count", activeExperiments, AtomicInteger::get)
                .description("Number of currently running experiments")
                .register(registry);

        Gauge.builder("feature_flags.enabled.count", enabledFlags, AtomicInteger::get)
                .description("Number of currently enabled feature flags")
                .register(registry);

        Gauge.builder("experiments.total.count", totalExperiments, AtomicInteger::get)
                .description("Total number of experiments in the system")
                .register(registry);

        Gauge.builder("feature_flags.total.count", totalFlags, AtomicInteger::get)
                .description("Total number of feature flags in the system")
                .register(registry);

        assignmentTimer = Timer.builder("experiment.assignment.duration")
                .description("Time to assign a variant to a user")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        variantsPerExperiment = DistributionSummary.builder("experiment.variants.count")
                .description("Number of variants per created experiment")
                .register(registry);
    }

    // ==================== Experiments ====================

    public void recordAssignment(String experimentName, String variantName) {
        Counter.builder("experiment.assignments.total")
                .description("Total variant assignments")
                .tag("experiment", experimentName)
                .tag("variant", variantName)
                .register(registry)
                .increment();
    }

    public void recordAssignmentDuration(long nanos) {
        assignmentTimer.record(nanos, TimeUnit.NANOSECONDS);
    }

    public void recordAssignmentError(String reason) {
        Counter.builder("experiment.assignment.errors")
                .description("Errors during variant assignment")
                .tag("reason", reason)
                .register(registry)
                .increment();
    }

    public void recordExperimentCreated(int variantCount) {
        Counter.builder("experiments.created.total")
                .description("Total experiments created")
                .register(registry)
                .increment();

        totalExperiments.incrementAndGet();
        variantsPerExperiment.record(variantCount);
    }

    public void recordExperimentStarted() {
        activeExperiments.incrementAndGet();
    }

    public void recordExperimentStopped() {
        activeExperiments.decrementAndGet();
    }

    // ==================== Feature Flags ====================

    public void recordFlagCreated() {
        Counter.builder("feature_flags.created.total")
                .description("Total feature flags created")
                .register(registry)
                .increment();

        totalFlags.incrementAndGet();
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

    public void recordFlagCheck(String flagKey) {
        Counter.builder("feature_flags.checks.total")
                .description("Total feature flag lookups")
                .tag("flag", flagKey)
                .register(registry)
                .increment();
    }
}