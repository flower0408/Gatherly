package rs.ac.uns.ftn.eventhub.service.implementation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.service.MailService;

@Service
public class MailServiceImpl implements MailService {


    private JavaMailSender mailSender;


    // Adresa koja se prikazuje kao posiljalac. Kod nekih servisa se razlikuje od
    // korisnickog imena za SMTP prijavu, zato je odvojena u zasebno podesavanje.
    @Value("${app.mail-from}")
    private String sender;

    @Value("${spring.mail.username}")
    private String smtpUsername;

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private static final Logger logger = LogManager.getLogger(MailServiceImpl.class);

    @Override
    public void sendVerificationMail(User user) {
        String link = baseUrl + "/api/users/verify?token=" + user.getVerificationToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(user.getEmail());
        message.setSubject("Gatherly - confirm your registration");
        message.setText("Hello " + user.getFirstName() + ",\n\n"
                + "Thanks for signing up. Activate your account by clicking the link below:\n\n"
                + link + "\n\n"
                + "If you did not sign up for Gatherly, you can safely ignore this message.");

        send(message, user);
    }

    @Override
    public void sendPasswordChangedMail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(user.getEmail());
        message.setSubject("Gatherly - your password has been changed");
        message.setText("Hello " + user.getFirstName() + ",\n\n"
                + "The password for your account has just been changed.\n\n"
                + "If this was not you, please contact us right away.");

        send(message, user);
    }

    @Override
    public void sendRegistrationAcceptedMail(User user, Event event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(user.getEmail());
        message.setSubject("Gatherly - your spot is confirmed");
        message.setText("Hello " + user.getFirstName() + ",\n\n"
                + "Your registration for \"" + event.getTitle() + "\" has been accepted.\n\n"
                + "When: " + event.getStartsAt() + "\n"
                + "Where: " + event.getLocation() + "\n\n"
                + "See you there.");

        send(message, user);
    }

    @Override
    public void sendRegistrationRejectedMail(User user, Event event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(user.getEmail());
        message.setSubject("Gatherly - your registration was not accepted");
        message.setText("Hello " + user.getFirstName() + ",\n\n"
                + "Unfortunately your registration for \"" + event.getTitle() + "\" was not accepted.\n\n"
                + "You can still browse other events on Gatherly.");

        send(message, user);
    }

    @Override
    public void sendPromotedFromWaitlistMail(User user, Event event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(user.getEmail());
        message.setSubject("Gatherly - a spot opened up for you");
        message.setText("Hello " + user.getFirstName() + ",\n\n"
                + "Someone cancelled, so a spot opened up and you moved off the waiting list for \""
                + event.getTitle() + "\". Your place is now confirmed.\n\n"
                + "When: " + event.getStartsAt() + "\n"
                + "Where: " + event.getLocation() + "\n\n"
                + "If you can no longer make it, please cancel so someone else can take the spot.");

        send(message, user);
    }

    // Neuspelo slanje ne sme da obori zahtev koji ga je pokrenuo, zato se greska samo loguje
    private void send(SimpleMailMessage message, User user) {
        if (smtpUsername == null || smtpUsername.isBlank() || sender == null || sender.isBlank()) {
            logger.warn("SMTP account is not configured, mail to: " + user.getEmail() + " was not sent");
            return;
        }
        try {
            logger.info("Sending mail to: " + user.getEmail());
            mailSender.send(message);
            logger.info("Mail sent to: " + user.getEmail());
        } catch (Exception e) {
            logger.error("Mail to: " + user.getEmail() + " could not be sent: " + e.getMessage());
        }
    }
}
