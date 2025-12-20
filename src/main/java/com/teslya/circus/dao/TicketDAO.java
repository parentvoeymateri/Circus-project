package com.teslya.circus.dao;

import com.teslya.circus.model.Ticket;
import com.teslya.circus.model.User;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class TicketDAO {
    public void save(Ticket ticket) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.save(ticket);
            session.getTransaction().commit();
        }
    }

    public List<Ticket> findByUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Ticket> query = session.createQuery("FROM Ticket WHERE user = :user", Ticket.class);
            query.setParameter("user", user);
            return query.list();
        }
    }

    public List<Ticket> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Ticket", Ticket.class).list();
        }
    }

    public void update(Ticket ticket) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.update(ticket);
            session.getTransaction().commit();
        }
    }

    public void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Ticket ticket = session.get(Ticket.class, id);
            if (ticket != null) {
                session.delete(ticket);
            }
            session.getTransaction().commit();
        }
    }
}