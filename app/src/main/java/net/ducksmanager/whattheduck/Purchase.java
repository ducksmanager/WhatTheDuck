package net.ducksmanager.whattheduck;

public abstract class Purchase {
    Boolean noPurchase;
    Boolean newPurchase;

    Boolean isNoPurchase() {
        return noPurchase;
    }
    Boolean isNewPurchase() {
        return newPurchase;
    }
}

class SpecialPurchase extends Purchase {
    SpecialPurchase(Boolean noPurchase, Boolean newPurchase) {
        this.noPurchase=noPurchase;
        this.newPurchase=newPurchase;
    }
}

