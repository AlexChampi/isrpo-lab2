package ru.itmo.isrpo.isrpolab2.service;

import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.stereotype.Service;
import ru.itmo.isrpo.model.Variant;
import java.util.List;
import java.util.Random;

@Service
public class AssignService {

    private final Random random = new Random();

    @NewSpan("select-variant")
    public Variant select(List<Variant> variants) {
        return variants.get(random.nextInt(variants.size()));
    }
}