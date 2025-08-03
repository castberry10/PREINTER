package com.interviewee.preinter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.io.IOException;

@Component
public class SecretFetcher {

    private final SecretsManagerClient smClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public SecretFetcher(@Value("${aws.region}") String awsRegion) {
        this.smClient = SecretsManagerClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    /** 지정된 시크릿 이름과 키(key)를 가져와 반환 */
    public String getSecretValue(String secretName, String key) throws IOException {
        String secretJson = smClient.getSecretValue(
                GetSecretValueRequest.builder()
                        .secretId(secretName)
                        .build()
        ).secretString();
        JsonNode node = mapper.readTree(secretJson);
        return node.get(key).asText();
    }
}
