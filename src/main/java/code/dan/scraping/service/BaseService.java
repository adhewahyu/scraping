package code.dan.scraping.service;

public interface BaseService <I, O>{

    O execute(I input);

}
