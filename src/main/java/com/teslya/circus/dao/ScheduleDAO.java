package com.teslya.circus.dao;

import com.teslya.circus.model.Schedule;
import com.teslya.circus.model.Performer;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ScheduleDAO {
    public void save(Schedule schedule) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.save(schedule);
            session.getTransaction().commit();
        }
    }

    public List<Schedule> findByPerformer(Performer performer) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Schedule> query = session.createQuery("FROM Schedule WHERE performer = :performer", Schedule.class);
            query.setParameter("performer", performer);
            return query.list();
        }
    }

    public List<Schedule> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Schedule", Schedule.class).list();
        }
    }

    public void update(Schedule schedule) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.update(schedule);
            session.getTransaction().commit();
        }
    }

    public void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Schedule schedule = session.get(Schedule.class, id);
            if (schedule != null) {
                session.delete(schedule);
            }
            session.getTransaction().commit();
        }
    }
}