package ru.lisa.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            logger.debug("Инициализация SessionFactory из hibernate.cfg.xml...");

            // Автоматически загружает hibernate.cfg.xml из classpath
            SessionFactory factory = new Configuration().configure().buildSessionFactory();

            logger.info("✅ SessionFactory успешно создана из конфигурационного файла");
            return factory;
        } catch (Throwable ex) {
            logger.error("❌ Ошибка при создании SessionFactory", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        logger.info("Закрытие SessionFactory...");
        if (sessionFactory != null) {
            sessionFactory.close();
            logger.info("✅ SessionFactory закрыта");
        }
    }
}