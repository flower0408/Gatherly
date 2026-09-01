package rs.ac.uns.ftn.eventhub.service;

import rs.ac.uns.ftn.eventhub.model.entity.User;

public interface MailService {

    void sendVerificationMail(User user);

    void sendPasswordChangedMail(User user);

}
