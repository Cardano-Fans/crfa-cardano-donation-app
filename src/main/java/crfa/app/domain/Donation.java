package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;

import java.math.BigInteger;
import java.util.Date;

import static com.j256.ormlite.field.DataType.DATE;

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
    int epochNo;

    @DatabaseField(canBeNull = false, index = true)
    int superEpochNo;

    @DatabaseField(canBeNull = false, dataType = DATE)
    Date date;

    @DatabaseField(canBeNull = false, index = true)
    Cadence cadence;

    @DatabaseField(canBeNull = false, index = true)
    String entityId;

    @DatabaseField(canBeNull = false)
    String address;

    @DatabaseField(canBeNull = false)
    // amount in lovelaces
    BigInteger amount;

}
