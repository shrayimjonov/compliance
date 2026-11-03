alter table transfer_compliance_checks add column deleted boolean default false;

update transfer_compliance_checks
set deleted= false
where 1 = 1;

alter table transfer_compliance_checks
    ALTER
        column deleted set not null;