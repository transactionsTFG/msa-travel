package domainevent.command.reservation;

import java.util.ArrayList;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import business.qualifier.RemoveReservationTravelQualifier;
import business.travel.TravelDTO;
import business.travel.TravelHistoryDTO;
import domainevent.command.handler.BaseHandler;
import domainevent.command.handler.CommandHandler;
import msa.commons.commands.removereservation.RemoveBookingCommand;
import msa.commons.commands.removereservation.RemoveReservationCommand;
import msa.commons.event.EventData;
import msa.commons.event.EventId;
import msa.commons.event.eventoperation.reservation.CreateReservation;
import msa.commons.event.eventoperation.reservation.DeleteReservation;
import msa.commons.event.type.Type;
import msa.commons.saga.SagaPhases;

@Stateless
@RemoveReservationTravelQualifier
@Local(CommandHandler.class)
public class RemoveReservationTravel extends BaseHandler {
    private static final String CANCELADO = "CANCELADO";
    private static final Logger LOGGER = LogManager.getLogger(RemoveReservationTravel.class);

    @Override
    public void handleCommand(String json) {
        EventData e = this.gson.fromJson(json, EventData.class);
        if (DeleteReservation.DELETE_RESERVATION_ONLY_AIRLINE_BEGIN.equals(e.getOperation()) || DeleteReservation.DELETE_RESERVATION_ONLY_HOTEL_BEGIN.equals(e.getOperation()))
            this.jmsEventPublisher.publish(EventId.REMOVE_RESERVATION_TRAVEL, e);
        
        if (DeleteReservation.DELETE_RESERVATION_ONLY_AIRLINE_COMMIT.equals(e.getOperation()))
            this.handleRemoveReservationAirlineCommit(json);

        if (DeleteReservation.DELETE_RESERVATION_ONLY_HOTEL_COMMIT.equals(e.getOperation())) 
            this.handleRemoveReservationHotelCommit(json);

        if (DeleteReservation.DELETE_RESERVATION_ONLY_HOTEL_ROLLBACK.equals(e.getOperation()))
            this.handleRemoveReservationHotelRollback(json);
        
        if (DeleteReservation.DELETE_RESERVATION_ONLY_AIRLINE_ROLLBACK.equals(e.getOperation()))
            this.handleRemoveReservationAirlineRollback(json);
    }

    private void handleRemoveReservationAirlineCommit(final String json) {
        EventData e = EventData.fromJson(json, RemoveReservationCommand.class);
        RemoveReservationCommand command = (RemoveReservationCommand) e.getData();
        TravelDTO t = this.travelService.getTravelById(command.getIdTravel());
        if (t == null) 
            return;

        t.setActive(false);
        t.setStatus(CANCELADO);
        this.travelService.updateTransactionCommit(t, Type.AIRLINE, json);
    }

    private void handleRemoveReservationHotelCommit(final String json) {
        EventData e = EventData.fromJson(json, RemoveBookingCommand.class);
        RemoveBookingCommand command = (RemoveBookingCommand) e.getData();
        TravelDTO t = this.travelService.getTravelById(command.getIdTravel());
        if (t == null) 
            return;
        
        t.setActive(false);
        t.setStatus(CANCELADO);
        this.travelService.updateTransactionCommit(t, Type.HOTEL, json);
    }

    private void handleRemoveReservationHotelRollback(final String json) {
        EventData e = EventData.fromJson(json, RemoveBookingCommand.class);
        RemoveBookingCommand c = (RemoveBookingCommand) e.getData();
        TravelHistoryDTO travelHistoryDTO = this.travelService.getTravelHistoryBySagaId(e.getSagaId());
        if (travelHistoryDTO.isRollbackHotel()) 
            return;

        TravelDTO travelDTO = this.travelService.getTravelById(c.getIdTravel());
        if (travelDTO == null) { // Lanzar Rollback de Hotel y en otro caso lanzar el de Aerolinea
            LOGGER.error("Travel not found for id: {}", c.getIdTravel());
            TravelHistoryDTO th = this.travelService.getTravelHistoryBySagaId(e.getSagaId());
            if (th != null && th.getJsonCommandAirline() != null) {
                RemoveReservationCommand airlineCommand = this.gson.fromJson(th.getJsonCommandAirline(), RemoveReservationCommand.class);
                EventData hotelEvent = new EventData(e.getSagaId(), new ArrayList<>(), airlineCommand);
                hotelEvent.setOperation(DeleteReservation.DELETE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
                this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE);
                this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, hotelEvent);
            }
            this.travelService.updateTravelRollback(travelDTO, Type.HOTEL);
            e.setOperation(DeleteReservation.DELETE_RESERVATION_ONLY_HOTEL_ROLLBACK);
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
                    RemoveReservationCommand airlineCommand = this.gson.fromJson(history.getJsonCommandHotel(), RemoveReservationCommand.class);
                    EventData hotelEvent = new EventData(e.getSagaId(), new ArrayList<>(), airlineCommand);
                    hotelEvent.setOperation(DeleteReservation.DELETE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
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

    private void handleRemoveReservationAirlineRollback(final String json) {
        EventData e = EventData.fromJson(json, RemoveReservationCommand.class);
        RemoveReservationCommand c = (RemoveReservationCommand) e.getData();
        TravelHistoryDTO travelHistoryDTO = this.travelService.getTravelHistoryBySagaId(e.getSagaId());
        if (travelHistoryDTO.isRollbackAirline()) 
            return;
        TravelDTO travelDTO = this.travelService.getTravelById(c.getIdTravel());
        if (travelDTO == null) { // Lanzar Rollback de Hotel y en otro caso lanzar el de Aerolinea
            LOGGER.error("Travel not found for id: {}", c.getIdTravel());
            TravelHistoryDTO th = this.travelService.getTravelHistoryBySagaId(e.getSagaId());
            if (th != null && th.getJsonCommandHotel() != null) {
                RemoveBookingCommand hotelCommand = this.gson.fromJson(th.getJsonCommandHotel(), RemoveBookingCommand.class);
                EventData hotelEvent = new EventData(e.getSagaId(), new ArrayList<>(), hotelCommand);
                hotelEvent.setOperation(DeleteReservation.DELETE_RESERVATION_ONLY_HOTEL_ROLLBACK);
                this.travelService.updateTravelRollback(travelDTO, Type.HOTEL);
                this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, hotelEvent);
            }
            this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE);
            e.setOperation(DeleteReservation.DELETE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
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
                    RemoveBookingCommand hotelCommand = this.gson.fromJson(history.getJsonCommandHotel(), RemoveBookingCommand.class);
                    EventData hotelEvent = new EventData(e.getSagaId(), new ArrayList<>(), hotelCommand);
                    hotelEvent.setOperation(DeleteReservation.DELETE_RESERVATION_ONLY_HOTEL_ROLLBACK);
                    this.travelService.updateTravelRollback(travelDTO, Type.HOTEL);
                    this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, hotelEvent);
                });

            e.setOperation(DeleteReservation.DELETE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
            this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE);
            this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, e);
        } else {
            e.setOperation(DeleteReservation.DELETE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
            this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE);
            this.jmsEventPublisher.publish(EventId.CREATE_RESERVATION_TRAVEL, e);
        }
    }



}
