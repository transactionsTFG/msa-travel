package domainevent.command.reservation;

import java.util.ArrayList;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import business.qualifier.UpdateReservationTravelQualifier;
import business.travel.TravelDTO;
import business.travel.TravelHistoryDTO;
import domainevent.command.handler.BaseHandler;
import domainevent.command.handler.CommandHandler;
import msa.commons.commands.hotelbooking.UpdateHotelBookingCommand;
import msa.commons.commands.modifyreservation.UpdateReservationCommand;

import msa.commons.event.EventData;
import msa.commons.event.EventId;

import msa.commons.event.eventoperation.reservation.UpdateReservation;
import msa.commons.event.type.Type;
import msa.commons.saga.SagaPhases;

@Stateless
@UpdateReservationTravelQualifier
@Local(CommandHandler.class)
public class UpdateReservationTravel extends BaseHandler {
    private static final Logger LOGGER = LogManager.getLogger(UpdateReservationTravel.class);

    @Override
    public void handleCommand(String json) {
        EventData e = this.gson.fromJson(json, EventData.class);
        if (UpdateReservation.UPDATE_RESERVATION_ONLY_AIRLINE_BEGIN.equals(e.getOperation()) || UpdateReservation.UPDATE_RESERVATION_ONLY_HOTEL_BEGIN.equals(e.getOperation()))
            this.jmsEventPublisher.publish(EventId.UPDATE_RESERVATION_TRAVEL, e);

        if (UpdateReservation.UPDATE_RESERVATION_ONLY_AIRLINE_COMMIT.equals(e.getOperation()))
            this.handleUpdateReservationAirlineCommit(json);

        if (UpdateReservation.UPDATE_RESERVATION_ONLY_HOTEL_COMMIT.equals(e.getOperation())) 
            this.handleUpdateReservationHotelCommit(json);

        if (UpdateReservation.UPDATE_RESERVATION_ONLY_HOTEL_ROLLBACK.equals(e.getOperation()))
            this.handleUpdateReservationHotelRollback(json);

        if (UpdateReservation.UPDATE_RESERVATION_ONLY_AIRLINE_ROLLBACK.equals(e.getOperation()))
            this.handleUpdateReservationAirlineRollback(json);
    }

    private void handleUpdateReservationAirlineCommit(final String json) {
        EventData e = EventData.fromJson(json, UpdateReservationCommand.class);
        UpdateReservationCommand command = (UpdateReservationCommand) e.getData();
        TravelDTO travel = this.travelService.getTravelById(command.getIdTravel());
        if (travel == null)
            return;
        this.travelService.updateTransactionCommit(travel, Type.AIRLINE, this.gson.toJson(command));
        TravelDTO updatedTravel = this.travelService.getTravelById(command.getIdTravel());
        if (updatedTravel.getTransactionActive() == 0) {
            TravelHistoryDTO history = this.travelService.getTravelHistoryBySagaId(updatedTravel.getSagaId());
            UpdateReservationCommand updateCommandAirline = this.gson.fromJson(history.getJsonCommandAirline(), UpdateReservationCommand.class);
            updatedTravel.setFlightCost(updateCommandAirline.getTotalPrice());
            updatedTravel.setDate(updateCommandAirline.getMinDateTime());
            updatedTravel.setReturnDate(updateCommandAirline.getMaxDateTime());
            updatedTravel.setPassengerCounter(updateCommandAirline.getNumberOfSeats());
            if (history.getJsonCommandHotel() != null) {
                UpdateHotelBookingCommand updateCommandHotel = this.gson.fromJson(history.getJsonCommandHotel(), UpdateHotelBookingCommand.class);
                updatedTravel.setHotelCost(updateCommandHotel.getTotalPrice());
            }
            this.travelService.forceEndTransaction(updatedTravel);
        }
    }

