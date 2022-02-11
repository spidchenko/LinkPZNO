package linkpzno.data;

public class TestPoint {
    int id;
    int ptId;
    String name;
    String shortName;
    int audsNum;

    @Override
    public String toString() {
        return id + " - " + shortName + "(" + audsNum + " аудиторий)";
    }
}
