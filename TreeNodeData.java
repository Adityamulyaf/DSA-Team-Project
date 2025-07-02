import java.io.File;
import java.util.ArrayList;
import java.util.List;

// representasi node (visualisasi)
public class TreeNodeData {
    public File file;
    public int x, y; // Center coordinates of the node circle for rendering
    public int level; // Depth level in the tree (0 for root)
    public int index; // Index among siblings (not directly used for vertical positioning anymore, but kept)
    public int subtreeWidth; // Calculated width required by this node's subtree for layout
    public List<TreeNodeData> children;

    public TreeNodeData(File file, int level, int index) {
        this.file = file;
        this.level = level;
        this.index = index;
        this.children = new ArrayList<>();
        this.x = 0;
        this.y = 0;
        this.subtreeWidth = 0;
    }

    public void addChild(TreeNodeData child) {
        children.add(child);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }
}