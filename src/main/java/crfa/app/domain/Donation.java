package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;

import java.math.BigInteger;
import java.util.Date;

@Builder
@DatabaseTable(tableName = "donation")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Donation {

    @DatabaseField(id = true)
    String id;

    @DatabaseField(canBeNull = false, index = true)
    // e.g. epoch or super epoch number
    int epoch;

    @DatabaseField(canBeNull = false, index = true)
    int superEpoch;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE)
    Date date;

    @DatabaseField(canBeNull = false, index = true)
    Cadence cadence;

    @DatabaseField(canBeNull = false)
    String address;

    @DatabaseField(canBeNull = false)
    // amount in lovelaces
    BigInteger amount;

}
