package ru.lisa.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import ru.lisa.entity.User;
import ru.lisa.util.HibernateUtil;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private final SessionFactory sessionFactory;

    public UserDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Long save(User user) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            return user.getId();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка при сохранении пользователя", e);
        } finally {
            session.close();
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.find(User.class, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске пользователя по ID: " + id, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            User user = query.uniqueResult();
            return Optional.ofNullable(user);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске пользователя по email: " + email, e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении списка пользователей", e);
        }
    }

    @Override
    public void update(User user) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка при обновлении пользователя с ID: " + user.getId(), e);
        } finally {
            session.close();
        }
    }

    @Override
    public boolean delete(Long id) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            User user = session.find(User.class, id);

            if (user != null) {
                session.remove(user);
                transaction.commit();
                return true;
            } else {
                transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка при удалении пользователя с ID: " + id, e);
        } finally {
            session.close();
        }
    }
}