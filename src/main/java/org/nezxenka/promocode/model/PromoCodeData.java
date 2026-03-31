package org.nezxenka.promocode.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PromoCodeData {
    private String code;
    private int playerUses;
    private int globalUses;
    private String group;
    private boolean needLink;
    private List<String> messages;
    private List<String> commands;
}
