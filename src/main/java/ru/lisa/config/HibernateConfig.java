package ru.lisa.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lisa.entity.User;

import java.util.Properties;

public class HibernateConfig {
    // Инициализация логгера через SLF4J
    private static final Logger logger = LoggerFactory.getLogger(HibernateConfig.class);
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                logger.debug("Инициализация SessionFactory...");

                Configuration configuration = new Configuration();
                Properties settings = new Properties();

                // === НАСТРОЙКИ БАЗЫ ДАННЫХ ===
                settings.put(Environment.DRIVER, "org.postgresql.Driver");
                settings.put(Environment.URL, "jdbc:postgresql://localhost:5432/user_service");
                settings.put(Environment.USER, "postgres");
                settings.put(Environment.PASS, "password");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");

                // === НАСТРОЙКИ HIBERNATE ===
                settings.put(Environment.SHOW_SQL, "false"); // Отключаем, т.к. используем логирование
                settings.put(Environment.FORMAT_SQL, "true");
                settings.put(Environment.HBM2DDL_AUTO, "update");

                // === НАСТРОЙКИ ПУЛА СОЕДИНЕНИЙ ===
                settings.put(Environment.C3P0_MIN_SIZE, "5");
                settings.put(Environment.C3P0_MAX_SIZE, "20");
                settings.put(Environment.C3P0_TIMEOUT, "300");
                settings.put(Environment.C3P0_MAX_STATEMENTS, "50");

                // Включаем логирование SQL через SLF4J
                settings.put(Environment.LOG_SLOW_QUERY, "true");

                configuration.setProperties(settings);
                configuration.addAnnotatedClass(User.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);

                logger.info("✅ SessionFactory успешно создана");

            } catch (Exception e) {
                logger.error("❌ Ошибка при создании SessionFactory", e);
                throw new RuntimeException("Не удалось инициализировать Hibernate", e);
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            logger.info("SessionFactory закрыта");
        }
    }
}







/*

package ru.lisa.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

import java.util.HashMap;
import java.util.Objects;

public final class HibernateConfig {

    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (Objects.nonNull(sessionFactory)) {
            return sessionFactory;
        }
        return createSessionFactory();
    }

    private static SessionFactory createSessionFactory() {
        var settings = new HashMap<String, Object>();
        settings.put(Environment.DRIVER, "org.postgresql.Driver");
//        settings.put(Environment.URL, "jdbc:postgresql://postgres:5432/my_db");
        settings.put(Environment.URL, "jdbc:postgresql://localhost:5432/my_db");
        settings.put(Environment.USER, "developer");
        settings.put(Environment.PASS, "developer");
        settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");

        return new MetadataSources(
                new StandardServiceRegistryBuilder()
                        .applySettings(settings)
                        .build()
        )
            //    .addAnnotatedClass(User.class)
                .getMetadataBuilder()
                .build()
                .getSessionFactoryBuilder()
                .build();
    }
}





*/
