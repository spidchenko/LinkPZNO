package linkpzno.data;

public class EduCity {
    int id;
    String name;
    int districtId;

    public EduCity(int id, String name, int districtId) {
        this.id = id;
        this.name = name;
        this.districtId = districtId;
    }

    public int getId() {
        return id;
    }


    public int getDistrictId() {
        return districtId;
    }

    @Override
    public String toString() {
        return id + " - " + name + " (" + districtId + ")";
    }
}