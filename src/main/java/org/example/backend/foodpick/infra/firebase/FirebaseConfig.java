package org.example.backend.foodpick.infra.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) return;

        String envPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

        String[] candidates = new String[]{
                envPath,
                "/config/serviceAccountKey.json",
                "src/main/resources/serviceAccountKey.json"
        };

        String selected = null;
        for (String p : candidates) {
            if (p == null || p.isBlank()) continue;
            if (java.nio.file.Files.exists(java.nio.file.Path.of(p))) {
                selected = p;
                break;
            }
        }

        if (selected == null) {
            System.err.println("Firebase init failed: credential file not found.");
            return;
        }

        try (java.io.InputStream serviceAccount = new java.io.FileInputStream(selected)) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            System.out.println("Firebase initialized. credentialPath=" + selected);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
