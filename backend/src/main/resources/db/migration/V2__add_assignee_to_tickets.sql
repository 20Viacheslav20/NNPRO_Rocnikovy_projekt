ALTER TABLE tickets
ADD COLUMN assignee_id uuid NULL;

ALTER TABLE tickets
ADD CONSTRAINT fk_tickets_assignee
FOREIGN KEY (assignee_id) REFERENCES users(id);
