package com.project.cravely.services;

import com.project.cravely.models.MenuItem;
import com.project.cravely.repositories.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "cravely.images.bulk-upload.enabled",
        havingValue = "true"
)
public class BulkMenuImageUploader implements CommandLineRunner {

    private final S3Service s3Service;
    private final MenuItemRepository menuItemRepository;

    @Value("${cravely.images.bulk-upload.directory:food-images}")
    private String imageDirectory;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        Path directory = Path.of(imageDirectory);

        if (!Files.isDirectory(directory)) {
            log.error("Image directory does not exist: {}", directory.toAbsolutePath());
            return;
        }

        int uploaded = 0;
        int skipped = 0;
        int failed = 0;

        log.info("Starting Cravely bulk image upload from: {}",
                directory.toAbsolutePath());

        try (Stream<Path> files = Files.list(directory)) {

            for (Path imagePath : files
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedImage)
                    .sorted()
                    .toList()) {

                String fileName = imagePath.getFileName().toString();
                String menuItemName = toMenuItemName(fileName);

                try {
                    Optional<MenuItem> optionalMenuItem =
                            menuItemRepository.findByNameIgnoreCase(menuItemName);

                    if (optionalMenuItem.isEmpty()) {
                        log.warn("No menu item found for image: {}", fileName);
                        skipped++;
                        continue;
                    }

                    MenuItem menuItem = optionalMenuItem.get();

                    byte[] imageBytes = Files.readAllBytes(imagePath);
                    String contentType = getContentType(imagePath);

                    String imageKey = s3Service.uploadImage(
                            imageBytes,
                            contentType
                    );

                    menuItem.updateImageKey(imageKey);
                    menuItemRepository.save(menuItem);

                    uploaded++;

                    log.info("Uploaded image for {} -> {}",
                            menuItem.getName(), imageKey);

                } catch (Exception exception) {
                    failed++;
                    log.error("Failed to upload image: {}",
                            fileName, exception);
                }
            }
        }

        log.info("========== BULK IMAGE UPLOAD SUMMARY ==========");
        log.info("Uploaded: {}", uploaded);
        log.info("Skipped: {}", skipped);
        log.info("Failed: {}", failed);
        log.info("================================================");
    }

    private boolean isSupportedImage(Path path) {
        String name = path.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        return name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".png")
                || name.endsWith(".webp");
    }

    private String toMenuItemName(String fileName) {

        String nameWithoutExtension = fileName.replaceFirst(
                "(?i)\\.(jpg|jpeg|png|webp)$",
                ""
        );

        return nameWithoutExtension
                .replace('-', ' ')
                .trim();
    }

    private String getContentType(Path path) {

        String name = path.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        if (name.endsWith(".png")) {
            return "image/png";
        }

        if (name.endsWith(".webp")) {
            return "image/webp";
        }

        throw new IllegalArgumentException(
                "Unsupported image format: " + name
        );
    }
}