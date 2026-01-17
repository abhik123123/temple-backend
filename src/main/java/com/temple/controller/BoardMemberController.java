package com.temple.controller;

import com.temple.entity.BoardMember;
import com.temple.service.BoardMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/board-members")
@CrossOrigin(origins = "http://localhost:3000")
public class BoardMemberController {

    @Autowired
    private BoardMemberService boardMemberService;

    @GetMapping
    public ResponseEntity<List<BoardMember>> getAll() {
        List<BoardMember> members = boardMemberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<BoardMember>> getByDepartment(@PathVariable String department) {
        List<BoardMember> members = boardMemberService.getMembersByDepartment(department);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardMember> getById(@PathVariable Long id) {
        BoardMember member = boardMemberService.getMemberById(id);
        if (member != null) {
            return ResponseEntity.ok(member);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<BoardMember> create(@RequestBody BoardMember member) {
        BoardMember savedMember = boardMemberService.saveMember(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMember);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardMember> update(@PathVariable Long id, @RequestBody BoardMember member) {
        BoardMember updatedMember = boardMemberService.updateMember(id, member);
        if (updatedMember != null) {
            return ResponseEntity.ok(updatedMember);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (boardMemberService.getMemberById(id) != null) {
            boardMemberService.deleteMember(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

