/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linkpzno;

import linkpzno.data.EduCity;
import linkpzno.data.TestDay;

import java.sql.*;
import java.util.Arrays;

/**
 * @author spidchenko.d
 */
public class DBConnection {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/khersont_db";
    private static final String USER = "root";
    private static final String PASS = "root";


//    private boolean isInitDone = false;

//    private Connection con = null;
    //    private Statement stmt = null;
//    private Statement updateStmt = null;
//    private ResultSet rs = null;
//    private final ResultSet rs2 = null;
//    private final ResultSet rs3 = null;


//    boolean init() {     //Возвращает true если все норм
//        boolean returnStatus = false;
////        appSettings currentSettings = new appSettings();
//
//        try {
//            con = DriverManager.getConnection(URL, USER, PASS);
//
////            updateStmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
//            System.out.println("Database connection initialization OK!");
//            isInitDone = true;
//            returnStatus = true;
//        } catch (SQLException sqlEx) {
//            sqlEx.printStackTrace();
//
//        }
//
//        return returnStatus;
//    }


    private Connection getDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }


    void setTestDays(TestDay[] testDayList) {

        String getTestDaysQuery = """
                SELECT id, predm AS name, date FROM `test_dates` WHERE session LIKE 'Пробне'""";

        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(getTestDaysQuery);
                ResultSet rs = stmt.executeQuery()
        ) {
            int i = 0;
            while (rs.next()) {
                testDayList[i] = new TestDay();
                testDayList[i].id = rs.getInt("id");
                testDayList[i].name = rs.getString("name");
                testDayList[i].date = rs.getString("date");
                i++;
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }


    void setEduCity(EduCity[] eduCityList) {

        String getEduCityQuery = """
                SELECT id, name, district_id FROM `city_passage` WHERE status = 1""";

        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(getEduCityQuery);
                ResultSet rs = stmt.executeQuery();
        ) {
            int i = 0;
            while (rs.next()) {
                eduCityList[i] = new EduCity();
                eduCityList[i].id = rs.getInt("id");
                eduCityList[i].name = rs.getString("name");
                eduCityList[i].districtId = rs.getInt("district_id");
                i++;
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }


    void setTestPoint(TestPoint[] testPointList, int testDayId, int eduCityId) {

        String getTestPointsQuery = """
                SELECT test_points.id, pt_id, auds, name, short_name FROM `test_points`
                JOIN pt ON pt.id = test_points.pt_id
                WHERE archive LIKE "Активний" AND district = ? AND date_id = ?""";

//        System.err.println(getTestPointsQuery);
        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement pstmt = connection.prepareStatement(getTestPointsQuery);
        ) {
            pstmt.setInt(1, eduCityId);
            pstmt.setInt(2, testDayId);

            try (ResultSet rs = pstmt.executeQuery()) {
                Arrays.fill(testPointList, null);
                int i = 0;
                while (rs.next()) {
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
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }

    void getSubjectLangList(SubjectLang[] subjectLangList, int eduCityId, int testDayId) {

        //String getSubjectsQuery = "SELECT id,name FROM `subject` WHERE id IN (SELECT DISTINCT subject_id FROM `subject_user` WHERE payment = 1) AND parent_id = "+testDayComboBoxSelectedIndex;
        //Выборка предметов из одной сессии которые есть в оплаченых

        String getSubjectLangQuery = """
                SELECT subject_id, lang_id, subject.name AS subject_name, lang.name AS lang_name, COUNT( subject_user.id ) AS NUM
                FROM `subject_user`
                JOIN subject ON subject_id = subject.id
                JOIN lang ON lang_id = lang.id
                WHERE payment = 1
                AND subject.parent_id = ?
                AND user_id
                IN
                (
                   SELECT id
                   FROM users
                   WHERE role_id = 1
                   AND city_passage = ?
                )
                AND subject_user.id NOT IN
                (
                    SELECT test_id
                    FROM allocation
                )
                GROUP BY subject_id, lang_id""";

        int subjectLangCounter = 0;

        Arrays.fill(subjectLangList, null);


        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(getSubjectLangQuery);
        ) {
            stmt.setInt(1, testDayId);
            stmt.setInt(2, eduCityId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subjectLangList[subjectLangCounter++] =
                            new SubjectLang(
                                    rs.getInt("subject_id"),
                                    rs.getInt("lang_id"),
                                    rs.getString("subject_name"),
                                    rs.getString("lang_name"),
                                    Integer.parseInt(rs.getString("NUM")),
                                    eduCityId
                            );
                }
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }


    private void createEmptyAuditories(int testPointId, int audsNumToCreate) {

        final String newAudQuery = """
                INSERT INTO auds_list (testpoint_id, aud_num, predmet_id, lang_id, free, code)
                VALUES(?, ?, ?, ?, ?, ?)""";

        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement updateStmt = connection.prepareStatement(newAudQuery);
        ) {
            for (int audIndex = 1; audIndex <= audsNumToCreate; audIndex++) {
//                updateStmt.executeUpdate("INSERT INTO auds_list (testpoint_id, aud_num, predmet_id, lang_id, free, code) VALUES (" + testPointId + ", " + i + ", " + 0 + ", " + 0 + "," + 15 + " , \"061" + Integer.toString(testPointId + 100000).substring(1) + "00" + Integer.toString(100 + i).substring(1) + "\")");
                updateStmt.setInt(1, testPointId);
                updateStmt.setInt(2, audIndex);
                updateStmt.setInt(3, 0);
                updateStmt.setInt(4, 0);
                updateStmt.setInt(5, 15);
                String audCode = "061" + Integer.toString(testPointId + 100000).substring(1) + "00" + Integer.toString(100 + audIndex).substring(1);
                updateStmt.setString(6, audCode);
                updateStmt.executeUpdate();
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }


    int getLastFullAuditoryNumber(int testPointId) {
        int lastFull = 0;
        String getFullAuditoryNumberQuery = """
                SELECT aud_num FROM `auds_list`
                WHERE free = 0 AND testpoint_id = ?
                ORDER BY aud_num DESC
                LIMIT 1""";

        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(getFullAuditoryNumberQuery)
        ) {
            stmt.setInt(1, testPointId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lastFull = rs.getInt("aud_num");
                }
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return lastFull;
    }


    void getAuditoryList(Auditory[] auditoryList, int testPointId, int audsNum) {

        String getAuditoryListCountQuery = "SELECT COUNT(id) AS n FROM `auds_list` WHERE testpoint_id = ?";
        String getAuditoryListQuery = "SELECT * FROM `auds_list` WHERE testpoint_id = ?";
        int countQueryResult = -1;
        System.err.println(getAuditoryListCountQuery);


        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(getAuditoryListCountQuery);
                PreparedStatement listStmt = connection.prepareStatement(getAuditoryListQuery)
        ) {
            stmt.setInt(1, testPointId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    countQueryResult = rs.getInt("n");
                }
            }

            if ((countQueryResult != -1) && (countQueryResult != audsNum)) {
                System.err.println("countQueryResult != audsNum: " + countQueryResult + " " + audsNum);
                createEmptyAuditories(testPointId, audsNum);
            }

            Arrays.fill(auditoryList, null);


            listStmt.setInt(1, testPointId);
            try (ResultSet rs = listStmt.executeQuery()) {
                int i = 0;
                while (rs.next()) {
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
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }


    void setAuditorySubjectLangId(int testPointId, int audsNum) {

//        System.err.println(getAuditoryListCountQuery);
        final String subjectLangQuery = """
                SELECT subject_id, lang_id FROM subject_user
                JOIN allocation ON allocation.test_id= subject_user.id
                WHERE testpoint_id = ? AND aud = ?
                GROUP BY subject_id, lang_id""";

        final String updateSubjectLangQuery = """
                UPDATE auds_list
                SET
                    predmet_id = ?,
                    lang_id = ?
                WHERE testpoint_id = ? AND aud_num = ?""";

        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(subjectLangQuery);
                PreparedStatement updateStmt = connection.prepareStatement(updateSubjectLangQuery);
        ) {

//            updateStmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

            for (int audIndex = 1; audIndex <= audsNum; audIndex++) {
                stmt.setInt(1, testPointId);
                stmt.setInt(2, audIndex);
                try (ResultSet rs = stmt.executeQuery()) {
                    int currentSubjectId = -1;
                    int currentLangId = -1;

                    int audSubjectId = -1;
                    int audLangId = -1;

                    while (rs.next()) {
                        currentSubjectId = rs.getInt("subject_id");
                        currentLangId = rs.getInt("lang_id");
                        //Check subject
                        if ((currentSubjectId != audSubjectId) && (audSubjectId != -1)) {
                            audSubjectId = 99;
                        } else {
                            audSubjectId = currentSubjectId;
                        }
                        //Check lang
                        if ((currentLangId != audLangId) && (audLangId != -1)) {
                            audLangId = 9;//Multilang - UKR + RUS
                        } else {
                            audLangId = currentLangId;
                        }
                    }

                    System.out.println(audIndex + ". audSubjectId = " + audSubjectId + "; audLangId = " + audLangId);

                    updateStmt.setInt(1, audSubjectId);
                    updateStmt.setInt(2, audLangId);
                    updateStmt.setInt(3, testPointId);
                    updateStmt.setInt(4, audIndex);
                    updateStmt.executeUpdate();
//                    updateStmt.executeUpdate("UPDATE auds_list SET predmet_id = " + audSubjectId + ", lang_id = " + audLangId + " WHERE testpoint_id = " + testPointId + " AND aud_num = " + audIndex);
                }
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }


    void link(int testPointIdToLink, int audNumToLink, int numPupilsToLink, SubjectLang subjectLangToLink) {

        final String getNPupils = """
                SELECT su.id, su.user_id, su.bill
                FROM subject_user AS su
                JOIN users u ON u.id = su.user_id
                WHERE payment = 1 AND subject_id = ? AND lang_id = ? AND city_passage = ?
                AND su.id NOT IN
                (
                    SELECT test_id
                    FROM allocation
                )
                LIMIT ?""";

        final String getNumPupilsInThisAud = """
                SELECT MAX(place)
                FROM `allocation`
                WHERE testpoint_id = ? AND aud = ?""";

        final String newAllocationRow = """
                INSERT INTO allocation (test_id, testpoint_id, aud, place, test_code)
                VALUES (?, ?, ?, ?, ?)""";

        final String getFreeInAudQuery = """
                SELECT free
                FROM auds_list
                WHERE testpoint_id = ? AND aud_num = ?""";

        final String setFreeInAudQuery = """
                UPDATE auds_list
                SET free = ?
                WHERE testpoint_id = ? AND aud_num = ?""";

        final String getAudInfoQuery = """
                SELECT subject_id, lang_id FROM subject_user
                JOIN allocation ON allocation.test_id = subject_user.id
                WHERE testpoint_id = ? AND aud = ?
                GROUP BY subject_id, lang_id""";

        int[] pupilsIdToLink = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};   //15
        int numPupilsInThisAud = -1;    //Заполненость аудитории по allocation
        int freeInAud = -1;             //Количество свободных мест в аудитории по aud_list

        if (numPupilsToLink > subjectLangToLink.numPupils) {     //Если выбрано для рассадки людей больше чем есть то завершаем работу функции
            System.err.println("numPupilsToLink > subjectLangToLink.numPupils");
            return;
        }


        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement getNPupilsStmt = connection.prepareStatement(getNPupils);
                PreparedStatement getNumPupilsInThisAudStmt = connection.prepareStatement(getNumPupilsInThisAud);
                PreparedStatement newAllocationStmt = connection.prepareStatement(newAllocationRow);
                PreparedStatement getFreeInAudStmt = connection.prepareStatement(getFreeInAudQuery);
                PreparedStatement setFreeInAudStmt = connection.prepareStatement(setFreeInAudQuery);
                PreparedStatement getAudInfoStmt = connection.prepareStatement(getAudInfoQuery);
        ) {
            //Выбираем н человек из базы
            getNPupilsStmt.setInt(1, subjectLangToLink.subjectId);
            getNPupilsStmt.setInt(2, subjectLangToLink.langId);
            getNPupilsStmt.setInt(3, subjectLangToLink.eduCityId);
            getNPupilsStmt.setInt(4, numPupilsToLink);
            try (ResultSet rs = getNPupilsStmt.executeQuery()) {
                System.out.println(getNPupils);

                int i = 0;
                while (rs.next()) {
                    pupilsIdToLink[i++] = rs.getInt("id");
                }

                System.out.println(Arrays.toString(pupilsIdToLink));
            }
//            rs = stmt.executeQuery(getNPupils);


//            String getNumPupilsInThisAud = "SELECT MAX(place) FROM `allocation` WHERE testpoint_id = " + testPointIdToLink + " AND aud = " + audNumToLink;
            getNumPupilsInThisAudStmt.setInt(1, testPointIdToLink);
            getNumPupilsInThisAudStmt.setInt(2, audNumToLink);

            try (ResultSet rs = getNumPupilsInThisAudStmt.executeQuery()) {
                while (rs.next()) {
                    numPupilsInThisAud = rs.getInt("MAX(place)");
                }
            }
//            System.out.println(getNumPupilsInThisAud);
//            rs = getNPupilsStmt.executeQuery(getNumPupilsInThisAud);

            if (numPupilsToLink + numPupilsInThisAud > 15) {   //Если получается больше 15 человек в аудитории
                System.err.println("AudFull!!!11");
                return;
            }

//            updateStmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

            for (int j = 0; j < numPupilsToLink; j++) {   //Записали в allocation
//                updateStmt.executeUpdate("INSERT INTO allocation (test_id, testpoint_id, aud, place, test_code) VALUES (" + pupilsIdToLink[j] + ", " + testPointIdToLink + ", " + audNumToLink + ", " + (++numPupilsInThisAud) + ", \"" + Integer.toString((testPointIdToLink + pupilsIdToLink[j]) % 10) + Integer.toString(testPointIdToLink + 1000).substring(1) + Integer.toString(pupilsIdToLink[j] + 100000).substring(1) + "\")");
                newAllocationStmt.setInt(1, pupilsIdToLink[j]);
                newAllocationStmt.setInt(2, testPointIdToLink);
                newAllocationStmt.setInt(3, audNumToLink);
                newAllocationStmt.setInt(4, ++numPupilsInThisAud);
                String testCode = (testPointIdToLink + pupilsIdToLink[j]) % 10 +
                                  Integer.toString(testPointIdToLink + 1000).substring(1) +
                                  Integer.toString(pupilsIdToLink[j] + 100000).substring(1);
                newAllocationStmt.setString(5, testCode);
                newAllocationStmt.executeUpdate();
            }
//<<<<<<<<<<<
//            rs = getNPupilsStmt.executeQuery("SELECT free FROM auds_list WHERE testpoint_id = " + testPointIdToLink + " AND aud_num = " + audNumToLink);

            getFreeInAudStmt.setInt(1, testPointIdToLink);
            getFreeInAudStmt.setInt(2, audNumToLink);
            try (ResultSet rs = getFreeInAudStmt.executeQuery()) {
                while (rs.next()) {
                    freeInAud = rs.getInt("free");
                }
            }

            setFreeInAudStmt.setInt(1, (freeInAud - numPupilsToLink));
            setFreeInAudStmt.setInt(2, testPointIdToLink);
            setFreeInAudStmt.setInt(3, audNumToLink);
            setFreeInAudStmt.executeUpdate();

//            updateStmt.executeUpdate("UPDATE auds_list SET free = " + (freeInAud - numPupilsToLink) + " WHERE testpoint_id = " + testPointIdToLink + " AND aud_num = " + audNumToLink);

//В БАЗУ ПОКА НЕ ПИШЕТ


//            rs = getNPupilsStmt.executeQuery("SELECT subject_id, lang_id FROM subject_user\n" +
//                                             "JOIN allocation ON allocation.test_id= subject_user.id \n" +
//                                             "WHERE testpoint_id = " + testPointIdToLink + " AND aud = " + audNumToLink + "\n" +
//                                             "GROUP BY subject_id, lang_id");
            //Пройдемся по результату. если есть разные subject_id / lang_id то в свойствах аудитории 99, иначе predmet_id = subject_id, lang_id = lang_id

            //??????????????????????????????????????????????
            //??????????????????????????????????????????????
            //??????????????????????????????????????????????
            //??????????????????????????????????????????????
            //??????????????????????????????????????????????

            getAudInfoStmt.setInt(1, testPointIdToLink);
            getAudInfoStmt.setInt(2, audNumToLink);

            try (ResultSet rs = getAudInfoStmt.executeQuery()) {
                int currentSubjectId = -1;
                int currentLangId = -1;

                int audSubjectId = -1;
                int audLangId = -1;

                while (rs.next()) {
                    currentSubjectId = rs.getInt("subject_id");
                    currentLangId = rs.getInt("lang_id");
                    //Check subject
                    if ((currentSubjectId != audSubjectId) && (audSubjectId != -1)) {
                        audSubjectId = 99;
                    } else {
                        audSubjectId = currentSubjectId;
                    }
                    //Check lang
                    if ((currentLangId != audLangId) && (audLangId != -1)) {
                        audLangId = 99;
                    } else {
                        audLangId = currentLangId;
                    }
                }

                System.out.println("audSubjectId = " + audSubjectId + "; audLangId = " + audLangId);
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }
}