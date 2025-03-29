package cn.czh.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url:jdbc:sqlite:./upload-file.db}")
    private String defaultUrl;

    @Value("${spring.datasource.driver-class-name:org.sqlite.JDBC}")
    private String defaultDriverClassName;

    @Value("${mysql.datasource.url:}")
    private String mysqlUrl;

    @Value("${mysql.datasource.username:}")
    private String mysqlUsername;

    @Value("${mysql.datasource.password:}")
    private String mysqlPassword;

    @Value("${mysql.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String mysqlDriverClassName;

    @Bean
    public DataSource dataSource() {
        boolean useMySQL = !mysqlUrl.isEmpty() && !mysqlUsername.isEmpty() && !mysqlPassword.isEmpty();

        if (useMySQL) {
            log.info("Using MySQL database.");
            return DataSourceBuilder.create()
                    .url(mysqlUrl)
                    .username(mysqlUsername)
                    .password(mysqlPassword)
                    .driverClassName(mysqlDriverClassName)
                    .build();
        } else {
            log.info("Using SQLite database.");
            // 默认使用 SQLite，无需 username 和 password
            return DataSourceBuilder.create()
                    .url(defaultUrl)
                    .driverClassName(defaultDriverClassName)
                    .build();
        }
    }

    @Bean
    @DependsOn("dataSource")
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();

        boolean useMySQL = !mysqlUrl.isEmpty() && !mysqlUsername.isEmpty() && !mysqlPassword.isEmpty();

        if (useMySQL) {
            initializer.setEnabled(false);
        } else {
            populator.addScript(new ClassPathResource("schema.sql"));
            populator.addScript(new ClassPathResource("data.sql"));
            initializer.setEnabled(true);
        }

        initializer.setDatabasePopulator(populator);
        initializer.afterPropertiesSet();
        return initializer;
    }
}