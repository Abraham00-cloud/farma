package com.project.farma.finance.mapper;

import com.project.farma.batch.model.Batch;
import com.project.farma.finance.dto.BatchFinancialPnlResponseDto;
import com.project.farma.finance.dto.FinancialCategoryBreakdownDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FinancialPnlMapper {
    public BatchFinancialPnlResponseDto toPnlResponse(
            Batch batch,
            double revenue,
            double expenses,
            double netProfit,
            double marginPercentage,
            double costPerBird,
            double revenuePerBird,
            double profitPerBird,
            List<FinancialCategoryBreakdownDto> chartBreakdown
    ) {
        return new BatchFinancialPnlResponseDto(
                batch.getId(),
                batch.getBatchNumber(),
                revenue,
                expenses,
                netProfit,
                marginPercentage,
                costPerBird,
                revenuePerBird,
                profitPerBird,
                chartBreakdown,
                netProfit >= 0
        );
    }
}
