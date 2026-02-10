package se.kth.dd2480.group15.services;

import org.springframework.stereotype.Service;
import se.kth.dd2480.group15.api.dto.response.BuildListResponse;
import se.kth.dd2480.group15.api.dto.response.BuildLogResponse;
import se.kth.dd2480.group15.api.dto.response.BuildMetaResponse;
import se.kth.dd2480.group15.infrastructure.persistence.BuildRepository;

import java.util.UUID;

@Service
public class BuildService {

    private final BuildRepository buildRepository;

    public BuildService(BuildRepository buildRepository) {
        this.buildRepository = buildRepository;
    }

    public BuildListResponse getAllBuilds() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public BuildMetaResponse getBuild(UUID buildId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public BuildLogResponse getBuildLog(UUID buildId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
