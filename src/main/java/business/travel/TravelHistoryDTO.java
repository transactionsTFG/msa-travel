package business.travel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TravelHistoryDTO {
    private String sagaId;
    private long travelId;
    private String jsonCommandHotel;
    private String jsonCommandAirline;
}
