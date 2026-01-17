package com.temple.service;

import com.temple.entity.BoardMember;
import com.temple.entity.BoardMemberService;
import com.temple.repository.BoardMemberRepository;
import com.temple.repository.BoardMemberServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class BoardMemberServiceService {

    private static final Logger logger = LoggerFactory.getLogger(BoardMemberServiceService.class);

    @Autowired
    private BoardMemberServiceRepository boardMemberServiceRepository;

    @Autowired
    private BoardMemberRepository boardMemberRepository;

    /**
     * Add service period for a board member
     */
    public BoardMemberService addServicePeriod(Long boardMemberId, String serviceTitle,
                                               String serviceDescription, LocalDate startDate,
                                               LocalDate endDate, String remarks) {
        logger.info("BOARD_SERVICE_ADD_START | boardMemberId={} | service={}", boardMemberId, serviceTitle);

        try {
            BoardMember boardMember = boardMemberRepository.findById(boardMemberId)
                    .orElseThrow(() -> new RuntimeException("Board member not found"));

            BoardMemberService service = BoardMemberService.builder()
                    .boardMember(boardMember)
                    .serviceTitle(serviceTitle)
                    .serviceDescription(serviceDescription)
                    .serviceStartDate(startDate)
                    .serviceEndDate(endDate)
                    .status(determineStatus(startDate, endDate))
                    .remarks(remarks)
                    .build();

            BoardMemberService savedService = boardMemberServiceRepository.save(service);
            logger.info("BOARD_SERVICE_ADD_SUCCESS | serviceId={}", savedService.getId());
            return savedService;

        } catch (Exception e) {
            logger.error("BOARD_SERVICE_ADD_ERROR | error={}", e.getMessage(), e);
            throw new RuntimeException("Failed to add service: " + e.getMessage());
        }
    }

    /**
     * Get all services for a board member
     */
    public List<BoardMemberService> getServicesByBoardMember(Long boardMemberId) {
        logger.debug("BOARD_SERVICE_FETCH_START | boardMemberId={}", boardMemberId);
        return boardMemberServiceRepository.findByBoardMemberId(boardMemberId);
    }

    /**
     * Get all active services
     */
    public List<BoardMemberService> getActiveServices() {
        logger.debug("BOARD_SERVICE_FETCH_START | status=Active");
        return boardMemberServiceRepository.findByStatus("Active");
    }

    /**
     * Get service by ID
     */
    public BoardMemberService getServiceById(Long serviceId) {
        return boardMemberServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + serviceId));
    }

    /**
     * Update service period
     */
    public BoardMemberService updateServicePeriod(Long serviceId, String serviceTitle,
                                                   String serviceDescription, LocalDate startDate,
                                                   LocalDate endDate, String remarks) {
        logger.info("BOARD_SERVICE_UPDATE_START | serviceId={}", serviceId);

        try {
            BoardMemberService service = getServiceById(serviceId);
            service.setServiceTitle(serviceTitle);
            service.setServiceDescription(serviceDescription);
            service.setServiceStartDate(startDate);
            service.setServiceEndDate(endDate);
            service.setStatus(determineStatus(startDate, endDate));
            service.setRemarks(remarks);

            BoardMemberService updatedService = boardMemberServiceRepository.save(service);
            logger.info("BOARD_SERVICE_UPDATE_SUCCESS | serviceId={}", serviceId);
            return updatedService;

        } catch (Exception e) {
            logger.error("BOARD_SERVICE_UPDATE_ERROR | error={}", e.getMessage(), e);
            throw new RuntimeException("Failed to update service: " + e.getMessage());
        }
    }

    /**
     * Delete/deactivate service
     */
    public void deleteService(Long serviceId) {
        logger.info("BOARD_SERVICE_DELETE_START | serviceId={}", serviceId);

        BoardMemberService service = getServiceById(serviceId);
        service.setStatus("Inactive");
        boardMemberServiceRepository.save(service);

        logger.info("BOARD_SERVICE_DELETE_SUCCESS | serviceId={}", serviceId);
    }

    /**
     * Determine status based on dates
     */
    private String determineStatus(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {
            return "Upcoming";
        } else if (today.isAfter(endDate)) {
            return "Completed";
        } else {
            return "Active";
        }
    }

    /**
     * Get all board member services
     */
    public List<BoardMemberService> getAllServices() {
        return boardMemberServiceRepository.findAll();
    }
}

