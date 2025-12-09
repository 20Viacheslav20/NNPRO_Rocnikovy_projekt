ALTER TABLE users
DROP CONSTRAINT IF EXISTS users_role_check;

UPDATE users
SET role = 'USER'
WHERE role IN ('SYSTEM_USER');

ALTER TABLE users
ADD CONSTRAINT users_role_check
CHECK (role IN ('ADMIN', 'PROJECT_MANAGER', 'USER'));
