package business.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHotelBookingDTO {
    private long bookingId;
    private long travelId;
    private String customerDNI;
    private String startDate;
    private String endDate;
    private int numberOfNights;
    private Boolean withBreakfast;
    private int peopleNumber;
    private List<Long> roomsInfo;
}
