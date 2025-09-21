package ru.lisa.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import ru.lisa.entity.User;

import java.util.HashMap;
import java.util.Objects;

public class HibernateConfig {

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
                .addAnnotatedClass(User.class)
                .getMetadataBuilder()
                .build()
                .getSessionFactoryBuilder()
                .build();
    }
}
