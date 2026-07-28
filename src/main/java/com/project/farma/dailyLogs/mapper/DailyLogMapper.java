package com.project.farma.dailyLogs.mapper;

import com.project.farma.dailyLogs.dto.DailyLogRequestDto;
import com.project.farma.dailyLogs.dto.DailyLogResponseDto;
import com.project.farma.dailyLogs.model.DailyLog;
import org.springframework.stereotype.Component;

@Component
public class DailyLogMapper {

    public DailyLog toDailyLogEntity(DailyLogRequestDto requestDto) {
        return DailyLog.builder()
                .logDate(requestDto.logDate())
                .feedQuantityUsed(requestDto.feedQuantityUsed())
                .medicineQuantityUsed(requestDto.medicineQuantityUsed())
                .administrationMethod(requestDto.administrationMethod())
                .mortalityCount(requestDto.mortalityCount() != null ? requestDto.mortalityCount() : 0)
                .averageWeight(requestDto.averageWeight())
                .observations(requestDto.observations())
                .build();
    }

    public DailyLogResponseDto toDailyLogResponseDto(DailyLog dailyLog) {
        if (dailyLog == null) return null;

        return new DailyLogResponseDto(
                dailyLog.getId(),
                dailyLog.getLogDate(),
                dailyLog.getBatch() != null ? dailyLog.getBatch().getId() : null,
                dailyLog.getBatch() != null ? dailyLog.getBatch().getBatchNumber() : "UNKNOWN_BATCH",


                dailyLog.getFeedInventory() != null ? dailyLog.getFeedInventory().getName() : "None Applied",
                dailyLog.getFeedQuantityUsed(),


                dailyLog.getMedicineInventory() != null ? dailyLog.getMedicineInventory().getName() : "None Applied",
                dailyLog.getMedicineQuantityUsed(),

                dailyLog.getMortalityCount(),
                dailyLog.getAverageWeight(),
                dailyLog.getObservations(),


                dailyLog.getRecordedBy() != null
                        ? dailyLog.getRecordedBy().getFirstName() + " " + dailyLog.getRecordedBy().getLastName()
                        : "System Automated",

                dailyLog.getAssignedTo() != null ? dailyLog.getAssignedTo().getFirstName() : "unassigned",
                dailyLog.getCreatedAt()
        );
    }
}