package domainevent.command.user;


import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import business.qualifier.CommitUserQualifier;
import domainevent.command.handler.BaseEventHandler;
import domainevent.command.handler.EventHandler;
import domainevent.publisher.jmseventpublisher.IEventPublisher;
import domainevent.publisher.userqueue.JMSUserPublisherQualifier;
import msa.commons.event.EventId;

@Stateless
@CommitUserQualifier
@Local(EventHandler.class)
public class CommitUserEvent extends BaseEventHandler {
    @Inject
    @Override
    public void setJmsEventDispatcher(@JMSUserPublisherQualifier IEventPublisher jmsEventDispatcher) {
        this.jmsEventDispatcher = jmsEventDispatcher;
    }
    @Override
    public EventId getEventId() {
        return EventId.CREATE_USER;
    }
}
