package rs.ac.uns.ftn.eventhub.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

// Provera pravila za lozinku, postavljenih prema NIST SP 800-63B i prema radu
// Shay i saradnici, "Designing Password Policies for Strength and Usability" (2016).
class PasswordPolicyTest {

    private PasswordPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new PasswordPolicy();
        policy.loadCommonPasswords();
    }

    @Test
    void acceptsALongPassphraseWithSpaces() {
        assertNull(policy.check("tri zelene jabuke", "mika", "mika@mail.com"));
    }

    @Test
    void acceptsUnicodeCharacters() {
        assertNull(policy.check("sunce iznad reke šćž", "mika", "mika@mail.com"));
    }

    // NIST ne trazi mesanje vrsta znakova, pa lozinka samo od malih slova mora da prodje
    @Test
    void acceptsLettersOnlyWhenLongEnough() {
        assertNull(policy.check("plavikisobrannakisi", "mika", "mika@mail.com"));
    }

    @Test
    void rejectsPasswordShorterThanTheMinimum() {
        assertNotNull(policy.check("kratka12345", "mika", "mika@mail.com"));
    }

    @Test
    void rejectsEmptyPassword() {
        assertNotNull(policy.check("   ", "mika", "mika@mail.com"));
        assertNotNull(policy.check(null, "mika", "mika@mail.com"));
    }

    // NIST trazi poredjenje sa spiskom poznatih i cesto koriscenih lozinki
    @Test
    void rejectsPasswordFromTheCommonList() {
        assertNotNull(policy.check("crvenazvezda", "mika", "mika@mail.com"));
    }

    @Test
    void rejectsPasswordFromTheCommonListRegardlessOfCase() {
        assertNotNull(policy.check("CrvenaZvezda", "mika", "mika@mail.com"));
    }

    @Test
    void rejectsPasswordThatContainsTheUsername() {
        assertNotNull(policy.check("markomarko2026", "markomarko", "marko@mail.com"));
    }

    @Test
    void rejectsPasswordThatContainsTheEmailName() {
        assertNotNull(policy.check("jovanajovana12", "korisnik", "jovana@mail.com"));
    }

    @Test
    void rejectsTheSameCharacterRepeated() {
        assertNotNull(policy.check("aaaaaaaaaaaaaa", "mika", "mika@mail.com"));
    }

    // BCrypt racuna samo prvih 72 bajta, pa se duza lozinka odbija umesto da se preseca
    @Test
    void rejectsPasswordLongerThanBcryptCanUse() {
        StringBuilder duga = new StringBuilder();
        while (duga.length() <= PasswordPolicy.NAJVISE_BAJTOVA) {
            duga.append("duga fraza ");
        }
        assertNotNull(policy.check(duga.toString(), "mika", "mika@mail.com"));
    }

    @Test
    void acceptsPasswordExactlyAtTheMinimumLength() {
        String naGranici = "abcdefghijkl";
        assertNull(policy.check(naGranici, "mika", "mika@mail.com"));
    }
}
