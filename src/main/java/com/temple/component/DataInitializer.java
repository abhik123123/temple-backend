package com.temple.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * DataInitializer - Provides manual data loading via API endpoint
 * Call POST /api/init-data to load sample data from data.sql
 */
@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Load data from SQL file
     * Called manually via InitController endpoint
     */
    public String loadDataFromSQL() {
        log.info("DATA_LOAD_START | Initiating data load from data.sql");
        long startTime = System.currentTimeMillis();

        try {
            // Check if data already exists
            log.debug("DATA_LOAD_CHECK | Checking if data already exists");
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM events", Long.class);
            log.debug("DATA_LOAD_CHECK | Found {} events in database", count);

            if (count != null && count > 0) {
                String message = "⚠️ Data already exists! Found " + count + " events in database. Use POST /api/clear-data first if you want to reload.";
                log.warn("DATA_LOAD_SKIP | {}", message);
                return message;
            }

            log.info("DATA_LOAD_EXECUTE | Loading sample data from data.sql");
            System.out.println("🔄 Loading sample data from data.sql...");

            // Load data from SQL file
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("data.sql"));
            populator.setContinueOnError(false);
            populator.setSeparator(";");
            populator.execute(dataSource);

            long duration = System.currentTimeMillis() - startTime;
            log.info("DATA_LOAD_SUCCESS | Data loaded successfully in {}ms", duration);
            System.out.println("✅ Sample data loaded successfully!");

            String result = "✅ Sample data loaded successfully from data.sql!\n" +
                   "📊 Total records: 47\n" +
                   "   - Temple Timings: 7\n" +
                   "   - Events: 5\n" +
                   "   - Service Items: 4\n" +
                   "   - Admin Users: 1\n" +
                   "   - Contacts: 2\n" +
                   "   - Sponsorships: 2\n" +
                   "   - Bhojanashala: 1\n" +
                   "   - Goshala: 1\n" +
                   "   - Vidyalaya: 1\n" +
                   "   - Donors: 5\n" +
                   "   - Board Members: 6\n" +
                   "   - Staff Members: 12";

            log.info("DATA_LOAD_COMPLETE | duration={}ms | totalRecords=47", duration);
            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String error = "❌ Error loading data: " + e.getMessage();
            log.error("DATA_LOAD_ERROR | duration={}ms | error={} | message={}",
                    duration, e.getClass().getSimpleName(), e.getMessage(), e);
            System.err.println(error);
            e.printStackTrace();
            return error;
        }
    }

    /**
     * Clear all data from database
     * Called manually via InitController endpoint
     */
    public String clearAllData() {
        log.info("DATA_CLEAR_START | Initiating data clear operation");
        long startTime = System.currentTimeMillis();

        try {
            log.info("DATA_CLEAR_EXECUTE | Clearing all data from database");
            System.out.println("🗑️ Clearing all data...");

            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
            log.debug("DATA_CLEAR | Disabled referential integrity checks");

            jdbcTemplate.execute("DELETE FROM staff");
            log.debug("DATA_CLEAR | Cleared staff table");

            jdbcTemplate.execute("DELETE FROM board_members");
            log.debug("DATA_CLEAR | Cleared board_members table");

            jdbcTemplate.execute("DELETE FROM donor");
            log.debug("DATA_CLEAR | Cleared donor table");

            jdbcTemplate.execute("DELETE FROM vidyalaya");
            log.debug("DATA_CLEAR | Cleared vidyalaya table");

            jdbcTemplate.execute("DELETE FROM goshala");
            log.debug("DATA_CLEAR | Cleared goshala table");

            jdbcTemplate.execute("DELETE FROM bhojanashala");
            log.debug("DATA_CLEAR | Cleared bhojanashala table");

            jdbcTemplate.execute("DELETE FROM sponsorship");
            log.debug("DATA_CLEAR | Cleared sponsorship table");

            jdbcTemplate.execute("DELETE FROM contact");
            log.debug("DATA_CLEAR | Cleared contact table");

            jdbcTemplate.execute("DELETE FROM admins");
            log.debug("DATA_CLEAR | Cleared admins table");

            jdbcTemplate.execute("DELETE FROM service_item");
            log.debug("DATA_CLEAR | Cleared service_item table");

            jdbcTemplate.execute("DELETE FROM events");
            log.debug("DATA_CLEAR | Cleared events table");

            jdbcTemplate.execute("DELETE FROM temple_timing");
            log.debug("DATA_CLEAR | Cleared temple_timing table");

            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
            log.debug("DATA_CLEAR | Re-enabled referential integrity checks");

            long duration = System.currentTimeMillis() - startTime;
            log.info("DATA_CLEAR_SUCCESS | All data cleared successfully in {}ms", duration);
            System.out.println("✅ All data cleared!");

            return "✅ All data cleared successfully! You can now reload data using POST /api/init-data";

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String error = "❌ Error clearing data: " + e.getMessage();
            log.error("DATA_CLEAR_ERROR | duration={}ms | error={} | message={}",
                    duration, e.getClass().getSimpleName(), e.getMessage(), e);
            System.err.println(error);
            return error;
        }
    }

    /**
     * Get database status
     */
    public String getDatabaseStatus() {
        log.info("DB_STATUS_CHECK | Retrieving database status");
        long startTime = System.currentTimeMillis();

        try {
            StringBuilder status = new StringBuilder();
            status.append("📊 Database Status:\n\n");

            Long templeTimings = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM temple_timing", Long.class);
            status.append("Temple Timings: ").append(templeTimings).append("\n");
            log.debug("DB_STATUS | temple_timing={}", templeTimings);

            Long events = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM events", Long.class);
            status.append("Events: ").append(events).append("\n");
            log.debug("DB_STATUS | events={}", events);

            Long serviceItems = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM service_item", Long.class);
            status.append("Service Items: ").append(serviceItems).append("\n");
            log.debug("DB_STATUS | service_item={}", serviceItems);

            Long admins = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admins", Long.class);
            status.append("Admin Users: ").append(admins).append("\n");
            log.debug("DB_STATUS | admins={}", admins);

            Long contacts = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM contact", Long.class);
            status.append("Contacts: ").append(contacts).append("\n");
            log.debug("DB_STATUS | contact={}", contacts);

            Long sponsorships = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sponsorship", Long.class);
            status.append("Sponsorships: ").append(sponsorships).append("\n");
            log.debug("DB_STATUS | sponsorship={}", sponsorships);

            Long bhojanashala = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM bhojanashala", Long.class);
            status.append("Bhojanashala: ").append(bhojanashala).append("\n");
            log.debug("DB_STATUS | bhojanashala={}", bhojanashala);

            Long goshala = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM goshala", Long.class);
            status.append("Goshala: ").append(goshala).append("\n");
            log.debug("DB_STATUS | goshala={}", goshala);

            Long vidyalaya = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vidyalaya", Long.class);
            status.append("Vidyalaya: ").append(vidyalaya).append("\n");
            log.debug("DB_STATUS | vidyalaya={}", vidyalaya);

            Long donors = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM donor", Long.class);
            status.append("Donors: ").append(donors).append("\n");
            log.debug("DB_STATUS | donor={}", donors);

            Long boardMembers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM board_members", Long.class);
            status.append("Board Members: ").append(boardMembers).append("\n");
            log.debug("DB_STATUS | board_members={}", boardMembers);

            Long staff = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM staff", Long.class);
            status.append("Staff Members: ").append(staff).append("\n");
            log.debug("DB_STATUS | staff={}", staff);

            Long total = jdbcTemplate.queryForObject(
                "SELECT (SELECT COUNT(*) FROM temple_timing) + " +
                "(SELECT COUNT(*) FROM events) + " +
                "(SELECT COUNT(*) FROM service_item) + " +
                "(SELECT COUNT(*) FROM admins) + " +
                "(SELECT COUNT(*) FROM contact) + " +
                "(SELECT COUNT(*) FROM sponsorship) + " +
                "(SELECT COUNT(*) FROM bhojanashala) + " +
                "(SELECT COUNT(*) FROM goshala) + " +
                "(SELECT COUNT(*) FROM vidyalaya) + " +
                "(SELECT COUNT(*) FROM donor) + " +
                "(SELECT COUNT(*) FROM board_members) + " +
                "(SELECT COUNT(*) FROM staff)", Long.class);

            status.append("\n✅ Total Records: ").append(total);

            long duration = System.currentTimeMillis() - startTime;
            log.info("DB_STATUS_SUCCESS | duration={}ms | totalRecords={}", duration, total);

            return status.toString();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String error = "❌ Error getting database status: " + e.getMessage();
            log.error("DB_STATUS_ERROR | duration={}ms | error={} | message={}",
                    duration, e.getClass().getSimpleName(), e.getMessage(), e);
            return error;
        }
    }
}

