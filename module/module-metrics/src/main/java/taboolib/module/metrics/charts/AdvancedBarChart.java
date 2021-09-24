package taboolib.module.metrics.charts;

import taboolib.common.Isolated;
import taboolib.module.metrics.CustomChart;
import taboolib.module.metrics.JsonBuilder;

import java.util.Map;
import java.util.concurrent.Callable;

@Isolated
public class AdvancedBarChart extends CustomChart {

    private final Callable<Map<String, int[]>> callable;

    /**
     * Class constructor.
     *
     * @param chartId  The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */
    public AdvancedBarChart(String chartId, Callable<Map<String, int[]>> callable) {
        super(chartId);
        this.callable = callable;
    }

    @Override
    protected JsonBuilder.JsonObject getChartData() throws Exception {
        JsonBuilder valuesBuilder = new JsonBuilder();
        Map<String, int[]> map = callable.call();
        if (map == null || map.isEmpty()) {
            // Null = skip the chart
            return null;
        }
        boolean allSkipped = true;
        for (Map.Entry<String, int[]> entry : map.entrySet()) {
            if (entry.getValue().length == 0) {
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