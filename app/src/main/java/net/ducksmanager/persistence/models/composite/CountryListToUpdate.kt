package net.ducksmanager.persistence.models.composite

import com.google.gson.annotations.Expose

class CountryListToUpdate(
    @Expose
    private val countries: Set<String>
)