    private void handleUpdateReservationHotelCommit(final String json) {
        EventData e = EventData.fromJson(json, UpdateHotelBookingCommand.class);
        UpdateHotelBookingCommand command = (UpdateHotelBookingCommand) e.getData();
        TravelDTO travel = this.travelService.getTravelById(command.getIdTravel());
        if (travel == null)
            return;
        this.travelService.updateTransactionCommit(travel, Type.HOTEL, this.gson.toJson(command));
        TravelDTO updatedTravel = this.travelService.getTravelById(command.getIdTravel());
        if (updatedTravel.getTransactionActive() == 0) {
            TravelHistoryDTO history = this.travelService.getTravelHistoryBySagaId(updatedTravel.getSagaId());
            UpdateHotelBookingCommand updateCommandBooking = this.gson.fromJson(history.getJsonCommandHotel(), UpdateHotelBookingCommand.class);
            updatedTravel.setHotelCost(updateCommandBooking.getTotalPrice());
            if (history.getJsonCommandAirline() != null) {
                UpdateReservationCommand updateCommandAirline = this.gson.fromJson(history.getJsonCommandAirline(), UpdateReservationCommand.class);
                updatedTravel.setFlightCost(updateCommandAirline.getTotalPrice());
                updatedTravel.setDate(updateCommandAirline.getMinDateTime());
                updatedTravel.setReturnDate(updateCommandAirline.getMaxDateTime());
                updatedTravel.setPassengerCounter(updateCommandAirline.getNumberOfSeats());
            }
            this.travelService.forceEndTransaction(updatedTravel);
        }
    }

    private void handleUpdateReservationHotelRollback(final String json) {
        EventData e = EventData.fromJson(json, UpdateHotelBookingCommand.class);
        UpdateHotelBookingCommand c = (UpdateHotelBookingCommand) e.getData();
        TravelHistoryDTO travelHistoryDTO = this.travelService.getTravelHistoryBySagaId(e.getSagaId());
        if (travelHistoryDTO.isRollbackHotel()) 
            return;

        TravelDTO travelDTO = this.travelService.getTravelById(c.getIdTravel());
        if (travelDTO == null) { // Lanzar Rollback de Hotel y en otro caso lanzar el de Aerolinea
            LOGGER.error("Travel not found for id: {}", c.getIdTravel());
            TravelHistoryDTO th = this.travelService.getTravelHistoryBySagaId(e.getSagaId());
            if (th != null && th.getJsonCommandAirline() != null) {
                UpdateReservationCommand airlineCommand = this.gson.fromJson(th.getJsonCommandAirline(), UpdateReservationCommand.class);
                EventData hotelEvent = new EventData(e.getSagaId(), new ArrayList<>(), airlineCommand);
                hotelEvent.setOperation(UpdateReservation.UPDATE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
                this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE, false);
                this.jmsEventPublisher.publish(EventId.UPDATE_RESERVATION_TRAVEL, hotelEvent);
            }
            this.travelService.updateTravelRollback(travelDTO, Type.HOTEL, false);
            e.setOperation(UpdateReservation.UPDATE_RESERVATION_ONLY_HOTEL_ROLLBACK);
            this.jmsEventPublisher.publish(EventId.UPDATE_RESERVATION_TRAVEL, e);
            return;
        }

        if (travelDTO.getSagaPhases().equals(SagaPhases.STARTED)){
            travelDTO.getHistory().stream()
                .filter(h -> h.getSagaId().equals(e.getSagaId()))
                .findFirst()
                .ifPresent(history -> {
                    if (history.getJsonCommandHotel() != null) 
                        return;
                    UpdateReservationCommand airlineCommand = this.gson.fromJson(history.getJsonCommandHotel(), UpdateReservationCommand.class);
                    EventData hotelEvent = new EventData(e.getSagaId(), new ArrayList<>(), airlineCommand);
                    hotelEvent.setOperation(UpdateReservation.UPDATE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
                    this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE, false);
                    this.jmsEventPublisher.publish(EventId.UPDATE_RESERVATION_TRAVEL, hotelEvent);
                });

            e.setOperation(UpdateReservation.UPDATE_RESERVATION_ONLY_HOTEL_ROLLBACK);
            this.travelService.updateTravelRollback(travelDTO, Type.HOTEL, false);
            this.jmsEventPublisher.publish(EventId.UPDATE_RESERVATION_TRAVEL, e);
        } else {
            e.setOperation(UpdateReservation.UPDATE_RESERVATION_ONLY_HOTEL_ROLLBACK);
            this.travelService.updateTravelRollback(travelDTO, Type.HOTEL, false);
            this.jmsEventPublisher.publish(EventId.UPDATE_RESERVATION_TRAVEL, e);
        }
        
    }

