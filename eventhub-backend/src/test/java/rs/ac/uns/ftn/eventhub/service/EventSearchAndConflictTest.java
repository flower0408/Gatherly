package rs.ac.uns.ftn.eventhub.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import rs.ac.uns.ftn.eventhub.model.entity.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Za razliku od testova sa laznim objektima, ovi idu do prave baze, jer se pretraga
// i otkrivanje preklapanja oslanjaju na upite pisane u SQL-u. Podaci su oni iz data.sql,
// koji se pri svakom pokretanju upisuju ispocetka, pa je ishod uvek isti.
@SpringBootTest
class EventSearchAndConflictTest {

    @Autowired
    private EventService eventService;

    @Test
    void searchWithoutFiltersReturnsEveryActiveEvent() {
        List<Event> found = eventService.searchEvents(null, null, null, null, false);

        assertFalse(found.isEmpty());
        // Otkazana radionica je meko obrisana, pa se ne sme pojaviti
        assertTrue(found.stream().noneMatch((event) -> event.isDeleted()));
    }

    @Test
    void searchByTermLooksIntoTitleDescriptionAndPlace() {
        List<Event> byTitle = eventService.searchEvents("catan", null, null, null, false);
        assertEquals(1, byTitle.size());
        assertEquals("Catan tournament", byTitle.get(0).getTitle());

        List<Event> byPlace = eventService.searchEvents("kulturni", null, null, null, false);
        assertFalse(byPlace.isEmpty());
    }

    @Test
    void searchByTermIgnoresLetterCase() {
        List<Event> lowercase = eventService.searchEvents("catan", null, null, null, false);
        List<Event> uppercase = eventService.searchEvents("CATAN", null, null, null, false);

        assertEquals(lowercase.size(), uppercase.size());
    }

    @Test
    void searchByCategoryReturnsOnlyThatCategory() {
        List<Event> found = eventService.searchEvents(null, "MUSIC", null, null, false);

        assertFalse(found.isEmpty());
        assertTrue(found.stream().allMatch((event) -> event.getCategory().name().equals("MUSIC")));
    }

    // Datumi iz data.sql se pomeraju pri svakom pokretanju, pa se opseg racuna iz dva upisana dogadjaja:
    // setnje fotografa (id 5) i okupljanja zajednice (id 3), koja su dan jedno za drugim
    @Test
    void searchByDateRangeKeepsOnlyEventsInsideIt() {
        LocalDateTime first = eventService.findById(5L).getStartsAt().toLocalDate().atStartOfDay();
        LocalDateTime last = eventService.findById(3L).getStartsAt().toLocalDate().atTime(23, 59, 59);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<Event> found = eventService.searchEvents(null, null, first.format(format), last.format(format), false);

        assertFalse(found.isEmpty());
        for (Event event : found) {
            assertFalse(event.getStartsAt().isBefore(first));
            assertFalse(event.getStartsAt().isAfter(last));
        }
    }

    @Test
    void searchCanLeaveOutEventsThatAlreadyHappened() {
        List<Event> all = eventService.searchEvents(null, null, null, null, false);
        List<Event> upcoming = eventService.searchEvents(null, null, null, null, true);

        assertTrue(upcoming.size() < all.size());
        assertTrue(upcoming.stream().allMatch((event) -> event.getStartsAt().isAfter(LocalDateTime.now())));
    }

    @Test
    void searchReturnsNothingForATermNobodyUsed() {
        assertTrue(eventService.searchEvents("nepostojeci pojam", null, null, null, false).isEmpty());
    }

    // Upozorenje o preklapanju: ana ima potvrdjeno mesto na dogadjaju 8, koji traje od 08:00 do 09:30.
    // Dan se pomera uz data.sql, pa se probni dogadjaji vezuju za vreme upisanog dogadjaja.
    @Test
    void overlapIsFoundForAnEventAtTheSameTime() {
        LocalDateTime start = eventService.findById(8L).getStartsAt();
        Event overlapping = new Event();
        overlapping.setId(-1L);
        overlapping.setStartsAt(start.plusHours(1));
        overlapping.setEndsAt(start.plusHours(3));

        assertNotNull(eventService.findConflictingEventTitle(3L, overlapping));
    }

    @Test
    void noOverlapWhenTheNewEventStartsAfterTheOldOneEnds() {
        LocalDateTime start = eventService.findById(8L).getStartsAt();
        Event later = new Event();
        later.setId(-1L);
        later.setStartsAt(start.plusHours(4));
        later.setEndsAt(start.plusHours(6));

        assertNull(eventService.findConflictingEventTitle(3L, later));
    }

    @Test
    void anEventDoesNotOverlapWithItself() {
        Event same = eventService.findById(8L);

        assertNull(eventService.findConflictingEventTitle(3L, same));
    }
}
