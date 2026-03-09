package ru.itmo.isrpo.isrpolab2.controller;

import ru.itmo.isrpo.api.FeatureFlagsApi;
import ru.itmo.isrpo.model.FeatureFlag;
import ru.itmo.isrpo.model.FeatureFlagCreate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class FeatureFlagsController implements FeatureFlagsApi {

    private final Map<Integer, FeatureFlag> flags = new HashMap<>();
    private int idCounter = 1;

    @Override
    public ResponseEntity<List<FeatureFlag>> featureFlagsGet() {
        return ResponseEntity.ok(new ArrayList<>(flags.values()));
    }

    @Override
    public ResponseEntity<FeatureFlag> featureFlagsPost(FeatureFlagCreate featureFlagCreate) {

        FeatureFlag flag = new FeatureFlag();

        flag.setId(idCounter);
        flag.setKey(featureFlagCreate.getKey());
        flag.setEnabled(false);

        flags.put(idCounter, flag);

        idCounter++;

        return ResponseEntity.ok(flag);
    }

    @Override
    public ResponseEntity<FeatureFlag> featureFlagsIdGet(Integer id) {

        FeatureFlag flag = flags.get(id);

        if (flag == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(flag);
    }

    @Override
    public ResponseEntity<Void> featureFlagsIdEnablePost(Integer id) {

        FeatureFlag flag = flags.get(id);

        if (flag == null) {
            return ResponseEntity.notFound().build();
        }

        flag.setEnabled(true);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> featureFlagsIdDisablePost(Integer id) {

        FeatureFlag flag = flags.get(id);

        if (flag == null) {
            return ResponseEntity.notFound().build();
        }

        flag.setEnabled(false);

        return ResponseEntity.ok().build();
    }
}