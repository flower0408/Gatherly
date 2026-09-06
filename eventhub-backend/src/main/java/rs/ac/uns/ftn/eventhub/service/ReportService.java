package rs.ac.uns.ftn.eventhub.service;

import rs.ac.uns.ftn.eventhub.model.entity.Comment;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.Report;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.ReportReason;

import java.util.List;

public interface ReportService {

    Report findById(Long id);

    List<Report> findAll(boolean onlyPending);

    List<Report> findReportsForCommunity(Long communityId);

    Report findPendingReportFromUser(Long byUserId, Long onUserId, Long onEventId, Long onCommentId);

    Report createReport(ReportReason reason, User byUser, User onUser, Event onEvent, Comment onComment);

    Report decide(Report report, boolean accepted);
}
