package com.eventbooking.service;

import com.eventbooking.entity.Event;
import com.eventbooking.entity.enums.EventCategory;

import java.util.List;
import java.util.Optional;

public interface EventService {

    // Méthodes de base
    Event createEvent(Event event);
    Event createEventWithImage(Event event, String base64Image);

    Event updateEvent(Long id, Event event);
    Event updateEventWithImage(Long id, Event event, String base64Image);

    void deleteEvent(Long id);
    void deleteEventWithImage(Long id);

    Optional<Event> getEventById(Long id);
    List<Event> getAllEvents();

    // Méthodes pour les organisateurs
    List<Event> getEventsByOrganisateur(Long organisateurId);
    List<Event> getEventsByOrganizer(Long organizerId); // Alias pour getEventsByOrganisateur
    List<Event> getUpcomingEventsByOrganisateur(Long organisateurId);

    // Méthodes de filtrage
    List<Event> getPublishedEvents();
    List<Event> getUpcomingEvents();
    List<Event> getEventsByCategory(EventCategory category);
    List<Event> getEventsByCategory(String category); // Méthode avec String
    List<Event> getEventsByVille(String ville);
    List<Event> searchEvents(String keyword);
    List<Event> searchEventsByTitre(String titre);

    // Méthodes de gestion d'état
    Event publishEvent(Long id);
    Event cancelEvent(Long id);

    // Statistiques
    int getAvailablePlaces(Long eventId);
    long getTotalEventsCount();
    long getPublishedEventsCount();
    long getOrganizerEventsCount(Long organizerId);
    long getOrganizerPublishedEventsCount(Long organizerId);
    long getUpcomingEventsCount();
}