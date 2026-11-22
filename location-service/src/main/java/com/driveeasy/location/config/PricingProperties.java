package com.driveeasy.location.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Business rules read from config-repo/location-service.yml (driveeasy.location.*).
 * Changing a discount = change the YAML file, no code change, no recompilation.
 */
@ConfigurationProperties(prefix = "driveeasy.location")
public record PricingProperties(
        BigDecimal remiseFidele,        // 0.05 -> 5 %
        BigDecimal remiseVip,           // 0.10 -> 10 %
        BigDecimal coefficientPenalite, // 1.5 x daily price per late day
        int seuilFidele,                // FIDELE after 5 completed rentals
        int seuilVip                    // VIP after 15 completed rentals
) {}
