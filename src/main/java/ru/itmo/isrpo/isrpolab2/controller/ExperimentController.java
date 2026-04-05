package ru.itmo.isrpo.isrpolab2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.isrpo.api.ExperimentsApi;
import ru.itmo.isrpo.model.Experiment;
import ru.itmo.isrpo.model.ExperimentCreate;
import ru.itmo.isrpo.model.Variant;
import ru.itmo.isrpo.isrpolab2.metrics.ExperimentMetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
public class ExperimentController implements ExperimentsApi {

    private final Map<Integer, Experiment> experiments = new HashMap<>();
    private final Random random = new Random();
    private final ExperimentMetrics metrics;
    private int idCounter = 1;

    public ExperimentController(ExperimentMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public ResponseEntity<List<Experiment>> experimentsGet() {
        return ResponseEntity.ok(new ArrayList<>(experiments.values()));
    }

    @Override
    public ResponseEntity<Experiment> experimentsPost(ExperimentCreate experimentCreate) {

        Experiment experiment = new Experiment();

        experiment.setId(idCounter);
        experiment.setName(experimentCreate.getName());
        experiment.setVariants(experimentCreate.getVariants());
        experiment.setStatus("DRAFT");

        experiments.put(idCounter, experiment);

        idCounter++;

        metrics.recordExperimentCreated();

        return ResponseEntity.ok(experiment);
    }

    @Override
    public ResponseEntity<Experiment> experimentsIdGet(Integer id) {

        Experiment experiment = experiments.get(id);

        if (experiment == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(experiment);
    }

    @Override
    public ResponseEntity<Void> experimentsIdStartPost(Integer id) {

        Experiment experiment = experiments.get(id);

        if (experiment == null) {
            return ResponseEntity.notFound().build();
        }

        experiment.setStatus("RUNNING");
        metrics.recordExperimentStarted();

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> experimentsIdStopPost(Integer id) {

        Experiment experiment = experiments.get(id);

        if (experiment == null) {
            return ResponseEntity.notFound().build();
        }

        experiment.setStatus("STOPPED");
        metrics.recordExperimentStopped();

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Variant> experimentsIdAssignGet(Integer id) {

        Experiment experiment = experiments.get(id);

        if (experiment == null) {
            return ResponseEntity.notFound().build();
        }

        List<Variant> variants = experiment.getVariants();

        if (variants == null || variants.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Variant variant = variants.get(random.nextInt(variants.size()));

        metrics.recordAssignment(experiment.getName(), variant.getName());

        return ResponseEntity.ok(variant);
    }
}
