
CREATE TABLE users (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email text UNIQUE NOT NULL,
    password text NOT NULL,
    enabled boolean NOT NULL,
    first_name text NOT NULL,
    last_name text NOT NULL,
    birth_date date NOT NULL,
    address text,
    phone_number text
);

CREATE TABLE users_authorities (
    user_id bigint REFERENCES users(id) ON DELETE CASCADE,
    role text CHECK (role IN ('BASIC', 'ADMIN'))
);
