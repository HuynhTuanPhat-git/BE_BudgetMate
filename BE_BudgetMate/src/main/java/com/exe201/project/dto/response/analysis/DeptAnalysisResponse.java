package com.exe201.project.dto.response.analysis;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeptAnalysisResponse {
    String name;
    LocalDate deadline;
    Double target;
    Double currentAmount;
}