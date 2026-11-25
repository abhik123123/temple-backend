-- Local development sample data (profile "local" only; see application-local.yml).
-- Runs after every migration but only inserts into empty tables, so it never
-- overwrites data you have entered. Never loaded in production.

INSERT INTO temple_timings (day_of_week, opening_time, closing_time, special_notes)
SELECT * FROM (VALUES
    ('Monday',    TIME '08:30', TIME '11:30', 'Morning Hours'),
    ('Monday',    TIME '16:30', TIME '20:30', 'Evening Hours'),
    ('Tuesday',   TIME '08:30', TIME '11:30', 'Morning Hours'),
    ('Tuesday',   TIME '16:30', TIME '20:30', 'Evening Hours'),
    ('Wednesday', TIME '08:30', TIME '11:30', 'Morning Hours'),
    ('Wednesday', TIME '16:30', TIME '20:30', 'Evening Hours'),
    ('Thursday',  TIME '08:30', TIME '11:30', 'Morning Hours'),
    ('Thursday',  TIME '16:30', TIME '20:30', 'Evening Hours'),
    ('Friday',    TIME '08:30', TIME '11:30', 'Morning Hours'),
    ('Friday',    TIME '16:30', TIME '20:30', 'Evening Hours | Bhojanasala: 6:30 PM - 8:30 PM'),
    ('Saturday',  TIME '08:30', TIME '20:30', 'Full Day | Bhojanasala: 11:30 AM - 8:30 PM | Ayyappa Abhishekam & Bhajans: 7:00 PM'),
    ('Sunday',    TIME '08:30', TIME '20:30', 'Full Day | Bhojanasala: 11:30 AM - 8:30 PM')
) AS v(day_of_week, opening_time, closing_time, special_notes)
WHERE NOT EXISTS (SELECT 1 FROM temple_timings);

INSERT INTO events (event_name, description, event_date, start_time, end_time, location, category, status)
SELECT * FROM (VALUES
    ('Monthly Prayer Meeting', 'Community prayer session with spiritual discourse.', CURRENT_DATE + 7, TIME '09:00', TIME '11:00', 'Prayer Hall', 'Prayer', 'Upcoming'),
    ('Goda Kalyanam', 'Special Goda Kalyanam ceremony during Dhanur Masa.', CURRENT_DATE + 21, TIME '18:30', TIME '20:00', 'Main Temple', 'Festival', 'Upcoming'),
    ('Annual Charity Drive', 'Annual charity event to support underprivileged communities.', CURRENT_DATE + 45, TIME '08:00', TIME '18:00', 'Temple Grounds', 'Charity', 'Upcoming')
) AS v(event_name, description, event_date, start_time, end_time, location, category, status)
WHERE NOT EXISTS (SELECT 1 FROM events);

INSERT INTO service_items (name, price, description, details, active)
SELECT * FROM (VALUES
    ('Daily Pujas', 'Free', 'Participate in our daily morning and evening prayer services.', 'Morning Prayers at 5:00 AM|Evening Prayers at 7:00 PM|Guided meditation', TRUE),
    ('Special Prayers', '₹500 - ₹2000', 'Customized prayer ceremonies for special occasions and personal blessings.', 'Family blessings|Health & wellness prayers|Success rituals', TRUE),
    ('Wedding Ceremonies', '₹5000 - ₹15000', 'Complete wedding planning and sacred ceremony arrangements.', 'Venue decoration|Priest coordination|Ritual planning', TRUE),
    ('Home Blessings', '₹1000 - ₹5000', 'Priests visit your home for sacred blessing ceremonies.', 'Vastu consultation|Space blessing|Protection rituals', TRUE)
) AS v(name, price, description, details, active)
WHERE NOT EXISTS (SELECT 1 FROM service_items);

INSERT INTO staff (full_name, position, department, phone_number, email, biography, status)
SELECT * FROM (VALUES
    ('Swami Ananda', 'Head Priest', 'Religious', '(555) 101-0001', 'swami.ananda@example.org', 'Spiritual leader with extensive knowledge of Vedic practices.', 'Active'),
    ('Priest Ramesh Kumar', 'Senior Priest', 'Religious', '(555) 101-0002', 'ramesh.kumar@example.org', 'Conducts marriage ceremonies and family blessing rituals.', 'Active'),
    ('Ravi Iyer', 'Facilities Manager', 'Operations', '(555) 201-0001', 'ravi.iyer@example.org', 'Oversees temple facilities and maintenance.', 'Active'),
    ('Priya Menon', 'Volunteer Coordinator', 'Community', '(555) 301-0001', 'priya.menon@example.org', 'Manages volunteer programs and outreach.', 'Active')
) AS v(full_name, position, department, phone_number, email, biography, status)
WHERE NOT EXISTS (SELECT 1 FROM staff);

INSERT INTO board_members (full_name, position, department, phone_number, email, biography, status)
SELECT * FROM (VALUES
    ('Sri Ramakrishna Sharma', 'President', 'Administration', '(555) 123-4567', 'president@example.org', 'Over 35 years of experience in temple administration.', 'Active'),
    ('Dr. Rajesh Kumar', 'Vice President', 'Finance', '(555) 234-5678', 'vp@example.org', 'Oversees financial operations and resource management.', 'Active'),
    ('Smt. Lakshmi Devi', 'Secretary', 'Operations', '(555) 345-6789', 'secretary@example.org', 'Ensures smooth operation of all temple activities.', 'Active')
) AS v(full_name, position, department, phone_number, email, biography, status)
WHERE NOT EXISTS (SELECT 1 FROM board_members);

INSERT INTO contact_info (name, email, phone, subject, message, active)
SELECT 'Temple Office', 'info@example.org', '640-204-1027', 'Temple General Information',
       'For queries please call during Bhojanasala hours.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM contact_info);

INSERT INTO bhojana_shala (name, capacity, timings, service_type, status, active)
SELECT 'Main Community Kitchen', 200, 'Friday: 6:30 PM - 8:30 PM | Saturday & Sunday: 11:30 AM - 8:30 PM', 'Free Food Distribution', 'Active', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bhojana_shala);
