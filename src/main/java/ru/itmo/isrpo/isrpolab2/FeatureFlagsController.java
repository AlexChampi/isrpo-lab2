package ru.itmo.isrpo.isrpolab2.controller;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ru.itmo.isrpo.api.FeatureFlagsApi;
import ru.itmo.isrpo.model.FeatureFlag;
import ru.itmo.isrpo.model.FeatureFlagCreate;
import ru.itmo.isrpo.isrpolab2.metrics.ExperimentMetrics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class FeatureFlagsController implements FeatureFlagsApi {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagsController.class);

    private final Map<Integer, FeatureFlag> flags = new HashMap<>();
    private final ExperimentMetrics metrics;
    private final Tracer tracer;
    private int idCounter = 1;

    public FeatureFlagsController(ExperimentMetrics metrics, Tracer tracer) {
        this.metrics = metrics;
        this.tracer = tracer;
    }

    @Override
    public ResponseEntity<List<FeatureFlag>> featureFlagsGet() {
        log.info("Listing all feature flags, total count: {}", flags.size());
        return ResponseEntity.ok(new ArrayList<>(flags.values()));
    }

    @Override
    public ResponseEntity<FeatureFlag> featureFlagsPost(FeatureFlagCreate featureFlagCreate) {

        FeatureFlag flag = new FeatureFlag();
        flag.setId(idCounter);
        flag.setKey(featureFlagCreate.getKey());
        flag.setEnabled(false);

        flags.put(idCounter, flag);

        log.info("Created feature flag id={} key='{}'", idCounter, featureFlagCreate.getKey());
        idCounter++;
        metrics.recordFlagCreated();

        return ResponseEntity.ok(flag);
    }

    @Override
    public ResponseEntity<FeatureFlag> featureFlagsIdGet(Integer id) {

        FeatureFlag flag = flags.get(id);

        if (flag == null) {
            log.warn("Feature flag not found: id={}", id);
            return ResponseEntity.notFound().build();
        }

        MDC.put("flagKey", flag.getKey());
        log.debug("Feature flag checked: id={} key='{}' enabled={}", id, flag.getKey(), flag.getEnabled());
        MDC.clear();

        metrics.recordFlagCheck(flag.getKey());
        return ResponseEntity.ok(flag);
    }

    @Override
    public ResponseEntity<Void> featureFlagsIdEnablePost(Integer id) {

        // ── Кастомный спан: переключение флага ──────────────────────────
        Span span = tracer.nextSpan()
                .name("toggle-feature-flag")
                .tag("flag.id", String.valueOf(id))
                .tag("flag.action", "enable")
                .start();

        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {

            FeatureFlag flag = flags.get(id);

            if (flag == null) {
                span.tag("error", "true").tag("error.reason", "flag_not_found");
                log.warn("Cannot enable feature flag: id={} not found", id);
                return ResponseEntity.notFound().build();
            }

            flag.setEnabled(true);
            span.tag("flag.key", flag.getKey());
            metrics.recordFlagToggle(flag.getKey(), true);
            log.info("Feature flag ENABLED: id={} key='{}'", id, flag.getKey());

            return ResponseEntity.ok().build();

        } finally {
            span.end();
        }
    }

    @Override
    public ResponseEntity<Void> featureFlagsIdDisablePost(Integer id) {

        Span span = tracer.nextSpan()
                .name("toggle-feature-flag")
                .tag("flag.id", String.valueOf(id))
                .tag("flag.action", "disable")
                .start();

        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {

            FeatureFlag flag = flags.get(id);

            if (flag == null) {
                span.tag("error", "true").tag("error.reason", "flag_not_found");
                log.warn("Cannot disable feature flag: id={} not found", id);
                return ResponseEntity.notFound().build();
            }

            flag.setEnabled(false);
            span.tag("flag.key", flag.getKey());
            metrics.recordFlagToggle(flag.getKey(), false);
            log.info("Feature flag DISABLED: id={} key='{}'", id, flag.getKey());

            return ResponseEntity.ok().build();

        } finally {
            span.end();
        }
    }
}
