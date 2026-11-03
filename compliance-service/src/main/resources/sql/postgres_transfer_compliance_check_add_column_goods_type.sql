alter table transfer_compliance_checks
    add column if not exists goods_type varchar(16);

alter table transfer_compliance_checks
    drop constraint if exists chk_transfer_compliance_checks_goods_type;

alter table transfer_compliance_checks
    add constraint chk_transfer_compliance_checks_goods_type
        check (goods_type is null or goods_type in ('PRODUCT', 'SERVICE'));

create index if not exists idx_transfer_compliance_checks_goods_type
    on transfer_compliance_checks (goods_type);
