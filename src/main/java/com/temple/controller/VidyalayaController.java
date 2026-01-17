package com.temple.controller;

import com.temple.entity.Vidyalaya;
import com.temple.repository.VidyalayaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/vidyalaya")
public class VidyalayaController {

    @Autowired
    private VidyalayaRepository repo;

    @GetMapping
    public List<Vidyalaya> getAll(){
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vidyalaya> getById(@PathVariable Long id){
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Vidyalaya create(@RequestBody Vidyalaya vidyalaya){
        return repo.save(vidyalaya);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vidyalaya> update(@PathVariable Long id, @RequestBody Vidyalaya vidyalaya){
        return repo.findById(id)
                .map(existing -> {
                    vidyalaya.setId(id);
                    return ResponseEntity.ok(repo.save(vidyalaya));
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
