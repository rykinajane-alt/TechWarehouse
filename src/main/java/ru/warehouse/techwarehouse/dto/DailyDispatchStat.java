
package ru.warehouse.techwarehouse.dto;

public class DailyDispatchStat {

    private final String dateLabel;
    private final long count;
    private final int widthPercent;

    public DailyDispatchStat(
            String dateLabel,
            long count,
            int widthPercent) {
        this.dateLabel = dateLabel;
        this.count = count;
        this.widthPercent = widthPercent;
    }

    public String getDateLabel() {
        return dateLabel;
    }

    public long getCount() {
        return count;
    }

    public int getWidthPercent() {
        return widthPercent;
    }
}
