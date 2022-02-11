package linkpzno.data;

public class SubjectLang {
    int subjectId;      //ид предмета
    int langId;         //ид перевода
    String subjectName; //Название предмета
    String langName;    //Название перевода
    int numPupils;      //Количество учеников, выбравших этот предмет-перевод
    int eduCityId;      //ид города сдачи

    SubjectLang(int newSubjectId, int newLangId, String newSubjectName, String newLangName, int newNumPupils, int newEduCityId) {
        this.subjectId = newSubjectId;
        this.langId = newLangId;
        this.subjectName = newSubjectName;
        this.langName = newLangName.replace("Українська", "Без перекладу");
        this.numPupils = newNumPupils;
        this.eduCityId = newEduCityId;
    }

    @Override
    public String toString() {
        return subjectName + " (" + langName + ") - " + numPupils;
    }

}
