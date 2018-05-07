package me.skymc.taboolib.object;

public class WeightCategory {

    private String category;
    private Integer weight;

    public WeightCategory() {
        super();
    }

    public WeightCategory(String category, Integer weight) {
        super();
        this.setCategory(category);
        this.setWeight(weight);
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}   