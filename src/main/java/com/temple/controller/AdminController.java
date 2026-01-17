package com.temple.controller;

import com.temple.entity.ServiceItem;
import com.temple.entity.Staff;
import com.temple.entity.BoardMember;
import com.temple.security.AdminOnly;
import com.temple.service.ServiceItemService;
import com.temple.service.StaffService;
import com.temple.service.BoardMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private ServiceItemService serviceItemService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private BoardMemberService boardMemberService;

    // ======================== SERVICE ITEMS MANAGEMENT ========================

    /**
     * Add new service (Admin only)
     */
    @AdminOnly
    @PostMapping("/services/add")
    public ResponseEntity<?> addService(@RequestBody ServiceItem serviceItem) {
        logger.info("SERVICE_ADD_REQUEST | serviceName={}", serviceItem.getName());

        try {
            ServiceItem savedService = serviceItemService.saveServiceItem(serviceItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedService);
        } catch (Exception e) {
            logger.error("SERVICE_ADD_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(400).body("Failed to add service: " + e.getMessage());
        }
    }

    /**
     * Get all services
     */
    @GetMapping("/services/all")
    public ResponseEntity<List<ServiceItem>> getAllServices() {
        logger.info("SERVICE_FETCH_ALL");
        List<ServiceItem> services = serviceItemService.getAllServiceItems();
        return ResponseEntity.ok(services);
    }

    /**
     * Get service by ID
     */
    @GetMapping("/services/{serviceId}")
    public ResponseEntity<?> getServiceById(@PathVariable Long serviceId) {
        logger.info("SERVICE_FETCH_REQUEST | serviceId={}", serviceId);

        try {
            ServiceItem service = serviceItemService.getServiceItemById(serviceId);
            return ResponseEntity.ok(service);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Service not found");
        }
    }

    /**
     * Update service (Admin only)
     */
    @AdminOnly
    @PutMapping("/services/{serviceId}")
    public ResponseEntity<?> updateService(@PathVariable Long serviceId, @RequestBody ServiceItem serviceItem) {
        logger.info("SERVICE_UPDATE_REQUEST | serviceId={}", serviceId);

        try {
            serviceItem.setId(serviceId);
            ServiceItem updatedService = serviceItemService.updateServiceItem(serviceId, serviceItem);
            return ResponseEntity.ok(updatedService);
        } catch (Exception e) {
            logger.error("SERVICE_UPDATE_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(400).body("Failed to update service: " + e.getMessage());
        }
    }

    /**
     * Delete service (Admin only)
     */
    @AdminOnly
    @DeleteMapping("/services/{serviceId}")
    public ResponseEntity<?> deleteService(@PathVariable Long serviceId) {
        logger.info("SERVICE_DELETE_REQUEST | serviceId={}", serviceId);

        try {
            serviceItemService.deleteServiceItem(serviceId);
            return ResponseEntity.ok("Service deleted successfully");
        } catch (Exception e) {
            logger.error("SERVICE_DELETE_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(404).body("Service not found");
        }
    }

    // ======================== STAFF MANAGEMENT ========================

    /**
     * Add new staff member with photo (Admin only)
     */
    @AdminOnly
    @PostMapping("/staff/add")
    public ResponseEntity<?> addStaff(
            @RequestPart("staff") Staff staff,
            @RequestPart(value = "photo", required = false) MultipartFile photoFile,
            Authentication authentication) {

        logger.info("STAFF_ADD_REQUEST | staffName={} | hasPhoto={}",
                   staff.getFullName(), photoFile != null && !photoFile.isEmpty());

        try {
            Staff savedStaff = staffService.saveStaff(staff, photoFile, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStaff);
        } catch (Exception e) {
            logger.error("STAFF_ADD_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(400).body("Failed to add staff: " + e.getMessage());
        }
    }

    /**
     * Get all staff
     */
    @GetMapping("/staff/all")
    public ResponseEntity<List<Staff>> getAllStaff() {
        logger.info("STAFF_FETCH_ALL");
        List<Staff> staffList = staffService.getAllStaff();
        return ResponseEntity.ok(staffList);
    }

    /**
     * Get staff by ID
     */
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<?> getStaffById(@PathVariable Long staffId) {
        logger.info("STAFF_FETCH_REQUEST | staffId={}", staffId);

        try {
            Staff staff = staffService.getStaffById(staffId);
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Staff not found");
        }
    }

    /**
     * Update staff with photo (Admin only)
     */
    @AdminOnly
    @PutMapping("/staff/{staffId}")
    public ResponseEntity<?> updateStaff(
            @PathVariable Long staffId,
            @RequestPart("staff") Staff staff,
            @RequestPart(value = "photo", required = false) MultipartFile photoFile,
            Authentication authentication) {

        logger.info("STAFF_UPDATE_REQUEST | staffId={} | hasPhoto={}",
                   staffId, photoFile != null && !photoFile.isEmpty());

        try {
            Staff updatedStaff = staffService.updateStaff(staffId, staff, photoFile, authentication.getName());
            return ResponseEntity.ok(updatedStaff);
        } catch (Exception e) {
            logger.error("STAFF_UPDATE_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(400).body("Failed to update staff: " + e.getMessage());
        }
    }

    // ======================== BOARD MEMBERS MANAGEMENT ========================

    /**
     * Add new board member with photo (Admin only)
     */
    @AdminOnly
    @PostMapping("/board-members/add")
    public ResponseEntity<?> addBoardMember(
            @RequestPart("boardMember") BoardMember boardMember,
            @RequestPart(value = "photo", required = false) MultipartFile photoFile,
            Authentication authentication) {

        logger.info("BOARD_MEMBER_ADD_REQUEST | memberName={} | hasPhoto={}",
                   boardMember.getFullName(), photoFile != null && !photoFile.isEmpty());

        try {
            BoardMember savedMember = boardMemberService.saveMember(boardMember, photoFile, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMember);
        } catch (Exception e) {
            logger.error("BOARD_MEMBER_ADD_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(400).body("Failed to add board member: " + e.getMessage());
        }
    }

    /**
     * Get all board members
     */
    @GetMapping("/board-members/all")
    public ResponseEntity<List<BoardMember>> getAllBoardMembers() {
        logger.info("BOARD_MEMBER_FETCH_ALL");
        List<BoardMember> members = boardMemberService.getAllBoardMembers();
        return ResponseEntity.ok(members);
    }

    /**
     * Get board member by ID
     */
    @GetMapping("/board-members/{memberId}")
    public ResponseEntity<?> getBoardMemberById(@PathVariable Long memberId) {
        logger.info("BOARD_MEMBER_FETCH_REQUEST | memberId={}", memberId);

        try {
            BoardMember member = boardMemberService.getBoardMemberById(memberId);
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Board member not found");
        }
    }

    /**
     * Update board member with photo (Admin only)
     */
    @AdminOnly
    @PutMapping("/board-members/{memberId}")
    public ResponseEntity<?> updateBoardMember(
            @PathVariable Long memberId,
            @RequestPart("boardMember") BoardMember boardMember,
            @RequestPart(value = "photo", required = false) MultipartFile photoFile,
            Authentication authentication) {

        logger.info("BOARD_MEMBER_UPDATE_REQUEST | memberId={} | hasPhoto={}",
                   memberId, photoFile != null && !photoFile.isEmpty());

        try {
            BoardMember updatedMember = boardMemberService.updateMember(memberId, boardMember, photoFile, authentication.getName());
            return ResponseEntity.ok(updatedMember);
        } catch (Exception e) {
            logger.error("BOARD_MEMBER_UPDATE_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(400).body("Failed to update board member: " + e.getMessage());
        }
    }
}

