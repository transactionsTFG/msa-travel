package business.travel;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import msa.commons.saga.SagaPhases;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Travel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@Column(nullable = false)
	private Long userId;
	private String date;
	private String returnDate;
    @Column(nullable = false)
	private int passengerCounter;
    @Column(nullable = false)
	private double cost;
    @Column(nullable = false)
	private String status;
	private long flightReservationID;
	private long hotelReservationID;
	private double flightCost;
	private double hotelCost;
    private boolean active;
    @Column(nullable = false) 
    private LocalDateTime dateCreation;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status_saga")
    private SagaPhases statusSaga;
    @Column(nullable = false, name = "saga_id")
    private String sagaId;
    @Column(nullable = false, name = "transaction_active")
    private int transactionActive;
    @OneToMany(
        mappedBy = "travel",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<TravelHistoryCommits> historyCommits;
    @Version
    private int version;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    public Travel(TravelDTO dto){
        this.id = dto.getId();
        this.userId = dto.getUserId();
        this.date = dto.getDate();
        this.returnDate = dto.getReturnDate();
        this.passengerCounter = dto.getPassengerCounter();
        this.cost = dto.getCost();
        this.status = dto.getStatus();
        this.flightReservationID = dto.getFlightReservationID();
        this.hotelReservationID = dto.getHotelReservationID();
        this.flightCost = dto.getFlightCost();
        this.hotelCost = dto.getHotelCost();
        this.active = dto.isActive();
        this.statusSaga = dto.getSagaPhases();
        this.sagaId = dto.getSagaId();
        this.transactionActive = dto.getTransactionActive();
    }

    public TravelDTO toDTO(){
        return TravelDTO.builder()
            .id(this.id)
            .dateCreation(this.dateCreation)
            .userId(this.getUserId())
            .date(this.date)
            .returnDate(this.returnDate)
            .passengerCounter(this.passengerCounter)
            .cost(this.cost)
            .status(this.status)
            .flightReservationID(this.flightReservationID)
            .hotelReservationID(this.hotelReservationID)
            .flightCost(this.flightCost)
            .hotelCost(this.hotelCost)
            .active(this.active)
            .sagaPhases(this.statusSaga)
            .sagaId(this.sagaId)
            .transactionActive(this.transactionActive)
            .history(this.historyCommits.stream()
                .map(TravelHistoryCommits::toDTO)
                .toList())
            .build();
    }
}