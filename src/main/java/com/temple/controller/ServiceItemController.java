package com.temple.controller;

import com.temple.entity.ServiceItem;
import com.temple.repository.ServiceItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceItemController {

    @Autowired
    private ServiceItemRepository repo;

    @GetMapping
    public List<ServiceItem> getAll(){
        return repo.findByActive(true);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceItem> getById(@PathVariable Long id){
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ServiceItem create(@RequestBody ServiceItem service){
        return repo.save(service);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceItem> update(@PathVariable Long id, @RequestBody ServiceItem service){
        return repo.findById(id)
                .map(existing -> {
                    service.setId(id);
                    return ResponseEntity.ok(repo.save(service));
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
