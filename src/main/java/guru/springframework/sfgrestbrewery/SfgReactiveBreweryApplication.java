package guru.springframework.sfgrestbrewery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@SpringBootApplication
@EnableR2dbcAuditing
public class SfgReactiveBreweryApplication {
  public static void main(final String[] args) {
    SpringApplication.run(SfgReactiveBreweryApplication.class, args);
  }
}
