package ir.sharif.aic.hideandseek;

import ir.sharif.aic.hideandseek.config.GameConfigInjector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HideAndSeekApplication {
  public static void main(String[] args) {
    GameConfigInjector.handleCMDArgs(args);
    SpringApplication.run(HideAndSeekApplication.class, args);
  }
}
