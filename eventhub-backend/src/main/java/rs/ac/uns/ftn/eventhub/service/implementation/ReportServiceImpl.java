package rs.ac.uns.ftn.eventhub.service.implementation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.eventhub.model.entity.Comment;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.Report;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.ReportReason;
import rs.ac.uns.ftn.eventhub.repository.ReportRepository;
import rs.ac.uns.ftn.eventhub.service.ReportService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {


    private ReportRepository reportRepository;


    @Autowired
    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    private static final Logger logger = LogManager.getLogger(ReportServiceImpl.class);

    @Override
    public Report findById(Long id) {
        return reportRepository.findById(id).orElse(null);
    }

    @Override
    public List<Report> findAll(boolean onlyPending) {
        if (onlyPending)
            return reportRepository.findPendingReports().orElse(Collections.emptyList());
        return reportRepository.findAllReports().orElse(Collections.emptyList());
    }

    @Override
    public List<Report> findReportsForCommunity(Long communityId) {
        return reportRepository.findReportsForCommunity(communityId).orElse(Collections.emptyList());
    }

    @Override
    public Report findPendingReportFromUser(Long byUserId, Long onUserId, Long onEventId, Long onCommentId) {
        return reportRepository.findPendingReportFromUser(byUserId, onUserId, onEventId, onCommentId).orElse(null);
    }

    @Override
    public Report createReport(ReportReason reason, User byUser, User onUser, Event onEvent, Comment onComment) {
        Report newReport = new Report();
        newReport.setReason(reason);
        newReport.setTimestamp(LocalDate.now());
        newReport.setByUser(byUser);
        newReport.setOnUser(onUser);
        newReport.setOnEvent(onEvent);
        newReport.setOnComment(onComment);
        // Dok ne bude obradjena, prijava nije ni prihvacena ni odbijena
        newReport.setAccepted(null);
        newReport.setDeleted(false);

        return reportRepository.save(newReport);
    }

    @Override
    public Report decide(Report report, boolean accepted) {
        logger.info("Report with id: " + report.getId() + " is " + (accepted ? "accepted" : "rejected"));
        report.setAccepted(accepted);

        return reportRepository.save(report);
    }
}
