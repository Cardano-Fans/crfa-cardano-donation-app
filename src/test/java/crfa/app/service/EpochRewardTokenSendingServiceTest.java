package crfa.app.service;

import crfa.app.domain.RaffleForDelegatorEpochRewardEvent;
import crfa.app.domain.PoolDelegator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpochRewardTokenSendingServiceTest {

    @Test
    public void testFormatMessage() {
        var epochReward = RaffleForDelegatorEpochRewardEvent.builder()
                .rewardAmount(15)
                .transactionId("ebb2f57a12c5e097096c97ffba06e255376b3aad589df0becb921ade12244e09")
                .epochNo(293)
                .delegatorStakeAddress("stake1u867eujysgjfve7rhm68u7z6tfh2p99ce0ry0vaq5yn9p6grp7rl6")
                .build();
        var poolDelegator = PoolDelegator.builder()
                .sinceEpochNo(280)
                .stakeAddress("stake1u867eujysgjfve7rhm68u7z6tfh2p99ce0ry0vaq5yn9p6grp7rl6")
                .stakeAmount(10000)
                .build();

        var msg = EpochRewardTokenSendingService.formatTwitterMessage(epochReward, poolDelegator);

        System.out.println(msg);

        assertEquals("ADA raffle for epoch: 293 won!\n" +
                "\n" +
                "https://cardanoscan.io/transaction/ebb2f57a12c5e097096c97ffba06e255376b3aad589df0becb921ade12244e09\n" +
                "\n" +
                "Winner: stake1u867eujysgjfve7rhm68u7z6tfh2p99ce0ry0vaq5yn9p6grp7rl6 has been with the pool since epoch: 280 and has total stake of: 10k ADA.", msg);
    }

}
