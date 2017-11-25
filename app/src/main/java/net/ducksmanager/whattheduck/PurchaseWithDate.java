package net.ducksmanager.whattheduck;

import java.util.Date;

public class PurchaseWithDate extends Purchase {
    Integer id;
    private final Date purchaseDate;
    private final String purchaseName;

    public PurchaseWithDate(Integer id, Date purchaseDate, String purchaseName) {
        this.id = id;
        this.purchaseDate = purchaseDate;
        this.purchaseName = purchaseName;
        this.noPurchase = Boolean.FALSE;
        this.newPurchase = Boolean.FALSE;
    }

    public Integer getId() {
        return id;
    }

    Date getPurchaseDate() {
        return purchaseDate;
    }

    String getPurchaseName() {
        return purchaseName;
    }
}
