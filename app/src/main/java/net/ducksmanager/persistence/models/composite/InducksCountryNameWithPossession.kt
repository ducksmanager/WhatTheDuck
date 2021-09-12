package net.ducksmanager.persistence.models.composite

import androidx.room.Embedded
import net.ducksmanager.persistence.models.coa.InducksCountryName

class InducksCountryNameWithPossession : OwnershipAndReferenceCount() {
    @Embedded
    lateinit var country: InducksCountryName
}