package dev.svenehrke.demo.inbound.web;

public interface HonoWebApiSharedConsts {

    interface HonoWebApiConsts {
        String PERSON = "/person/{id}";
        String DELETE = "/delete";
    }

    interface EvtBackendEvents {
        String PERSON_UPDATED = "person-updated";
    }
}
