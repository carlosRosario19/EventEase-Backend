package com.centennial.eventease_backend.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "EVENTS")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EVENT_ID")
    private int id;
    @Column(name = "TITLE")
    private String title;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "CATEGORY")
    private String category;
    @Column(name = "DATE_TIME")
    private LocalDateTime dateTime;
    @Column(name = "LOCATION")
    private String location;
    @Column(name = "TOTAL_TICKETS")
    private int totalTickets;
    @Column(name = "TICKETS_SOLD")
    private int ticketsSold;
    @Column(name = "PRICE_PER_TICKET")
    private float pricePerTicket;
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "MEMBER_ID")
    private Member member;
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public Event(){}

    public Event(String title, String description, String category, LocalDateTime dateTime, String location, int totalTickets, int ticketsSold, float pricePerTicket, Member member, LocalDateTime createdAt) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.dateTime = dateTime;
        this.location = location;
        this.totalTickets = totalTickets;
        this.ticketsSold = ticketsSold;
        this.pricePerTicket = pricePerTicket;
        this.member = member;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public int getTicketsSold() {
        return ticketsSold;
    }

    public void setTicketsSold(int ticketsSold) {
        this.ticketsSold = ticketsSold;
    }

    public float getPricePerTicket() {
        return pricePerTicket;
    }

    public void setPricePerTicket(float pricePerTicket) {
        this.pricePerTicket = pricePerTicket;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", dateTime=" + dateTime +
                ", location='" + location + '\'' +
                ", totalTickets=" + totalTickets +
                ", ticketsSold=" + ticketsSold +
                ", pricePerTicket=" + pricePerTicket +
                ", member=" + member +
                ", createdAt=" + createdAt +
                '}';
    }
}


