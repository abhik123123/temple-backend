package com.temple.controller;

import com.temple.entity.ServiceItem;
import com.temple.exception.NotFoundException;
import com.temple.repository.ServiceItemRepository;
import jakarta.validation.Valid;
import java.util.List;
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

/** /api/services — pujas and sevas offered by the temple. Writes require ADMIN. */
@RestController
@RequestMapping("/services")
@Transactional
public class ServiceItemController {

    private final ServiceItemRepository repo;

    public ServiceItemController(ServiceItemRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ServiceItem> getAll() {
        return repo.findByActive(true);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ServiceItem getById(@PathVariable Long id) {
        return find(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceItem create(@Valid @RequestBody ServiceItem service) {
        service.setId(null);
        service.setActive(true);
        return repo.save(service);
    }

    @PutMapping("/{id}")
    public ServiceItem update(@PathVariable Long id, @Valid @RequestBody ServiceItem changes) {
        find(id);
        changes.setId(id);
        changes.setActive(true);
        return repo.save(changes);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repo.delete(find(id));
    }

    private ServiceItem find(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Service", id));
    }
}
