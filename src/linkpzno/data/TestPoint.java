package linkpzno.data;

public class TestPoint {
    int id;
    int ptId;
    String name;
    String shortName;
    int audsNum;

    public TestPoint(int id, int ptId, String name, String shortName, int audsNum) {
        this.id = id;
        this.ptId = ptId;
        this.name = name;
        this.shortName = shortName;
        this.audsNum = audsNum;
    }

    public int getId() {
        return id;
    }

    public int getAudsNum() {
        return audsNum;
    }

    @Override
    public String toString() {
        return id + " - " + shortName + "(" + audsNum + " аудиторий)";
    }
}
