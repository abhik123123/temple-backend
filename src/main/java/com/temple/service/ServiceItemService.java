package com.temple.service;

import com.temple.entity.ServiceItem;
import com.temple.repository.ServiceItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ServiceItemService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceItemService.class);

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    /**
     * Get all service items
     */
    public List<ServiceItem> getAllServiceItems() {
        logger.debug("SERVICE_ITEM_FETCH_ALL");
        return serviceItemRepository.findAll();
    }

    /**
     * Get service item by ID
     */
    public ServiceItem getServiceItemById(Long id) {
        logger.debug("SERVICE_ITEM_FETCH_REQUEST | id={}", id);
        return serviceItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service item not found with id: " + id));
    }

    /**
     * Save service item
     */
    public ServiceItem saveServiceItem(ServiceItem serviceItem) {
        logger.info("SERVICE_ITEM_SAVE | name={}", serviceItem.getName());
        return serviceItemRepository.save(serviceItem);
    }

    /**
     * Update service item
     */
    public ServiceItem updateServiceItem(Long id, ServiceItem serviceItem) {
        logger.info("SERVICE_ITEM_UPDATE | id={}", id);

        if (serviceItemRepository.existsById(id)) {
            serviceItem.setId(id);
            return serviceItemRepository.save(serviceItem);
        }
        throw new RuntimeException("Service item not found with id: " + id);
    }

    /**
     * Delete service item
     */
    public void deleteServiceItem(Long id) {
        logger.info("SERVICE_ITEM_DELETE | id={}", id);

        if (serviceItemRepository.existsById(id)) {
            serviceItemRepository.deleteById(id);
        } else {
            throw new RuntimeException("Service item not found with id: " + id);
        }
    }

    /**
     * Get active service items
     */
    public List<ServiceItem> getActiveServiceItems() {
        logger.debug("SERVICE_ITEM_FETCH_ACTIVE");
        return serviceItemRepository.findAll().stream()
                .filter(ServiceItem::isActive)
                .toList();
    }
}

