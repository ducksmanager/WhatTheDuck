package net.ducksmanager.persistence.models.composite;

import com.google.gson.annotations.Expose;

import java.util.List;

public class CountryListToUpdate {
    @Expose
    private final List<String> countries;

    public CountryListToUpdate(List<String> countries) {
        this.countries = countries;
    }
}
