package com.project.farma.transaction.repostiory;

import com.project.farma.transaction.dto.TransactionRequestDto;
import com.project.farma.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByOrganisationIdAndIsCashFlowTrueOrderByTransactionDateDesc(Long organisationId);

    List<Transaction> findByBatchIdAndOrganisationIdOrderByTransactionDateDesc(Long batchId, Long organisationId);

    @Query("""
            SELECT t FROM Transaction t WHERE t.batch.section.farm.id = :farmId 
            AND t.organisation.id = :organisationId ORDER BY t.transactionDate DESC
            """)
    List<Transaction> findByFarmIdAndOrganisationId(@Param("farmId") Long farmId, @Param("organisationId") Long organisationId);

    @Query("""
            SELECT SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE -t.amount END) 
            FROM Transaction t WHERE t.batch.id = :batchId AND t.organisation.id = :organisationId
            """)
    Double calculateNetProfitsByBatchAndOrganisation(@Param("batchId") Long batchId,@Param("organisationId") Long organisationId);

    @Query("""
            SELECT SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE -t.amount END)
            FROM Transaction t WHERE t.batch.section.farm.id = :farmId AND t.organisation.id = :organisationId
            """)
    Double calculateNetProfitByFarm(@Param("farmId") Long farmId, @Param("organisationId") Long organisationId);
}
