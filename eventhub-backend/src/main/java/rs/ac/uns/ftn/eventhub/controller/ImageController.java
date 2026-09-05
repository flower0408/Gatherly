package rs.ac.uns.ftn.eventhub.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.eventhub.model.dto.ImageDTO;
import rs.ac.uns.ftn.eventhub.model.entity.Image;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.security.TokenUtils;
import rs.ac.uns.ftn.eventhub.service.ImageService;
import rs.ac.uns.ftn.eventhub.service.UserService;
import rs.ac.uns.ftn.eventhub.service.implementation.ImageServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.UserServiceImpl;


@RestController
@RequestMapping("api/images")
public class ImageController {


    ImageService imageService;


    UserService userService;


    TokenUtils tokenUtils;

    private static final Logger logger = LogManager.getLogger(ImageController.class);

    @Autowired
    public ImageController(ImageServiceImpl imageService, UserServiceImpl userService, TokenUtils tokenUtils) {
        this.imageService = imageService;
        this.userService = userService;
        this.tokenUtils = tokenUtils;
    }

    // Fajl se prvo otprema, pa se dobijena putanja salje uz dogadjaj ili profil
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Storing uploaded file for user with id: " + user.getId());
        String path = imageService.storeFile(file);
        if (path == null) {
            logger.error("File could not be stored");
            return new ResponseEntity<>("Only image files up to 5MB are allowed.", HttpStatus.BAD_REQUEST);
        }
        ImageDTO imageDTO = new ImageDTO();
        imageDTO.setPath(path);
        logger.info("Created and sent response");

        return new ResponseEntity<>(imageDTO, HttpStatus.CREATED);
    }

    @PostMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> setProfileImage(@RequestBody ImageDTO imageDTO,
                                             @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (imageDTO.getPath() == null || imageDTO.getPath().isBlank()) {
            logger.error("Image path is missing");
            return new ResponseEntity<>("Image path is missing.", HttpStatus.BAD_REQUEST);
        }
        logger.info("Setting profile image of user with id: " + user.getId());
        Image image = imageService.createProfileImage(imageDTO.getPath(), user);

        return new ResponseEntity<>(new ImageDTO(image), HttpStatus.OK);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ImageDTO> getProfileImage(@PathVariable String userId) {
        logger.info("Finding profile image of user with id: " + userId);
        Image image = imageService.findProfileImageForUser(Long.parseLong(userId));
        if (image == null) {
            logger.error("User with id: " + userId + " has no profile image");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new ImageDTO(image), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Image image = imageService.findById(Long.parseLong(id));
        if (image == null) {
            logger.error("Image not found with id: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        logger.info("Deleting image with id: " + id);
        imageService.deleteImage(image.getId());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private User findUserByToken(String token) {
        String cleanToken = token.substring(7);
        String username = tokenUtils.getUsernameFromToken(cleanToken);
        return userService.findByUsername(username);
    }
}
