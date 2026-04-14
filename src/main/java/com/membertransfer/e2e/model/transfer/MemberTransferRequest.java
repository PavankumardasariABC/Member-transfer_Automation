package com.membertransfer.e2e.model.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.With;

@With
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberTransferRequest {

    String memberId;
    String toClubNumber;
    String auditUser;
}
