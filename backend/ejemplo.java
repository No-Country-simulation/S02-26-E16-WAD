package com.elevideoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;

import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class ElevideoAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElevideoAppApplication.class, args);
    }
}

@RestController
@RequestMapping("/api/videos")
class VideoController {

    private final Path videoStorage = Paths.get("videos");
    private final Path shortsStorage = Paths.get("shorts");

    public VideoController() throws IOException {
        Files.createDirectories(videoStorage);
        Files.createDirectories(shortsStorage);
    }

    // Upload horizontal video
    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty() || !file.getContentType().startsWith("video/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid video file.");
        }
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetLocation = videoStorage.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("Video uploaded successfully: " + filename);
    }

    // List all uploaded videos
    @GetMapping("/list")
    public List<String> listVideos() throws IOException {
        try (var paths = Files.list(videoStorage)) {
            return paths.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }

    // Generate short from a video (simulate short generation)
    @PostMapping("/generateShort/{videoName}")
    public ResponseEntity<String> generateShort(@PathVariable String videoName) throws IOException {
        Path source = videoStorage.resolve(videoName);
        if (!Files.exists(source)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video not found.");
        }
        // Simulate short generation by copying file with "short_" prefix
        String shortName = "short_" + videoName;
        Path shortPath = shortsStorage.resolve(shortName);
        Files.copy(source, shortPath, StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("Short generated: " + shortName);
    }

    // List all shorts
    @GetMapping("/shorts/list")
    public List<String> listShorts() throws IOException {
        try (var paths = Files.list(shortsStorage)) {
            return paths.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }

    // Download video or short
    @GetMapping("/download/{type}/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String type, @PathVariable String filename) throws IOException {
        Path folder = switch (type.toLowerCase()) {
            case "video" -> videoStorage;
            case "short" -> shortsStorage;
            default -> null;
        };
        if (folder == null) {
            return ResponseEntity.badRequest().build();
        }
        Path filePath = folder.resolve(filename).normalize();
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new UrlResource(filePath.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}

@RestController
@RequestMapping("/api/networking")
class NetworkingController {

    private final Map<String, UserProfile> users = new HashMap<>();

    // Register user profile
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserProfile profile) {
        if (profile.getUsername() == null || profile.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("Username is required.");
        }
        users.put(profile.getUsername(), profile);
        return ResponseEntity.ok("User registered: " + profile.getUsername());
    }

    // Get user profile
    @GetMapping("/profile/{username}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable String username) {
        UserProfile profile = users.get(username);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    // List all users for networking
    @GetMapping("/users")
    public List<UserProfile> listUsers() {
        return new ArrayList<>(users.values());
    }
}

class UserProfile {
    private String username;
    private String fullName;
    private String bio;
    private List<String> socialLinks;
    private List<String> skills;

    public UserProfile() {
        socialLinks = new ArrayList<>();
        skills = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<String> socialLinks) {
        this.socialLinks = socialLinks;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}
