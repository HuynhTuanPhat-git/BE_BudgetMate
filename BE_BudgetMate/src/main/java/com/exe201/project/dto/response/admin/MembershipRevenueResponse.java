package com.exe201.project.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipRevenueResponse {
    private Long membershipPlanId;
    private String membershipName;
    private Double revenue;
    private Integer subscriptionCount;
    private Double averageOrderValue;
}
