package crfa.app.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;

import java.math.BigInteger;
import java.util.Date;

@Builder
@DatabaseTable(tableName = "donation_history")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DonationHistory {

    @DatabaseField(id = true)
    String id;

    @DatabaseField(canBeNull = false)
    int epochNo;

    @DatabaseField(canBeNull = false)
    Date date;

    @DatabaseField(canBeNull = false)
    String address;

    @DatabaseField(canBeNull = false)
    BigInteger amount;

}
