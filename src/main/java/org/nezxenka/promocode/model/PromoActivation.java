package org.nezxenka.promocode.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoActivation {
    private int id;
    private String playerName;
    private String ipAddress;
    private String promoCode;
    private String promoGroup;
    private int activationCount;
    private Timestamp firstActivation;
    private Timestamp lastActivation;
}
