package ir.sharif.aic.hideandseek.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperInjector {
  @Bean
  public ObjectMapper createObjectMapper() {
    return new ObjectMapper();
  }
}
