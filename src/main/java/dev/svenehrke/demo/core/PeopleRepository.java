package dev.svenehrke.demo.core;

import dev.svenehrke.demo.inbound.web.PersonDetailModel;
import dev.svenehrke.demo.inbound.web.PersonEditModel;
import dev.svenehrke.demo.inbound.web.PersonTableModel;
import dev.svenehrke.demo.inbound.web.PersonTableRowModel;

import java.util.List;

public interface PeopleRepository {
    PersonTableModel people();
    PersonTableModel peopleForSearch(String search);
    int total();
    PersonTableRowModel personTableRowModel(int id);
    PersonEditModel personEditModel(int id);
    PersonDetailModel personDetailModel(int id);
    int deleteByIds(List<Integer> ids);
    int updatePerson(int id, PersonEditModel personEditModel);
}
