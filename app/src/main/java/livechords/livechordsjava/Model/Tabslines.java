package livechords.livechordsjava.Model;

public class Tabslines {
    private boolean lyrics;
    private String text = "none";
    private String group = "none";
    private boolean keyword;
    private boolean chords;

    public Tabslines(){}

    public Tabslines(boolean lyrics, String text, String group, boolean keyword, boolean chords){
        this.lyrics = lyrics;
        this.text = text;
        this.group = group;
        this.keyword = keyword;
        this.chords = chords;
    }

    public boolean isLyrics() {
        return lyrics;
    }

    public void setLyrics(boolean lyrics) {
        this.lyrics = lyrics;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isKeyword() {
        return keyword;
    }

    public void setKeyword(boolean keyword) {
        this.keyword = keyword;
    }

    public boolean isChords() {
        return chords;
    }

    public void setChords(boolean chords) {
        this.chords = chords;
    }
}
