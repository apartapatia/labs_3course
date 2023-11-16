package model;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Data
public class TreeItem {
    private final String name;
    private final int level;
    private final List<TreeItem> children;

    public TreeItem(String name, int level) {
        this.name = name;
        this.level = level;
        this.children = new ArrayList<>();
    }

    public void addChild(TreeItem child) {
        children.add(child);
    }
}
