/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linkpzno;

import linkpzno.data.*;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JProgressBar;

/**
 * @author spidchenko.d
 */


public class Controller {
    private final List<TestDay> testDays;
    private final List<EduCity> eduCities;
    private final ArrayList<TestPoint> testPoints = new ArrayList<>();
    private final ArrayList<Classroom> classrooms = new ArrayList<>();
    private final ArrayList<SubjectLang> subjectLanguages = new ArrayList<>();
    private final ArrayList<JProgressBar> progressBars = new ArrayList<>();  //26 MAX

    Controller() {
        DBConnection dBConnection = new DBConnection();
        testDays = dBConnection.getTestDays();
        eduCities = dBConnection.getEduCities();
    }

    public void initProgressBars(List<JProgressBar> progressBars) {
        this.progressBars.addAll(progressBars);
    }

    void initAuditoryPanel(JComboBox audNumList, int comboBoxPTSelectedIndex, JComboBox subjectList, int comboBoxTestDaySelectedIndex, int comboBoxEduCitySelectedIndex) {

        DBConnection dBConnection = new DBConnection();
        dBConnection.getSubjectLangList(subjectLanguages, eduCities.get(comboBoxEduCitySelectedIndex).getId(), testDays.get(comboBoxTestDaySelectedIndex).getId());

        audNumList.removeAllItems();
        int lastFullAudNumber = dBConnection.getLastFullAuditoryNumber(testPoints.get(comboBoxPTSelectedIndex).getId());
//        System.out.println(lastFullAudNumber);

        for (int i = lastFullAudNumber + 1; i <= testPoints.get(comboBoxPTSelectedIndex).getAudsNum(); i++) {
            audNumList.addItem(i);
        }


        subjectList.removeAllItems();

        for (SubjectLang subjectLanguage : subjectLanguages) {
            subjectList.addItem(subjectLanguage.toString());
        }

//        
//        int i =0;
//        while(auditoryList[i] == null){
//            audNumList.addItem(auditoryList[i].audNum);
//        }


    }

    void showAuditoryListOnPanel() {

        for (JProgressBar jProgressBar : progressBars) {
            jProgressBar.setEnabled(false);
            jProgressBar.setValue(0);
        }

        for (int i = 0; i < classrooms.size(); i++) {
            progressBars.get(i).setEnabled(true);
            progressBars.get(i).setValue(15 - classrooms.get(i).getFree());
        }
    }

    void setAuditorySubjectLangId(int comboBoxSelIndex) {
        DBConnection dBConnection = new DBConnection();
        dBConnection.updateSubjectLangIdsInAud(
                testPoints.get(comboBoxSelIndex).getId(),
                testPoints.get(comboBoxSelIndex).getAudsNum());
    }

    void getAuditoryList(int comboBoxSelIndex) {

        DBConnection dBConnection = new DBConnection();
        dBConnection.getAuditoryList(
                classrooms,
                testPoints.get(comboBoxSelIndex).getId(),
                testPoints.get(comboBoxSelIndex).getAudsNum()
        );
        System.out.println(classrooms);
    }

    void setTestDay(JComboBox comboBoxToSet) {
        for (TestDay testDay : testDays) {
            comboBoxToSet.addItem(testDay.toString());
//            System.out.println(testDay);
        }
    }

    void setEduCity(JComboBox comboBoxToSet) {
        for (EduCity eduCity : eduCities) {
            comboBoxToSet.addItem(eduCity.toString());
//            System.out.println(eduCity);
        }

    }

    void link(int comboBoxPTSelectedIndex, int comboBoxAudNumSelectedIndex, String textFieldNumPupilsText, int comboBoxSubjectSelectedIndex) {
        System.out.println("id = " + comboBoxAudNumSelectedIndex);
        DBConnection dBConnection = new DBConnection();
        dBConnection.link(testPoints.get(comboBoxPTSelectedIndex).getId(), comboBoxAudNumSelectedIndex, Integer.parseInt(textFieldNumPupilsText), subjectLanguages.get(comboBoxSubjectSelectedIndex));
    }

    void setTestPoint(JComboBox comboBoxToSet, int testDayListNumber, int eduCityIdListNumber) {

        DBConnection dBConnection = new DBConnection();
        // TODO look here
        dBConnection.setTestPoint(testPoints, testDays.get(testDayListNumber).getId(), eduCities.get(eduCityIdListNumber).getDistrictId());

//        System.out.println(testDays.get(testDayListNumber).getId() + " " + eduCities.get(eduCityIdListNumber).getDistrictId());
        comboBoxToSet.removeAllItems();

        for (TestPoint testPoint : testPoints) {
            comboBoxToSet.addItem(testPoint.toString());
//            System.out.println(testPoint);
        }
    }
}
