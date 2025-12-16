package com.eventbooking.service.impl;

import com.eventbooking.entity.Event;
import com.eventbooking.entity.Reservation;
import com.eventbooking.entity.enums.EventCategory;
import com.eventbooking.entity.enums.EventStatus;
import com.eventbooking.repository.EventRepository;
import com.eventbooking.repository.ReservationRepository;
import com.eventbooking.service.EventService;
import com.eventbooking.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // ==================== IMPLÉMENTATION DES MÉTHODES ====================

    @Override
    public Event createEvent(Event event) {
        event.setStatut(EventStatus.BROUILLON);
        event.setDateCreation(LocalDateTime.now());
        event.setDateModification(LocalDateTime.now());
        return eventRepository.save(event);
    }

    @Override
    public Event createEventWithImage(Event event, String base64Image) {
        try {
            if (base64Image != null && !base64Image.isEmpty()) {
                String imageUrl = fileStorageService.storeBase64Image(base64Image, "event_" + event.getTitre());
                event.setImageUrl(imageUrl);
            }

            event.setStatut(EventStatus.BROUILLON);
            event.setDateCreation(LocalDateTime.now());
            event.setDateModification(LocalDateTime.now());
            return eventRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création de l'événement avec image", e);
        }
    }

    @Override
    public Event updateEvent(Long id, Event event) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        existingEvent.setTitre(event.getTitre());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setCategorie(event.getCategorie());
        existingEvent.setDateDebut(event.getDateDebut());
        existingEvent.setDateFin(event.getDateFin());
        existingEvent.setLieu(event.getLieu());
        existingEvent.setVille(event.getVille());
        existingEvent.setCapaciteMax(event.getCapaciteMax());
        existingEvent.setPrixUnitaire(event.getPrixUnitaire());
        existingEvent.setImageUrl(event.getImageUrl());
        existingEvent.setOrganisateur(event.getOrganisateur());
        existingEvent.setStatut(event.getStatut());
        existingEvent.setDateModification(LocalDateTime.now());

        return eventRepository.save(existingEvent);
    }

    @Override
    public Event updateEventWithImage(Long id, Event event, String base64Image) {
        Optional<Event> existingEventOpt = eventRepository.findById(id);
        if (existingEventOpt.isEmpty()) {
            throw new RuntimeException("Événement non trouvé");
        }

        Event existingEvent = existingEventOpt.get();

        try {
            if (base64Image != null && !base64Image.isEmpty()) {
                String oldImageUrl = existingEvent.getImageUrl();
                if (oldImageUrl != null && oldImageUrl.contains("/uploads/")) {
                    fileStorageService.deleteFile(oldImageUrl);
                }

                String newImageUrl = fileStorageService.storeBase64Image(base64Image, "event_" + event.getTitre());
                existingEvent.setImageUrl(newImageUrl);
            }

            existingEvent.setTitre(event.getTitre());
            existingEvent.setDescription(event.getDescription());
            existingEvent.setCategorie(event.getCategorie());
            existingEvent.setDateDebut(event.getDateDebut());
            existingEvent.setDateFin(event.getDateFin());
            existingEvent.setLieu(event.getLieu());
            existingEvent.setVille(event.getVille());
            existingEvent.setCapaciteMax(event.getCapaciteMax());
            existingEvent.setPrixUnitaire(event.getPrixUnitaire());
            existingEvent.setOrganisateur(event.getOrganisateur());
            existingEvent.setStatut(event.getStatut());
            existingEvent.setDateModification(LocalDateTime.now());

            return eventRepository.save(existingEvent);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'événement avec image", e);
        }
    }

    @Override
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        List<Reservation> reservations = reservationRepository.findByEvenementId(id);
        if (!reservations.isEmpty()) {
            reservationRepository.deleteAll(reservations);
        }

        eventRepository.deleteById(id);
    }

    @Override
    public void deleteEventWithImage(Long id) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) {
            throw new RuntimeException("Événement non trouvé");
        }

        Event event = eventOpt.get();

        try {
            String imageUrl = event.getImageUrl();
            if (imageUrl != null && imageUrl.contains("/uploads/")) {
                fileStorageService.deleteFile(imageUrl);
            }

            List<Reservation> reservations = reservationRepository.findByEvenementId(id);
            if (!reservations.isEmpty()) {
                reservationRepository.deleteAll(reservations);
            }

            eventRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression de l'événement", e);
        }
    }

    @Override
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // ==================== MÉTHODES DE FILTRAGE ====================

    @Override
    public List<Event> getEventsByOrganisateur(Long organisateurId) {
        return eventRepository.findByOrganisateurId(organisateurId);
    }

    @Override
    public List<Event> getEventsByOrganizer(Long organizerId) {
        return getEventsByOrganisateur(organizerId);
    }

    @Override
    public List<Event> getUpcomingEventsByOrganisateur(Long organisateurId) {
        List<Event> events = eventRepository.findByOrganisateurId(organisateurId);
        return events.stream()
                .filter(e -> e.getDateDebut().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getPublishedEvents() {
        return eventRepository.findUpcomingPublishedEvents(LocalDateTime.now());
    }

    @Override
    public List<Event> getUpcomingEvents() {
        return eventRepository.findUpcomingPublishedEvents(LocalDateTime.now());
    }

    @Override
    public List<Event> getEventsByCategory(EventCategory category) {
        return eventRepository.findPublishedEventsByCategory(category, LocalDateTime.now());
    }

    @Override
    public List<Event> getEventsByCategory(String category) {
        try {
            EventCategory eventCategory = EventCategory.valueOf(category.toUpperCase());
            return getEventsByCategory(eventCategory);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @Override
    public List<Event> getEventsByVille(String ville) {
        return eventRepository.findByVille(ville);
    }

    @Override
    public List<Event> searchEvents(String keyword) {
        return eventRepository.findByTitreContainingIgnoreCase(keyword);
    }

    @Override
    public List<Event> searchEventsByTitre(String titre) {
        return searchEvents(titre);
    }

    // ==================== MÉTHODES DE GESTION D'ÉTAT ====================

    @Override
    public Event publishEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
        event.setStatut(EventStatus.PUBLIE);
        event.setDateModification(LocalDateTime.now());
        return eventRepository.save(event);
    }

    @Override
    public Event cancelEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
        event.setStatut(EventStatus.ANNULE);
        event.setDateModification(LocalDateTime.now());
        return eventRepository.save(event);
    }

    // ==================== STATISTIQUES ====================

    @Override
    public int getAvailablePlaces(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        Integer reservedPlaces = reservationRepository.countConfirmedPlacesByEvent(eventId);
        int reserved = (reservedPlaces != null) ? reservedPlaces : 0;

        return event.getCapaciteMax() - reserved;
    }

    @Override
    public long getTotalEventsCount() {
        return eventRepository.count();
    }

    @Override
    public long getPublishedEventsCount() {
        return eventRepository.countByStatut(EventStatus.PUBLIE);
    }

    @Override
    public long getOrganizerEventsCount(Long organizerId) {
        return eventRepository.findByOrganisateurId(organizerId).size();
    }

    @Override
    public long getOrganizerPublishedEventsCount(Long organizerId) {
        List<Event> events = eventRepository.findByOrganisateurId(organizerId);
        return events.stream()
                .filter(e -> e.getStatut() == EventStatus.PUBLIE)
                .count();
    }

    @Override
    public long getUpcomingEventsCount() {
        return eventRepository.findUpcomingPublishedEvents(LocalDateTime.now()).size();
    }
}