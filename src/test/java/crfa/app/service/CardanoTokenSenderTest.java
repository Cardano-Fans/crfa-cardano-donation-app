package crfa.app.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.backend.api.BlockService;
import com.bloxbean.cardano.client.backend.api.helper.FeeCalculationService;
import com.bloxbean.cardano.client.backend.api.helper.TransactionHelperService;
import com.bloxbean.cardano.client.backend.api.helper.model.TransactionResult;
import com.bloxbean.cardano.client.backend.exception.ApiException;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.backend.model.Result;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.transaction.model.PaymentTransaction;
import com.bloxbean.cardano.client.transaction.model.TransactionDetailsParams;
import com.google.common.collect.ImmutableList;
import crfa.app.domain.Cadence;
import crfa.app.domain.Donation;
import crfa.app.service.transaction.TransactionRequestDto;
import crfa.app.service.transaction.TransactionRequestDtoFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static crfa.app.service.CardanoTokenSender.HEREAFTER_SLOT_COUNT;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class CardanoTokenSenderTest {

    @Mock
    private TransactionRequestDtoFactory transactionRequestDtoFactory;
    @Mock
    private FeeCalculationService feeCalculationService;
    @Mock
    private BlockService blockService;
    @Mock
    private TransactionHelperService transactionHelperService;
    @Mock
    private Account donationAccount;

    @InjectMocks
    CardanoTokenSender tokenSender = new CardanoTokenSender();

    @Test
    public void sendDonations_happy() throws ApiException, AddressExcepion, CborSerializationException {
        Donation donation = createDonation("addr1", new BigInteger("5"));
        List<Donation> donations = Collections.singletonList(donation);
        String sentBy = "sentBy";

        long currentSlot = 500L;
        Block block = Block.builder().slot(currentSlot).build();
        Result<Block> result = Result.success(block.toString()).withValue(block).code(200);

        PaymentTransaction paymentTx0 = new PaymentTransaction();
        PaymentTransaction paymentTx1 = new PaymentTransaction();
        ImmutableList<PaymentTransaction> paymentTransactions =
                ImmutableList.<PaymentTransaction>builder().add(paymentTx0, paymentTx1).build();
        TransactionDetailsParams detailsParams = TransactionDetailsParams.builder().build();
        Metadata metadata = new CBORMetadata();
        TransactionRequestDto transactionRequestDto = new TransactionRequestDto(paymentTransactions, detailsParams, metadata);
        BigInteger fee = new BigInteger("200");

        TransactionResult txResult = new TransactionResult("signed".getBytes(), "txId");
        Result<TransactionResult> wrappedTxResult = Result.success("response").withValue(txResult).code(200);

        when(blockService.getLastestBlock()).thenReturn(result);
        when(transactionRequestDtoFactory.create(donations, donationAccount, sentBy, currentSlot + HEREAFTER_SLOT_COUNT))
                .thenReturn(transactionRequestDto);
        when(feeCalculationService.calculateFee(paymentTransactions, detailsParams, metadata)).thenReturn(fee);
        when(transactionHelperService.transfer(paymentTransactions, detailsParams, metadata)).thenReturn(wrappedTxResult);

        Result<TransactionResult> transactionResultResult = tokenSender.sendDonations(donations, sentBy);
        TransactionResult value = transactionResultResult.getValue();
        assertSame(txResult, value);
    }

    private Donation createDonation(String address, BigInteger amount) {
        return new Donation("1", 1, 1, null, Cadence.EPOCH, "entity", address, amount, null);
    }

}