package com.enuri.brndmkr.modelmatch.config.datasource;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.enuri.brndmkr.modelmatch.repository.epmondb",
						entityManagerFactoryRef = "epMonDbEntityManagerFactory",
						transactionManagerRef = "epMonDbTransactionManager")
public class EpMonDbDataSourceConfig {

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.epmondb.jpa.property")
	public Properties epMonDbHibernateProperties() {
		return new Properties();
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.epmondb.datasource.hikari")
	public HikariConfig epMonDbHikariConfig() {
		return new HikariConfig();
	}

    @Bean
    public DataSource epMonDbDataSource() {
        return new HikariDataSource(epMonDbHikariConfig());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean epMonDbEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(epMonDbDataSource());
        em.setPackagesToScan("com.enuri.brndmkr.modelmatch.model.entity.epmondb");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaProperties(epMonDbHibernateProperties());
        em.setPersistenceUnitName("epmondb");
        return em;
    }

	@Bean
	public JpaTransactionManager epMonDbTransactionManager() {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(epMonDbEntityManagerFactory().getObject());
		return transactionManager;
	}
}