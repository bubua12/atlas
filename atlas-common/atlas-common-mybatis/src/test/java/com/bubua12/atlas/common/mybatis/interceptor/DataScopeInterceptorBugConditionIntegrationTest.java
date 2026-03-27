package com.bubua12.atlas.common.mybatis.interceptor;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bubua12.atlas.common.core.context.SecurityContextHolder;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.mybatis.annotation.DataScope;
import com.bubua12.atlas.common.mybatis.enums.DataScopeType;
import com.bubua12.atlas.common.mybatis.handler.DataScopeHandler;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Condition Exploration Integration Test for DataScopeInterceptor
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3, 2.1, 2.2**
 * 
 * **Property 1: Bug Condition** - 分页查询中数据权限条件丢失
 * 
 * CRITICAL: This test MUST FAIL on unfixed code - failure confirms the bug exists
 * 
 * Bug Condition:
 * WHEN executing a paginated query with @DataScope annotation
 * AND the DataScopeHandler generates a valid filter condition
 * THEN the final executed SQL SHOULD contain the data scope condition
 * BUT on unfixed code, the condition is lost after pagination processing
 * 
 * This test uses a real MyBatis environment with H2 database to accurately reproduce the bug.
 */
public class DataScopeInterceptorBugConditionIntegrationTest {

    private SqlSessionFactory sqlSessionFactory;
    private DataSource dataSource;
    private final ByteArrayOutputStream sqlLogCapture = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    /**
     * Test entity
     */
    @Data
    @TableName("test_user")
    public static class TestUser {
        @TableId
        private Long userId;
        private String username;
        private Long deptId;
        private Integer deleted;
    }

    /**
     * Test Mapper with @DataScope annotation
     */
    @Mapper
    public interface TestUserMapper extends BaseMapper<TestUser> {
        
        /**
         * Paginated query with @DataScope annotation
         * This is the bug condition: pagination + @DataScope
         */
        @DataScope(userAlias = "u", deptAlias = "u")
        @Select("SELECT u.* FROM test_user u WHERE u.deleted = 0")
        IPage<TestUser> selectUserPage(Page<TestUser> page);
        
        /**
         * Non-paginated query with @DataScope annotation
         * This is the baseline: no pagination, should work correctly
         */
        @DataScope(userAlias = "u", deptAlias = "u")
        @Select("SELECT u.* FROM test_user u WHERE u.deleted = 0")
        java.util.List<TestUser> selectUserList();
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Create H2 in-memory database
        dataSource = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:test-schema.sql")
            .addScript("classpath:test-data.sql")
            .build();

        // Setup MyBatis Configuration
        Environment environment = new Environment("test", new JdbcTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.setLogImpl(StdOutImpl.class); // Enable SQL logging
        configuration.addMapper(TestUserMapper.class);

        // Setup interceptors in the CURRENT (unfixed) order
        // 1. DataScopeInterceptor as standalone MyBatis interceptor
        DataScopeHandler dataScopeHandler = new DataScopeHandler();
        DataScopeInterceptor dataScopeInterceptor = new DataScopeInterceptor(dataScopeHandler);
        configuration.addInterceptor(dataScopeInterceptor);

        // 2. MybatisPlusInterceptor with PaginationInnerInterceptor
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        configuration.addInterceptor(mybatisPlusInterceptor);

        // Build SqlSessionFactory
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        // Setup LoginUser with SELF_ONLY data scope
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(112L);
        loginUser.setUsername("testuser");
        loginUser.setDeptId(5L);
        loginUser.setDataScope(DataScopeType.SELF_ONLY.getValue());
        SecurityContextHolder.setLoginUser(loginUser);

        // Capture SQL logs
        System.setOut(new PrintStream(sqlLogCapture));
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clear();
        System.setOut(originalOut);
        
        // Print captured SQL logs for debugging
        String sqlLogs = sqlLogCapture.toString();
        if (!sqlLogs.isEmpty()) {
            System.out.println("\n=== Captured SQL Logs ===");
            System.out.println(sqlLogs);
            System.out.println("=========================\n");
        }
    }

    /**
     * Test Case 1: BUG CONDITION - Paginated query with @DataScope loses data scope condition
     * 
     * This is the core bug condition test. It simulates:
     * 1. A Mapper method with @DataScope annotation
     * 2. A paginated query (Page parameter)
     * 3. A user with SELF_ONLY data scope (generates condition: u.user_id = 112)
     * 
     * Expected on UNFIXED code: FAILS - final SQL does NOT contain "u.user_id = 112"
     * Expected on FIXED code: PASSES - final SQL contains both data scope condition and LIMIT
     */
    @Test
    public void testBugCondition_PaginatedQueryWithDataScope_LosesCondition() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            TestUserMapper mapper = session.getMapper(TestUserMapper.class);
            
