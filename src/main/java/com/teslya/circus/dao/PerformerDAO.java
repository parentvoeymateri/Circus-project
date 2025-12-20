package com.teslya.circus.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;
import com.teslya.circus.model.Performer;

import java.util.List;

public class PerformerDAO {

    public List<Performer> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Performer", Performer.class).list();
        }
    }

    public List<Performer> search(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Performer> query = session.createQuery(
                    "FROM Performer WHERE lower(name) LIKE lower(:keyword) OR lower(specialty) LIKE lower(:keyword)", Performer.class);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.list();
        }
    }

    public List<Performer> sortByName() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Performer ORDER BY name ASC", Performer.class).list();
        }
    }

    public List<Performer> sortByExperienceDesc() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Performer ORDER BY experience DESC", Performer.class).list();
        }
    }

    public void save(Performer performer) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.save(performer);
            session.getTransaction().commit();
        }
    }

    public void update(Performer performer) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.update(performer);
            session.getTransaction().commit();
        }
    }

    public void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Performer performer = session.get(Performer.class, id);
            if (performer != null) {
                session.delete(performer);
            }
            session.getTransaction().commit();
        }
    }
}