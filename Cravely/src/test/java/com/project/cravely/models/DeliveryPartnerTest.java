
package com.project.cravely.models;

import com.project.cravely.enums.DeliveryPartnerStatus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryPartnerTest {

    private DeliveryPartner createPartner() {
        return DeliveryPartner.builder()
                .name("Rahul Kumar")
                .phone("9876543210")
                .build();
    }

    @Test
    void shouldCreatePartnerAsAvailable() {
        DeliveryPartner partner = createPartner();

        assertEquals(
                DeliveryPartnerStatus.AVAILABLE,
                partner.getStatus()
        );
    }

    @Test
    void shouldMarkPartnerOnDelivery() {
        DeliveryPartner partner = createPartner();

        partner.markOnDelivery();

        assertEquals(
                DeliveryPartnerStatus.ON_DELIVERY,
                partner.getStatus()
        );
    }

    @Test
    void shouldNotAssignPartnerTwice() {
        DeliveryPartner partner = createPartner();

        partner.markOnDelivery();

        assertThrows(
                IllegalStateException.class,
                partner::markOnDelivery
        );
    }

    @Test
    void shouldMakePartnerAvailableAfterDelivery() {
        DeliveryPartner partner = createPartner();

        partner.markOnDelivery();
        partner.markAvailable();

        assertEquals(
                DeliveryPartnerStatus.AVAILABLE,
                partner.getStatus()
        );
    }

    @Test
    void shouldNotDeactivatePartnerDuringDelivery() {
        DeliveryPartner partner = createPartner();

        partner.markOnDelivery();

        assertThrows(
                IllegalStateException.class,
                partner::deactivate
        );
    }

    @Test
    void shouldDeactivateAvailablePartner() {
        DeliveryPartner partner = createPartner();

        partner.deactivate();

        assertEquals(
                DeliveryPartnerStatus.INACTIVE,
                partner.getStatus()
        );
    }

    @Test
    void shouldReactivateInactivePartner() {
        DeliveryPartner partner = createPartner();

        partner.deactivate();
        partner.activate();

        assertEquals(
                DeliveryPartnerStatus.AVAILABLE,
                partner.getStatus()
        );
    }
}
