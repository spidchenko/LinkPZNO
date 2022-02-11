package linkpzno.data;

public class Auditory {
    int id;
    int testPointId;
    int audNum;     //Номер аудитории
    int predmetId;  //ид предмета или 99 для мультипредметных
    int langId;     //ид перевода или ...!!!!!!!!!!!!!!111111111
    int free;       //Количество свободных мест
    String code;

    public Auditory(int id, int testPointId, int audNum, int predmetId, int langId, int free, String code) {
        this.id = id;
        this.testPointId = testPointId;
        this.audNum = audNum;
        this.predmetId = predmetId;
        this.langId = langId;
        this.free = free;
        this.code = code;
    }

    public int getFree() {
        return free;
    }

}
