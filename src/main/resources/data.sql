-- ============================================================
-- Temple Database - Initialization Script
-- ============================================================
-- Individual INSERT statements for better compatibility
-- Date: November 27, 2025
-- ============================================================

-- Insert Temple Timings - Updated Schedule
-- Weekdays (Mon-Fri): 8:30 AM - 11:30 AM, 4:30 PM - 8:30 PM
INSERT INTO temple_timing (day_of_week, opening_time, closing_time, special_notes) VALUES ('Monday', '08:30:00', '11:30:00', 'Morning Hours');
INSERT INTO temple_timing (day_of_week, opening_time, closing_time, special_notes) VALUES ('Monday', '16:30:00', '20:30:00', 'Evening Hours');
INSERT INTO temple_timing (day_of_week, opening_time, closing_time, special_notes) VALUES ('Tuesday', '08:30:00', '11:30:00', 'Morning Hours');
INSERT INTO temple_timing (day_of_week, opening_time, closing_time, special_notes) VALUES ('Tuesday', '16:30:00', '20:30:00', 'Evening Hours');
INSERT INTO temple_timing (day_of_week, opening_time, closing_time, special_notes) VALUES ('Wednesday', '08:30:00', '11:30:00', 'Morning Hours');
INSERT INTO temple_timing (day_of_week, opening_time, closing_time, special_notes) VALUES ('Wednesday', '16:30:00', '20:30:00', 'Evening Hours');
INSERT INTO temple_timing (day_of_week, opening_time, closing_time, special_notes) VALUES ('Thursday', '08:30:00', '11:30:00', 'Morning Hours');
INSERT INTO temple_timing (day_of_week, opening_time, closing_time, special_notes) VALUES ('Thursday', '16:30:00', '20:30:00', 'Evening Hours');
INSERT INTO temple_timing (day_of_week, opening_time, closing_time, special_notes) VALUES ('Friday', '08:30:00', '11:30:00', 'Morning Hours');
INSERT INTO temple_timing (day_of_week, opening_time, closing_time, special_notes) VALUES ('Friday', '16:30:00', '20:30:00', 'Evening Hours | Bhojanasala: 6:30 PM - 8:30 PM');
-- Weekends & National Holidays: 8:30 AM - 8:30 PM
INSERT INTO temple_timing (day_of_week, opening_time, closing_time, special_notes) VALUES ('Saturday', '08:30:00', '20:30:00', 'Full Day | Bhojanasala: 11:30 AM - 8:30 PM | Ayyappa Abhishekam & Bhajans: 7:00 PM');
INSERT INTO temple_timing (day_of_week, opening_time, closing_time, special_notes) VALUES ('Sunday', '08:30:00', '20:30:00', 'Full Day | Bhojanasala: 11:30 AM - 8:30 PM');

