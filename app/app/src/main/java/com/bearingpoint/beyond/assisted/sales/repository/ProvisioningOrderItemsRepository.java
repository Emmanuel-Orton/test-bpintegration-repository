package com.bearingpoint.beyond.test-bpintegration.repository;

import com.bearingpoint.beyond.test-bpintegration.repository.domain.ProvisioningOrderItemEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ProvisioningOrderItemsRepository extends CrudRepository<ProvisioningOrderItemEntity, Long> {

    List<ProvisioningOrderItemEntity> findByWholesaleOrderIdAndDafWorkflowId(Long wholesaleOrderId, String dafWorkflowId);

    List<ProvisioningOrderItemEntity> findByWholesaleOrderId(Long wholesaleOrderId);

    List<ProvisioningOrderItemEntity> findByRetailOrderId(Long retailOrderId);

    List<ProvisioningOrderItemEntity> findByDafWorkflowId(String dafWorkflowId);

    @Query("from ProvisioningOrderItemEntity c where c.deleteAfter < :currentTime")
    List<ProvisioningOrderItemEntity> findDeletableItems(Instant currentTime);

    List<ProvisioningOrderItemEntity> findByWholesaleOrderIdAndTaskId(Long wholesaleOrderId, Long taskId);

    @Modifying
    @Query("update ProvisioningOrderItemEntity entity set entity.taskId = :taskId where entity.id = :id")
    List<ProvisioningOrderItemEntity> updateServiceEntryTaskIdById(@Param("taskId") Long taskId, @Param("id")Long id);

    @Modifying
    @Query(value = "update assisted_sales.provisioning_order_items set delete_after = :deleteAfter where wholesale_order_id = :wholesaleOrderId", nativeQuery = true)
    void updateServiceEntryDeleteAfter(@Param("deleteAfter") Instant deleteAfter, @Param("wholesaleOrderId")Long wholesaleOrderId);

}
