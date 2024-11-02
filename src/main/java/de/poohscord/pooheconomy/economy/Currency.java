package de.poohscord.pooheconomy.economy;

public enum Currency {

    HONIGTROPFEN("%honigtropfen%"),
    HONIGKRISTALLE("%honigkristalle%");

    private final String placeholder;

    Currency(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return this.placeholder;
    }

}
