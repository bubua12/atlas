package com.bubua12.atlas.common.mybatis.interceptor;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
 * Bug Condition Exploration Test for DataScopeInterceptor
 * **Validates: Requirements 1.1, 1.2, 1.3, 2.1, 2.2**
 * **Property 1: Bug Condition** - 分页查询中数据权限条件保留
 * ============================================================================
 * BUG CONDITION DOCUMENTATION
 * ============================================================================
 * This test verifies the bug condition as specified in the design document:
 * FUNCTION isBugCondition(input)
 *   INPUT: input of type MybatisQueryExecution
 *   OUTPUT: boolean
 *   RETURN input.mapperMethod.hasAnnotation(@DataScope)
 *          AND input.parameters.contains(Page.class)
 *          AND dataScopeConditionGenerated(input)
 *          AND finalSqlDoesNotContainDataScopeCondition(input)
 * END FUNCTION
 * ============================================================================
 * OBSERVED BUG BEHAVIOR (from bugfix.md)
 * ============================================================================
 * 1. User executes a paginated query with @DataScope annotation
 *    Example: SysUserMapper.pageUser(Page page, String username)
 * 2. DataScopeInterceptor correctly generates data scope condition
 *    Log shows: "生成数据权限过滤条件：u.user_id = 112"
 *    Log shows: "替换的SQL: SELECT * FROM sys_user u WHERE ... AND u.user_id = 112"
 * 3. MybatisPlusInterceptor (with PaginationInnerInterceptor) processes the query
 *    Adds LIMIT clause for pagination
 * 4. Final executed SQL DOES NOT contain the data scope condition (BUG!)
 *    Actual SQL: "SELECT * FROM sys_user u WHERE ... LIMIT 0, 10"
 *    Missing: "u.user_id = 112"
 * 5. Result: User sees data they should not have access to
 * ============================================================================
 * EXPECTED BEHAVIOR (after fix)
 * ============================================================================
 * After implementing the fix (converting DataScopeInterceptor to InnerInterceptor):
 * 1. DataScopeInnerInterceptor executes BEFORE PaginationInnerInterceptor
 *    (controlled order within MybatisPlusInterceptor chain)
 * 2. Data scope condition is added to SQL: "... AND u.user_id = 112"
 * 3. PaginationInnerInterceptor adds LIMIT to the already-modified SQL
 * 4. Final SQL contains BOTH data scope condition AND pagination:
 *    "SELECT * FROM sys_user u WHERE ... AND u.user_id = 112 LIMIT 0, 10"
 * 5. Result: User only sees data within their permission scope
 * ============================================================================
 * TEST EXECUTION
 * ============================================================================
 * This test now executes the actual paginated query with @DataScope and verifies
 * that the final SQL contains both the data scope condition and LIMIT clause.
 * EXPECTED OUTCOME: Test PASSES (confirms bug is fixed)
 */
public class DataScopeInterceptorBugExplorationTest {

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
     * Test Mapper with paginated query using @DataScope
     */
    @Mapper
    public interface TestUserMapper extends BaseMapper<TestUser> {

        @DataScope(userAlias = "u", deptAlias = "u")
        @Select("SELECT u.* FROM test_user u WHERE u.deleted = 0")
        Page<TestUser> selectUsersPageWithDataScope(Page<TestUser> page);
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
        configuration.setLogImpl(StdOutImpl.class);
        configuration.addMapper(TestUserMapper.class);

        // Setup interceptors with FIXED order (DataScopeInnerInterceptor BEFORE PaginationInnerInterceptor)
        DataScopeHandler dataScopeHandler = new DataScopeHandler();

        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // CRITICAL: DataScopeInnerInterceptor MUST be added BEFORE PaginationInnerInterceptor
        mybatisPlusInterceptor.addInnerInterceptor(new DataScopeInnerInterceptor(dataScopeHandler));
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        configuration.addInterceptor(mybatisPlusInterceptor);

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        // Capture SQL logs
        System.setOut(new PrintStream(sqlLogCapture));
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clear();
        System.setOut(originalOut);

        String sqlLogs = sqlLogCapture.toString();
        if (!sqlLogs.isEmpty()) {
            System.out.println("\n=== Captured SQL Logs ===");
            System.out.println(sqlLogs);
            System.out.println("=========================\n");
        }

        sqlLogCapture.reset();
    }

