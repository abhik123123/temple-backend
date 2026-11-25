-- V2: production hardening. Safe to run on databases created by V1 or by the
-- old Hibernate auto-DDL (constraint names there were generated, so they are
-- looked up dynamically).

-- Admin accounts can be disabled without deleting them.
ALTER TABLE admins ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;

-- Donations: one donor may give many times, so email can no longer be unique.
DO $$
DECLARE c RECORD;
BEGIN
    FOR c IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_attribute att ON att.attrelid = rel.oid AND att.attnum = ANY (con.conkey)
        WHERE rel.relname = 'donors' AND con.contype = 'u' AND att.attname = 'email'
    LOOP
        EXECUTE format('ALTER TABLE donors DROP CONSTRAINT %I', c.conname);
    END LOOP;
END $$;

-- Money as exact decimals, plus the fields the website's donations page records.
ALTER TABLE donors ALTER COLUMN donation_amount TYPE NUMERIC(12, 2) USING donation_amount::NUMERIC(12, 2);
ALTER TABLE donors ADD COLUMN IF NOT EXISTS donation_date DATE;
ALTER TABLE donors ADD COLUMN IF NOT EXISTS in_memory_of VARCHAR(150);
ALTER TABLE donors ADD COLUMN IF NOT EXISTS donation_reason VARCHAR(500);
CREATE INDEX IF NOT EXISTS idx_donors_donation_date ON donors (donation_date);

ALTER TABLE sponsorships ALTER COLUMN amount TYPE NUMERIC(12, 2) USING amount::NUMERIC(12, 2);

-- Years of service shown on staff / board cards.
ALTER TABLE staff ADD COLUMN IF NOT EXISTS experience INTEGER;
ALTER TABLE board_members ADD COLUMN IF NOT EXISTS experience INTEGER;

-- Service catalogue fields used by the admin screen.
ALTER TABLE service_items ADD COLUMN IF NOT EXISTS duration INTEGER;
ALTER TABLE service_items ADD COLUMN IF NOT EXISTS category VARCHAR(50);
ALTER TABLE service_items ADD COLUMN IF NOT EXISTS icon VARCHAR(20);

-- Newsletter: name is optional; one-click unsubscribe links use a secret token
-- instead of the email address (which let anyone unsubscribe anyone).
ALTER TABLE email_subscriptions ALTER COLUMN full_name DROP NOT NULL;
ALTER TABLE email_subscriptions ADD COLUMN IF NOT EXISTS unsubscribe_token VARCHAR(64);
UPDATE email_subscriptions
   SET unsubscribe_token = md5(random()::text || id::text || clock_timestamp()::text)
 WHERE unsubscribe_token IS NULL;
ALTER TABLE email_subscriptions ALTER COLUMN unsubscribe_token SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_email_subscriptions_token ON email_subscriptions (unsubscribe_token);
ALTER TABLE email_subscriptions ADD COLUMN IF NOT EXISTS last_sent_at TIMESTAMP(6);
UPDATE email_subscriptions SET email = lower(trim(email));

-- Additional family members in an event registration may skip phone/email.
ALTER TABLE event_registration_members ALTER COLUMN phone_no DROP NOT NULL;
ALTER TABLE event_registration_members ALTER COLUMN email DROP NOT NULL;

-- Lookups used on every page load.
CREATE INDEX IF NOT EXISTS idx_events_event_date ON events (event_date);
CREATE INDEX IF NOT EXISTS idx_photos_type_related ON photos (photo_type, related_id);
CREATE INDEX IF NOT EXISTS idx_registrations_event ON event_registrations (event_id);
CREATE INDEX IF NOT EXISTS idx_registration_members_registration ON event_registration_members (registration_id);
CREATE INDEX IF NOT EXISTS idx_staff_department ON staff (department);
CREATE INDEX IF NOT EXISTS idx_board_members_department ON board_members (department);

-- Photo links generated before this release pointed at a URL that never
-- existed (/api/photos/download/{type}/{id}/{name}). Repoint them to the
-- real image endpoint.
UPDATE photos SET photo_url = '/api/photos/' || id || '/content';
UPDATE events e SET image_url = '/api/photos/' || p.id || '/content'
  FROM photos p WHERE p.photo_type = 'events' AND p.related_id = e.id AND p.status = 'Active'
   AND e.image_url LIKE '/api/photos/download/%';
UPDATE staff s SET profile_image_url = '/api/photos/' || p.id || '/content'
  FROM photos p WHERE p.photo_type = 'staff' AND p.related_id = s.id AND p.status = 'Active'
   AND s.profile_image_url LIKE '/api/photos/download/%';
UPDATE board_members b SET profile_image_url = '/api/photos/' || p.id || '/content'
  FROM photos p WHERE p.photo_type = 'board_members' AND p.related_id = b.id AND p.status = 'Active'
   AND b.profile_image_url LIKE '/api/photos/download/%';
