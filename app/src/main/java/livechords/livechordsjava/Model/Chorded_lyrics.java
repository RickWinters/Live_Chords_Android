package livechords.livechordsjava.Model;

public class Chorded_lyrics {
    private String lyrics = "no_file_found";
    private double stop;
    private double start;
    private String group = "no_file_found";
    private String chords = "no_file_found";
    private double end;

    public Chorded_lyrics(){};

    public Chorded_lyrics(String lyrics, double stop, double start, String group, String chords, double end){
        this.lyrics = lyrics;
        this.stop = stop;
        this.start = start;
        this.group = group;
        this.chords = chords;
        this.end = end;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public double getStop() {
        return stop;
    }

    public void setStop(double stop) {
        this.stop = stop;
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getChords() {
        return chords;
    }

    public void setChords(String chords) {
        this.chords = chords;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }
}
