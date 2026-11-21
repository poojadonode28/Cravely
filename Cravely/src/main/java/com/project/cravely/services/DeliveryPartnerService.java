
package com.project.cravely.services;

import com.project.cravely.dto.CreateDeliveryPartnerRequest;
import com.project.cravely.enums.DeliveryPartnerStatus;
import com.project.cravely.models.DeliveryPartner;
import com.project.cravely.repositories.DeliveryPartnerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;

    public DeliveryPartner register(
            CreateDeliveryPartnerRequest request
    ) {
        if (deliveryPartnerRepository.existsByPhone(request.phone())) {
            throw new IllegalArgumentException(
                    "A delivery partner with this phone number already exists"
            );
        }

        DeliveryPartner partner = DeliveryPartner.builder()
                .name(request.name())
                .phone(request.phone())
                .build();

        return deliveryPartnerRepository.save(partner);
    }

    @Transactional(readOnly = true)
    public DeliveryPartner getById(Long partnerId) {
        return deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Delivery partner not found: " + partnerId
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<DeliveryPartner> getAll() {
        return deliveryPartnerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DeliveryPartner> getAvailablePartners() {
        return deliveryPartnerRepository.findByStatus(
                DeliveryPartnerStatus.AVAILABLE
        );
    }

    public DeliveryPartner deactivate(Long partnerId) {
        DeliveryPartner partner = getById(partnerId);
        partner.deactivate();
        return deliveryPartnerRepository.save(partner);
    }

    public DeliveryPartner activate(Long partnerId) {
        DeliveryPartner partner = getById(partnerId);
        partner.activate();
        return deliveryPartnerRepository.save(partner);
    }
}
