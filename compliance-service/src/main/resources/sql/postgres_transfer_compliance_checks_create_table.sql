CREATE SEQUENCE IF NOT EXISTS transfer_compliance_check_number_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS transfer_compliance_checks (
    id                                  UUID PRIMARY KEY,
    check_number                        VARCHAR(64) NOT NULL,
    external_id                         UUID NOT NULL,
    branch_code                         VARCHAR(32) NOT NULL,
    client_name                         TEXT NOT NULL,
    contract_type_code                  VARCHAR(32) NOT NULL,
    contract_number                     VARCHAR(128) NOT NULL,
    client_tin                          VARCHAR(32),
    enterprise_registration_date        DATE,
    activity_type                       TEXT,
    contract_date                       DATE,
    contract_idn                        VARCHAR(128),
    foreign_partner_name                TEXT,
    foreign_partner_tin                 VARCHAR(64),
    director_and_related_parties        TEXT,
    foreign_partner_country_code        VARCHAR(8),
    foreign_partner_bank                TEXT,
    bic_swift                           VARCHAR(32),
    foreign_partner_bank_country_code   VARCHAR(8),
    goods_name                          TEXT,
    goods_tnved_code                    VARCHAR(64),
    goods_origin_country_code           VARCHAR(8),
    consignor                           TEXT,
    consignee                           TEXT,
    currency_code                       VARCHAR(8),
    operation_amount                    NUMERIC(20, 2),
    last_operation_date                 DATE,
    fea_officer_full_name               VARCHAR(256),
    decision_note                       TEXT,
    description                         TEXT,
    status                              VARCHAR(16) NOT NULL,
    checked_by                          VARCHAR(255),
    checked_date                        TIMESTAMP,
    created_date                        TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_transfer_compliance_checks_check_number
    ON transfer_compliance_checks (check_number);

CREATE UNIQUE INDEX IF NOT EXISTS idx_transfer_compliance_checks_external_id
    ON transfer_compliance_checks (external_id);

CREATE INDEX IF NOT EXISTS idx_transfer_compliance_checks_status
    ON transfer_compliance_checks (status);

CREATE INDEX IF NOT EXISTS idx_transfer_compliance_checks_created_date
    ON transfer_compliance_checks (created_date);

CREATE INDEX IF NOT EXISTS idx_transfer_compliance_checks_branch_code
    ON transfer_compliance_checks (branch_code);

CREATE INDEX IF NOT EXISTS idx_transfer_compliance_checks_contract_number
    ON transfer_compliance_checks (contract_number);

CREATE INDEX IF NOT EXISTS idx_transfer_compliance_checks_client_tin
    ON transfer_compliance_checks (client_tin);