    private void handleUpdateReservationAirlineRollback(final String json) {
        EventData e = EventData.fromJson(json, UpdateReservationCommand.class);
        UpdateReservationCommand c = (UpdateReservationCommand) e.getData();
        TravelHistoryDTO travelHistoryDTO = this.travelService.getTravelHistoryBySagaId(e.getSagaId());
        if (travelHistoryDTO.isRollbackAirline()) 
            return;
        TravelDTO travelDTO = this.travelService.getTravelById(c.getIdTravel());
        if (travelDTO == null) { // Lanzar Rollback de Hotel y en otro caso lanzar el de Aerolinea
            LOGGER.error("Travel not found for id: {}", c.getIdTravel());
            TravelHistoryDTO th = this.travelService.getTravelHistoryBySagaId(e.getSagaId());
            if (th != null && th.getJsonCommandHotel() != null) {
                UpdateHotelBookingCommand hotelCommand = this.gson.fromJson(th.getJsonCommandHotel(), UpdateHotelBookingCommand.class);
                EventData hotelEvent = new EventData(e.getSagaId(), new ArrayList<>(), hotelCommand);
                hotelEvent.setOperation(UpdateReservation.UPDATE_RESERVATION_ONLY_HOTEL_ROLLBACK);
                this.travelService.updateTravelRollback(travelDTO, Type.HOTEL, false);
                this.jmsEventPublisher.publish(EventId.UPDATE_RESERVATION_TRAVEL, hotelEvent);
            }
            this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE, false);
            e.setOperation(UpdateReservation.UPDATE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
            this.jmsEventPublisher.publish(EventId.UPDATE_RESERVATION_TRAVEL, e);
            return;
        }

        if (travelDTO.getSagaPhases().equals(SagaPhases.STARTED)){
            travelDTO.getHistory().stream()
                .filter(h -> h.getSagaId().equals(e.getSagaId()))
                .findFirst()
                .ifPresent(history -> {
                    if (history.getJsonCommandHotel() != null) 
                        return;
                    UpdateHotelBookingCommand hotelCommand = this.gson.fromJson(history.getJsonCommandHotel(), UpdateHotelBookingCommand.class);
                    EventData hotelEvent = new EventData(e.getSagaId(), new ArrayList<>(), hotelCommand);
                    hotelEvent.setOperation(UpdateReservation.UPDATE_RESERVATION_ONLY_HOTEL_ROLLBACK);
                    this.travelService.updateTravelRollback(travelDTO, Type.HOTEL, false);
                    this.jmsEventPublisher.publish(EventId.UPDATE_RESERVATION_TRAVEL, hotelEvent);
                });

            e.setOperation(UpdateReservation.UPDATE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
            this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE, false);
            this.jmsEventPublisher.publish(EventId.UPDATE_RESERVATION_TRAVEL, e);
        } else {
            e.setOperation(UpdateReservation.UPDATE_RESERVATION_ONLY_AIRLINE_ROLLBACK);
            this.travelService.updateTravelRollback(travelDTO, Type.AIRLINE, false);
            this.jmsEventPublisher.publish(EventId.UPDATE_RESERVATION_TRAVEL, e);
        }
    }
}

