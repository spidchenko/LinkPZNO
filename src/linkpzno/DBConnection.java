/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linkpzno;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

/**
 *
 * @author spidchenko.d
 */
public class DBConnection {
    
//Test SQL server:
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/khersontes_db";
    private static final String USER = "root";
    private static final String PASS = "root";
    
    
    private final String getTestDaysQuery = "SELECT id, predm AS name, date FROM `test_dates` WHERE session LIKE \"Пробне\""; 
    
    private final String getEduCityQuery = "SELECT id, name, district_id FROM `city_passage`\n" +
                                           "JOIN city_district ON city_district.city_id = city_passage.id";
    

    
    private boolean isInitDone = false;
    
    private Connection con = null;
    private Statement stmt = null;
    private Statement updateStmt = null;
    private ResultSet rs = null;
    private ResultSet rs2 = null;
    private ResultSet rs3 = null;
    
    
    boolean init(){     //Возвращает true если все норм 
        boolean returnStatus = false;
//        appSettings currentSettings = new appSettings();
        
        try{
            con = DriverManager.getConnection(URL,USER,PASS);
            stmt = con.createStatement();
            updateStmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            System.out.println("Database connection initialization OK!"); 
            isInitDone = true;
            returnStatus = true;
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
            
        }

        return returnStatus;
    }
    
