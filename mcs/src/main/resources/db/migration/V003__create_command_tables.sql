CREATE TABLE T_COMMAND (
    ID BIGINT NOT NULL AUTO_INCREMENT,
    COMMAND TEXT NOT NULL,
    VERSION INT NOT NULL,
    ATTEMPTS INT NOT NULL,
    LOCKED BIGINT NOT NULL,
    IDEMPOTENCY_ID VARCHAR(36) NOT NULL,
    CONTEXT TEXT NOT NULL,

    PRIMARY KEY (ID)
);

