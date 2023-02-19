package ru.artdy.service.enums;

public enum LinkType {
    GET_DOC("file/get_doc"),
    GET_PHOTO("file/get_photo");

    private final String link;

    LinkType(String link) {
        this.link = link;
    }


    @Override
    public String toString() {
        return link;
    }
}