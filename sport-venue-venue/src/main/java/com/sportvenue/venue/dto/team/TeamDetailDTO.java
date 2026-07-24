package com.sportvenue.venue.dto.team;

import com.sportvenue.venue.dto.booking.BookingDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class TeamDetailDTO {
    private TeamDTO team;
    private Map<String, Object> recordSummary;
    private List<BookingDTO> recentBookings;
    private List<TeamAuditDTO> audits;
}
