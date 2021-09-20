package taboolib.module.metrics.charts;

import taboolib.common.Isolated;
import taboolib.module.metrics.CustomChart;
import taboolib.module.metrics.JsonBuilder;

import java.util.concurrent.Callable;

@Isolated
public class SingleLineChart extends CustomChart {

    private final Callable<Integer> callable;

    /**
     * Class constructor.
     *
     * @param chartId  The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */
    public SingleLineChart(String chartId, Callable<Integer> callable) {
        super(chartId);
        this.callable = callable;
    }

    @Override
    protected JsonBuilder.JsonObject getChartData() throws Exception {
        int value = callable.call();
        if (value == 0) {
            // Null = skip the chart
            return null;
        }
        return new JsonBuilder().appendField("value", value).build();
    }
}