-- Insert Events
INSERT INTO events (event_name, description, event_date, start_time, end_time, location, category, status, image_url) VALUES ('Ayyappa Mandalam 2024-2025', 'Ayyappa Abhishekam & Bhajans. Sastha Preethi on Dec 13, 2025 (Registration Deadline: Dec 13)', '2025-11-16', '19:00:00', '20:00:00', 'Main Temple', 'Festival', 'Upcoming', NULL);
INSERT INTO events (event_name, description, event_date, start_time, end_time, location, category, status, image_url) VALUES ('Nariyaneeyam Dinam', 'Celebration of Nariyaneeyam with Abhishekam and Parayanam. 8:45 AM Abhishekam, 9:30 AM Parayanam', '2025-12-13', '08:45:00', '10:30:00', 'Main Temple', 'Spiritual', 'Upcoming', NULL);
INSERT INTO events (event_name, description, event_date, start_time, end_time, location, category, status, image_url) VALUES ('Guruvayoor Ekadasi & Gita Jayanthi', '9:00 AM Abhishekam, 4:30 PM Bhagavad Gita Parayanam, 7:00 PM 100 sloka abhishekam', '2025-12-01', '09:00:00', '19:00:00', 'Main Temple', 'Religious', 'Upcoming', NULL);
INSERT INTO events (event_name, description, event_date, start_time, end_time, location, category, status, image_url) VALUES ('Dhanur Masa Puja - Weekdays', 'Dhanur Masa Puja Weekday Hours: 5:00 AM - 11:30 AM, 4:30 PM - 8:30 PM. Daily: Suprabhata Seva, Darshanam, Archana, Abhishekam', '2025-12-16', '05:00:00', '20:30:00', 'Main Temple', 'Festival', 'Upcoming', NULL);
INSERT INTO events (event_name, description, event_date, start_time, end_time, location, category, status, image_url) VALUES ('Dhanur Masa Puja - Weekends', 'Dhanur Masa Puja Weekend Hours: 5:00 AM - 8:30 PM. Daily: Suprabhata Seva, Darshanam, Archana, Abhishekam', '2025-12-20', '05:00:00', '20:30:00', 'Main Temple', 'Festival', 'Upcoming', NULL);
INSERT INTO events (event_name, description, event_date, start_time, end_time, location, category, status, image_url) VALUES ('Goda Kalyanam', 'Special Goda Kalyanam ceremony during Dhanur Masa', '2026-01-13', '18:30:00', '20:00:00', 'Main Temple', 'Festival', 'Upcoming', NULL);
INSERT INTO events (event_name, description, event_date, start_time, end_time, location, category, status, image_url) VALUES ('Diwali Festival', 'Celebrate the festival of lights with special prayers and gatherings.', '2025-11-01', '18:00:00', '22:00:00', 'Main Hall', 'Festival', 'Upcoming', NULL);
INSERT INTO events (event_name, description, event_date, start_time, end_time, location, category, status, image_url) VALUES ('Monthly Prayer Meeting', 'Community prayer session with spiritual discourse.', '2025-12-05', '09:00:00', '11:00:00', 'Prayer Hall', 'Prayer', 'Upcoming', NULL);
INSERT INTO events (event_name, description, event_date, start_time, end_time, location, category, status, image_url) VALUES ('New Year Celebration', 'Special prayers and celebrations to welcome the new year.', '2025-12-31', '20:00:00', '23:59:00', 'Temple Grounds', 'Celebration', 'Upcoming', NULL);
INSERT INTO events (event_name, description, event_date, start_time, end_time, location, category, status, image_url) VALUES ('Krishna Janmashtami', 'Celebrate the birth of Lord Krishna with devotional songs and prayers.', '2025-12-14', '12:00:00', '20:00:00', 'Main Temple', 'Festival', 'Upcoming', NULL);
INSERT INTO events (event_name, description, event_date, start_time, end_time, location, category, status, image_url) VALUES ('Annual Charity Drive', 'Annual charity event to support underprivileged communities.', '2026-01-15', '08:00:00', '18:00:00', 'Temple Grounds', 'Charity', 'Upcoming', NULL);

-- Insert Service Items
INSERT INTO service_item (name, price, description, details) VALUES ('Daily Pujas', 'Free', 'Participate in our daily morning and evening prayer services.', 'Morning Prayers at 5:00 AM|Evening Prayers at 7:00 PM|Guided meditation');
INSERT INTO service_item (name, price, description, details) VALUES ('Special Prayers', '₹500 - ₹2000', 'Customized prayer ceremonies for special occasions and personal blessings.', 'Family blessings|Health & wellness prayers|Success rituals');
INSERT INTO service_item (name, price, description, details) VALUES ('Wedding Ceremonies', '₹5000 - ₹15000', 'Complete wedding planning and sacred ceremony arrangements.', 'Venue decoration|Priest coordination|Ritual planning');
INSERT INTO service_item (name, price, description, details) VALUES ('Home Blessings', '₹1000 - ₹5000', 'Priests visit your home for sacred blessing ceremonies.', 'Vastu consultation|Space blessing|Protection rituals');

-- Insert Admin User
INSERT INTO admins (username, password, email, name, role) VALUES ('admin', 'admin123', 'admin@temple.com', 'Admin User', 'ADMIN');

-- Insert Contacts
-- Temple Main Contact
INSERT INTO contact (name, email, phone, subject, message) VALUES ('Temple Office', 'info@temple.com', '640-204-1027', 'Temple General Information', 'Hindu American Temple and Cultural Center (HATCC) - Non-profit registered in New Jersey since 1988. For queries during Bhojanasala hours.');
-- Existing contacts
INSERT INTO contact (name, email, phone, subject, message) VALUES ('Rajesh Kumar', 'rajesh@email.com', '9876543210', 'Event Inquiry', 'Interested in booking the temple for a wedding ceremony.');
INSERT INTO contact (name, email, phone, subject, message) VALUES ('Priya Singh', 'priya@email.com', '9123456789', 'Service Request', 'Would like to book home blessing service.');

