package com.teslya.circus.model;

import javax.persistence.*;

@Entity
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "performer_id", nullable = false)
    private Performer performer;

    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(nullable = false)
    private String roleInShow;

    public Schedule() {}

    public Schedule(Performer performer, Show show, String roleInShow) {
        this.performer = performer;
        this.show = show;
        this.roleInShow = roleInShow;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Performer getPerformer() {
        return performer;
    }

    public void setPerformer(Performer performer) {
        this.performer = performer;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public String getRoleInShow() {
        return roleInShow;
    }

    public void setRoleInShow(String roleInShow) {
        this.roleInShow = roleInShow;
    }
}