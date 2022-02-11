package linkpzno.data;

public class EduCity {
    int id;
    String name;
    int districtId;

    @Override
    public String toString() {
        return id + " - " + name + " (" + districtId + ")";
    }
}