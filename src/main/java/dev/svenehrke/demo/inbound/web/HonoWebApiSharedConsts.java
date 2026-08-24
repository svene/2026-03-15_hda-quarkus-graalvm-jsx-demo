package dev.svenehrke.demo.inbound.web;

public interface HonoWebApiSharedConsts {

    interface HonoWebApiConsts {
        String PAGE = "/page";
        String PAGE_MENU_ID = "oob";
        String PERSON_TABLE = "/persontable";
        String PERSON_DETAILS = "/person/{id}/details";
        String PERSON_DETAILS_ROW = "/person/{id}/detailsrow";
        String PERSON_EDIT = "/person/{id}/edit";
        String PERSON = "/person/{id}";
        String PERSON_ROW = "/person/{id}/row";
        String PERSON_DETAILS_CARD = "/person/{id}/detailscard";
        String DELETE = "/delete";
    }

    interface EvtBackendEvents {
        String PERSON_UPDATED = "person-updated";
    }
}
