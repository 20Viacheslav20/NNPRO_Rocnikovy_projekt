CREATE TABLE users (
	id uuid NOT NULL,
	created_at timestamptz(6) NOT NULL,
	email varchar(255) NOT NULL,
	"name" varchar(120) NOT NULL,
	password_hash text NOT NULL,
	password_changed_at timestamptz(6) NULL,
	reset_code varchar(64) NULL,
	reset_code_exp timestamptz(6) NULL,
	reset_token_id uuid NULL,
	"role" varchar(255) NULL,
	surname varchar(120) NOT NULL,
	username varchar(60) NOT NULL,
	CONSTRAINT users_pkey PRIMARY KEY (id),
	CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['SYSTEM_USER'::character varying, 'SYSTEM_ADMIN'::character varying, 'SYSTEM_RESEARCHER'::character varying])::text[]))),
	CONSTRAINT ux_users_email UNIQUE (email),
	CONSTRAINT ux_users_username UNIQUE (username)
);

CREATE TABLE projects (
	id uuid NOT NULL,
	created_at timestamptz(6) NOT NULL,
	description text NULL,
	"name" varchar(120) NOT NULL,
	status varchar(16) NOT NULL,
	owner_id uuid NOT NULL,
	CONSTRAINT projects_pkey PRIMARY KEY (id),
	CONSTRAINT projects_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'ARCHIVED'::character varying])::text[])))
);


CREATE TABLE tickets (
	id uuid NOT NULL,
	created_at timestamptz(6) NOT NULL,
	description text NULL,
	"name" varchar(160) NOT NULL,
	priority varchar(8) NOT NULL,
	state varchar(16) NOT NULL,
	"type" varchar(8) NOT NULL,
	project_id uuid NOT NULL,
	author_id uuid NOT NULL,
	CONSTRAINT tickets_pkey PRIMARY KEY (id),
	CONSTRAINT tickets_priority_check CHECK (((priority)::text = ANY ((ARRAY['low'::character varying, 'med'::character varying, 'high'::character varying])::text[]))),
	CONSTRAINT tickets_state_check CHECK (((state)::text = ANY ((ARRAY['open'::character varying, 'in_progress'::character varying, 'done'::character varying])::text[]))),
	CONSTRAINT tickets_type_check CHECK (((type)::text = ANY ((ARRAY['bug'::character varying, 'feature'::character varying, 'task'::character varying])::text[])))
);

ALTER TABLE tickets ADD CONSTRAINT fkjnv9sdj9rohfitws1kxui8r4x FOREIGN KEY (author_id) REFERENCES users(id);
ALTER TABLE tickets ADD CONSTRAINT fkp9858ag8eff6chcg2sbv5fm3x FOREIGN KEY (project_id) REFERENCES projects(id);
ALTER TABLE projects ADD CONSTRAINT fkmueqy6cpcwpfl8gnnag4idjt9 FOREIGN KEY (owner_id) REFERENCES users(id);
