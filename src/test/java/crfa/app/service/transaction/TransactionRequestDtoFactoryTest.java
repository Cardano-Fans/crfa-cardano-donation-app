package crfa.app.service.transaction;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.transaction.model.PaymentTransaction;
import com.bloxbean.cardano.client.transaction.model.TransactionDetailsParams;
import com.google.common.collect.ImmutableList;
import crfa.app.domain.Cadence;
import crfa.app.domain.Donation;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE;
import static crfa.app.service.transaction.TransactionRequestDtoFactory.*;
import static org.junit.jupiter.api.Assertions.*;


class TransactionRequestDtoFactoryTest {

    TransactionRequestDtoFactory factory = new TransactionRequestDtoFactory();

    @Test
    public void create_noDonations() {
        assertThrows(RuntimeException.class,
                () -> factory.create(new ArrayList<>(), new Account(), "sentBy", 0L));
    }

    @Test
    public void create_withDonations() {
        Donation donation0 = createDonation("addr1", new BigInteger("3"));
        Donation donation1 = createDonation("addr2", new BigInteger("5"));
        List<Donation> donations = Arrays.asList(donation0, donation1);
        Account account = new Account();
        String sentBy = "sentBy";
        long ttl = 1500L;

        TransactionRequestDto dto = factory.create(donations, account, sentBy, ttl);
        ImmutableList<PaymentTransaction> paymentTransactions = dto.getPaymentTransactions();
        assertEquals(donations.size(), paymentTransactions.size());

        PaymentTransaction paymentTransaction0 = paymentTransactions.get(0);
        assertSame(account, paymentTransaction0.getSender());
        assertEquals(donation0.getAmount(), paymentTransaction0.getAmount());
        assertEquals(donation0.getAddress(), paymentTransaction0.getReceiver());
        assertSame(LOVELACE, paymentTransaction0.getUnit());
        assertEquals(BigInteger.ZERO, paymentTransaction0.getFee());

        PaymentTransaction paymentTransaction1 = paymentTransactions.get(1);
        assertSame(account, paymentTransaction1.getSender());
        assertEquals(donation1.getAmount(), paymentTransaction1.getAmount());
        assertEquals(donation1.getAddress(), paymentTransaction1.getReceiver());
        assertSame(LOVELACE, paymentTransaction1.getUnit());
        assertEquals(BigInteger.ZERO, paymentTransaction1.getFee());

        TransactionDetailsParams transactionDetailsParams = dto.getTransactionDetailsParams();
        assertEquals(ttl, transactionDetailsParams.getTtl());

        CBORMetadata metadata = (CBORMetadata) dto.getMetadata();
        Map data = metadata.getData();
        Collection<DataItem> keys = data.getKeys();
        assertEquals(1, keys.size());

        String cip20Key = keys.toArray()[0].toString();
        assertEquals(CIP20_META_DATUM_LABEL, cip20Key);
        Collection<DataItem> values = data.getValues();
        assertEquals( 1, values.size());
        Map map = (Map) values.toArray()[0];
        Collection<DataItem> messageKeys = map.getKeys();
        assertEquals(1, messageKeys.size());
        String messageKey = messageKeys.toArray()[0].toString();
        assertEquals(METADATA_MESSAGES_KEY, messageKey);
        Collection<DataItem> metadataMessages = map.getValues();
        assertEquals(1, metadataMessages.size());
        Array memoList = (Array) map.getValues().toArray()[0];
        List<DataItem> dataItems = memoList.getDataItems();
        assertEquals(3, dataItems.size());
        assertEquals(MEMO_APP_NAME, dataItems.get(0).toString());
        assertEquals(String.format(MEMO_SENT_BY, sentBy), dataItems.get(1).toString());
        assertEquals(MEMO_ATTRIBUTION, dataItems.get(2).toString());
    }

    private Donation createDonation(String address, BigInteger amount) {
        return new Donation("1", 1, 1, null, Cadence.EPOCH, "entity", address, amount, null);
    }
}