package code.mentor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// @EnableScheduling
// Thay @EnableScheduling bằng Quartz
public class DailyNewApplication {
    public static void main(String[] args) {
        SpringApplication.run(DailyNewApplication.class, args);
    }
}