-- Insert Sponsorships
-- Existing sponsorships
INSERT INTO sponsorship (donor_name, amount, purpose, date, status) VALUES ('Mr. Vikram Patel', 50000.0, 'Temple Renovation', '2025-10-15', 'Completed');
INSERT INTO sponsorship (donor_name, amount, purpose, date, status) VALUES ('Mrs. Anjali Sharma', 25000.0, 'Community Kitchen Fund', '2025-11-01', 'Active');
-- New sponsorships from temple data
INSERT INTO sponsorship (donor_name, amount, purpose, date, status) VALUES ('Anonymous', NULL, 'Laddu Prasadam Sponsorship - Every Saturday for Lord Venkateswara', '2025-11-27', 'Active');
INSERT INTO sponsorship (donor_name, amount, purpose, date, status) VALUES ('Anonymous', NULL, 'Vastram Sponsorship for Lord Sri Venkateswara Swamy', '2025-11-27', 'Active');
INSERT INTO sponsorship (donor_name, amount, purpose, date, status) VALUES ('Community', 1000000.0, 'Cow Barn & Chariot Storage Project', '2025-11-27', 'In Progress');
INSERT INTO sponsorship (donor_name, amount, purpose, date, status) VALUES ('Anonymous', NULL, 'Square Foot Wall Inscription Sponsorship', '2025-11-27', 'Active');
INSERT INTO sponsorship (donor_name, amount, purpose, date, status) VALUES ('Anonymous', NULL, 'Daily Patram-Pushpam Flower Offering Sponsorship', '2025-11-27', 'Active');

-- Insert Bhojanashala
INSERT INTO bhojanashala (name, capacity, timings, service_type, status) VALUES ('Main Community Kitchen', 200, 'Friday: 6:30 PM - 8:30 PM | Saturday & Sunday: 11:30 AM - 8:30 PM', 'Free Food Distribution', 'Active');

-- Insert Goshala
INSERT INTO goshala (name, number_of_cows, location, caretaker, daily_routine, status) VALUES ('Sacred Cow Sanctuary', 50, 'Temple Premises', 'Sharma', 'Morning: 6 AM, Afternoon: 12 PM, Evening: 6 PM', 'Active');

-- Insert Vidyalaya
INSERT INTO vidyalaya (name, director, students_enrolled, subjects, operating_days, timings, status) VALUES ('Temple School of Vedic Studies', 'Dr. Murthy', 150, 'Vedic Studies|Sanskrit|Temple Rituals', 'Monday to Friday', '4:00 PM - 6:00 PM', 'Active');

-- Insert Donors
INSERT INTO donor (name, email, phone_number, donation_amount, donation_type, address, city, state, zip_code) VALUES ('Amit Verma', 'amit.verma@email.com', '9876543210', 25000.0, 'Temple Renovation', '123 Krishna Lane', 'Bangalore', 'Karnataka', '560001');
INSERT INTO donor (name, email, phone_number, donation_amount, donation_type, address, city, state, zip_code) VALUES ('Priya Sharma', 'priya.sharma@email.com', '9123456789', 15000.0, 'Community Kitchen Fund', '456 Temple Road', 'Hyderabad', 'Telangana', '500001');
INSERT INTO donor (name, email, phone_number, donation_amount, donation_type, address, city, state, zip_code) VALUES ('Rajesh Patel', 'rajesh.patel@email.com', '8765432109', 50000.0, 'Goshala Maintenance', '789 Sacred Street', 'Mumbai', 'Maharashtra', '400001');
INSERT INTO donor (name, email, phone_number, donation_amount, donation_type, address, city, state, zip_code) VALUES ('Anjali Singh', 'anjali.singh@email.com', '9234567890', 10000.0, 'Vedic School Support', '321 Spiritual Way', 'Delhi', 'Delhi', '110001');
INSERT INTO donor (name, email, phone_number, donation_amount, donation_type, address, city, state, zip_code) VALUES ('Vikram Nair', 'vikram.nair@email.com', '9456789012', 35000.0, 'Event Organization', '654 Festival Lane', 'Pune', 'Maharashtra', '411001');

