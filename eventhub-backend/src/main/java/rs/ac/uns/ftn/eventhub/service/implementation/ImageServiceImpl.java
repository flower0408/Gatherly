package rs.ac.uns.ftn.eventhub.service.implementation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.Image;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.repository.ImageRepository;
import rs.ac.uns.ftn.eventhub.service.ImageService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {

    // Dozvoljeni tipovi slika, sve ostalo se odbija
    private static final List<String> ALLOWED_TYPES =
            Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");


    private ImageRepository imageRepository;


    @Value("${app.upload-dir}")
    private String uploadDir;

    @Autowired
    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    private static final Logger logger = LogManager.getLogger(ImageServiceImpl.class);

    @Override
    public Image findById(Long id) {
        Optional<Image> image = imageRepository.findById(id);
        if (!image.isEmpty())
            return image.get();
        logger.error("Repository search for image with id: " + id + " returned null");
        return null;
    }

    @Override
    public List<Image> findImagesForEvent(Long eventId) {
        return imageRepository.findImagesForEvent(eventId).orElse(Collections.emptyList());
    }

    @Override
    public Image findProfileImageForUser(Long userId) {
        return imageRepository.findProfileImageForUser(userId).orElse(null);
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.error("Uploaded file is empty");
            return null;
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            logger.error("Uploaded file has unsupported type: " + file.getContentType());
            return null;
        }

        // Ime fajla koje je poslao klijent se ne koristi, jer moze da sadrzi putanju.
        // Cuva se samo ekstenzija, a ime se generise, cime se izbegava i sudar istoimenih fajlova.
        String extension = extensionFor(file.getContentType());
        String storedName = UUID.randomUUID().toString() + extension;

        try {
            Path folder = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(folder);
            try (InputStream stream = file.getInputStream()) {
                Files.copy(stream, folder.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
            }
            logger.info("Stored uploaded file as: " + storedName);
        } catch (IOException e) {
            logger.error("Uploaded file could not be stored: " + e.getMessage());
            return null;
        }

        return "/uploads/" + storedName;
    }

    @Override
    public Image createEventImage(String path, Event event) {
        Image newImage = new Image();
        newImage.setPath(path);
        newImage.setBelongsToEvent(event);
        newImage.setDeleted(false);

        return imageRepository.save(newImage);
    }

    @Override
    public Image createProfileImage(String path, User user) {
        // Korisnik ima jednu profilnu sliku, pa se prethodna gasi
        imageRepository.deleteProfileImagesForUser(user.getId());

        Image newImage = new Image();
        newImage.setPath(path);
        newImage.setBelongsToUser(user);
        newImage.setDeleted(false);

        return imageRepository.save(newImage);
    }

    @Override
    public Integer deleteImage(Long id) {
        return imageRepository.deleteImageById(id);
    }

    @Override
    public Integer deleteImagesForEvent(Long eventId) {
        return imageRepository.deleteImagesForEvent(eventId);
    }

    private String extensionFor(String contentType) {
        switch (contentType) {
            case "image/png":
                return ".png";
            case "image/gif":
                return ".gif";
            case "image/webp":
                return ".webp";
            default:
                return ".jpg";
        }
    }
}
