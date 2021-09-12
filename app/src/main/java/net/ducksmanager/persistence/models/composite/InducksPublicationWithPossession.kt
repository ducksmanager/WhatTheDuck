package net.ducksmanager.persistence.models.composite

import androidx.room.Embedded
import net.ducksmanager.persistence.models.coa.InducksPublication

class InducksPublicationWithPossession : OwnershipAndReferenceCount() {
    @Embedded
    lateinit var publication: InducksPublication
}