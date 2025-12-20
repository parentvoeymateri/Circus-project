package com.teslya.circus.dao;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import com.teslya.circus.model.*;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure() // hibernate.cfg.xml
                    .build();
            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Performer.class)
                    .addAnnotatedClass(Show.class)
                    .addAnnotatedClass(Ticket.class)
                    .addAnnotatedClass(Schedule.class)
                    .buildMetadata()
                    .buildSessionFactory();
        }
        return sessionFactory;
    }
}