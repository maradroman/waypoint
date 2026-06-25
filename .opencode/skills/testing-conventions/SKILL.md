---
name: testing-conventions
description: Use when writing or editing tests for the Waypoint API (waypoint-api). Covers JUnit 5, AssertJ, Mockito patterns, test data organization, JPA repository testing, and controller slicing.
---

# Testing Conventions: Waypoint API

## Overview

This skill enforces consistent, maintainable tests for `waypoint-api`. All tests follow profile-based configuration (`test` profile with H2), centralized test data, and clear assertion patterns.

---

## 🚨 CRITICAL RULES

### 1. NO Inline Builders — Use TestData* Classes Exclusively

Never use `.builder()` directly in test code. Create specialized builder methods in `TestData*` classes.

```java
// ❌ FORBIDDEN
var goal = Goal.builder().title("Save").build();

// ✅ CORRECT
var goal = goalWithTitle("Save");
```

**Rule**: If you type `.builder()` in a test file, stop and create a builder method in the appropriate `TestData*` class instead.

### 2. NO Lambdas — Use Method References

```java
// ❌ WRONG
.extracting(g -> g.getTitle())

// ✅ CORRECT
.extracting(Goal::getTitle)
```

### 3. Use `.extracting()` for Multiple Field Assertions

```java
// ❌ WRONG — multiple separate assertions
assertThat(actual.getTitle()).isEqualTo("Save");
assertThat(actual.getCost()).isEqualTo(5000);

// ✅ CORRECT
assertThat(actual)
    .extracting(Goal::getTitle, Goal::getCost)
    .containsExactly("Save", 5000);
```

### 4. Realistic Test Data Only

```java
// ❌ WRONG — fake data
public static final String GOAL_TITLE = "Test";
public static final String EMAIL = "test@test.com";

// ✅ CORRECT — realistic data
public static final String GOAL_TITLE = "Emergency Fund";
public static final String EMAIL = "alice@example.com";
```

### 5. No Section Separator Comments in TestData Classes

```java
// ❌ WRONG
// ==================== Goal Builders ====================

// ✅ just methods
```

---

## Test Data Organization

### 1. `TestDataConstant.java` — All Constants

- `@UtilityClass` from Lombok, all fields `public static final`
- `SCREAMING_SNAKE_CASE` with domain prefixes:
  - `GOAL_*`, `MILESTONE_*`, `DEPOSIT_*`, `TRANSFER_*`, `USER_*`, `AUTH_*`
- Use realistic values (real emails, real names, real amounts in cents)

### 2. `TestData*Entity.java` — JPA Entity Builders

- `@UtilityClass` with static factory methods
- Name pattern: `build{Entity}(String id)` or `build{Entity}With{Variant}(...)`
- Use constants from `TestDataConstant` for defaults
- Set required audit/relationship fields

### 3. `TestData*Dto.java` — DTO Builders

- `@UtilityClass` with static factory methods
- Name pattern: `{domain}Dto()` or `{domain}DtoWith{Variant}()`
- Edge-case variants: `*WithNo*`, `*WithBlank*`, `*WithAllFields*`

### 4. `TestDataJpa.java` — JPA Test Base Class

- `@DataJpaTest`, `@AutoConfigureTestDatabase(replace = Replace.NONE)` (uses H2 via `test` profile)
- `@ActiveProfiles("test")`
- `protected EntityManager em`
- `@BeforeEach void clearAllData()` — clean slate
- Typed `persist*` helpers that flush immediately

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
public abstract class TestDataJpa {
    @Autowired protected EntityManager em;

    @BeforeEach
    void clearAllData() {
        em.createQuery("DELETE FROM Completion").executeUpdate();
        em.createQuery("DELETE FROM Transfer").executeUpdate();
        em.createQuery("DELETE FROM Milestone").executeUpdate();
        em.createQuery("DELETE FROM Deposit").executeUpdate();
        em.createQuery("DELETE FROM Goal").executeUpdate();
        em.createQuery("DELETE FROM RefreshToken").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.flush();
    }

    protected void flushAndClear() {
        em.flush();
        em.clear();
    }
}
```

---

## Test Structure & Style

### Naming

- **Class**: `{ClassUnderTest}Test` — e.g. `GoalServiceTest`, `GoalControllerTest`
- **Method**: `method_scenario_expectedOutcomeTest` — e.g. `findById_returnsGoal_whenExists`, `createGoal_throws_whenTitleBlank`
- **Always end with `Test`**

### Use `@Nested` for Grouping

```java
class GoalServiceTest {
    @Nested
    class CreateGoal {
        @Test
        void createGoal_savesAndReturnsGoalTest() { ... }
    }
}
```

### Test Order in Each Class

1. Guard clause tests (null, blank, invalid)
2. Happy path tests
3. Edge case tests (boundary, uniqueness)
4. Integration scenarios

### Use @DisplayName, NOT Inline Comments

- No `// given`, `// when`, `// then`
- Use `@DisplayName` + descriptive method name instead

