package rs.ac.uns.ftn.eventhub.service;

import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.User;

public interface MailService {

    void sendVerificationMail(User user);

    void sendPasswordChangedMail(User user);

    void sendRegistrationAcceptedMail(User user, Event event);

    void sendRegistrationRejectedMail(User user, Event event);

    void sendPromotedFromWaitlistMail(User user, Event event);

}
