package com.temple.controller;

import com.temple.entity.BoardMember;
import com.temple.service.BoardMemberService;
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
 * /api/board-members. Reads are public; writes require ADMIN (SecurityConfig).
 * Create/update accept JSON, or multipart with a "boardMember" JSON part and an optional "photo".
 */
@RestController
@RequestMapping("/board-members")
public class BoardMemberController {

    private final BoardMemberService service;

    public BoardMemberController(BoardMemberService service) {
        this.service = service;
    }

    @GetMapping
    public List<BoardMember> list(@RequestParam(required = false) String department) {
        return service.getMembersByDepartment(department);
    }

    @GetMapping("/department/{department}")
    public List<BoardMember> byDepartment(@PathVariable String department) {
        return service.getMembersByDepartment(department);
    }

    @GetMapping("/{id}")
    public BoardMember get(@PathVariable Long id) {
        return service.getMemberById(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BoardMember create(@Valid @RequestBody BoardMember body, Authentication auth) {
        return service.saveMember(body, null, auth.getName());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BoardMember createWithPhoto(@Valid @RequestPart("boardMember") BoardMember body,
                               @RequestPart(value = "photo", required = false) MultipartFile photo,
                               Authentication auth) {
        return service.saveMember(body, photo, auth.getName());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BoardMember update(@PathVariable Long id, @Valid @RequestBody BoardMember body, Authentication auth) {
        return service.updateMember(id, body, null, auth.getName());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BoardMember updateWithPhoto(@PathVariable Long id,
                               @Valid @RequestPart("boardMember") BoardMember body,
                               @RequestPart(value = "photo", required = false) MultipartFile photo,
                               Authentication auth) {
        return service.updateMember(id, body, photo, auth.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteMember(id);
    }
}
