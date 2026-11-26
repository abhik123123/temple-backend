-- Per-event switches the admin controls from the Events admin page.
--   registration_open     : devotees can register from the public Events page.
--   attendee_list_public  : the public Events page shows who has registered (names only).
ALTER TABLE events ADD COLUMN IF NOT EXISTS registration_open    BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE events ADD COLUMN IF NOT EXISTS attendee_list_public BOOLEAN NOT NULL DEFAULT FALSE;
