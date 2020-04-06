import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class App {
  private static final Logger log = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    try {
      loadJdbcDriver();
      Connection connection = getConnection();
      createTable(connection);

      String insertSql = "INSERT INTO employees(name) VALUES(?)";
      PreparedStatement insertStmnt = connection.prepareStatement(insertSql);
      insert(insertStmnt, "sas");
      insert(insertStmnt, "dut");
      insertStmnt.close();

      List<String> names = select(connection);
      log.info("Names = {}", names);

      connection.close();
    } catch (Exception e) {
      log.error("Unexpected", e);
    }
  }

  static void loadJdbcDriver() throws ClassNotFoundException {
    // should load automatically, but just to be safe
    Class.forName("org.h2.Driver");
    log.info("Loaded driver");
  }

  static Connection getConnection() throws SQLException {
    Properties properties = new Properties();
    properties.setProperty("javax.persistence.jdbc.user", "sa");
    properties.setProperty("javax.persistence.jdbc.password", "");

    Connection connection =
        DriverManager.getConnection("jdbc:h2:mem:test", properties);
    log.info("created connection");

    return connection;
  }

  static void createTable(Connection connection) throws SQLException {
    String tableSql = "CREATE TABLE IF NOT EXISTS employees"
        + "(emp_id int PRIMARY KEY AUTO_INCREMENT, name varchar(30))";

    executeUpdateOnce(connection.prepareStatement(tableSql));
  }

  static int insert(PreparedStatement statement, String name) throws SQLException {
    log.info("Inserting {}", name);
    statement.setString(1, name);
    return statement.executeUpdate();
  }

  static int executeUpdateOnce(PreparedStatement statement) throws SQLException {
    int count = statement.executeUpdate();
    statement.close();
    return count;
  }

  static List<String> select(Connection connection) throws SQLException {
    String selectSql = "SELECT name FROM employees";
    Statement stmt = connection.createStatement();
    ResultSet resultSet = stmt.executeQuery(selectSql);

    List<String> results = new ArrayList<>();
    while (resultSet.next()) {
      results.add(resultSet.getString("name"));
    }

    resultSet.close();
    stmt.close();
    return results;
  }
}
