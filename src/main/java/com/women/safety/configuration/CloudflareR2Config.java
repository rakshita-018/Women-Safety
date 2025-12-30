package com.women.safety.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Cloudflare R2 Storage Configuration
 *
 * Configures AWS S3 SDK to work with Cloudflare R2
 * R2 is S3-compatible, so we use the AWS SDK
 */
@Configuration
public class CloudflareR2Config {

    @Value("${cloudflare.r2.account-id}")
    private String accountId;

    @Value("${cloudflare.r2.access-key}")
    private String accessKey;

    @Value("${cloudflare.r2.secret-key}")
    private String secretKey;

    @Value("${cloudflare.r2.endpoint}")
    private String endpoint;

    @Value("${storage.mode:LOCAL}")
    private String storageMode;

    @Bean
    public S3Client s3Client() {
        // Only create S3 client if R2 mode is enabled
        if (!"R2".equalsIgnoreCase(storageMode)) {
            return null; // Return null for local storage mode
        }

        // Validate R2 configuration
        if (accessKey == null || accessKey.isEmpty() ||
                secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException(
                    "Cloudflare R2 credentials not configured. " +
                            "Set CLOUDFLARE_R2_ACCESS_KEY and CLOUDFLARE_R2_SECRET_KEY environment variables."
            );
        }

        // Build endpoint URL
        String r2Endpoint = endpoint;
        if (r2Endpoint == null || r2Endpoint.isEmpty()) {
            r2Endpoint = String.format("https://%s.r2.cloudflarestorage.com", accountId);
        }

        // Create AWS credentials
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        // Build S3 client configured for R2
        return S3Client.builder()
                .region(Region.US_EAST_1) // R2 doesn't use regions, but SDK requires it
                .endpointOverride(URI.create(r2Endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(false) // Use virtual-hosted-style requests
                        .build())
                .build();
    }
}
