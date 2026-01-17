package com.temple.controller;

import com.temple.entity.Sponsorship;
import com.temple.repository.SponsorshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/sponsorships")
public class SponsorshipController {

    @Autowired
    private SponsorshipRepository repo;

    @GetMapping
    public List<Sponsorship> getAll(){
        return repo.findByActive(true);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sponsorship> getById(@PathVariable Long id){
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/purpose/{purpose}")
    public List<Sponsorship> getByPurpose(@PathVariable String purpose){
        return repo.findByPurpose(purpose);
    }

    @PostMapping
    public Sponsorship create(@RequestBody Sponsorship sponsorship){
        return repo.save(sponsorship);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sponsorship> update(@PathVariable Long id, @RequestBody Sponsorship sponsorship){
        return repo.findById(id)
                .map(existing -> {
                    sponsorship.setId(id);
                    return ResponseEntity.ok(repo.save(sponsorship));
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
