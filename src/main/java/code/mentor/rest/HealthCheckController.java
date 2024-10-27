package code.mentor.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/api/health-check")
public class HealthCheckController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping
    public void healthCheck() {
        logger.info("Health check");
    }
}
