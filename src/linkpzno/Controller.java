/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linkpzno;

import linkpzno.data.*;

import java.util.Arrays;
import javax.swing.JComboBox;
import javax.swing.JProgressBar;

/**
 * @author spidchenko.d
 */



public class Controller {
    private final TestDay[] testDayList = new TestDay[2];
    private final EduCity[] eduCityList = new EduCity[16];
    private TestPoint[] testPointList = new TestPoint[20]; //Почему 10? сделать List
    private Auditory[] auditoryList = new Auditory[30];    //30 - Макс. количество аудиторий в пункте
    SubjectLang[] subjectLangList = new SubjectLang[30];   //30 макс комбинаций предмет-перевод
    JProgressBar[] progressBarList = new JProgressBar[26];  //26 - Макс. количество аудиторий в пункте

    Controller() {
        DBConnection dBConnection = new DBConnection();
        dBConnection.setTestDays(testDayList);
        dBConnection.setEduCity(eduCityList);
    }


    void initAuditoryPanel(JComboBox audNumList, int comboBoxPTSelectedIndex, JComboBox subjectList, int comboBoxTestDaySelectedIndex, int comboBoxEduCitySelictedIndex) {

        DBConnection dBConnection = new DBConnection();
        dBConnection.getSubjectLangList(subjectLangList, eduCityList[comboBoxEduCitySelictedIndex].getId(), testDayList[comboBoxTestDaySelectedIndex].getId());//!!!!!!!!!

        audNumList.removeAllItems();
        int lastFullAudNumber = dBConnection.getLastFullAuditoryNumber(testPointList[comboBoxPTSelectedIndex].getId());
        System.out.println(lastFullAudNumber);
        for (int i = lastFullAudNumber + 1; i <= testPointList[comboBoxPTSelectedIndex].getAudsNum(); i++)
            audNumList.addItem(i);

        subjectList.removeAllItems();

        int i = 0;
        while (subjectLangList[i] != null) {
            subjectList.addItem(subjectLangList[i].toString());
            i++;
        }

//        
//        int i =0;
//        while(auditoryList[i] == null){
//            audNumList.addItem(auditoryList[i].audNum);
//        }


    }

    void showAuditoryListOnPanel() {

        for (JProgressBar jProgressBar : progressBarList) {
            jProgressBar.setEnabled(false);
            jProgressBar.setValue(0);
        }

        int i = 0;
        while (auditoryList[i] != null) {
            progressBarList[i].setEnabled(true);
            progressBarList[i].setValue(15 - auditoryList[i].getFree());
            i++;
        }
    }

    void setAuditorySubjectLangId(int comboBoxSelIndex) {
        DBConnection dBConnection = new DBConnection();
        dBConnection.setAuditorySubjectLangId(
                testPointList[comboBoxSelIndex].getId(),
                testPointList[comboBoxSelIndex].getAudsNum());
    }

    void getAuditoryList(int comboBoxSelIndex) {
        //System.out.println(testPointList[comboBoxSelIndex].toString());

        DBConnection dBConnection = new DBConnection();
        dBConnection.getAuditoryList(auditoryList,
                testPointList[comboBoxSelIndex].getId(),
                testPointList[comboBoxSelIndex].getAudsNum());
        System.out.println(Arrays.toString(auditoryList));//////////////////////
    }

    void setTestDay(JComboBox comboBoxToSet) {
        for (TestDay testDay1 : testDayList) {
            if (testDay1 != null) {
                comboBoxToSet.addItem(testDay1.toString());
                System.out.println(testDay1.toString());
            }
        }
    }

    void setEduCity(JComboBox comboBoxToSet) {
        for (EduCity eduCity1 : eduCityList) {
            if ((eduCity1 != null)) {
                comboBoxToSet.addItem(eduCity1.toString());
                System.out.println(eduCity1.toString());
            }
        }

    }

    void link(int comboBoxPTSelectedIndex, int comboBoxAudNumSelectedIndex, String textFieldNumPupilsText, int comboBoxSubjectSelectedIndex) {
        DBConnection dBConnection = new DBConnection();
        dBConnection.link(testPointList[comboBoxPTSelectedIndex].getId(), comboBoxAudNumSelectedIndex, Integer.parseInt(textFieldNumPupilsText), subjectLangList[comboBoxSubjectSelectedIndex]);
    }

    void setTestPoint(JComboBox comboBoxToSet, int testDayListNumber, int eduCityIdListNumber) {

        DBConnection dBConnection = new DBConnection();
        dBConnection.setTestPoint(testPointList, testDayList[testDayListNumber].getId(), eduCityList[eduCityIdListNumber].getDistrictId());

        System.out.println(testDayList[testDayListNumber].getId() + " " + eduCityList[eduCityIdListNumber].getDistrictId());//////////////////////////////////
        comboBoxToSet.removeAllItems();

        for (TestPoint testPoint1 : testPointList) {
            if ((testPoint1 != null)) {
                comboBoxToSet.addItem(testPoint1.toString());
                System.out.println(testPoint1.toString());
            }
        }
    }
}
