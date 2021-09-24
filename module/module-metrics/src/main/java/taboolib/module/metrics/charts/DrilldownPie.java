package taboolib.module.metrics.charts;

import taboolib.common.Isolated;
import taboolib.module.metrics.CustomChart;
import taboolib.module.metrics.JsonBuilder;

import java.util.Map;
import java.util.concurrent.Callable;

@Isolated
public class DrilldownPie extends CustomChart {

    private final Callable<Map<String, Map<String, Integer>>> callable;

    /**
     * Class constructor.
     *
     * @param chartId  The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */
    public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) {
        super(chartId);
        this.callable = callable;
    }

    @Override
    public JsonBuilder.JsonObject getChartData() throws Exception {
        JsonBuilder valuesBuilder = new JsonBuilder();
        Map<String, Map<String, Integer>> map = callable.call();
        if (map == null || map.isEmpty()) {
            // Null = skip the chart
            return null;
        }
        boolean reallyAllSkipped = true;
        for (Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
            JsonBuilder valueBuilder = new JsonBuilder();
            boolean allSkipped = true;
            for (Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
                valueBuilder.appendField(valueEntry.getKey(), valueEntry.getValue());
                allSkipped = false;
            }
            if (!allSkipped) {
                reallyAllSkipped = false;
                valuesBuilder.appendField(entryValues.getKey(), valueBuilder.build());
            }
        }
        if (reallyAllSkipped) {
            // Null = skip the chart
            return null;
        }
        return new JsonBuilder().appendField("values", valuesBuilder.build()).build();
    }
}