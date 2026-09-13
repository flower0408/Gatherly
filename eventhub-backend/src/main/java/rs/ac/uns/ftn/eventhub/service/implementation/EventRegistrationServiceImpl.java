package rs.ac.uns.ftn.eventhub.service.implementation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.EventRegistration;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.RegistrationStatus;
import rs.ac.uns.ftn.eventhub.repository.EventRegistrationRepository;
import rs.ac.uns.ftn.eventhub.service.EventRegistrationService;
import rs.ac.uns.ftn.eventhub.service.MailService;
import rs.ac.uns.ftn.eventhub.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class EventRegistrationServiceImpl implements EventRegistrationService {


    private EventRegistrationRepository eventRegistrationRepository;


    private UserService userService;


    private MailService mailService;


    @Autowired
    public EventRegistrationServiceImpl(EventRegistrationRepository eventRegistrationRepository,
                                        UserServiceImpl userService, MailService mailService) {
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.userService = userService;
        this.mailService = mailService;
    }

    // Kada se oslobodi mesto, prvi sa liste cekanja prelazi u prihvacene i dobija obavestenje.
    // Stoji u servisu jer mesto moze da oslobodi i otkazivanje i odluka organizatora i blokada.
    @Override
    public void promoteFromWaitlist(Event event) {
        if (event == null)
            return;
        if (countTakenSpots(event.getId()) >= event.getCapacity()) {
            logger.info("Event with id: " + event.getId() + " is still full, nobody is promoted");
            return;
        }
        EventRegistration next = findFirstWaitlisted(event.getId());
        if (next == null) {
            logger.info("Waiting list for event with id: " + event.getId() + " is empty");
            return;
        }
        logger.info("Promoting registration with id: " + next.getId() + " from the waiting list");
        updateStatus(next, RegistrationStatus.ACCEPTED);
        mailService.sendPromotedFromWaitlistMail(userService.findById(next.getCreatedBy().getId()), event);
    }

    private static final Logger logger = LogManager.getLogger(EventRegistrationServiceImpl.class);

    @Override
    public EventRegistration findById(Long id) {
        Optional<EventRegistration> registration = eventRegistrationRepository.findById(id);
        if (!registration.isEmpty())
            return registration.get();
        logger.error("Repository search for registration with id: " + id + " returned null");
        return null;
    }

    @Override
    public List<EventRegistration> findRegistrationsForEvent(Long eventId) {
        return eventRegistrationRepository.findRegistrationsByEventId(eventId).orElse(Collections.emptyList());
    }

    @Override
    public List<EventRegistration> findAcceptedForEvent(Long eventId) {
        return eventRegistrationRepository.findAcceptedByEventId(eventId).orElse(Collections.emptyList());
    }

    @Override
    public List<EventRegistration> findRegistrationsForUser(Long userId) {
        return eventRegistrationRepository.findRegistrationsByUserId(userId).orElse(Collections.emptyList());
    }

    @Override
    public EventRegistration findActiveRegistration(Long userId, Long eventId) {
        return eventRegistrationRepository.findActiveRegistration(userId, eventId).orElse(null);
    }

    @Override
    public Integer countTakenSpots(Long eventId) {
        return eventRegistrationRepository.countTakenSpots(eventId);
    }

    @Override
    public EventRegistration findFirstWaitlisted(Long eventId) {
        return eventRegistrationRepository.findFirstWaitlisted(eventId).orElse(null);
    }


    // Skor pouzdanosti je procenat dolazaka u odnosu na dogadjaje na kojima se korisnik ocekivao.
    // Otkazane prijave se ne racunaju, jer je otkazivanje pozeljno ponasanje - oslobadja mesto drugome.
    // Vraca null ako korisnik jos nema istoriju, da se ne bi prikazivala nula bez pokrica.
    // Broj dogadjaja na kojima je zabelezeno da li se korisnik pojavio ili nije
    @Override
    public Integer countAttendanceRecords(Long userId) {
        Integer attended = eventRegistrationRepository.countByUserAndStatus(userId, RegistrationStatus.ATTENDED.name());
        Integer noShow = eventRegistrationRepository.countByUserAndStatus(userId, RegistrationStatus.NO_SHOW.name());
        return attended + noShow;
    }

    @Override
    public Integer calculateReliability(Long userId) {
        Integer attended = eventRegistrationRepository.countByUserAndStatus(userId, RegistrationStatus.ATTENDED.name());
        Integer noShow = eventRegistrationRepository.countByUserAndStatus(userId, RegistrationStatus.NO_SHOW.name());
        Integer total = attended + noShow;

        if (total == 0) {
            return null;
        }

        return Math.round((attended * 100.0f) / total);
    }

    @Override
    public EventRegistration createRegistration(User user, Event event, RegistrationStatus status) {
        EventRegistration newRegistration = new EventRegistration();
        newRegistration.setStatus(status);
        newRegistration.setCreatedAt(LocalDateTime.now());
        newRegistration.setCreatedBy(user);
        newRegistration.setForEvent(event);
        newRegistration.setDeleted(false);
        newRegistration = eventRegistrationRepository.save(newRegistration);

        return newRegistration;
    }

    @Override
    public EventRegistration updateStatus(EventRegistration registration, RegistrationStatus status) {
        logger.info("Changing status of registration with id: " + registration.getId()
                + " from " + registration.getStatus() + " to " + status);
        registration.setStatus(status);
        // Polje 'at' belezi trenutak kada je prijava obradjena
        registration.setAt(LocalDateTime.now());

        return eventRegistrationRepository.save(registration);
    }

    @Override
    public EventRegistration saveRegistration(EventRegistration registration) {
        return eventRegistrationRepository.save(registration);
    }

    @Override
    public Integer deleteRegistrationsForEvent(Long eventId) {
        return eventRegistrationRepository.deleteRegistrationsForEvent(eventId);
    }
}
