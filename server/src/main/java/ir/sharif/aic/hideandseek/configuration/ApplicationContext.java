package ir.sharif.aic.hideandseek.configuration;

import ir.sharif.aic.hideandseek.database.InMemoryDataBase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationContext {

    @Bean
    public InMemoryDataBase dataBase(){
        return new InMemoryDataBase();
    }
}
