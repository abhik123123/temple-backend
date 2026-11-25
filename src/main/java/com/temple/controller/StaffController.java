package com.temple.controller;

import com.temple.entity.Staff;
import com.temple.service.StaffService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * /api/staff. Reads are public; writes require ADMIN (SecurityConfig).
 * Create/update accept JSON, or multipart with a "staff" JSON part and an optional "photo".
 */
@RestController
@RequestMapping("/staff")
public class StaffController {

    private final StaffService service;

    public StaffController(StaffService service) {
        this.service = service;
    }

    @GetMapping
    public List<Staff> list(@RequestParam(required = false) String department) {
        return service.getStaffByDepartment(department);
    }

    @GetMapping("/department/{department}")
    public List<Staff> byDepartment(@PathVariable String department) {
        return service.getStaffByDepartment(department);
    }

    @GetMapping("/{id}")
    public Staff get(@PathVariable Long id) {
        return service.getStaffById(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Staff create(@Valid @RequestBody Staff body, Authentication auth) {
        return service.saveStaff(body, null, auth.getName());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Staff createWithPhoto(@Valid @RequestPart("staff") Staff body,
                               @RequestPart(value = "photo", required = false) MultipartFile photo,
                               Authentication auth) {
        return service.saveStaff(body, photo, auth.getName());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Staff update(@PathVariable Long id, @Valid @RequestBody Staff body, Authentication auth) {
        return service.updateStaff(id, body, null, auth.getName());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Staff updateWithPhoto(@PathVariable Long id,
                               @Valid @RequestPart("staff") Staff body,
                               @RequestPart(value = "photo", required = false) MultipartFile photo,
                               Authentication auth) {
        return service.updateStaff(id, body, photo, auth.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteStaff(id);
    }
}
