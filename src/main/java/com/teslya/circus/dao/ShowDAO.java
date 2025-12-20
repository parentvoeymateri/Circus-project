package com.teslya.circus.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;
import com.teslya.circus.model.Show;

import java.util.List;

public class ShowDAO {

    public List<Show> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Show", Show.class).list();
        }
    }

    public List<Show> search(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Show> query = session.createQuery(
                    "FROM Show WHERE lower(name) LIKE lower(:keyword)", Show.class);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.list();
        }
    }

    public List<Show> sortByDate() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Show ORDER BY date ASC", Show.class).list();
        }
    }

    public List<Show> sortByDurationDesc() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Show ORDER BY duration DESC", Show.class).list();
        }
    }

    public double averageDuration() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery("SELECT AVG(duration) FROM Show", Double.class);
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
        }
    }
    public boolean hasOverlap(Show show) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(s) FROM Show s WHERE s.date = :date AND s.id <> :id",
                    Long.class
            );
            query.setParameter("date", show.getDate());
            query.setParameter("id", show.getId() == null ? 0L : show.getId()); // 0 для нового
            Long count = query.uniqueResult();
            return count > 0; // Если есть хоть одно шоу в эту дату — считаем пересечением
        }
    }
    public void save(Show show) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.save(show);
            session.getTransaction().commit();
        }
    }

    public void update(Show show) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.update(show);
            session.getTransaction().commit();
        }
    }

    public void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Show show = session.get(Show.class, id);
            if (show != null) {
                session.delete(show);
            }
            session.getTransaction().commit();
        }
    }
}