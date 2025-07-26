package business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightInstanceSeatsDTO {
    private long idFlightInstance;
    private int numberSeats;
}
