package com.temple.controller;

import com.temple.entity.TempleTiming;
import com.temple.repository.TempleTimingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/timings")
public class TempleTimingController {

    @Autowired
    private TempleTimingRepository repo;

    @GetMapping
    public List<TempleTiming> getAll(){
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TempleTiming> getById(@PathVariable Long id){
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/day/{dayOfWeek}")
    public List<TempleTiming> getByDayOfWeek(@PathVariable String dayOfWeek){
        return repo.findByDayOfWeek(dayOfWeek);
    }

    @PostMapping
    public TempleTiming create(@RequestBody TempleTiming timing){
        return repo.save(timing);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TempleTiming> update(@PathVariable Long id, @RequestBody TempleTiming timing){
        return repo.findById(id)
                .map(existing -> {
                    timing.setId(id);
                    return ResponseEntity.ok(repo.save(timing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        if(repo.existsById(id)){
            repo.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