            // Execute paginated query with @DataScope
            Page<TestUser> page = new Page<>(1, 10);
            IPage<TestUser> result = mapper.selectUserPage(page);
            
            // Get captured SQL logs
            String sqlLogs = sqlLogCapture.toString();
            System.setOut(originalOut); // Restore output for assertions
            
            System.out.println("\n=== Test: Paginated Query with @DataScope ===");
            System.out.println("SQL Logs:\n" + sqlLogs);
            System.out.println("Result count: " + result.getRecords().size());
            System.out.println("Total: " + result.getTotal());
            
            // Extract the final executed SQL from logs
            String finalSql = extractFinalSqlFromLogs(sqlLogs);
            System.out.println("Final SQL: " + finalSql);
            
            // Expected data scope condition
            String expectedCondition = "user_id = 112";
            
            // CRITICAL ASSERTION: Verify data scope condition is preserved in final SQL
            // On UNFIXED code, this will FAIL - proving the bug exists
            // On FIXED code, this will PASS - confirming the fix works
            assertTrue(finalSql.contains(expectedCondition) || finalSql.contains("user_id=112"),
                "BUG DETECTED: Data scope condition lost after pagination!\n" +
                "Expected condition: " + expectedCondition + "\n" +
                "Final SQL: " + finalSql + "\n" +
                "The pagination interceptor overwrote the data scope condition.\n" +
                "Full SQL Logs:\n" + sqlLogs);
            
            // Verify pagination was applied
            assertTrue(finalSql.contains("LIMIT") || finalSql.contains("limit"),
                "Final SQL should contain LIMIT clause for pagination\nFinal SQL: " + finalSql);
            
            // Verify result only contains user 112's data
            for (TestUser user : result.getRecords()) {
                assertEquals(112L, user.getUserId(),
                    "Result should only contain user 112's data due to data scope filtering");
            }
        }
    }

    /**
     * Test Case 2: BASELINE - Non-paginated query with @DataScope works correctly
     * 
     * This test verifies that non-paginated queries work correctly on unfixed code.
     * It serves as a baseline to confirm the bug is specific to pagination.
     * 
     * Expected: PASSES on both unfixed and fixed code
     */
    @Test
    public void testBaseline_NonPaginatedQueryWithDataScope_WorksCorrectly() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            TestUserMapper mapper = session.getMapper(TestUserMapper.class);
            
            // Execute non-paginated query with @DataScope
            java.util.List<TestUser> result = mapper.selectUserList();
            
            // Get captured SQL logs
            String sqlLogs = sqlLogCapture.toString();
            System.setOut(originalOut); // Restore output for assertions
            
            System.out.println("\n=== Test: Non-Paginated Query with @DataScope ===");
            System.out.println("SQL Logs:\n" + sqlLogs);
            System.out.println("Result count: " + result.size());
            
            // Extract the final executed SQL from logs
            String finalSql = extractFinalSqlFromLogs(sqlLogs);
            System.out.println("Final SQL: " + finalSql);
            
            // Expected data scope condition
            String expectedCondition = "user_id = 112";
            
            // Verify data scope condition is present
            assertTrue(finalSql.contains(expectedCondition) || finalSql.contains("user_id=112"),
                "Non-paginated query should contain data scope condition: " + expectedCondition +
                "\nFinal SQL: " + finalSql +
                "\nFull SQL Logs:\n" + sqlLogs);
            
            // Should NOT contain LIMIT (no pagination)
            assertFalse(finalSql.contains("LIMIT") && finalSql.contains("limit"),
                "Non-paginated query should NOT contain LIMIT clause\nFinal SQL: " + finalSql);
            
            // Verify result only contains user 112's data
            for (TestUser user : result) {
                assertEquals(112L, user.getUserId(),
                    "Result should only contain user 112's data due to data scope filtering");
            }
        }
    }

    /**
     * Helper: Extract the final executed SQL from MyBatis logs
     */
    private String extractFinalSqlFromLogs(String logs) {
        // MyBatis logs SQL in format: "==>  Preparing: SELECT ..."
        String[] lines = logs.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i];
            if (line.contains("Preparing:")) {
                return line.substring(line.indexOf("Preparing:") + 10).trim();
            }
        }
        return "";
    }
}
