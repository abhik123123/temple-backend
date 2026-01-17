package com.temple.controller;

import com.temple.entity.Goshala;
import com.temple.repository.GoshalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/goshala")
public class GoshalaController {

    @Autowired
    private GoshalaRepository repo;

    @GetMapping
    public List<Goshala> getAll(){
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Goshala> getById(@PathVariable Long id){
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Goshala create(@RequestBody Goshala goshala){
        return repo.save(goshala);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Goshala> update(@PathVariable Long id, @RequestBody Goshala goshala){
        return repo.findById(id)
                .map(existing -> {
                    goshala.setId(id);
                    return ResponseEntity.ok(repo.save(goshala));
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
