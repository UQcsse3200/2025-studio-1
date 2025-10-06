package com.csse3200.game.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscoveryServiceTest {
    private DiscoveryService discovery;

    @BeforeEach
    void setUp() {
        discovery = new DiscoveryService();
    }

    @Test
    void normalize_handlesNullAndSpacesAndCase() {
        assertEquals("", DiscoveryService.normalize(null));
        assertEquals("mainhall", DiscoveryService.normalize("Main Hall"));
        assertEquals("server", DiscoveryService.normalize("SERVER"));
        assertEquals("research", DiscoveryService.normalize(" research "));
    }

    @Test
    void discover_marksAreasAnd_isDiscovered_checks() {
        assertFalse(discovery.isDiscovered("Reception"));
        discovery.discover("Reception");
        assertTrue(discovery.isDiscovered("Reception"));
        // Case-insensitive + spaces
        assertTrue(discovery.isDiscovered(" reception "));
        assertTrue(discovery.isDiscovered("RECEPTION"));
    }

    @Test
    void clear_resetsDiscoveredSet() {
        discovery.discover("Office");
        assertTrue(discovery.isDiscovered("Office"));
        discovery.clear();
        assertFalse(discovery.isDiscovered("Office"));
    }
}

