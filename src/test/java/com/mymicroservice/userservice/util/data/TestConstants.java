package com.mymicroservice.userservice.util.data;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;

@UtilityClass
public class TestConstants {

    public static final Long ENTITY_ID = 1L;
    public static final Long USER_ID = 1L;
    public static final Long SECOND_ENTITY_ID = 2L;
    public static final Long THIRD_ENTITY_ID = 3L;
    public static final Long NON_EXISTENT_ID = 999L;

    public static final String USER_EMAIL = "test@test.by";
    public static final String ADMIN_USER_EMAIL = "admin@test.by";
    public static final String NEW_USER_EMAIL = "new@test.by";
    public static final String UPDATED_USER_EMAIL = "updated_email@test.by";
    public static final String NON_EXISTENT_EMAIL = "nonexistent@example.com";

    public static final String USER_NAME = "Name";
    public static final String NEW_USER_NAME = "New Name";
    public static final String USER_SURNAME = "SurName";
    public static final String NEW_USER_SURNAME = "New SurName";

    public static final String USER_PASSWORD = "password";
    public static final String NEW_ENCODED_PASSWORD = "newEncodedPassword";

    public static final String CARD_NUMBER = "1111222233334444";
    public static final String SECOND_CARD_NUMBER = "1111222233335555";
    public static final String EXPIRED_CARD_NUMBER = "1111222233330000";
    public static final String HOLDER = "Test Holder";
    public static final String NEW_HOLDER = "New Holder";
    public static final String NON_EXISTENT_CARD_NUMBER = "non-existing-number";

    public static final LocalDate USER_BIRTH_DATE = LocalDate.of(2000, 2, 2);
    public static final LocalDate ADMIN_BIRTH_DATE = LocalDate.of(1985, 5, 5);
    public static final LocalDate BORN_AFTER_DATE = LocalDate.of(1995, 1, 1);
    public static final LocalDate BORN_AFTER_QUERY_DATE = LocalDate.of(1990, 1, 1);
    public static final LocalDate REPOSITORY_BORN_AFTER_DATE = LocalDate.of(2000, 1, 1);
    public static final LocalDate REPOSITORY_NO_RESULTS_DATE = LocalDate.of(2020, 1, 1);

    public static final LocalDate FUTURE_EXPIRATION_DATE = LocalDate.of(2030, 3, 3);
    public static final LocalDate EXPIRED_CARD_DATE = LocalDate.of(2024, 3, 3);
    public static final LocalDate SECOND_CARD_EXPIRATION_DATE = LocalDate.of(2025, 3, 3);

    public static final String INTERNAL_CALL_HEADER = "X-Internal-Call";
    public static final String INTERNAL_CALL_TRUE = "true";
    public static final String INTERNAL_CALL_FALSE = "false";

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int PAGINATION_PAGE_SIZE = 2;

    public static final String BCRYPT_PREFIX = "$2a$";
}
