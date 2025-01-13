package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class ProjectNameRestController {

    private final String projectName;

    public ProjectNameRestController(@Value("${configuration.projectName}") String projectName) {
        this.projectName = projectName;
    }

    @GetMapping("/project-name")
    String getProjectName() {
        return projectName;
    }
}