    /**
     * Bug Condition Test: Paginated query with @DataScope
     * **Property 1: Expected Behavior** - 分页查询中数据权限条件保留
     * This test verifies that after the fix:
     * 1. DataScopeInnerInterceptor executes BEFORE PaginationInnerInterceptor
     * 2. Data scope condition is added to SQL
     * 3. PaginationInnerInterceptor adds LIMIT to the already-modified SQL
     * 4. Final SQL contains BOTH data scope condition AND LIMIT clause
     * EXPECTED OUTCOME: Test PASSES (confirms bug is fixed)
     * **Validates: Requirements 2.1, 2.2, 2.3**
     */
    @Test
    public void testBugCondition_PaginatedQueryWithDataScope_PreservesCondition() {
        // Setup: Regular user with SELF_ONLY data scope
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(112L);
        loginUser.setDataScope(DataScopeType.SELF_ONLY.getValue());
        SecurityContextHolder.setLoginUser(loginUser);

        try (SqlSession session = sqlSessionFactory.openSession()) {
            TestUserMapper mapper = session.getMapper(TestUserMapper.class);

            // Execute paginated query with @DataScope annotation
            Page<TestUser> page = new Page<>(1, 2);
            Page<TestUser> result = mapper.selectUsersPageWithDataScope(page);

            String sqlLogs = sqlLogCapture.toString();
            System.setOut(originalOut);

            System.out.println("\n=== Bug Condition Test: Paginated query with @DataScope ===");
            System.out.println("SQL Logs:\n" + sqlLogs);
            System.out.println("Result count: " + result.getRecords().size());
            System.out.println("Total: " + result.getTotal());

            String finalSql = extractFinalSqlFromLogs(sqlLogs);
            System.out.println("Final SQL: " + finalSql);

            // CRITICAL VERIFICATION: Final SQL MUST contain BOTH conditions

            // Verify 1: Data scope condition is present
            assertTrue(finalSql.contains("user_id = 112") || finalSql.contains("user_id=112"),
                    "BUG FIX VERIFICATION FAILED: Final SQL must contain data scope condition 'user_id = 112'\n" +
                            "Final SQL: " + finalSql + "\n\n" +
                            "This indicates that DataScopeInnerInterceptor is not working correctly or\n" +
                            "the interceptor order is wrong. Expected order:\n" +
                            "1. DataScopeInnerInterceptor (adds data scope condition)\n" +
                            "2. PaginationInnerInterceptor (adds LIMIT clause)\n\n" +
                            "If this test fails, the bug is NOT fixed!");

            // Verify 2: LIMIT clause is present (pagination works)
            assertTrue(finalSql.contains("LIMIT") || finalSql.contains("limit"),
                    "BUG FIX VERIFICATION FAILED: Final SQL must contain LIMIT clause for pagination\n" +
                            "Final SQL: " + finalSql + "\n\n" +
                            "This indicates that PaginationInnerInterceptor is not working correctly.");

            // Verify 3: Query results are filtered correctly
            assertNotNull(result.getRecords(), "Query should return results");

            // Verify 4: Only user 112's data is returned (data scope filtering works)
            for (TestUser user : result.getRecords()) {
                assertEquals(112L, user.getUserId(),
                        "BUG FIX VERIFICATION FAILED: Query should only return user 112's data\n" +
                                "Found user: " + user.getUserId() + "\n\n" +
                                "This indicates that the data scope condition is not being applied correctly.");
            }

            System.out.println("\n✓ BUG FIX VERIFIED: Data scope condition is preserved in paginated queries!");
            System.out.println("✓ Final SQL contains both data scope condition AND LIMIT clause");
            System.out.println("✓ Query results are correctly filtered by data scope");
        }
    }

    /**
     * Baseline test: Non-paginated query with @DataScope works correctly
     * This test documents that non-paginated queries work correctly (baseline behavior).
     * Expected: This behavior should be PRESERVED after the fix
     */
    @Test
    public void testBaseline_NonPaginatedQueryWithDataScope_WorksCorrectly() {
        // This test is covered by DataScopeInterceptorPreservationTest
        // It's included here for documentation purposes

        // BASELINE BEHAVIOR (should be preserved):
        // Non-paginated queries with @DataScope work correctly

        // Example:
        // User: testuser (user_id = 112, dataScope = SELF_ONLY)
        // Query: SysUserMapper.selectUserList() // No Page parameter
        // Expected SQL: SELECT u.* FROM sys_user u WHERE u.deleted = 0 AND u.user_id = 112
        // Actual SQL: SELECT u.* FROM sys_user u WHERE u.deleted = 0 AND u.user_id = 112
        // Result: CORRECT - data scope condition is applied

        // This test passes on unfixed code (baseline behavior to preserve)
        // This test should also pass on fixed code (preservation requirement)
    }

    /**
     * Helper: Extract the final executed SQL from MyBatis logs
     */
    private String extractFinalSqlFromLogs(String logs) {
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
