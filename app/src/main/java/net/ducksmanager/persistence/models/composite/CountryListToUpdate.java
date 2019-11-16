package net.ducksmanager.persistence.models.composite;

import com.google.gson.annotations.Expose;

import java.util.Set;

public class CountryListToUpdate {
    @Expose
    private final Set<String> countries;

    public CountryListToUpdate(Set<String> countries) {
        this.countries = countries;
    }
}
