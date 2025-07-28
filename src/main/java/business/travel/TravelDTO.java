package business.travel;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import msa.commons.saga.SagaPhases;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelDTO {
    private long id;
    private long userId;
    private LocalDateTime date;
    private LocalDateTime returnDate;
    private int passengerCounter;
    private double cost;
    private String status;
    private long flightReservationID;
    private long hotelReservationID;
    private double flightCost;
    private double hotelCost;
    private boolean active;
    private LocalDateTime dateCreation;
    private SagaPhases sagaPhases;
    private String sagaId;
    private int transactionActive;
    private List<TravelHistoryDTO> history;
}