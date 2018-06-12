package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import java.io.Serializable;

public class Word implements Serializable {
    private int id;
    private int inpack;
    private String word;
    private String detail1;
    private String detail2;

    public Word(int id, String word, String detail1, String detail2) {
        this.id = id;
        this.word = word;
        this.detail1 = detail1;
        this.detail2 = detail2;
    }

    public Word(int id, String word, String detail1, String detail2, int inpack) {
        this.id = id;
        this.word = word;
        this.detail1 = detail1;
        this.detail2 = detail2;
        this.inpack = inpack;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDetail1() {
        return detail1;
    }

    public void setDetail1(String detail1) {
        this.detail1 = detail1;
    }

    public String getDetail2() {
        return detail2;
    }

    public void setDetail2(String detail2) {
        this.detail2 = detail2;
    }

    public int getInpack() {
        return inpack;
    }

    public void setInpack(int inpack) {
        this.inpack = inpack;
    }
}
