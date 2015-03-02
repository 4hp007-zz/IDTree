package id3;

import java.util.*;

public class Tree<T> {

    T data;
    Tree<T> parent;
    List<Tree<T>> children;

    public Tree(T data) {
        this.data = data;
        this.children = new ArrayList<>();
    }

    public Tree<T> addChild(T child) {
        Tree<T> Node = new Tree<>(child);
        Node.parent = this;
        this.children.add(Node);
        return Node;
    }
           
    public void print() {
        print("", true);
    }

    private void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + data);
        for (int i = 0; i < children.size() - 1; i++) {
            children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
        }
        if (children.size() > 0) {
            children.get(children.size() - 1).print(prefix + (isTail ?"    " : "│   "), true);
        }
    }
 

}