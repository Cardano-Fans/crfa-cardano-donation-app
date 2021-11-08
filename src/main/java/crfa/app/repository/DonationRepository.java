package crfa.app.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;
import crfa.app.domain.Donation;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

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

//   public List<Donation> () {
//       try {
//           return jdbcRepository.getCommunityDelegatorTokenEventDao().queryForAll();
//       } catch (SQLException e) {
//           log.error("db error", e);
//           throw new RuntimeException(e);
//       }
//   }

//    public Optional<Donation> findDonationByCadenceValueAndEntityId(int cadenceValue, String entityId) {
//        try {
//            Dao<CommunityDelegatorTokenEvent, String> communityDelegatorTokenEventDao = jdbcRepository.getCommunityDelegatorTokenEventDao();
//
//            QueryBuilder<CommunityDelegatorTokenEvent, String> statementBuilder = communityDelegatorTokenEventDao.queryBuilder();
//
//            statementBuilder.where().eq("symbol",  tokenSymbol)
//                    .and()
//                    .eq("delegatorStakeAddress", stakeAddr);
//
//            // for now one tokenSymbol per whole lifetime of stake address
//            return communityDelegatorTokenEventDao.query(statementBuilder.prepare()).stream().findFirst();
//        } catch (SQLException e) {
//            log.error("db error", e);
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void insertNewCommunityTokenEvent(CommunityDelegatorTokenEvent delegatorTokenEvent) {
//        try {
//            Dao<CommunityDelegatorTokenEvent, String> communityDelegatorTokenEventDao = jdbcRepository.getCommunityDelegatorTokenEventDao();
//
//            communityDelegatorTokenEventDao.createIfNotExists(delegatorTokenEvent);
//        } catch (SQLException e) {
//            log.error("db error", e);
//            throw new RuntimeException(e);
//        }
//    }

    public void createDbsIfNecessary() throws SQLException {
        TableUtils.createTableIfNotExists(this.connectionSource, Donation.class);
    }

}
