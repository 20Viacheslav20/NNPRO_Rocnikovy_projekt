create table ticket_history (
    id uuid primary key,
    ticket_id uuid not null,
    author_id uuid not null,

    action varchar(30) not null,   -- CREATED, UPDATED, DELETED
    field varchar(50),             -- name, priority, state, etc
    old_value text,
    new_value text,

    created_at timestamptz not null default now(),

    constraint fk_ticket_history_ticket
        foreign key (ticket_id) references tickets(id) on delete cascade,

    constraint fk_ticket_history_author
        foreign key (author_id) references users(id)
);

create index idx_ticket_history_ticket
    on ticket_history(ticket_id);

create index idx_ticket_history_created_at
    on ticket_history(created_at desc);
