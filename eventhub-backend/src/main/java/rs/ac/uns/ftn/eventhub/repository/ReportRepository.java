package rs.ac.uns.ftn.eventhub.repository;

import rs.ac.uns.ftn.eventhub.model.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query(nativeQuery = true,
            value = "select * from report where is_deleted = false order by timestamp desc")
    Optional<List<Report>> findAllReports();

    @Query(nativeQuery = true,
            value = "select * from report where is_deleted = false and accepted is null order by timestamp desc")
    Optional<List<Report>> findPendingReports();

    // Prijave koje se ticu sadrzaja unutar jedne zajednice: njenih dogadjaja i komentara na njima
    @Query(nativeQuery = true,
            value = "select r.* from report r where r.is_deleted = false and (" +
                    "  r.on_event_id in (select event_id from community_events where community_id = :communityId) " +
                    "  or r.on_comment_id in (select c.id from comment c where c.belongs_to_event_id in " +
                    "        (select event_id from community_events where community_id = :communityId))" +
                    ") order by r.timestamp desc")
    Optional<List<Report>> findReportsForCommunity(@Param("communityId") Long communityId);

    // Ista osoba ne moze dvaput da prijavi isti sadrzaj dok prva prijava nije obradjena
    @Query(nativeQuery = true,
            value = "select * from report where by_user_id = :byUserId and is_deleted = false and accepted is null " +
                    "and ((on_user_id = :onUserId) or (on_event_id = :onEventId) or (on_comment_id = :onCommentId)) limit 1")
    Optional<Report> findPendingReportFromUser(@Param("byUserId") Long byUserId, @Param("onUserId") Long onUserId,
                                               @Param("onEventId") Long onEventId, @Param("onCommentId") Long onCommentId);

    @Transactional
    Integer deleteReportById(Long id);
}
