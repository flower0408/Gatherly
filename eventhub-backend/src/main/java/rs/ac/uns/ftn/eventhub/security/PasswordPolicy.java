package rs.ac.uns.ftn.eventhub.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

// Pravila za lozinku, postavljena prema NIST SP 800-63B (revizija 4, avgust 2025) i prema
// istrazivanju Shay i saradnici, "Designing Password Policies for Strength and Usability",
// ACM TISSEC 18(4), 2016. Oba izvora vode ka istom zakljucku: duzina i provera spiska
// poznatih lozinki daju vise nego zahtevi za mesanjem velikih slova, cifara i znakova.
@Component
public class PasswordPolicy {

    // NIST za lozinku kao jedini cinilac trazi najmanje 15 znakova, a uz drugi cinilac 8.
    // Ovde je uzeto 12, jer je to duzina koju pomenuto istrazivanje nalazi kao onu koja
    // je istovremeno upotrebljiva i otporna, a aplikacija nema drugi cinilac.
    public static final int NAJMANJA_DUZINA = 12;

    // BCrypt racuna samo prvih 72 bajta lozinke, a ostatak tiho zanemaruje. Zato se duza
    // lozinka odbija sa objasnjenjem, umesto da korisnik misli da koristi nesto sto se ne koristi.
    // NIST trazi da se dozvoli bar 64 znaka, sto ovo i dalje ispunjava.
    public static final int NAJVISE_BAJTOVA = 72;

    private Set<String> cesteLozinke = new HashSet<>();

    private static final Logger logger = LogManager.getLogger(PasswordPolicy.class);

    @PostConstruct
    public void loadCommonPasswords() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("common-passwords.txt").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String clean = line.trim().toLowerCase();
                if (!clean.isEmpty() && !clean.startsWith("#"))
                    cesteLozinke.add(clean);
            }
            logger.info("Loaded " + cesteLozinke.size() + " common passwords for the blocklist");
        } catch (Exception e) {
            logger.error("Common password list could not be read: " + e.getMessage());
        }
    }

    // Vraca poruku o problemu, ili null ako je lozinka prihvatljiva
    public String check(String password, String username, String email) {
        if (password == null || password.isBlank())
            return "Please enter a password.";
        if (password.length() < NAJMANJA_DUZINA)
            return "The password must have at least " + NAJMANJA_DUZINA + " characters. "
                    + "A longer phrase is easier to remember and harder to guess than a short one with symbols.";
        if (password.getBytes(StandardCharsets.UTF_8).length > NAJVISE_BAJTOVA)
            return "That password is too long to be stored whole. Please keep it under "
                    + NAJVISE_BAJTOVA + " characters.";

        String lower = password.toLowerCase();
        if (cesteLozinke.contains(lower))
            return "That password appears on lists of the most common passwords. Please choose another one.";

        // Lozinka koja sadrzi korisnicko ime ili adresu je pogodna za pogadjanje
        if (username != null && !username.isBlank() && lower.contains(username.toLowerCase()))
            return "The password must not contain your username.";

        if (email != null && email.contains("@")) {
            String localPart = email.substring(0, email.indexOf('@')).toLowerCase();
            if (localPart.length() > 2 && lower.contains(localPart))
                return "The password must not contain your email address.";
        }

        // Niz istih znakova, ili niz koji je samo jedan znak ponovljen, ne nosi nikakvu snagu
        if (isSingleRepeatedCharacter(password))
            return "The password must not be the same character repeated.";

        return null;
    }

    private boolean isSingleRepeatedCharacter(String password) {
        char first = password.charAt(0);
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) != first)
                return false;
        }
        return true;
    }
}
