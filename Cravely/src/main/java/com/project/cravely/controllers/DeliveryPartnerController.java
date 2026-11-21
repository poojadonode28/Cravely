
package com.project.cravely.controllers;

import com.project.cravely.dto.CreateDeliveryPartnerRequest;
import com.project.cravely.models.DeliveryPartner;
import com.project.cravely.services.DeliveryPartnerService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-partners")
@RequiredArgsConstructor
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryPartnerService;

    @PostMapping
    public DeliveryPartner register(
            @Valid @RequestBody CreateDeliveryPartnerRequest request
    ) {
        return deliveryPartnerService.register(request);
    }

    @GetMapping("/{partnerId}")
    public DeliveryPartner getById(
            @PathVariable Long partnerId
    ) {
        return deliveryPartnerService.getById(partnerId);
    }

    @GetMapping
    public List<DeliveryPartner> getAll() {
        return deliveryPartnerService.getAll();
    }

    @GetMapping("/available")
    public List<DeliveryPartner> getAvailablePartners() {
        return deliveryPartnerService.getAvailablePartners();
    }

    @PatchMapping("/{partnerId}/deactivate")
    public DeliveryPartner deactivate(
            @PathVariable Long partnerId
    ) {
        return deliveryPartnerService.deactivate(partnerId);
    }

    @PatchMapping("/{partnerId}/activate")
    public DeliveryPartner activate(
            @PathVariable Long partnerId
    ) {
        return deliveryPartnerService.activate(partnerId);
    }
}
