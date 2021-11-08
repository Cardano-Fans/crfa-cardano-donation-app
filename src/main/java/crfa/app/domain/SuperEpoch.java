package crfa.app.domain;

import com.google.common.collect.Range;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
public class SuperEpoch {

    public final static int START_EPOCH = 0;
    public final static int STEP = 7;

    @Getter
    private final int from;

    @Getter
    private final int to;

    @Getter
    private final int id;

    private Range range;

    public SuperEpoch(int from, int to) {
        this.id = BigDecimal.valueOf(from)
                .subtract(BigDecimal.valueOf(START_EPOCH))
                .divide(BigDecimal.valueOf(STEP))
                .add(BigDecimal.valueOf(1))
                .intValue();

        this.from = from;
        this.to = to;
        this.range = Range.closed(from, to);
    }

    public boolean containsEpoch(int epochNo) {
        return range.contains(epochNo);
    }

}
