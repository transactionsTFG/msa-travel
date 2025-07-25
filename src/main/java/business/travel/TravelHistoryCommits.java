package business.travel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Getter
@Setter
@NoArgsConstructor
public class TravelHistoryCommits {
    @Id
    private String sagaId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "travel_id")
    private Travel travel;
    @Column(nullable = true, name = "json_command_hotel")
    private String jsonCommandHotel;
    @Column(nullable = true, name = "json_command_airline")
    private String jsonCommandAirline;
    @Version
    private int version;

    public TravelHistoryDTO toDTO() {
        return TravelHistoryDTO.builder()
                .sagaId(this.sagaId)
                .travelId(this.travel.getId())
                .jsonCommandHotel(this.jsonCommandHotel)
                .jsonCommandAirline(this.jsonCommandAirline)
                .build();
    }
}
