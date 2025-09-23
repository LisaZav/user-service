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
    private static final Logger logger = LoggerFactory.getLogger(HibernateConfig.class);
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                logger.debug("Инициализация SessionFactory...");


                Properties settings = new Properties();

                settings.put(Environment.DRIVER, "org.postgresql.Driver");
                settings.put(Environment.URL, "jdbc:postgresql://localhost:5432/my_db");
                settings.put(Environment.USER, "developer");
                settings.put(Environment.PASS, "developer");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
                settings.put(Environment.HBM2DDL_AUTO, "update"); //создание таблицы из Entity

                Configuration configuration = new Configuration();
                configuration.setProperties(settings);
                configuration.addAnnotatedClass(User.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);

                logger.info("✅ SessionFactory успешно создана");

            } catch (Exception e) {
                logger.error("❌ Ошибка при создании SessionFactory", e);
                throw new RuntimeException(e);
            }
        }
        return sessionFactory;
    }
}