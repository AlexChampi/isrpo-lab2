package ru.itmo.isrpo.isrpolab2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.isrpo.api.ExperimentsApi;
import ru.itmo.isrpo.model.Experiment;
import ru.itmo.isrpo.model.ExperimentCreate;
import ru.itmo.isrpo.model.Variant;
import ru.itmo.isrpo.isrpolab2.metrics.ExperimentMetrics;
import ru.itmo.isrpo.isrpolab2.service.AssignService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ExperimentController implements ExperimentsApi {

    private static final Logger log = LoggerFactory.getLogger(ExperimentController.class);

    private final Map<Integer, Experiment> experiments = new HashMap<>();
    private final ExperimentMetrics metrics;
    private final AssignService assignService;
    private int idCounter = 1;

    public ExperimentController(ExperimentMetrics metrics, AssignService assignService) {
        this.metrics = metrics;
        this.assignService = assignService;
    }

    @Override
    public ResponseEntity<List<Experiment>> experimentsGet() {
        log.info("Listing all experiments, total count: {}", experiments.size());
        return ResponseEntity.ok(new ArrayList<>(experiments.values()));
    }

    @Override
    public ResponseEntity<Experiment> experimentsPost(ExperimentCreate experimentCreate) {
        Experiment experiment = new Experiment();
        experiment.setId(idCounter);
        experiment.setName(experimentCreate.getName());
        experiment.setVariants(experimentCreate.getVariants());
        experiment.setStatus("DRAFT");

        int variantCount = experimentCreate.getVariants() != null
                ? experimentCreate.getVariants().size() : 0;

        experiments.put(idCounter, experiment);
        log.info("Created experiment id={} name='{}' with {} variants",
                idCounter, experimentCreate.getName(), variantCount);
        idCounter++;
        metrics.recordExperimentCreated(variantCount);

        return ResponseEntity.ok(experiment);
    }

    @Override
    public ResponseEntity<Experiment> experimentsIdGet(Integer id) {
        Experiment experiment = experiments.get(id);
        if (experiment == null) {
            log.warn("Experiment not found: id={}", id);
            return ResponseEntity.notFound().build();
        }
        log.debug("Fetched experiment id={} name='{}'", id, experiment.getName());
        return ResponseEntity.ok(experiment);
    }

    @Override
    public ResponseEntity<Void> experimentsIdStartPost(Integer id) {
        Experiment experiment = experiments.get(id);
        if (experiment == null) {
            log.warn("Cannot start experiment: id={} not found", id);
            return ResponseEntity.notFound().build();
        }
        experiment.setStatus("RUNNING");
        metrics.recordExperimentStarted();
        log.info("Experiment STARTED: id={} name='{}'", id, experiment.getName());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> experimentsIdStopPost(Integer id) {
        Experiment experiment = experiments.get(id);
        if (experiment == null) {
            log.warn("Cannot stop experiment: id={} not found", id);
            return ResponseEntity.notFound().build();
        }
        experiment.setStatus("STOPPED");
        metrics.recordExperimentStopped();
        log.info("Experiment STOPPED: id={} name='{}'", id, experiment.getName());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Variant> experimentsIdAssignGet(Integer id) {
        long start = System.nanoTime();

        Experiment experiment = experiments.get(id);
        if (experiment == null) {
            log.warn("Assignment failed: experiment id={} not found", id);
            metrics.recordAssignmentError("experiment_not_found");
            return ResponseEntity.notFound().build();
        }

        List<Variant> variants = experiment.getVariants();
        if (variants == null || variants.isEmpty()) {
            log.error("Assignment failed: experiment id={} has no variants", id);
            metrics.recordAssignmentError("no_variants");
            return ResponseEntity.badRequest().build();
        }

        Variant variant = assignService.select(variants);

        long elapsed = System.nanoTime() - start;
        MDC.put("experimentId", String.valueOf(id));
        MDC.put("experimentName", experiment.getName());
        MDC.put("variant", variant.getName());
        log.info("Variant assigned: experiment='{}' variant='{}' duration_us={}",
                experiment.getName(), variant.getName(), elapsed / 1000);
        MDC.clear();

        metrics.recordAssignment(experiment.getName(), variant.getName());
        metrics.recordAssignmentDuration(elapsed);
        return ResponseEntity.ok(variant);
    }
}