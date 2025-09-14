package com.csse3200.game.components.shop;

import java.util.ArrayList;


public class CatalogService {
    private final ArrayList<CatalogEntry> entries;

    public CatalogService(ArrayList<CatalogEntry> entries) {
        this.entries = entries;
    }

    public CatalogEntry get(CatalogEntry item) {
        for (CatalogEntry entry : entries) {
            if (entry.equals(item)) {
                return entry;
            }
        }
        return null;
    }

    public ArrayList<CatalogEntry> list() {
        return entries;
    }
}