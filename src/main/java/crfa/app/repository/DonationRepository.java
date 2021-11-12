package crfa.app.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import crfa.app.domain.Cadence;
import crfa.app.domain.Donation;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Optional;

import static crfa.app.domain.Cadence.EPOCH;
import static crfa.app.domain.Cadence.SUPER_EPOCH;

@Singleton
@Slf4j
public class DonationRepository {

    private JdbcConnectionSource connectionSource;

    @Value("${dbPath:crfa-cardano-donation-app-dev.db}")
    private String dbPath;
    private Dao<Donation, String> donationDao;

    @EventListener
    public void onStartup(ServerStartupEvent event) throws SQLException {
        log.info("Starting DonationRepository..., dbPath:{}", dbPath);

        String databaseUrl = String.format("jdbc:sqlite:%s", dbPath);
        // create a connection source to our database
        this.connectionSource = new JdbcConnectionSource(databaseUrl);

        this.donationDao = DaoManager.createDao(connectionSource, Donation.class);

        createDbsIfNecessary();
    }

    public Optional<Donation> findDonationByEpochNo(int epochNo, String entityId) {
        try {
            QueryBuilder<Donation, String> statementBuilder = donationDao.queryBuilder();

            statementBuilder.where()
                    .eq("cadence", EPOCH)
                    .and()
                    .eq("epochNo", epochNo)
                    .and()
                    .eq("entityId", entityId);

            // for now one tokenSymbol per whole lifetime of stake address
            return donationDao.query(statementBuilder.prepare()).stream().findFirst();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Optional<Donation> findDonationBySuperEpochNo(int superEpochNo, String entityId) {
        try {
            QueryBuilder<Donation, String> statementBuilder = donationDao.queryBuilder();

            statementBuilder.where()
                    .eq("cadence", SUPER_EPOCH)
                    .and()
                    .eq("superEpochNo", superEpochNo)
                    .and()
                    .eq("entityId", entityId);

            // for now one tokenSymbol per whole lifetime of stake address
            return donationDao.query(statementBuilder.prepare()).stream().findFirst();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void insertDonation(Donation donation) {
        try {
            donationDao.create(donation);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void createDbsIfNecessary() throws SQLException {
        TableUtils.createTableIfNotExists(this.connectionSource, Donation.class);
    }

}
