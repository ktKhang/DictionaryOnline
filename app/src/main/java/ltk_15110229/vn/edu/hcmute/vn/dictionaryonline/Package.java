package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import java.io.Serializable;

public class Package implements Serializable {
    private int id;
    private String name;
    private String created_date;
    private int number_words;

    public Package(String name, String created_date, int number_words) {
        this.name = name;
        this.created_date = created_date;
        this.number_words = number_words;
    }

    public Package(int id, String name, String created_date, int number_words) {
        this.id = id;
        this.name = name;
        this.created_date = created_date;
        this.number_words = number_words;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public int getNumber_words() {
        return number_words;
    }

    public void setNumber_words(int number_words) {
        this.number_words = number_words;
    }
}
