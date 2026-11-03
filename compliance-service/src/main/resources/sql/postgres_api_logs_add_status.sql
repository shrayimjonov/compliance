alter table api_logs
    add column if not exists status varchar(16);

update api_logs
set status = 'CREATED'
where status is null;

alter table api_logs
    alter column status set default 'CREATED',
    alter column status set not null;