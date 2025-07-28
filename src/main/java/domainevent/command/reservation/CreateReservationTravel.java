package domainevent.command.reservation;

import domainevent.command.handler.BaseHandler;

import java.util.ArrayList;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import business.qualifier.CreateReservationTravelQualifier;
import business.travel.TravelDTO;
import business.travel.TravelHistoryDTO;
import domainevent.command.handler.CommandHandler;
import msa.commons.commands.createreservation.CreateReservationCommand;
import msa.commons.commands.hotelbooking.CreateHotelBookingCommand;
import msa.commons.event.EventData;
import msa.commons.event.EventId;
import msa.commons.event.eventoperation.reservation.CreateReservation;
import msa.commons.event.type.Type;
import msa.commons.saga.SagaPhases;

@Stateless
@CreateReservationTravelQualifier
@Local(CommandHandler.class)
public class CreateReservationTravel extends BaseHandler {
    private static final Logger LOGGER = LogManager.getLogger(CreateReservationTravel.class);
    @Override
    public void handleCommand(String json) {
        EventData e = this.gson.fromJson(json, EventData.class);

        if (CreateReservation.CREATE_RESERVATION_ONLY_AIRLINE_COMMIT.equals(e.getOperation())) 
            handleCreateReservationAirlineCommit(json);

        if (CreateReservation.CREATE_RESERVATION_ONLY_AIRLINE_ROLLBACK.equals(e.getOperation())) 
            handleCreateReservationAirlineRollback(json);

        if (CreateReservation.CREATE_RESERVATION_ONLY_HOTEL_COMMIT.equals(e.getOperation())) 
            handleCreateReservationHotelCommit(json);

        if (CreateReservation.CREATE_RESERVATION_ONLY_HOTEL_ROLLBACK.equals(e.getOperation())) 
            handleCreateReservationHotelRollback(json);

    }

    private void handleCreateReservationAirlineCommit(final String json) {
        EventData e = EventData.fromJson(json, CreateReservationCommand.class);
        CreateReservationCommand c = (CreateReservationCommand) e.getData();
        LOGGER.info("Commit Create Reservation only Arline: {}", e.getSagaId());
        TravelDTO travelDTO = this.travelService.getTravelById(c.getIdTravelAgency());
        if (travelDTO != null && travelDTO.getSagaId().equals(e.getSagaId()) && travelDTO.getSagaPhases().equals(SagaPhases.STARTED)) {
            double totalCost = c.getFlightInstanceInfo().stream().mapToDouble(f -> f.getPrice() * f.getNumberSeats()).reduce(0.0, Double::sum);
            int passengers = c.getFlightInstanceInfo().stream().mapToInt(f -> f.getNumberSeats()).sum();
            travelDTO.setUserId(c.getIdUser());
            travelDTO.setFlightCost(totalCost);
            travelDTO.setPassengerCounter(passengers);
            travelDTO.setStatus("COMPLETADO");
            travelDTO.setActive(true);
            travelDTO.setFlightReservationID(c.getIdReservation());
            this.travelService.updateTransactionCommit(travelDTO, Type.AIRLINE, this.gson.toJson(c));
        } else {
            e.setOperation(CreateReservation.CREATE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
            this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, e);
        }
    }

