package com.temple.controller;

import com.temple.entity.TempleTiming;
import com.temple.exception.NotFoundException;
import com.temple.repository.TempleTimingRepository;
import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** /api/timings — weekly opening sessions, Monday first. Writes require ADMIN. */
@RestController
@RequestMapping("/timings")
@Transactional
public class TempleTimingController {

    private static final Comparator<TempleTiming> WEEK_ORDER = Comparator
            .comparing((TempleTiming t) -> dayIndex(t.getDayOfWeek()))
            .thenComparing(TempleTiming::getOpeningTime, Comparator.nullsLast(Comparator.naturalOrder()));

    private final TempleTimingRepository repo;

    public TempleTimingController(TempleTimingRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<TempleTiming> getAll() {
        return repo.findAll().stream().sorted(WEEK_ORDER).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public TempleTiming getById(@PathVariable Long id) {
        return find(id);
    }

    @GetMapping("/day/{dayOfWeek}")
    @Transactional(readOnly = true)
    public List<TempleTiming> getByDayOfWeek(@PathVariable String dayOfWeek) {
        String day = capitalize(dayOfWeek);
        return getAll().stream().filter(t -> day.equalsIgnoreCase(t.getDayOfWeek())).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TempleTiming create(@Valid @RequestBody TempleTiming timing) {
        timing.setId(null);
        timing.setDayOfWeek(capitalize(timing.getDayOfWeek()));
        return repo.save(timing);
    }

    @PutMapping("/{id}")
    public TempleTiming update(@PathVariable Long id, @Valid @RequestBody TempleTiming changes) {
        TempleTiming timing = find(id);
        timing.setDayOfWeek(capitalize(changes.getDayOfWeek()));
        timing.setOpeningTime(changes.getOpeningTime());
        timing.setClosingTime(changes.getClosingTime());
        timing.setSpecialNotes(changes.getSpecialNotes());
        return timing;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repo.delete(find(id));
    }

    private TempleTiming find(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Timing", id));
    }

    private static String capitalize(String day) {
        String d = day == null ? "" : day.trim().toLowerCase(Locale.ROOT);
        return d.isEmpty() ? d : Character.toUpperCase(d.charAt(0)) + d.substring(1);
    }

    private static int dayIndex(String day) {
        try {
            return DayOfWeek.valueOf(day.trim().toUpperCase(Locale.ROOT)).getValue();
        } catch (RuntimeException e) {
            return 8;
        }
    }
}