---

## Mocking Conventions

### Plain Unit Tests — Use `@Mock` + `@InjectMocks`

```java
@ExtendWith(MockitoExtension.class)
class GoalServiceTest {
    @Mock private GoalRepository goalRepository;
    @Mock private MilestoneService milestoneService;
    @InjectMocks private GoalService goalService;
}
```

### Spring Slice Tests — Use `@MockitoBean`

```java
@WebMvcTest(GoalController.class)
@ActiveProfiles("test")
class GoalControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private GoalService goalService;
}
```

Never use `Mockito.mock()` programmatically.

---

## Assertion Patterns (AssertJ Only)

- Always use AssertJ: `import static org.assertj.core.api.Assertions.*`
- Never use JUnit assertions (`assertEquals`, `assertTrue`, etc.)
- Chain assertions on the same subject

### Variable Naming

```java
// ✅ CORRECT
var actualResult = goalService.findById(id);

// ❌ WRONG
var goal = goalService.findById(id);
```

Use `actualResult` for the method-under-test result. Use descriptive names for setup data (`request`, `expected`, `mockGoal`).

### Common Assertions

```java
// Null
assertThat(mapper.map(null)).isNull();

// Optional
assertThat(repository.findById(id)).isPresent();

// Collection — always method references
assertThat(actualResult)
    .extracting(Goal::getTitle)
    .containsExactly("Save");

// Multiple fields
assertThat(actualResult)
    .extracting(Goal::getTitle, Goal::getCost)
    .containsExactly("Save", 5000);

// Timestamps
assertThat(actualResult.getCreatedAt())
    .isNotNull()
    .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
```

---

## Profile-Aware Testing

All tests use `@ActiveProfiles("test")`. The `test` profile provides:
- H2 in-memory database (via `application-test.yml`)
- JPA `ddl-auto: create-drop`
- No Flyway migrations

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class GoalRepositoryTest extends TestDataJpa {
    private final GoalRepository goalRepository;

    GoalRepositoryTest(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }
}
```

---

## JPA Repository Testing

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class GoalRepositoryTest extends TestDataJpa {
    private final GoalRepository goalRepository;

    GoalRepositoryTest(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    @Test
    void findByUserId_returnsGoalsForUserTest() {
        var user = persistUser(USER_ID);
        persistGoal(GOAL_ID, user, "Emergency Fund");
        flushAndClear();

        var actualResult = goalRepository.findByUserId(USER_ID);

        assertThat(actualResult)
            .extracting(Goal::getTitle)
            .containsExactly("Emergency Fund");
    }
}
```

---

## Controller / MockMvc Testing

```java
@WebMvcTest(GoalController.class)
@ActiveProfiles("test")
class GoalControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private GoalService goalService;

    @Test
    void getGoal_returns200_whenGoalExistsTest() throws Exception {
        when(goalService.findGoalForUser(any(), any())).thenReturn(goalDto());

        mockMvc.perform(get("/goals/{id}", GOAL_ID)
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(GOAL_TITLE));
    }
}
```

---

## Static Imports Convention

```java
// AssertJ
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// TestData builders — wildcard is OK
import static io.github.maradroman.waypointapi.testdata.TestDataGoal.*;
import static io.github.maradroman.waypointapi.testdata.TestDataGoalDto.*;

// TestDataConstant — EXPLICIT imports only, never wildcard
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_TITLE;
```

---

## Best Practices Checklist

1. ✓ **NO `.builder()` in test code** — always use `TestData*` builder methods
2. ✓ **Method references** over lambdas in `.extracting()` and streams
3. ✓ **Single `.extracting()`** for multiple field assertions instead of separate `assertThat()` calls
4. ✓ **Realistic test data** — real names, emails, amounts
5. ✓ **`actualResult`** for method-under-test result variable
6. ✓ **`@Mock` + `@InjectMocks` + `@ExtendWith(MockitoExtension.class)`** — never `Mockito.mock()`
7. ✓ **`@MockitoBean`** in `@WebMvcTest` slice tests
8. ✓ **AssertJ only** — never JUnit assertions
9. ✓ **Chained assertions** on the same subject
10. ✓ **`@ActiveProfiles("test")`** on all test classes
11. ✓ **No section separator comments** in `TestData*` classes
12. ✓ **`@DisplayName`** instead of `// given` / `// when` / `// then` comments
13. ✓ **Explicit imports** for `TestDataConstant` — never wildcard
