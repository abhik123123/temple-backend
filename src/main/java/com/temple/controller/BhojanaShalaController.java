package com.temple.controller;

import com.temple.entity.BhojanaShala;
import com.temple.repository.BhojanaShalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/bhojana-shala")
public class BhojanaShalaController {

    @Autowired
    private BhojanaShalaRepository repo;

    @GetMapping
    public List<BhojanaShala> getAll(){
        return repo.findByActive(true);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BhojanaShala> getById(@PathVariable Long id){
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public BhojanaShala create(@RequestBody BhojanaShala bhojanaShala){
        return repo.save(bhojanaShala);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BhojanaShala> update(@PathVariable Long id, @RequestBody BhojanaShala bhojanaShala){
        return repo.findById(id)
                .map(existing -> {
                    bhojanaShala.setId(id);
                    return ResponseEntity.ok(repo.save(bhojanaShala));
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
