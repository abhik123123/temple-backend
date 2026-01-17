package com.temple.controller;

import com.temple.entity.BoardMemberService;
import com.temple.security.AdminOnly;
import com.temple.service.BoardMemberServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/board-member-services")
public class BoardMemberServiceController {

    private static final Logger logger = LoggerFactory.getLogger(BoardMemberServiceController.class);

    @Autowired
    private BoardMemberServiceService boardMemberServiceService;

    /**
     * Add service period for board member (Admin only)
     */
    @AdminOnly
    @PostMapping("/add")
    public ResponseEntity<?> addServicePeriod(
            @RequestParam Long boardMemberId,
            @RequestParam String serviceTitle,
            @RequestParam(required = false) String serviceDescription,
            @RequestParam LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String remarks) {

        logger.info("BOARD_SERVICE_ADD_REQUEST | boardMemberId={} | service={}", boardMemberId, serviceTitle);

        try {
            BoardMemberService service = boardMemberServiceService.addServicePeriod(
                    boardMemberId, serviceTitle, serviceDescription, startDate, endDate, remarks);
            return ResponseEntity.ok(service);
        } catch (Exception e) {
            logger.error("BOARD_SERVICE_ADD_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(400).body("Failed to add service: " + e.getMessage());
        }
    }

    /**
     * Get all services for a board member
     */
    @GetMapping("/board-member/{boardMemberId}")
    public ResponseEntity<List<BoardMemberService>> getServicesByBoardMember(@PathVariable Long boardMemberId) {
        logger.info("BOARD_SERVICE_FETCH_REQUEST | boardMemberId={}", boardMemberId);
        List<BoardMemberService> services = boardMemberServiceService.getServicesByBoardMember(boardMemberId);
        return ResponseEntity.ok(services);
    }

    /**
     * Get all active services
     */
    @GetMapping("/active")
    public ResponseEntity<List<BoardMemberService>> getActiveServices() {
        logger.info("BOARD_SERVICE_FETCH_ACTIVE_REQUEST");
        List<BoardMemberService> services = boardMemberServiceService.getActiveServices();
        return ResponseEntity.ok(services);
    }

    /**
     * Get service by ID
     */
    @GetMapping("/{serviceId}")
    public ResponseEntity<?> getServiceById(@PathVariable Long serviceId) {
        logger.info("BOARD_SERVICE_FETCH_REQUEST | serviceId={}", serviceId);

        try {
            BoardMemberService service = boardMemberServiceService.getServiceById(serviceId);
            return ResponseEntity.ok(service);
        } catch (Exception e) {
            logger.error("BOARD_SERVICE_FETCH_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(404).body("Service not found");
        }
    }

    /**
     * Update service period (Admin only)
     */
    @AdminOnly
    @PutMapping("/{serviceId}")
    public ResponseEntity<?> updateServicePeriod(
            @PathVariable Long serviceId,
            @RequestParam String serviceTitle,
            @RequestParam(required = false) String serviceDescription,
            @RequestParam LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String remarks) {

        logger.info("BOARD_SERVICE_UPDATE_REQUEST | serviceId={}", serviceId);

        try {
            BoardMemberService service = boardMemberServiceService.updateServicePeriod(
                    serviceId, serviceTitle, serviceDescription, startDate, endDate, remarks);
            return ResponseEntity.ok(service);
        } catch (Exception e) {
            logger.error("BOARD_SERVICE_UPDATE_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(400).body("Failed to update service: " + e.getMessage());
        }
    }

    /**
     * Delete service (Admin only)
     */
    @AdminOnly
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<?> deleteService(@PathVariable Long serviceId) {
        logger.info("BOARD_SERVICE_DELETE_REQUEST | serviceId={}", serviceId);

        try {
            boardMemberServiceService.deleteService(serviceId);
            return ResponseEntity.ok("Service deleted successfully");
        } catch (Exception e) {
            logger.error("BOARD_SERVICE_DELETE_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(404).body("Service not found");
        }
    }

    /**
     * Get all board member services (Admin only)
     */
    @AdminOnly
    @GetMapping("/all")
    public ResponseEntity<List<BoardMemberService>> getAllServices() {
        logger.info("BOARD_SERVICE_FETCH_ALL_REQUEST");
        List<BoardMemberService> services = boardMemberServiceService.getAllServices();
        return ResponseEntity.ok(services);
    }
}

