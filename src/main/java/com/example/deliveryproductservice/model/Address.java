package com.example.deliveryproductservice.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.*;

import java.math.BigDecimal;
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Column(nullable = false, length = 200)
    private String street;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String region;

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String postalCode;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(length = 500)
    private String fullAddress;


    @PrePersist
    @PreUpdate
    public void generateFullAddress() {
        if (fullAddress == null || fullAddress.trim().isEmpty()) {
            this.fullAddress = getFormattedAddress();
        }
    }

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(street);
        sb.append(", ").append(city);
        if (region != null && !region.isEmpty()) {
            sb.append(", ").append(region);
        }
        if (country != null && !country.isEmpty()) {
            sb.append(", ").append(country);
        }
        return sb.toString();
    }

    // ✅ Метод для расчета расстояния между адресами:
    public double distanceToKm(Address other) {
        return calculateDistance(
                this.latitude.doubleValue(), this.longitude.doubleValue(),
                other.latitude.doubleValue(), other.longitude.doubleValue()
        );
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Радиус Земли в км

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}