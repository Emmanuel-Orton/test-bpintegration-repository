package com.bearingpoint.beyond.test-bpintegration.api.frontend.domain;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.ServiceorderingV1DomainServiceOrderItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InflightOrderChangesDto {

    List<String> addedItems = new ArrayList<>();
    List<String> removedItems = new ArrayList<>();
    List<String> modifiedItems = new ArrayList<>();

    public void addAddedItem(String id) {
        this.addedItems.add(id);
    }

    public void addModifiedItem(String id) {
        this.modifiedItems.add(id);
    }

    public void addCancelledItem(String id) {
        this.removedItems.add(id);
    }

}
