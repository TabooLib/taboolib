package taboolib.module.metrics.charts;

import taboolib.common.Isolated;
import taboolib.module.metrics.CustomChart;
import taboolib.module.metrics.JsonBuilder;

import java.util.Map;
import java.util.concurrent.Callable;

@Isolated
public class MultiLineChart extends CustomChart {

    private final Callable<Map<String, Integer>> callable;

    /**
     * Class constructor.
     *
     * @param chartId  The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */
    public MultiLineChart(String chartId, Callable<Map<String, Integer>> callable) {
        super(chartId);
        this.callable = callable;
    }

    @Override
    protected JsonBuilder.JsonObject getChartData() throws Exception {
        JsonBuilder valuesBuilder = new JsonBuilder();
        Map<String, Integer> map = callable.call();
        if (map == null || map.isEmpty()) {
            // Null = skip the chart
            return null;
        }
        boolean allSkipped = true;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() == 0) {
                // Skip this invalid
                continue;
            }
            allSkipped = false;
            valuesBuilder.appendField(entry.getKey(), entry.getValue());
        }
        if (allSkipped) {
            // Null = skip the chart
            return null;
        }
        return new JsonBuilder().appendField("values", valuesBuilder.build()).build();
    }
}