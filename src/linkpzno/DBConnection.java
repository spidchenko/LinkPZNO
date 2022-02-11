package linkpzno;

import linkpzno.data.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/khersont_db";
    private static final String USER = "root";
    private static final String PASS = "root";


    List<TestDay> getTestDays() {
        String getTestDaysQuery = """
                SELECT id, predm AS name, date
                FROM `test_dates`
                WHERE session LIKE 'Пробне'""";
        List<TestDay> testDays = new ArrayList<>();
        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(getTestDaysQuery);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                testDays.add(
                        new TestDay(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("date")
                        ));
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return testDays;
    }


    List<EduCity> getEduCities() {
        String getEduCityQuery = """
                SELECT id, name, district_id
                FROM `city_passage`
                WHERE status = 1""";
        List<EduCity> eduCityList = new ArrayList<>();
        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(getEduCityQuery);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                eduCityList.add(
                        new EduCity(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getInt("district_id")
                        ));
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return eduCityList;
    }


    void setTestPoint(ArrayList<TestPoint> testPoints, int testDayId, int eduCityId) {
        String getTestPointsQuery = """
                SELECT test_points.id, pt_id, auds, name, short_name
                FROM `test_points`
                JOIN pt ON pt.id = test_points.pt_id
                WHERE archive LIKE "Активний" AND district = ? AND date_id = ?""";
        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(getTestPointsQuery)
        ) {
            stmt.setInt(1, eduCityId);
            stmt.setInt(2, testDayId);
            try (ResultSet rs = stmt.executeQuery()) {
                testPoints.clear();
                while (rs.next()) {
                    testPoints.add(
                            new TestPoint(
                                    rs.getInt("id"),
                                    rs.getInt("pt_id"),
                                    rs.getString("name").replace("&quot;", "\""),
                                    rs.getString("short_name").replace("&quot;", "\""),
                                    rs.getInt("auds")
                            ));
                }
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }


    void getSubjectLangList(ArrayList<SubjectLang> subjectLangList, int eduCityId, int testDayId) {
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
        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(getSubjectLangQuery)
        ) {
            stmt.setInt(1, testDayId);
            stmt.setInt(2, eduCityId);
            try (ResultSet rs = stmt.executeQuery()) {
                subjectLangList.clear();
                while (rs.next()) {
                    subjectLangList.add(
                            new SubjectLang(
                                    rs.getInt("subject_id"),
                                    rs.getInt("lang_id"),
                                    rs.getString("subject_name"),
                                    rs.getString("lang_name"),
                                    Integer.parseInt(rs.getString("NUM")),
                                    eduCityId
                            ));
                }
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }


    private void createEmptyClassrooms(int testPointId, int audsNumToCreate) {
        final String newAudQuery = """
                INSERT INTO auds_list (testpoint_id, aud_num, predmet_id, lang_id, free, code)
                VALUES(?, ?, ?, ?, ?, ?)""";
        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement updateStmt = connection.prepareStatement(newAudQuery)
        ) {
            for (int audIndex = 1; audIndex <= audsNumToCreate; audIndex++) {
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


    void getAuditoryList(ArrayList<Classroom> classrooms, int testPointId, int audsNum) {
        String getAuditoryListCountQuery = """
                SELECT COUNT(id)
                FROM `auds_list`
                WHERE testpoint_id = ?""";
        String getAuditoryListQuery = """
                SELECT *
                FROM `auds_list`
                WHERE testpoint_id = ?""";

        int countQueryResult = -1;

        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement countStmt = connection.prepareStatement(getAuditoryListCountQuery);
                PreparedStatement listStmt = connection.prepareStatement(getAuditoryListQuery)
        ) {
            countStmt.setInt(1, testPointId);
            try (ResultSet rs = countStmt.executeQuery()) {
                while (rs.next()) {
                    countQueryResult = rs.getInt(1);
                }
            }

            if ((countQueryResult != -1) && (countQueryResult != audsNum)) {
                System.err.println("countQueryResult != audsNum: " + countQueryResult + " " + audsNum);
                createEmptyClassrooms(testPointId, audsNum);
            }

            listStmt.setInt(1, testPointId);
            try (ResultSet rs = listStmt.executeQuery()) {
                classrooms.clear();
                while (rs.next()) {
                    classrooms.add(
                            new Classroom(
                                    rs.getInt("id"),
                                    rs.getInt("testpoint_id"),
                                    rs.getInt("aud_num"),
                                    rs.getInt("predmet_id"),
                                    rs.getInt("lang_id"),
                                    rs.getInt("free"),
                                    rs.getString("code")
                            ));
                }
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }



    void updateSubjectLangIdsInAud(int testPointId, int audsNum) {

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
                PreparedStatement updateStmt = connection.prepareStatement(updateSubjectLangQuery)
        ) {
            for (int audIndex = 1; audIndex <= audsNum; audIndex++) {
                stmt.setInt(1, testPointId);
                stmt.setInt(2, audIndex);
                try (ResultSet rs = stmt.executeQuery()) {
                    int currentSubjectId;
                    int currentLangId;

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
                            audLangId = 9;                                          //Multilingual - UKR + RUS
                        } else {
                            audLangId = currentLangId;
                        }
                    }

                    System.out.println(audIndex + ". audSubjectId = " + audSubjectId + "; audLangId = " + audLangId);

                    if ((audSubjectId == -1) || (audLangId == -1)) {
                        System.err.println("Error! Classroom #" + audIndex + " is empty");
                        return;
                    }

                    updateStmt.setInt(1, audSubjectId);
                    updateStmt.setInt(2, audLangId);
                    updateStmt.setInt(3, testPointId);
                    updateStmt.setInt(4, audIndex);
                    updateStmt.executeUpdate();
                }
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }


    void link(int testPointIdToLink, int audNumToLink, int numPupilsToLink, SubjectLang subjectLangToLink) {

        final String newAllocationRow = """
                INSERT INTO allocation (test_id, testpoint_id, aud, place, test_code)
                VALUES (?, ?, ?, ?, ?)""";

        if (audNumToLink <= 0) {
            System.err.println("No classroom selected");
            return;
        }

        if (numPupilsToLink <= 0) {
            System.err.println("You selected 0 pupils!");
            return;
        }

        //Если выбрано для рассадки людей больше чем есть то завершаем работу функции
        if (numPupilsToLink > subjectLangToLink.getNumPupils()) {
            System.err.println("numPupilsToLink > subjectLangToLink.numPupils");
            return;
        }
        //Заполненость аудитории по allocation
        int numPupilsInThisAud = getTotalPeoplesInAud(testPointIdToLink, audNumToLink);
        //Количество свободных мест в аудитории по aud_list
        int totalFreePlacesInAud = getTotalFreePlacesInAud(testPointIdToLink, audNumToLink);
        List<Integer> pupilsIdToLink = getFirstNPeoplesToLink(subjectLangToLink, numPupilsToLink);

        //Если получается больше 15 человек в аудитории
        if (numPupilsToLink + numPupilsInThisAud > 15) {
            System.err.println("Classroom already full!");
            return;
        }

        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement newAllocationStmt = connection.prepareStatement(newAllocationRow)
        ) {
            //Записали в allocation
            for (int humanId : pupilsIdToLink) {
                newAllocationStmt.setInt(1, humanId);
                newAllocationStmt.setInt(2, testPointIdToLink);
                newAllocationStmt.setInt(3, audNumToLink);
                newAllocationStmt.setInt(4, ++numPupilsInThisAud);
                String testCode = (testPointIdToLink + humanId) % 10 +
                                  Integer.toString(testPointIdToLink + 1000).substring(1) +
                                  Integer.toString(humanId + 100000).substring(1);
                newAllocationStmt.setString(5, testCode);
                newAllocationStmt.executeUpdate();
            }

            setTotalFreePlacesInAud(
                    testPointIdToLink,
                    audNumToLink,
                    totalFreePlacesInAud - numPupilsToLink
            );

        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }


    //Выбираем н человек из базы
    private List<Integer> getFirstNPeoplesToLink(SubjectLang subjectLang, int peoplesNumber) {
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
        ArrayList<Integer> peoples = new ArrayList<>();
        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement getNPupilsStmt = connection.prepareStatement(getNPupils)
        ) {
            getNPupilsStmt.setInt(1, subjectLang.getSubjectId());
            getNPupilsStmt.setInt(2, subjectLang.getLangId());
            getNPupilsStmt.setInt(3, subjectLang.getEduCityId());
            getNPupilsStmt.setInt(4, peoplesNumber);
            try (ResultSet rs = getNPupilsStmt.executeQuery()) {
                while (rs.next()) {
                    peoples.add(rs.getInt("id"));
                }
                System.out.println("Linking peoples with this ids: " + peoples);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return peoples;
    }


    private int getTotalFreePlacesInAud(int testPointId, int audIndex) {
        final String getFreeInAudQuery = """
                SELECT free
                FROM auds_list
                WHERE testpoint_id = ? AND aud_num = ?""";
        int totalFreePlaces = 0;
        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement getFreeInAudStmt = connection.prepareStatement(getFreeInAudQuery)
        ) {
            getFreeInAudStmt.setInt(1, testPointId);
            getFreeInAudStmt.setInt(2, audIndex);
            try (ResultSet rs = getFreeInAudStmt.executeQuery()) {
                while (rs.next()) {
                    totalFreePlaces = rs.getInt("free");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalFreePlaces;
    }


    private void setTotalFreePlacesInAud(int testPointId, int audIndex, int freePlacesToSet) {
        final String setFreeInAudQuery = """
                UPDATE auds_list
                SET free = ?
                WHERE testpoint_id = ? AND aud_num = ?""";
        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement setFreeInAudStmt = connection.prepareStatement(setFreeInAudQuery)
        ) {
            setFreeInAudStmt.setInt(1, freePlacesToSet);
            setFreeInAudStmt.setInt(2, testPointId);
            setFreeInAudStmt.setInt(3, audIndex);
            setFreeInAudStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private int getTotalPeoplesInAud(int testPointId, int audIndex) {
        final String getNumPupilsInThisAud = """
                SELECT MAX(place)
                FROM `allocation`
                WHERE testpoint_id = ? AND aud = ?""";
        int totalPeoplesInAud = 0;
        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement getNumPupilsInThisAudStmt = connection.prepareStatement(getNumPupilsInThisAud)
        ) {
            getNumPupilsInThisAudStmt.setInt(1, testPointId);
            getNumPupilsInThisAudStmt.setInt(2, audIndex);
            try (ResultSet rs = getNumPupilsInThisAudStmt.executeQuery()) {
                while (rs.next()) {
                    totalPeoplesInAud = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalPeoplesInAud;
    }


    private Connection getDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}