    private void handleCreateReservationHotelCommit(final String json) {
        EventData e = EventData.fromJson(json, CreateHotelBookingCommand.class);
        CreateHotelBookingCommand c = (CreateHotelBookingCommand) e.getData();
        LOGGER.info("Commit Create Reservation only Hotel: {}", e.getSagaId());
        TravelDTO travelDTO = this.travelService.getTravelById(c.getIdTravelAgency());
        if (travelDTO != null && travelDTO.getSagaId().equals(e.getSagaId()) && travelDTO.getSagaPhases().equals(SagaPhases.STARTED)) {
            double totalCost = c.getRoomsInfo().stream().mapToDouble(f -> f.getDailyPrice() * c.getNumberOfNights()).sum();
            travelDTO.setHotelCost(totalCost);
            travelDTO.setActive(true);
            travelDTO.setStatus("COMPLETADO");
            travelDTO.setHotelReservationID(c.getBookingId());
            this.travelService.updateTransactionCommit(travelDTO, Type.HOTEL, this.gson.toJson(c));
        } else {
            e.setOperation(CreateReservation.CREATE_RESERVATION_ONLY_HOTEL_ROLLBACK);
            this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, e);    
        }
    }

    private void handleCreateReservationAirlineRollback(final String json) {
        EventData e = EventData.fromJson(json, CreateReservationCommand.class);
        CreateReservationCommand c = (CreateReservationCommand) e.getData();
        LOGGER.info("Rollback Create Reservation Arline: {}", e.getSagaId());
        TravelHistoryDTO travelHistoryDTO = this.travelService.getTravelHistoryBySagaId(e.getSagaId());
        if (travelHistoryDTO.isRollbackAirline()) 
            return;
        TravelDTO travelDTO = this.travelService.getTravelById(c.getIdTravelAgency());
        if (travelDTO == null) { // Lanzar Rollback de Hotel y en otro caso lanzar el de Aerolinea
            LOGGER.error("Travel not found for id: {}", c.getIdTravelAgency());
            TravelHistoryDTO th = this.travelService.getTravelHistoryBySagaId(e.getSagaId());
            if (th != null && th.getJsonCommandHotel() != null) {
                CreateHotelBookingCommand hotelCommand = this.gson.fromJson(th.getJsonCommandHotel(), CreateHotelBookingCommand.class);
                EventData hotelEvent = new EventData(e.getSagaId(), new ArrayList<>(), hotelCommand);
                hotelEvent.setOperation(CreateReservation.CREATE_RESERVATION_ONLY_HOTEL_ROLLBACK);
                this.travelService.updateTravelRollback(travelDTO, Type.HOTEL);
                this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, hotelEvent);
            }
            this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE);
            e.setOperation(CreateReservation.CREATE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
            this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, e);
            return;
        }

        if (travelDTO.getSagaPhases().equals(SagaPhases.STARTED)){
            travelDTO.getHistory().stream()
                .filter(h -> h.getSagaId().equals(e.getSagaId()))
                .findFirst()
                .ifPresent(history -> {
                    if (history.getJsonCommandHotel() != null) 
                        return;
                    CreateHotelBookingCommand hotelCommand = this.gson.fromJson(history.getJsonCommandHotel(), CreateHotelBookingCommand.class);
                    EventData hotelEvent = new EventData(e.getSagaId(), new ArrayList<>(), hotelCommand);
                    hotelEvent.setOperation(CreateReservation.CREATE_RESERVATION_ONLY_HOTEL_ROLLBACK);
                    this.travelService.updateTravelRollback(travelDTO, Type.HOTEL);
                    this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, hotelEvent);
                });

            e.setOperation(CreateReservation.CREATE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
            this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE);
            this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, e);
        } else {
            e.setOperation(CreateReservation.CREATE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
            this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE);
            this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, e);
        }
    }

    private void handleCreateReservationHotelRollback(final String json) {
        EventData e = EventData.fromJson(json, CreateHotelBookingCommand.class);
        CreateHotelBookingCommand c = (CreateHotelBookingCommand) e.getData();
        LOGGER.info("Rollback Create Reservation only Hotel: {}", e.getSagaId());
        TravelHistoryDTO travelHistoryDTO = this.travelService.getTravelHistoryBySagaId(c.getSagaId());
        if (travelHistoryDTO.isRollbackHotel()) 
            return;

        TravelDTO travelDTO = this.travelService.getTravelById(c.getIdTravelAgency());
        if (travelDTO == null) { // Lanzar Rollback de Hotel y en otro caso lanzar el de Aerolinea
            LOGGER.error("Travel not found for id: {}", c.getIdTravelAgency());
            TravelHistoryDTO th = this.travelService.getTravelHistoryBySagaId(e.getSagaId());
            if (th != null && th.getJsonCommandAirline() != null) {
                CreateReservationCommand airlineCommand = this.gson.fromJson(th.getJsonCommandAirline(), CreateReservationCommand.class);
                EventData hotelEvent = new EventData(e.getSagaId(), new ArrayList<>(), airlineCommand);
                hotelEvent.setOperation(CreateReservation.CREATE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
                this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE);
                this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, hotelEvent);
            }
            this.travelService.updateTravelRollback(travelDTO, Type.HOTEL);
            e.setOperation(CreateReservation.CREATE_RESERVATION_ONLY_HOTEL_ROLLBACK);
            this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, e);
            return;
        }

        if (travelDTO.getSagaPhases().equals(SagaPhases.STARTED)){
            travelDTO.getHistory().stream()
                .filter(h -> h.getSagaId().equals(e.getSagaId()))
                .findFirst()
                .ifPresent(history -> {
                    if (history.getJsonCommandHotel() != null) 
                        return;
                    CreateHotelBookingCommand airlineCommand = this.gson.fromJson(history.getJsonCommandHotel(), CreateHotelBookingCommand.class);
                    EventData hotelEvent = new EventData(e.getSagaId(), new ArrayList<>(), airlineCommand);
                    hotelEvent.setOperation(CreateReservation.CREATE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
                    this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE);
                    this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, hotelEvent);
                });

            e.setOperation(CreateReservation.CREATE_RESERVATION_ONLY_HOTEL_ROLLBACK);
            this.travelService.updateTravelRollback(travelDTO, Type.HOTEL);
            this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, e);
        } else {
            e.setOperation(CreateReservation.CREATE_RESERVATION_ONLY_HOTEL_ROLLBACK);
            this.travelService.updateTravelRollback(travelDTO, Type.HOTEL);
            this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, e);
        }
    }


}
