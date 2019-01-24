/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linkpzno;

import java.util.Arrays;
import javax.swing.JComboBox;
import javax.swing.JProgressBar;

/**
 *
 * @author spidchenko.d
 */

class SubjectLang{
    int subjectId;      //ид предмета
    int langId;         //ид перевода
    String subjectName; //Название предмета
    String langName;    //Название перевода
    int numPupils;      //Количество учеников, выбравших этот предмет-перевод
    int eduCityId;      //ид города сдачи
    
    SubjectLang(int newSubjectId, int newLangId, String newSubjectName, String newLangName, int newNumPupils, int newEduCityId){
        this.subjectId = newSubjectId;
        this.langId = newLangId;        
        this.subjectName = newSubjectName;
        this.langName = newLangName.replace("Українська", "Без перекладу");
        this.numPupils = newNumPupils;
        this.eduCityId = newEduCityId;
    }
    
    @Override public String toString(){
        return subjectName+" ("+langName+") - "+numPupils;
    }
    
}

class TestDay{
    int id;
    String name;
    String date;
    
    @Override public String toString(){
        return id+" - "+name;
    }
}

class EduCity{
    int id;
    String name;
    int districtId;
    
    @Override public String toString(){
        return id+" - "+name+" ("+districtId+")";
    }    
}

class Auditory{
    int id;
    int testPointId;
    int audNum;     //Номер аудитории
    int predmetId;  //ид предмета или 99 для мультипредметных
    int langId;     //ид перевода или ...!!!!!!!!!!!!!!111111111
    int free;       //Количество свободных мест
    String code;
}

class TestPoint{
    int id;
    int ptId;
    String name;
    String shortName;
    int audsNum;
   
    @Override public String toString(){
        return id+" - "+shortName+ "("+audsNum+" аудиторий)";
    }    
}



public class Controller {
    private TestDay [] testDayList = new TestDay[2];
    private EduCity [] eduCityList = new EduCity[16];
    private TestPoint [] testPointList = new TestPoint[10];
    private Auditory [] auditoryList = new Auditory[26];    //26 - Макс. количество аудиторий в пункте
    SubjectLang [] subjectLangList = new SubjectLang[30];   //30 макс комбинаций предмет-перевод
    JProgressBar[] progressBarList = new JProgressBar[26];  //26 - Макс. количество аудиторий в пункте
    
    Controller(){
        DBConnection dBConnection = new DBConnection();
        dBConnection.init();
        dBConnection.setTestDays(testDayList);
        dBConnection.setEduCity(eduCityList);
        dBConnection.closeConnection();
    }
    
    
    
    void initAuditoryPanel(JComboBox audNumList, int comboBoxPTSelectedIndex, JComboBox subjectList, int comboBoxTestDaySelectedIndex, int comboBoxEduCitySelictedIndex){
        
        DBConnection dBConnection = new DBConnection();
        dBConnection.init();
        dBConnection.getSubjectLangList(subjectLangList,eduCityList[comboBoxEduCitySelictedIndex].id, testDayList[comboBoxTestDaySelectedIndex].id);//!!!!!!!!!
        
        audNumList.removeAllItems();
        
        for(int i = 1; i <= testPointList[comboBoxPTSelectedIndex].audsNum; i++)
            audNumList.addItem(i);
        
        subjectList.removeAllItems();
        
        int i = 0;
        while(subjectLangList[i] != null){
            subjectList.addItem(subjectLangList[i].toString());
            i++;
        }
        
        dBConnection.closeConnection();
//        
//        int i =0;
//        while(auditoryList[i] == null){
//            audNumList.addItem(auditoryList[i].audNum);
//        }
        
        
    }
    
    void showAuditoryListOnPanel(){
        
        for(int i = 0; i<progressBarList.length; i++){
            progressBarList[i].setEnabled(false);
            progressBarList[i].setValue(0);
        }
        
        int i=0;
        while (auditoryList[i] != null){
            
            progressBarList[i].setEnabled(true);
            progressBarList[i].setValue(15-auditoryList[i].free);
            i++;
        }
    }
    
    void setAuditorySubjectLangId(int comboBoxSelIndex){
        
        DBConnection dBConnection = new DBConnection();
        dBConnection.init();
        
        dBConnection.setAuditorySubjectLangId(
                testPointList[comboBoxSelIndex].id,
                testPointList[comboBoxSelIndex].audsNum);
        dBConnection.closeConnection();
    }
    
    void getAuditoryList(int comboBoxSelIndex){
        //System.out.println(testPointList[comboBoxSelIndex].toString());
        
        DBConnection dBConnection = new DBConnection();
        dBConnection.init();
        
        dBConnection.getAuditoryList(auditoryList,
                testPointList[comboBoxSelIndex].id,
                testPointList[comboBoxSelIndex].audsNum);
        dBConnection.closeConnection();
        System.out.println(Arrays.toString(auditoryList));//////////////////////
    }
    
    void setTestDay(JComboBox comboBoxToSet){
        for (TestDay testDay1 : testDayList){
            if(testDay1 != null) {
                comboBoxToSet.addItem(testDay1.toString());
                System.out.println(testDay1.toString());
            }
        }
    }
    
    void setEduCity(JComboBox comboBoxToSet){
        for (EduCity eduCity1 : eduCityList) {
            if ((eduCity1 != null)) {
                comboBoxToSet.addItem(eduCity1.toString());
                System.out.println(eduCity1.toString());
            }
        }
        
    }
    
    void link(int comboBoxPTSelectedIndex, int comboBoxAudNumSelectedIndex, String textFieldNumPupilsText, int comboBoxSubjectSelectedIndex){
        DBConnection dBConnection = new DBConnection();
        dBConnection.init();
        dBConnection.link(testPointList[comboBoxPTSelectedIndex].id, comboBoxAudNumSelectedIndex+1, Integer.parseInt(textFieldNumPupilsText), subjectLangList[comboBoxSubjectSelectedIndex]);
        dBConnection.closeConnection();
    }
    
    void setTestPoint(JComboBox comboBoxToSet, int testDayListNumber, int eduCityIdListNumber){
        
        DBConnection dBConnection = new DBConnection();
        dBConnection.init();
        dBConnection.setTestPoint(testPointList, testDayList[testDayListNumber].id, eduCityList[eduCityIdListNumber].districtId);
        
        System.out.println(testDayList[testDayListNumber].id+" "+eduCityList[eduCityIdListNumber].districtId);//////////////////////////////////
        dBConnection.closeConnection();
        
        comboBoxToSet.removeAllItems();
                
        for (TestPoint testPoint1 : testPointList) {
            if ((testPoint1 != null)) {
                comboBoxToSet.addItem(testPoint1.toString());
                System.out.println(testPoint1.toString());
            }
        }
    }
    
      
}
