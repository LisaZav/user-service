package ru.lisa;

import org.hibernate.SessionFactory;
import ru.lisa.config.HibernateConfig;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        SessionFactory sessionFactory = HibernateConfig.getSessionFactory();
        System.out.println(sessionFactory.getCurrentSession().toString());
        sessionFactory.close();

    }
}
