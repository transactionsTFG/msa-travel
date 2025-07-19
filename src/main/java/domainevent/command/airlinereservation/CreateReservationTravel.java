package domainevent.command.airlinereservation;

import domainevent.command.handler.BaseHandler;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import business.qualifier.CreateReservationTravelQualifier;
import business.travel.TravelDTO;

import domainevent.command.handler.CommandHandler;
import msa.commons.commands.createreservation.CreateReservationCommand;
import msa.commons.event.EventData;
import msa.commons.event.eventoperation.reservation.ReservationAirline;
import msa.commons.saga.SagaPhases;

@Stateless
@CreateReservationTravelQualifier
@Local(CommandHandler.class)
public class CreateReservationTravel extends BaseHandler {
    private static final Logger LOGGER = LogManager.getLogger(CreateReservationTravel.class);
    @Override
    public void handleCommand(String json) {
        EventData e = this.gson.fromJson(json, EventData.class);
        if (ReservationAirline.CREATE_RESERVATION_ONLY_AIRLINE_COMMIT.name().equals(e.getOperation().getOperation())) 
            handleCreateReservationAirlineCommit(json);
        if (ReservationAirline.CREATE_RESERVATION_ONLY_AIRLINE_ROLLBACK.name().equals(e.getOperation().getOperation())) 
            handleCreateReservationAirlineRollback(json);
    }

    private void handleCreateReservationAirlineCommit(final String json) {
        EventData e = EventData.fromJson(json, CreateReservationCommand.class);
        CreateReservationCommand c = (CreateReservationCommand) e.getData();
        LOGGER.info("Commit Create Reservation only Arline: {}", e.getSagaId());
        c.getFlightInstanceInfo().forEach(f -> {
            TravelDTO travelDTO = this.travelService.getTravelById(f.getIdReservationTravel());
            if (travelDTO == null) {
                LOGGER.error("Travel not found for id: {}", f.getIdReservationTravel());
                return;
            }
            travelDTO.setUserId(c.getIdUser());
            travelDTO.setFlightCost(f.getPrice() * f.getNumberSeats());
            travelDTO.setFlightReservationID(f.getIdFlightInstance());
            travelDTO.setSagaPhases(SagaPhases.COMPLETED);
            travelDTO.setActive(true);
            travelDTO.setStatus("RESERVADO");
            travelDTO.setPassengerCounter(f.getNumberSeats());
            this.travelService.updateTravel(travelDTO);
        });
    }

    private void handleCreateReservationAirlineRollback(final String json) {
        EventData e = EventData.fromJson(json, CreateReservationCommand.class);
        CreateReservationCommand c = (CreateReservationCommand) e.getData();
        LOGGER.info("Rollback Create Reservation only Arline: {}", e.getSagaId());
        c.getFlightInstanceInfo().forEach(f -> {
            TravelDTO travelDTO = this.travelService.getTravelById(f.getIdReservationTravel());
            if (travelDTO == null) {
                LOGGER.error("Travel not found for id: {}", f.getIdReservationTravel());
                return;
            }
            travelDTO.setFlightCost(f.getPrice() * f.getNumberSeats());
            travelDTO.setFlightReservationID(f.getIdFlightInstance());
            travelDTO.setSagaPhases(SagaPhases.CANCELLED);
            travelDTO.setActive(false);
            travelDTO.setStatus("CANCELADO");
            travelDTO.setPassengerCounter(f.getNumberSeats());
            this.travelService.updateTravel(travelDTO);
        });

    }
}
