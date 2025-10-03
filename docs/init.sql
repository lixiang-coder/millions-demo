-- auto-generated definition
create table salaries
(
    emp_no    int  not null,
    salary    int  not null,
    from_date date not null,
    to_date   date not null,
    primary key (emp_no, from_date)
)
    collate = utf8mb4_unicode_ci;

create index idx_emp_no
    on salaries (emp_no);