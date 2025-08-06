package com.example.tpcm_voice_simulator.config;

import com.example.tpcm_voice_simulator.util.DataSourceUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.tpcm_voice_simulator.repository",
        entityManagerFactoryRef = "voiceEntityManagerFactory",
        transactionManagerRef = "voiceTransactionManager"
)
public class VoiceDataSourceConfig {

    @Primary
    @Bean
    @ConfigurationProperties("voice.datasource")
    public DataSourceProperties voiceDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "voiceDs")
    public DataSource voiceDataSource() {
        DataSourceProperties props = voiceDataSourceProperties();
        return DataSourceUtil.createHikariDataSource(
                props,
                "HikariPool-voice",
                null
        );
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean voiceEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(voiceDataSource())
                .packages("com.example.tpcm_voice_simulator.model")
                .persistenceUnit("voice")
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager voiceTransactionManager(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(
                Objects.requireNonNull(voiceEntityManagerFactory(builder).getObject())
        );
    }

    @Primary
    @Bean(name = "voiceJdbc")
    public JdbcTemplate voiceJdbcTemplate(@Qualifier("voiceDs") DataSource voiceDs) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(voiceDs);
        jdbcTemplate.setResultsMapCaseInsensitive(true);
        return jdbcTemplate;
    }
}