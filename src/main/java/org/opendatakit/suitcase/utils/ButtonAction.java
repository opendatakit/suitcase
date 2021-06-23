package org.opendatakit.suitcase.utils;

public enum ButtonAction {
    REMOVE("REMOVE"),
    ADD("ADD");
    final String action;

    ButtonAction(String s) {
        this.action = s;
    }

    public String getStringValueOfAction() {
        return this.action;
    }
}
