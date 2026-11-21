
package com.project.cravely.repositories;

import com.project.cravely.enums.DeliveryPartnerStatus;
import com.project.cravely.models.DeliveryPartner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryPartnerRepository
        extends JpaRepository<DeliveryPartner, Long> {

    List<DeliveryPartner> findByStatus(
            DeliveryPartnerStatus status
    );

    boolean existsByPhone(String phone);
}