    void setTestDays(TestDay [] testDayList){
        if (!isInitDone) init();
        try{
            rs = stmt.executeQuery(getTestDaysQuery);
            int i = 0;
            while(rs.next()){
                testDayList[i] = new TestDay();
                
                testDayList[i].id = rs.getInt("id");
                testDayList[i].name = rs.getString("name");
                testDayList[i].date = rs.getString("date");
                i++;
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            //close connection, stmt and resultset here
            try {if(con != null) con.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(stmt != null) stmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(updateStmt != null) updateStmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(rs != null) rs.close(); } catch(SQLException se) { /*can't do anything */ }
            isInitDone = false; //init Undone
        }     
    }
    
    void setEduCity(EduCity [] eduCityList){
        if (!isInitDone) init();
        try{
            rs = stmt.executeQuery(getEduCityQuery);
            int i = 0;
            while(rs.next()){
                eduCityList[i] = new EduCity();
                
                eduCityList[i].id = rs.getInt("id");
                eduCityList[i].name = rs.getString("name");
                eduCityList[i].districtId = rs.getInt("district_id");
                
                i++;
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            //close connection, stmt and resultset here
            try {if(con != null) con.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(stmt != null) stmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(updateStmt != null) updateStmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(rs != null) rs.close(); } catch(SQLException se) { /*can't do anything */ }
            isInitDone = false; //init Undone
        }     
    }
    
    void setTestPoint(TestPoint [] testPointList, int testDayId, int eduCityId){
        
        String getTestPointsQuery = "SELECT test_points.id, pt_id, auds, name, short_name FROM `test_points`\n" +
                                          "JOIN pt ON pt.id = test_points.pt_id\n" +
                                          "WHERE archive LIKE \"Активний\" AND district = "+eduCityId+" AND date_id = "+testDayId;    
        System.err.println(getTestPointsQuery);//////////////////////////////
        if (!isInitDone) init();
        try{
            rs = stmt.executeQuery(getTestPointsQuery);
            
            for(int i = 0; i < testPointList.length; i++)
                testPointList[i] = null;
            
            int i = 0;
            while(rs.next()){
                testPointList[i] = new TestPoint();
                
                testPointList[i].id = rs.getInt("id");
                testPointList[i].ptId = rs.getInt("pt_id");
                testPointList[i].name = rs.getString("name");
                testPointList[i].shortName = rs.getString("short_name");
                testPointList[i].audsNum = rs.getInt("auds");
                
                testPointList[i].name = testPointList[i].name.replace("&quot;", "\"");              //Replace &quot; -> "
                testPointList[i].shortName = testPointList[i].shortName.replace("&quot;", "\"");    //Replace &quot; -> "
                
                i++;
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            //close connection, stmt and resultset here
            try {if(con != null) con.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(stmt != null) stmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(updateStmt != null) updateStmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(rs != null) rs.close(); } catch(SQLException se) { /*can't do anything */ }
            isInitDone = false; //init Undone
        }     
    }    
   
    void getSubjectLangList(SubjectLang [] subjectLangList,int eduCityId, int testDayId){
        
        //String getSubjectsQuery = "SELECT id,name FROM `subject` WHERE id IN (SELECT DISTINCT subject_id FROM `subject_user` WHERE payment = 1) AND parent_id = "+testDayComboBoxSelectedIndex;
                                    //Выборка предметов из одной сессии которые есть в оплаченых
                                    
//        String getSubjectLangQuery ="SELECT subject_id, lang_id, subject.name AS subject_name, lang.name AS lang_name, COUNT( subject_user.id ) AS NUM " +
//                                    "FROM `subject_user` " +
//                                    "JOIN subject ON subject_id = subject.id " +
//                                    "JOIN lang ON lang_id = lang.id " +
//                                    "WHERE payment =1 " +
//                                    "AND subject.parent_id = "+testDayComboBoxSelectedIndex+" "+
//                                    "AND user_id " +
//                                    "IN " +
//                                    "( " +
//                                    "SELECT id " +
//                                    "FROM users " +
//                                    "WHERE role_id =1 " +
//                                    "AND city_passage = "+eduCityId+" "+
//                                    ") " +
//                                    "GROUP BY subject_id, lang_id";

        String getSubjectLangQuery =    "SELECT subject_id, lang_id, subject.name AS subject_name, lang.name AS lang_name, COUNT( subject_user.id ) AS NUM\n" +
                                        "FROM `subject_user`\n" +
                                        "JOIN subject ON subject_id = subject.id\n" +
                                        "JOIN lang ON lang_id = lang.id\n" +
                                        "WHERE payment =1\n" +
                                        "AND subject.parent_id = "+testDayId+"\n"+
                                        "AND user_id\n" +
                                        "IN\n" +
                                        "(\n" +
                                        "   SELECT id\n" +                  //Которые выбрали конкретный город для прохождения
                                        "   FROM users\n" +
                                        "   WHERE role_id =1\n" +
                                        "   AND city_passage = "+eduCityId+"\n"+
                                        ")\n" +
                                        "AND subject_user.id NOT IN\n" +
                                        "(\n" +
                                        "    SELECT test_id\n" +                //Которые еще не рассажены (их нет в таблице allocation)
                                        "    FROM allocation\n" +
                                        ")\n" +
                                        "GROUP BY subject_id, lang_id";
                                    
        //System.out.println(getSubjectLangQuery);
        
//        int currentSubjectId = -1;
//        String currentSubjectName;
//        int currentLangId = -1;
//        int currentNumPupils = -1;
        
        int subjectLangCounter = 0;
        
        for(int i = 0; i < subjectLangList.length; i++)
            subjectLangList[i] = null;
        
        if (!isInitDone) init();
        try{
            rs = stmt.executeQuery(getSubjectLangQuery);
            
            while(rs.next()){

                subjectLangList[subjectLangCounter++] = 
                        new SubjectLang(rs.getInt("subject_id"),
                                        rs.getInt("lang_id"),
                                        rs.getString("subject_name"),
                                        rs.getString("lang_name"),
                                        Integer.parseInt(rs.getString("NUM")),
                                        eduCityId
                        );
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            //close connection, stmt and resultset here
            try {if(con != null) con.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(stmt != null) stmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(updateStmt != null) updateStmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(rs != null) rs.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(rs2 != null) rs2.close(); } catch(SQLException se) { /*can't do anything */ }
            isInitDone = false; //init Undone
        } 
    }
    
    private void createEmptyAuditories(int testPointId, int audsNumToCreate){
       
        if (!isInitDone) init();
        try{
            updateStmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);             
            
            for(int i = 1; i<=audsNumToCreate; i++){
                updateStmt.executeUpdate("INSERT INTO auds_list (testpoint_id, aud_num, predmet_id, lang_id, free, code) VALUES ("+testPointId+", "+i+", "+0+", "+0+","+15+" , \"061"+Integer.toString(testPointId+100000).substring(1)+"00"+Integer.toString(100+i).substring(1)+"\")");
            }
            
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            //close connection, stmt and resultset here
            try {if(con != null) con.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(updateStmt != null) updateStmt.close(); } catch(SQLException se) { /*can't do anything */ }
            isInitDone = false; //init Undone
        }             
    }
    
    
    void getAuditoryList(Auditory [] auditoryList, int testPointId, int audsNum){
        
        String getAuditoryListCountQuery = "SELECT COUNT(id) AS n FROM `auds_list` WHERE testpoint_id = "+testPointId; 
        String getAuditoryListQuery = "SELECT * FROM `auds_list` WHERE testpoint_id = "+testPointId; 
        int countQueryResult = -1;
        System.err.println(getAuditoryListCountQuery);//////////////////////////////
        
        
        if (!isInitDone) init();
        try{
            rs = stmt.executeQuery(getAuditoryListCountQuery);
            
            while(rs.next()){
                countQueryResult = rs.getInt("n");
            }
            
            if((countQueryResult != -1)&&(countQueryResult != audsNum)){
                System.err.println("countQueryResult != audsNum: "+countQueryResult+" "+audsNum);
                createEmptyAuditories(testPointId,audsNum);
            }

            for(int i = 0; i < auditoryList.length; i++)
                auditoryList[i] = null;
            
            if (!isInitDone) init();
            rs = stmt.executeQuery(getAuditoryListQuery);

            int i = 0;
            while(rs.next()){
                auditoryList[i] = new Auditory();

                auditoryList[i].id = rs.getInt("id");
                auditoryList[i].testPointId = rs.getInt("testpoint_id");
                auditoryList[i].audNum = rs.getInt("aud_num");
                auditoryList[i].predmetId = rs.getInt("predmet_id");
                auditoryList[i].langId = rs.getInt("lang_id");
                auditoryList[i].free = rs.getInt("free");
                auditoryList[i].code = rs.getString("code");

                i++;
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            //close connection, stmt and resultset here
            try {if(con != null) con.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(stmt != null) stmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(updateStmt != null) updateStmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(rs != null) rs.close(); } catch(SQLException se) { /*can't do anything */ }
            isInitDone = false; //init Undone
        }     
    }    

    void setAuditorySubjectLangId(int testPointId, int audsNum){
        
//        String getAuditoryListCountQuery = "SELECT COUNT(id) AS n FROM `auds_list` WHERE testpoint_id = "+testPointId; 
//        String getAuditoryListQuery = "SELECT * FROM `auds_list` WHERE testpoint_id = "+testPointId; 
//        int countQueryResult = -1;
//        System.err.println(getAuditoryListCountQuery);//////////////////////////////
        
        
        if (!isInitDone) init();
        try{
            
            updateStmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            
            for(int i = 1; i<= audsNum; i++){
                rs = stmt.executeQuery("SELECT subject_id, lang_id FROM subject_user\n" +
                                        "JOIN allocation ON allocation.test_id= subject_user.id \n" +
                                        "WHERE testpoint_id = "+testPointId+" AND aud = "+i+"\n" +
                                        "GROUP BY subject_id, lang_id"); 
                
                int currentSubjectId = -1;
                int currentLangId = -1;

                int audSubjectId = -1;
                int audLangId = -1;

                while (rs.next()){
                    currentSubjectId = rs.getInt("subject_id");
                    currentLangId = rs.getInt("lang_id");
                    //Check subject
                    if((currentSubjectId != audSubjectId)&&(audSubjectId != -1)){
                        audSubjectId = 99;
                    } else {
                        audSubjectId = currentSubjectId;
                    }
                    //Check lang
                    if((currentLangId != audLangId)&&(audLangId != -1)){
                        audLangId = 99;
                    } else{
                        audLangId = currentLangId;
                    }
                }

                System.out.println(i+". audSubjectId = "+audSubjectId+"; audLangId = "+audLangId);                
                updateStmt.executeUpdate("UPDATE auds_list SET predmet_id = "+audSubjectId+", lang_id = "+audLangId+" WHERE testpoint_id = "+testPointId+" AND aud_num = "+i);
            }
            
            //Пройдемся по результату. если есть разные subject_id / lang_id то в свойствах аудитории 99, иначе predmet_id = subject_id, lang_id = lang_id
            

            
//            while(rs.next()){
//                countQueryResult = rs.getInt("n");
//            }
//            
//            if((countQueryResult != -1)&&(countQueryResult != audsNum)){
//                System.err.println("countQueryResult != audsNum: "+countQueryResult+" "+audsNum);
//                createEmptyAuditories(testPointId,audsNum);
//            }
//
//            for(int i = 0; i < auditoryList.length; i++)
//                auditoryList[i] = null;
            
//            if (!isInitDone) init();
//            rs = stmt.executeQuery(getAuditoryListQuery);

//            int i = 0;
//            while(rs.next()){
//                auditoryList[i] = new Auditory();
//
//                auditoryList[i].id = rs.getInt("id");
//                auditoryList[i].testPointId = rs.getInt("testpoint_id");
//                auditoryList[i].audNum = rs.getInt("aud_num");
//                auditoryList[i].predmetId = rs.getInt("predmet_id");
//                auditoryList[i].langId = rs.getInt("lang_id");
//                auditoryList[i].free = rs.getInt("free");
//                auditoryList[i].code = rs.getString("code");
//
//                i++;
//            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            //close connection, stmt and resultset here
            try {if(con != null) con.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(stmt != null) stmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(updateStmt != null) updateStmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(rs != null) rs.close(); } catch(SQLException se) { /*can't do anything */ }
            isInitDone = false; //init Undone
        }     
    }        
    
    void link(int testPointIdToLink, int audNumToLink, int numPupilsToLink, SubjectLang subjectLangToLink){
       
        int[] pupilsIdToLink = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};   //15
        int numPupilsInThisAud = -1;    //Заполненость аудитории по allication
        int freeInAud = -1;             //Количество свободных мест в аудитории по aud_list
        if (!isInitDone) init();
        try{
            
            if (numPupilsToLink > subjectLangToLink.numPupils){     //Если выбрано для рассадки людей больше чем есть то завершаем работу функции
                System.err.println("numPupilsToLink > subjectLangToLink.numPupils");
                return;
            }
            //Выбираем н человек из базы
            String getNPupils = "SELECT su.id, su.user_id, su.bill FROM subject_user AS su\n" +
                                "JOIN users u ON u.id = su.user_id\n" +
                                "WHERE payment = 1 AND subject_id = "+subjectLangToLink.subjectId+" AND lang_id = "+subjectLangToLink.langId+" AND city_passage = " +subjectLangToLink.eduCityId+"\n"+
                                "AND su.id NOT IN\n" +
                                "(" +
                                "    SELECT test_id " +
                                "    FROM allocation " +
                                ")"+
                                "LIMIT "+numPupilsToLink;
            rs = stmt.executeQuery(getNPupils);
            
            System.out.println(getNPupils);
            
            int i =0;
            while(rs.next()){
                
                pupilsIdToLink[i++] = rs.getInt("id");
                
//                System.out.println(
//                        rs.getInt("id")+ " "+
//                        rs.getInt("user_id")+ " "+
//                        rs.getString("bill")
//                );
            }
            
            System.out.println(Arrays.toString(pupilsIdToLink));
            
            String getNumPupilsInThisAud = "SELECT MAX(place) FROM `allocation` WHERE testpoint_id = "+testPointIdToLink+" AND aud = "+audNumToLink;
            
            System.out.println(getNumPupilsInThisAud);
            rs = stmt.executeQuery(getNumPupilsInThisAud);
             
            while(rs.next()){
                numPupilsInThisAud = rs.getInt("MAX(place)");
            }
            
            if (numPupilsToLink+numPupilsInThisAud > 15){   //Если получается больше 15 человек в аудитории
                System.err.println("AudFull!!!11");
                return;
            }
            
            updateStmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            
            for(int j = 0; j < numPupilsToLink; j++){   //Записали в allocation
                updateStmt.executeUpdate("INSERT INTO allocation (test_id, testpoint_id, aud, place, test_code) VALUES ("+pupilsIdToLink[j]+", "+testPointIdToLink+", "+audNumToLink+", "+(++numPupilsInThisAud)+", \""+Integer.toString((testPointIdToLink+pupilsIdToLink[j])%10)+Integer.toString(testPointIdToLink+1000).substring(1)+Integer.toString(pupilsIdToLink[j]+100000).substring(1)+"\")");
            }
            
            rs = stmt.executeQuery("SELECT free FROM auds_list WHERE testpoint_id = "+testPointIdToLink+" AND aud_num = "+audNumToLink);
            
            while(rs.next()){
                freeInAud = rs.getInt("free");
            }
            
            
            
            updateStmt.executeUpdate("UPDATE auds_list SET free = "+(freeInAud-numPupilsToLink)+" WHERE testpoint_id = "+testPointIdToLink+" AND aud_num = "+audNumToLink);
            
//В БАЗУ ПОКА НЕ ПИШЕТ
            
            rs = stmt.executeQuery("SELECT subject_id, lang_id FROM subject_user\n" +
                                    "JOIN allocation ON allocation.test_id= subject_user.id \n" +
                                    "WHERE testpoint_id = "+testPointIdToLink+" AND aud = "+audNumToLink+"\n" +
                                    "GROUP BY subject_id, lang_id");
            //Пройдемся по результату. если есть разные subject_id / lang_id то в свойствах аудитории 99, иначе predmet_id = subject_id, lang_id = lang_id
            
            int currentSubjectId = -1;
            int currentLangId = -1;
            
            int audSubjectId = -1;
            int audLangId = -1;
            
            while (rs.next()){
                currentSubjectId = rs.getInt("subject_id");
                currentLangId = rs.getInt("lang_id");
                //Check subject
                if((currentSubjectId != audSubjectId)&&(audSubjectId != -1)){
                    audSubjectId = 99;
                } else {
                    audSubjectId = currentSubjectId;
                }
                //Check lang
                if((currentLangId != audLangId)&&(audLangId != -1)){
                    audLangId = 99;
                } else{
                    audLangId = currentLangId;
                }
            }
            
            System.out.println("audSubjectId = "+audSubjectId+"; audLangId = "+audLangId);
            
//В БАЗУ ПОКА НЕ ПИШЕТ
            
            //ОБНОВИТЬ ПРЕДМЕТ И ПЕРЕВОД В АУДИТОРИИ!!!1111 rs.count
//            SELECT subject_id, lang_id FROM subject_user
//            JOIN allocation ON allocation.test_id= subject_user.id 
//            WHERE testpoint_id = 354 AND aud = 1
//            GROUP BY subject_id, lang_id
            
            
            
//SELECT su.id, su.user_id, su.bill FROM subject_user AS su
//JOIN users u ON u.id = su.user_id
//WHERE payment = 1 AND subject_id = 1 AND lang_id = 0 AND city_passage = 7
//LIMIT 15
            
//            for(int i = 1; i <= numPupilsToLink; i++){
//                
//            }
            
            
            
//            updateStmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);             
//            
//            for(int i = 1; i<=audsNumToCreate; i++){
//                updateStmt.executeUpdate("INSERT INTO auds_list (testpoint_id, aud_num, predmet_id, lang_id, free, code) VALUES ("+testPointId+", "+i+", "+0+", "+0+","+15+" , \"061"+Integer.toString(testPointId+100000).substring(1)+"00"+Integer.toString(100+i).substring(1)+"\")");
//            }
            
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            //close connection, stmt and resultset here
            try {if(con != null) con.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(stmt != null) stmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(rs != null) rs.close(); } catch(SQLException se) { /*can't do anything */ }
            try {if(updateStmt != null) updateStmt.close(); } catch(SQLException se) { /*can't do anything */ }
            isInitDone = false; //init Undone
        }             
    }
    
    void closeConnection(){
        try {if(con != null) con.close(); } catch(SQLException se) { /*can't do anything */ }
        try {if(stmt != null) stmt.close(); } catch(SQLException se) { /*can't do anything */ }
        try {if(updateStmt != null) updateStmt.close(); } catch(SQLException se) { /*can't do anything */ }
        try {if(rs != null) rs.close(); } catch(SQLException se) { /*can't do anything */ }
        isInitDone = false; //init Undone      
    }
}