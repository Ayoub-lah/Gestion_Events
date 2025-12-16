package com.eventbooking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Créer le dossier d'upload s'il n'existe pas
     */
    private void ensureUploadDirectoryExists() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    /**
     * Sauvegarde un fichier multipart (upload classique)
     */
    public String storeFile(MultipartFile file) throws IOException {
        ensureUploadDirectoryExists();

        // Générer un nom de fichier unique
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = Paths.get(uploadDir).resolve(uniqueFileName);

        // Copier le fichier
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Retourner le chemin relatif
        return "/uploads/" + uniqueFileName;
    }

    /**
     * Sauvegarde une image en base64
     */
    public String storeBase64Image(String base64Image, String prefix) throws IOException {
        ensureUploadDirectoryExists();

        if (base64Image == null || base64Image.isEmpty()) {
            return null;
        }

        try {
            // Extraire le type MIME et les données
            String[] parts = base64Image.split(",");
            String mimeType = parts[0].split(";")[0].split(":")[1];
            String extension = getExtensionFromMimeType(mimeType);
            String data = parts[1];

            // Générer un nom de fichier unique
            String uniqueFileName = (prefix != null ? prefix + "_" : "") +
                    UUID.randomUUID().toString() + extension;

            // Décoder et sauvegarder
            byte[] imageBytes = Base64.getDecoder().decode(data);
            Path filePath = Paths.get(uploadDir).resolve(uniqueFileName);
            Files.write(filePath, imageBytes);

            // Retourner le chemin relatif
            return "/uploads/" + uniqueFileName;
        } catch (Exception e) {
            throw new IOException("Erreur lors du traitement de l'image base64", e);
        }
    }

    /**
     * Supprime un fichier
     */
    public boolean deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || !fileUrl.contains("/uploads/")) {
            return false;
        }

        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        Path filePath = Paths.get(uploadDir, fileName);

        if (Files.exists(filePath)) {
            return Files.deleteIfExists(filePath);
        }
        return false;
    }

    /**
     * Convertit le type MIME en extension
     */
    private String getExtensionFromMimeType(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            default -> ".jpg";
        };
    }

    /**
     * Vérifie si un fichier est une image
     */
    public boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Vérifie si une chaîne est une image base64
     */
    public boolean isBase64Image(String str) {
        return str != null && str.startsWith("data:image/");
    }

    /**
     * Récupère le chemin complet d'un fichier
     */
    public String getFullUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        // Si le chemin contient déjà l'URL complète, le retourner tel quel
        if (filePath.startsWith("http")) {
            return filePath;
        }

        // Sinon, construire l'URL complète
        return baseUrl + filePath;
    }

    /**
     * Obtient le chemin relatif depuis une URL
     */
    public String getRelativePath(String fullUrl) {
        if (fullUrl == null || fullUrl.isEmpty()) {
            return null;
        }

        // Si c'est déjà un chemin relatif
        if (fullUrl.startsWith("/uploads/")) {
            return fullUrl;
        }

        // Extraire le chemin relatif de l'URL
        if (fullUrl.contains("/uploads/")) {
            int index = fullUrl.indexOf("/uploads/");
            return fullUrl.substring(index);
        }

        return fullUrl;
    }
}