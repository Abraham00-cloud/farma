package com.project.farma.transaction.repostiory;

import com.project.farma.transaction.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // FOR EXTERNAL API (Paginated)
    Page<Transaction> findByOrganisationId(Long organisationId, Pageable pageable);
    Page<Transaction> findByOrganisationIdAndIsCashFlowTrue(Long organisationId, Pageable pageable);
    Page<Transaction> findByBatchIdAndOrganisationId(Long batchId, Long organisationId, Pageable pageable);
    Page<Transaction> findByFarmIdAndOrganisationId(Long farmId, Long organisationId, Pageable pageable);


    // FOR INTERNAL ANALYTICS & CSV EXPORTS (Flat Lists)
    List<Transaction> findByBatchIdAndOrganisationIdOrderByTransactionDateDesc(Long batchId, Long organisationId);
    List<Transaction> findByFarmIdAndOrganisationId(Long farmId, Long organisationId);

    @Query("""
            SELECT t FROM Transaction t WHERE t.organisation.id = :organisationId 
            AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate 
            ORDER BY t.transactionDate DESC
            """)
    List<Transaction> findTransactionsForAudit(
            @Param("organisationId") Long organisationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    //FINANCIAL CALCULATIONS
    @Query("""
            SELECT SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE -t.amount END) 
            FROM Transaction t WHERE t.batch.id = :batchId AND t.organisation.id = :organisationId
            AND t.category NOT IN ('FEED_PURCHASE', 'MEDICINE_PURCHASE', 'VACCINE_PURCHASE', 'EQUIPMENT_PURCHASE')
            """)
    Double calculateNetProfitsByBatchAndOrganisation(@Param("batchId") Long batchId, @Param("organisationId") Long organisationId);

    @Query("""
            SELECT SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE -t.amount END)
            FROM Transaction t WHERE t.farm.id = :farmId AND t.organisation.id = :organisationId
            AND t.category NOT IN ('FEED_PURCHASE', 'MEDICINE_PURCHASE', 'VACCINE_PURCHASE', 'EQUIPMENT_PURCHASE')
            """)
    Double calculateNetProfitByFarm(@Param("farmId") Long farmId, @Param("organisationId") Long organisationId);
}