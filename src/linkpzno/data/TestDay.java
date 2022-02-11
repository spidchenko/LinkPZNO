package linkpzno.data;

public class TestDay {
    int id;
    String name;
    String date;

    public TestDay(int id, String name, String date) {
        this.id = id;
        this.name = name;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