-- Insert Board Members
INSERT INTO board_members (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Sri Ramakrishna Sharma', 'President', 'Administration', '(555) 123-4567', 'ramakrishna.sharma@temple.org', 'Sri Ramakrishna Sharma is the President of Raja Rajeshwara Devastanam with over 35 years of experience in temple administration and spiritual affairs.', 'https://via.placeholder.com/150?text=Ramakrishna+Sharma', 'Active');
INSERT INTO board_members (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Dr. Rajesh Kumar', 'Vice President', 'Finance', '(555) 234-5678', 'rajesh.kumar@temple.org', 'Dr. Rajesh Kumar, Ph.D. in Finance, serves as Vice President and oversees all financial operations and resource management.', 'https://via.placeholder.com/150?text=Rajesh+Kumar', 'Active');
INSERT INTO board_members (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Smt. Lakshmi Devi', 'Secretary', 'Operations', '(555) 345-6789', 'lakshmi.devi@temple.org', 'Smt. Lakshmi Devi, with her 28 years of experience in organizational management, ensures smooth operations of all temple activities.', 'https://via.placeholder.com/150?text=Lakshmi+Devi', 'Active');
INSERT INTO board_members (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Sri Venkatesh Gupta', 'Treasurer', 'Finance', '(555) 456-7890', 'venkatesh.gupta@temple.org', 'Sri Venkatesh Gupta is a certified accountant with 32 years of experience in financial management.', 'https://via.placeholder.com/150?text=Venkatesh+Gupta', 'Active');
INSERT INTO board_members (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Smt. Anjali Singh', 'Religious Affairs Lead', 'Religious', '(555) 567-8901', 'anjali.singh@temple.org', 'Smt. Anjali Singh is a renowned spiritual scholar and expert in Vedic traditions.', 'https://via.placeholder.com/150?text=Anjali+Singh', 'Active');
INSERT INTO board_members (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Sri Arun Patel', 'Community Relations', 'Community', '(555) 678-9012', 'arun.patel@temple.org', 'Sri Arun Patel leads all community engagement and outreach programs.', 'https://via.placeholder.com/150?text=Arun+Patel', 'Active');

-- Insert Staff Members
INSERT INTO staff (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Swami Ananda', 'Head Priest', 'Religious', '(555) 101-0001', 'swami.ananda@temple.org', 'Highly respected spiritual leader with extensive knowledge of ancient traditions and vedic practices.', 'https://via.placeholder.com/150?text=Swami+Ananda', 'Active');
INSERT INTO staff (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Priest Ramesh Kumar', 'Senior Priest', 'Religious', '(555) 101-0002', 'ramesh.kumar@temple.org', 'Expert in conducting marriage ceremonies and family blessing rituals with utmost devotion.', 'https://via.placeholder.com/150?text=Ramesh+Kumar', 'Active');
INSERT INTO staff (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Priest Vishnu Sharma', 'Spiritual Guide', 'Religious', '(555) 101-0003', 'vishnu.sharma@temple.org', 'Dedicated to teaching meditation and spiritual wellness practices to devotees.', 'https://via.placeholder.com/150?text=Vishnu+Sharma', 'Active');
INSERT INTO staff (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Priest Arjun Reddy', 'Junior Priest', 'Religious', '(555) 101-0004', 'arjun.reddy@temple.org', 'Enthusiastic priest assisting in daily temple rituals and prayers with dedication.', 'https://via.placeholder.com/150?text=Arjun+Reddy', 'Active');
INSERT INTO staff (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Ravi Iyer', 'Facilities Manager', 'Operations', '(555) 201-0001', 'ravi.iyer@temple.org', 'Oversees all temple facilities, maintenance, and infrastructure management.', 'https://via.placeholder.com/150?text=Ravi+Iyer', 'Active');
INSERT INTO staff (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Meera Nair', 'Event Coordinator', 'Operations', '(555) 201-0002', 'meera.nair@temple.org', 'Manages all temple events, festivals, and special ceremonies.', 'https://via.placeholder.com/150?text=Meera+Nair', 'Active');
INSERT INTO staff (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Karthik Reddy', 'Kitchen Supervisor', 'Operations', '(555) 201-0003', 'karthik.reddy@temple.org', 'Supervises the community kitchen and food distribution programs.', 'https://via.placeholder.com/150?text=Karthik+Reddy', 'Active');
INSERT INTO staff (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Priya Menon', 'Volunteer Coordinator', 'Community', '(555) 301-0001', 'priya.menon@temple.org', 'Manages volunteer programs and community outreach initiatives.', 'https://via.placeholder.com/150?text=Priya+Menon', 'Active');
INSERT INTO staff (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Suresh Patel', 'Youth Program Director', 'Community', '(555) 301-0002', 'suresh.patel@temple.org', 'Develops and leads youth engagement programs and cultural education.', 'https://via.placeholder.com/150?text=Suresh+Patel', 'Active');
INSERT INTO staff (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Anjali Desai', 'Accounts Manager', 'Finance', '(555) 401-0001', 'anjali.desai@temple.org', 'Manages daily accounting operations and financial records.', 'https://via.placeholder.com/150?text=Anjali+Desai', 'Active');
INSERT INTO staff (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Ganesh Kulkarni', 'Donation Manager', 'Finance', '(555) 401-0002', 'ganesh.kulkarni@temple.org', 'Oversees donation processing and donor relations.', 'https://via.placeholder.com/150?text=Ganesh+Kulkarni', 'Active');
INSERT INTO staff (full_name, position, department, phone_number, email, biography, profile_image_url, status) VALUES ('Sita Ramachandran', 'Administrative Assistant', 'Administration', '(555) 501-0001', 'sita.ramachandran@temple.org', 'Provides administrative support and manages temple communications.', 'https://via.placeholder.com/150?text=Sita+Ramachandran', 'Active');

-- Insert Sample Email Subscriptions for Newsletter
INSERT INTO email_subscription (email, full_name, phone, preferences, is_active, subscribed_at, created_at) VALUES ('ramakrishna@example.com', 'Ramakrishna Sharma', '(555) 123-4567', '{"frequency": "weekly", "interests": ["events", "news"]}', true, NOW(), NOW());
INSERT INTO email_subscription (email, full_name, phone, preferences, is_active, subscribed_at, created_at) VALUES ('priya.singh@example.com', 'Priya Singh', '(555) 234-5678', '{"frequency": "weekly", "interests": ["events", "festivals"]}', true, NOW(), NOW());
INSERT INTO email_subscription (email, full_name, phone, preferences, is_active, subscribed_at, created_at) VALUES ('rajesh.patel@example.com', 'Rajesh Patel', '(555) 345-6789', '{"frequency": "weekly", "interests": ["news", "announcements"]}', true, NOW(), NOW());
INSERT INTO email_subscription (email, full_name, phone, preferences, is_active, subscribed_at, created_at) VALUES ('anjali.desai@example.com', 'Anjali Desai', '(555) 456-7890', '{"frequency": "weekly", "interests": ["events", "news", "festivals"]}', true, NOW(), NOW());
INSERT INTO email_subscription (email, full_name, phone, preferences, is_active, subscribed_at, created_at) VALUES ('vikram.gupta@example.com', 'Vikram Gupta', '(555) 567-8901', '{"frequency": "weekly", "interests": ["events"]}', true, NOW(), NOW());

-- Insert Sample Subscription Logs
INSERT INTO subscription_log (subscription_id, action_type, details, email_subject, created_at) VALUES (1, 'SUBSCRIBE', 'New subscription created for newsletter', NULL, NOW());
INSERT INTO subscription_log (subscription_id, action_type, details, email_subject, created_at) VALUES (1, 'EMAIL_SENT', 'Subscription confirmation email sent successfully', 'Welcome to Raja Rajeshwara Temple Newsletter', NOW());
INSERT INTO subscription_log (subscription_id, action_type, details, email_subject, created_at) VALUES (2, 'SUBSCRIBE', 'New subscription created for newsletter', NULL, NOW());
INSERT INTO subscription_log (subscription_id, action_type, details, email_subject, created_at) VALUES (3, 'SUBSCRIBE', 'New subscription created for newsletter', NULL, NOW());
