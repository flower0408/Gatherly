package rs.ac.uns.ftn.eventhub.service;

import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.Image;
import rs.ac.uns.ftn.eventhub.model.entity.User;

import java.util.List;

public interface ImageService {

    Image findById(Long id);

    List<Image> findImagesForEvent(Long eventId);

    Image findProfileImageForUser(Long userId);

    // Snima fajl na disk i vraca putanju pod kojom je dostupan preko HTTP-a
    String storeFile(MultipartFile file);

    Image createEventImage(String path, Event event);

    Image createProfileImage(String path, User user);

    Integer deleteImage(Long id);

    Integer deleteImagesForEvent(Long eventId);
}
