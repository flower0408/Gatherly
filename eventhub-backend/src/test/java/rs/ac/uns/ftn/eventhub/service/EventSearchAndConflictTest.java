package rs.ac.uns.ftn.eventhub.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import rs.ac.uns.ftn.eventhub.model.entity.Event;

import java.time.LocalDateTime;
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

        List<Event> byPlace = eventService.searchEvents("kvartic", null, null, null, false);
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

    @Test
    void searchByDateRangeKeepsOnlyEventsInsideIt() {
        List<Event> found = eventService.searchEvents(null, null,
                "2026-09-13 00:00:00", "2026-09-14 23:59:59", false);

        assertFalse(found.isEmpty());
        for (Event event : found) {
            assertTrue(event.getStartsAt().isAfter(LocalDateTime.of(2026, 9, 12, 23, 59)));
            assertTrue(event.getStartsAt().isBefore(LocalDateTime.of(2026, 9, 15, 0, 0)));
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

    // Upozorenje o preklapanju: ana ima potvrdjeno mesto na dogadjaju 8, koji traje
    // 26. septembra od 08:00 do 09:30
    @Test
    void overlapIsFoundForAnEventAtTheSameTime() {
        Event overlapping = new Event();
        overlapping.setId(-1L);
        overlapping.setStartsAt(LocalDateTime.of(2026, 9, 26, 9, 0));
        overlapping.setEndsAt(LocalDateTime.of(2026, 9, 26, 11, 0));

        assertNotNull(eventService.findConflictingEventTitle(3L, overlapping));
    }

    @Test
    void noOverlapWhenTheNewEventStartsAfterTheOldOneEnds() {
        Event later = new Event();
        later.setId(-1L);
        later.setStartsAt(LocalDateTime.of(2026, 9, 26, 12, 0));
        later.setEndsAt(LocalDateTime.of(2026, 9, 26, 14, 0));

        assertNull(eventService.findConflictingEventTitle(3L, later));
    }

    @Test
    void anEventDoesNotOverlapWithItself() {
        Event same = eventService.findById(8L);

        assertNull(eventService.findConflictingEventTitle(3L, same));
    }
}
