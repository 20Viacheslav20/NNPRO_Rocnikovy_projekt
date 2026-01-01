CREATE TABLE ticket_comments (
    id UUID NOT NULL,
    ticket_id UUID NOT NULL,
    author_id UUID NOT NULL,
    text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_ticket_comments PRIMARY KEY (id),
    CONSTRAINT fk_ticket_comments_ticket
        FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_comments_author
        FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT
);
