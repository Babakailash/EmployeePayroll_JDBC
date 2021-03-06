package com.jdbc;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeePayrollDBService {

        private PreparedStatement employeePayrollDataStatement;
        private static EmployeePayrollDBService employeePayrollDBService;
        private EmployeePayrollDBService() {
        }

        public static EmployeePayrollDBService getInstance() {
            if (employeePayrollDBService == null)
                employeePayrollDBService = new EmployeePayrollDBService();
            return employeePayrollDBService;
        }


        private Connection getConnection() throws SQLException {
        String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
        String userName = "root";
        String password = "root";
        Connection connection;
        System.out.println("Connecting to DATABase " +jdbcURL);
        connection = DriverManager.getConnection(jdbcURL,userName,password);
        System.out.println("Connection Successful:" +connection);
        return connection;
    }


    public List<EmployeePayrollData> readData() {
        String sql = "SELECT * FROM employee_payroll;";
        return this.getEmployeePayrollDataUsingDB(sql);
    }

    private List<EmployeePayrollData> getEmployeePayrollDataUsingDB(String sql) {
        List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            employeePayrollList =this.getEmployeePayrollData(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeePayrollList;
    }

    public List<EmployeePayrollData> getEmployeePayrollForDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = String.format("SELECT * FROM employee_payroll where start between '%s' and '%s';",
                Date.valueOf(startDate), Date.valueOf(endDate));
        return this.getEmployeePayrollDataUsingDB(sql);
    }


    public int updateEmployeeData(String name, double salary) {
        String sql = String.format("update employee_payroll set salary = %.2f where name = '%s';", salary, name);
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int updateEmployeeDataUsingPreparedStatement(String name, double salary) {
        try (Connection connection = this.getConnection()) {
            String sql = "update employee_payroll set salary=? where name=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1, salary);
            preparedStatement.setString(2, name);
            int result = preparedStatement.executeUpdate();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<EmployeePayrollData> getEmployeePayrollData(String name) {
        List<EmployeePayrollData> employeePayrollList = null;
        if (this.employeePayrollDataStatement == null)
            this.prepareStatementForEmployeeData();
        try {
            employeePayrollDataStatement.setString(1, name);
            ResultSet resultSet = employeePayrollDataStatement.executeQuery();
            employeePayrollList = this.getEmployeePayrollData(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeePayrollList;
    }

    private List<EmployeePayrollData> getEmployeePayrollData(ResultSet resultSet) {
        List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
        try {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double salary = resultSet.getDouble("salary");
                LocalDate startDate = resultSet.getDate("start").toLocalDate();
                employeePayrollList.add(new EmployeePayrollData(id, name, salary, startDate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeePayrollList;
    }

    private void prepareStatementForEmployeeData() {
        try {
            Connection connection = this.getConnection();
            String sql = "select * from employee_payroll where name = ?";
            employeePayrollDataStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Double> getAverageSalaryByGender() {
        String sql = "select gender,avg(salary) as avg from employee_payroll group by gender;";
        Map<String, Double> genderToAverageSalaryMap = new HashMap<>();
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String gender = resultSet.getString("gender");
                double salary = resultSet.getDouble("avg");
                genderToAverageSalaryMap.put(gender, salary);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genderToAverageSalaryMap;
    }
}
